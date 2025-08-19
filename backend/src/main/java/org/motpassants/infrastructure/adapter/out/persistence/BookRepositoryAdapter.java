package org.motpassants.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.BookSearchCriteria;
import org.motpassants.domain.core.model.BookSortCriteria;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.port.out.BookRepository;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class BookRepositoryAdapter implements BookRepository {

    @Inject
    AgroalDataSource dataSource;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public PageResult<Book> findAll(String cursor, int limit, BookSortCriteria sortCriteria) {
        // Validate sort criteria
        if (sortCriteria == null) {
            sortCriteria = BookSortCriteria.DEFAULT;
        }
        
        // Cursor-based pagination with configurable ordering
        // Cursor format: base64("<sortValue>|<epochMicros>|<uuid>")
        // For updated_at/publication_date: epochMicros of the sort field
        // For title_sort: the actual string value (URL-encoded)
        
        String baseSql = "SELECT id, title, title_sort, has_cover, created_at, updated_at, publication_date, language_code " +
            "FROM books ";

        // Build ORDER BY clause with stable tiebreaker
        String orderClause = " ORDER BY " + sortCriteria.toSqlOrderClause() + ", created_at DESC, id DESC";

        // Parse cursor based on sort field type
        Object cursorSortValue = null;
        java.sql.Timestamp cursorTimestamp = null;
        UUID cursorUuid = null;
        
        if (cursor != null && !cursor.isBlank()) {
            try {
                String decoded = new String(java.util.Base64.getUrlDecoder().decode(cursor));
                String[] parts = decoded.split("\\|");
                if (parts.length == 3) {
                    // Parse sort value based on field type
                    switch (sortCriteria.getField()) {
                        case UPDATED_AT:
                        case PUBLICATION_DATE:
                            // Parse timestamp
                            long epochNumber = Long.parseLong(parts[0]);
                            if (epochNumber >= 1_000_000_000_000_000L) {
                                long seconds = epochNumber / 1_000_000L;
                                long microsRemainder = epochNumber % 1_000_000L;
                                long nanos = microsRemainder * 1_000L;
                                cursorSortValue = java.sql.Timestamp.from(java.time.Instant.ofEpochSecond(seconds, nanos));
                            } else {
                                cursorSortValue = new java.sql.Timestamp(epochNumber);
                            }
                            break;
                        case TITLE_SORT:
                            // Parse string value (URL-decoded)
                            cursorSortValue = java.net.URLDecoder.decode(parts[0], "UTF-8");
                            break;
                    }
                    
                    // Parse created_at tiebreaker timestamp
                    long createdAtEpoch = Long.parseLong(parts[1]);
                    if (createdAtEpoch >= 1_000_000_000_000_000L) {
                        long seconds = createdAtEpoch / 1_000_000L;
                        long microsRemainder = createdAtEpoch % 1_000_000L;
                        long nanos = microsRemainder * 1_000L;
                        cursorTimestamp = java.sql.Timestamp.from(java.time.Instant.ofEpochSecond(seconds, nanos));
                    } else {
                        cursorTimestamp = new java.sql.Timestamp(createdAtEpoch);
                    }
                    
                    cursorUuid = java.util.UUID.fromString(parts[2]);
                }
            } catch (Exception ignore) {
                // If cursor is invalid, treat as no cursor
                cursorSortValue = null;
                cursorTimestamp = null;
                cursorUuid = null;
            }
        }

        StringBuilder sql = new StringBuilder(baseSql);
        if (cursorSortValue != null && cursorTimestamp != null && cursorUuid != null) {
            // Build WHERE clause based on sort direction and field
            String sortColumn = sortCriteria.getField().getColumnName();
            boolean isDesc = sortCriteria.getDirection().getSqlKeyword().equals("DESC");
            
            if (isDesc) {
                // For DESC ordering: fetch rows with sort value < cursor OR (sort value = cursor AND created_at < cursor) OR (sort value = cursor AND created_at = cursor AND id < cursor)
                sql.append("WHERE (").append(sortColumn).append(" < ? OR ")
                   .append("(").append(sortColumn).append(" = ? AND created_at < ?) OR ")
                   .append("(").append(sortColumn).append(" = ? AND created_at = ? AND id < ?))");
            } else {
                // For ASC ordering: fetch rows with sort value > cursor OR (sort value = cursor AND created_at < cursor) OR (sort value = cursor AND created_at = cursor AND id < cursor)
                sql.append("WHERE (").append(sortColumn).append(" > ? OR ")
                   .append("(").append(sortColumn).append(" = ? AND created_at < ?) OR ")
                   .append("(").append(sortColumn).append(" = ? AND created_at = ? AND id < ?))");
            }
        }
        sql.append(orderClause).append(" LIMIT ").append(Math.max(1, limit + 1)); // fetch one extra to know hasNext

        List<Book> items = new ArrayList<>();
        boolean hasNext = false;
        String nextCursor = null;
        int totalCount = 0;

        try (Connection conn = dataSource.getConnection()) {
            // Count total (for convenience in UI; not required for paging correctness)
            try (PreparedStatement cps = conn.prepareStatement("SELECT COUNT(*) FROM books"); ResultSet crs = cps.executeQuery()) {
                if (crs.next()) totalCount = crs.getInt(1);
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                if (cursorSortValue != null && cursorTimestamp != null && cursorUuid != null) {
                    // Bind parameters for WHERE clause (6 total: sortValue x3, timestamp x2, uuid x1)
                    ps.setObject(idx++, cursorSortValue);
                    ps.setObject(idx++, cursorSortValue);
                    ps.setTimestamp(idx++, cursorTimestamp);
                    ps.setObject(idx++, cursorSortValue);
                    ps.setTimestamp(idx++, cursorTimestamp);
                    ps.setObject(idx++, cursorUuid);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Book book = mapRowToBookLight(rs);
                        items.add(book);
                    }
                }
            }

            if (items.size() > limit) {
                hasNext = true;
                // The (limit+1)th row indicates more pages; derive next cursor from the limit-th item
                Book lastOfPage = items.get(limit - 1);
                items = new ArrayList<>(items.subList(0, limit));
                
                // Build nextCursor based on sort field
                Object sortValue = null;
                long sortValueEpoch = 0L;
                String sortValueStr = "";
                
                switch (sortCriteria.getField()) {
                    case UPDATED_AT:
                        java.time.OffsetDateTime updatedAt = lastOfPage.getUpdatedAt();
                        if (updatedAt != null) {
                            long seconds = updatedAt.toInstant().getEpochSecond();
                            long nanos = updatedAt.toInstant().getNano();
                            sortValueEpoch = seconds * 1_000_000L + (nanos / 1_000L);
                        }
                        break;
                    case PUBLICATION_DATE:
                        java.time.LocalDate pubDate = lastOfPage.getPublicationDate();
                        if (pubDate != null) {
                            long seconds = pubDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().getEpochSecond();
                            sortValueEpoch = seconds * 1_000_000L;
                        }
                        break;
                    case TITLE_SORT:
                        sortValueStr = lastOfPage.getTitleSort() != null ? lastOfPage.getTitleSort() : "";
                        break;
                }
                
                // Get created_at for tiebreaker
                java.time.OffsetDateTime createdAt = lastOfPage.getCreatedAt();
                long createdAtMicros = 0L;
                if (createdAt != null) {
                    long seconds = createdAt.toInstant().getEpochSecond();
                    long nanos = createdAt.toInstant().getNano();
                    createdAtMicros = seconds * 1_000_000L + (nanos / 1_000L);
                }
                
                UUID id = lastOfPage.getId();
                String raw;
                if (sortCriteria.getField() == org.motpassants.domain.core.model.SortField.TITLE_SORT) {
                    // URL-encode string values
                    try {
                        sortValueStr = java.net.URLEncoder.encode(sortValueStr, "UTF-8");
                    } catch (Exception e) {
                        sortValueStr = "";
                    }
                    raw = sortValueStr + "|" + createdAtMicros + "|" + id;
                } else {
                    raw = sortValueEpoch + "|" + createdAtMicros + "|" + id;
                }
                nextCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
            } else if (!items.isEmpty()) {
                // No more pages
                hasNext = false;
                nextCursor = null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error fetching books with sorting", e);
        }

        return new PageResult<>(items, nextCursor, null, hasNext, false, totalCount);
    }

    @Override
    public Optional<Book> findById(UUID id) {
        String sql = "SELECT id, title, title_sort, isbn, path, file_size, file_hash, has_cover, created_at, updated_at, publication_date, language_code, publisher_id, metadata, search_vector FROM books WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error finding book by id", e);
        }
        return Optional.empty();
    }

    @Override
    public PageResult<Book> findBySeries(UUID seriesId, String cursor, int limit) {
        if (seriesId == null) {
            return new PageResult<>(List.of(), null, null, false, false, 0);
        }
        if (limit <= 0) limit = 20;

        // Parse cursor
        java.sql.Timestamp cursorTimestamp = null;
        UUID cursorUuid = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                String decoded = new String(java.util.Base64.getUrlDecoder().decode(cursor));
                String[] parts = decoded.split("\\|");
                if (parts.length == 2) {
                    long epochNumber = Long.parseLong(parts[0]);
                    if (epochNumber >= 1_000_000_000_000_000L) {
                        long seconds = epochNumber / 1_000_000L;
                        long microsRemainder = epochNumber % 1_000_000L;
                        long nanos = microsRemainder * 1_000L;
                        cursorTimestamp = java.sql.Timestamp.from(java.time.Instant.ofEpochSecond(seconds, nanos));
                    } else {
                        cursorTimestamp = new java.sql.Timestamp(epochNumber);
                    }
                    cursorUuid = java.util.UUID.fromString(parts[1]);
                }
            } catch (Exception ignore) {
                cursorTimestamp = null;
                cursorUuid = null;
            }
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT b.id, b.title, b.title_sort, b.isbn, b.path, b.file_size, b.file_hash, b.has_cover, b.created_at, b.updated_at, b.publication_date, b.language_code, b.publisher_id, b.metadata, b.search_vector ")
           .append("FROM books b JOIN book_series bs ON b.id = bs.book_id ")
           .append("WHERE bs.series_id = ? ");
        if (cursorTimestamp != null && cursorUuid != null) {
            sql.append("AND (b.created_at < ? OR (b.created_at = ? AND b.id < ?)) ");
        }
        sql.append("ORDER BY b.created_at DESC, b.id DESC LIMIT ").append(Math.max(1, limit + 1));

        List<Book> items = new ArrayList<>();
        int totalCount = 0;

        try (Connection conn = dataSource.getConnection()) {
            // Total count for series
            try (PreparedStatement cps = conn.prepareStatement("SELECT COUNT(*) FROM book_series WHERE series_id = ?")) {
                cps.setObject(1, seriesId);
                try (ResultSet crs = cps.executeQuery()) {
                    if (crs.next()) totalCount = crs.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                ps.setObject(idx++, seriesId);
                if (cursorTimestamp != null && cursorUuid != null) {
                    ps.setTimestamp(idx++, cursorTimestamp);
                    ps.setTimestamp(idx++, cursorTimestamp);
                    ps.setObject(idx++, cursorUuid);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Book book = mapRowToBook(rs);
                        hydratePublisher(conn, book);
                        hydrateFormats(conn, book);
                        hydrateFirstSeries(conn, book);
                        items.add(book);
                    }
                }
            }

            String nextCursor = null;
            boolean hasNext = false;
            if (items.size() > limit) {
                hasNext = true;
                Book lastOfPage = items.get(limit - 1);
                items = new ArrayList<>(items.subList(0, limit));
                java.time.OffsetDateTime createdAt = lastOfPage.getCreatedAt();
                UUID id = lastOfPage.getId();
                long micros;
                if (createdAt != null) {
                    long seconds = createdAt.toInstant().getEpochSecond();
                    long nanos = createdAt.toInstant().getNano();
                    micros = seconds * 1_000_000L + (nanos / 1_000L);
                } else {
                    micros = 0L;
                }
                String raw = micros + "|" + id;
                nextCursor = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes());
            }

            return new PageResult<>(items, nextCursor, null, hasNext, false, totalCount);
        } catch (SQLException e) {
            throw new RuntimeException("DB error listing books by series", e);
        }
    }

    @Override
    public java.util.List<Book> findBySeriesOrderByIndex(UUID seriesId, int limit) {
        if (seriesId == null) return java.util.List.of();
        if (limit <= 0) limit = 10;
        String sql = "SELECT b.id, b.title, b.title_sort, b.isbn, b.path, b.file_size, b.file_hash, b.has_cover, b.created_at, b.updated_at, b.publication_date, b.language_code, b.publisher_id, b.metadata, b.search_vector " +
                     "FROM books b JOIN book_series bs ON b.id = bs.book_id " +
                     "WHERE bs.series_id = ? " +
                     "ORDER BY bs.series_index NULLS LAST, b.created_at, b.id LIMIT " + Math.max(1, limit);
        java.util.List<Book> items = new java.util.ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, seriesId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Book book = mapRowToBook(rs);
                    // we only need has_cover flag for fallback, but hydrate minimal for consistency
                    hydratePublisher(conn, book);
                    hydrateFormats(conn, book);
                    hydrateFirstSeries(conn, book);
                    items.add(book);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error listing books by series ordered by index", e);
        }
        return items;
    }

    @Override
    public Optional<Book> findByPath(String path) {
    String sql = "SELECT id, title, title_sort, isbn, path, file_size, file_hash, has_cover, created_at, updated_at, publication_date, language_code, publisher_id, metadata, search_vector FROM books WHERE path = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error finding book by path", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
    String sql = "SELECT id, title, title_sort, isbn, path, file_size, file_hash, has_cover, created_at, updated_at, publication_date, language_code, publisher_id, metadata, search_vector FROM books WHERE isbn = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error finding book by isbn", e);
        }
        return Optional.empty();
    }

    @Override
    public Book save(Book book) {
        try (Connection conn = dataSource.getConnection()) {
            UUID id = book.getId() != null ? book.getId() : UUID.randomUUID();
            book.setId(id);
            // Default timestamps if missing
            if (book.getCreatedAt() == null) book.setCreatedAt(OffsetDateTime.now());
            book.setUpdatedAt(book.getUpdatedAt() != null ? book.getUpdatedAt() : OffsetDateTime.now());

            if (existsById(conn, id)) {
                // UPDATE existing row and preserve existing non-null path when incoming is null
                String upd = "UPDATE books SET title=?, title_sort=?, isbn=?, path=COALESCE(?, path), file_size=?, file_hash=?, has_cover=?, updated_at=?, publication_date=?, language_code=?, publisher_id=?, metadata=CAST(? AS JSONB), search_vector=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(upd)) {
                    ps.setString(1, book.getTitle());
                    ps.setString(2, book.getTitleSort() != null ? book.getTitleSort() : book.getTitle());
                    ps.setString(3, book.getIsbn());
                    ps.setString(4, book.getPath());
                    if (book.getFileSize() != null) ps.setLong(5, book.getFileSize()); else ps.setNull(5, Types.BIGINT);
                    ps.setString(6, book.getFileHash());
                    // has_cover is NOT NULL DEFAULT FALSE; never write NULL
                    ps.setBoolean(7, book.getHasCover() != null ? book.getHasCover() : false);
                    ps.setObject(8, toTimestamp(book.getUpdatedAt()));
                    if (book.getPublicationDate() != null) ps.setDate(9, java.sql.Date.valueOf(book.getPublicationDate())); else ps.setNull(9, Types.DATE);
                    ps.setString(10, book.getLanguage());
                    if (book.getPublisher() != null) ps.setObject(11, book.getPublisher().getId()); else ps.setNull(11, Types.OTHER);
                    ps.setString(12, serializeJson(book.getMetadata()));
                    ps.setString(13, book.getSearchVector());
                    ps.setObject(14, id);
                    ps.executeUpdate();
                }
                return book;
            } else {
                // INSERT new row; path must be non-null (service ensures generation)
                String ins = "INSERT INTO books (id, title, title_sort, isbn, path, file_size, file_hash, has_cover, created_at, updated_at, publication_date, language_code, publisher_id, metadata, search_vector) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS JSONB), ?)";
                try (PreparedStatement ps = conn.prepareStatement(ins)) {
                    ps.setObject(1, id);
                    ps.setString(2, book.getTitle());
                    ps.setString(3, book.getTitleSort() != null ? book.getTitleSort() : book.getTitle());
                    ps.setString(4, book.getIsbn());
                    ps.setString(5, book.getPath());
                    if (book.getFileSize() != null) ps.setLong(6, book.getFileSize()); else ps.setNull(6, Types.BIGINT);
                    ps.setString(7, book.getFileHash());
                    // has_cover is NOT NULL DEFAULT FALSE; never write NULL
                    ps.setBoolean(8, book.getHasCover() != null ? book.getHasCover() : false);
                    ps.setObject(9, toTimestamp(book.getCreatedAt()));
                    ps.setObject(10, toTimestamp(book.getUpdatedAt()));
                    if (book.getPublicationDate() != null) ps.setDate(11, java.sql.Date.valueOf(book.getPublicationDate())); else ps.setNull(11, Types.DATE);
                    ps.setString(12, book.getLanguage());
                    if (book.getPublisher() != null) ps.setObject(13, book.getPublisher().getId()); else ps.setNull(13, Types.OTHER);
                    ps.setString(14, serializeJson(book.getMetadata()));
                    ps.setString(15, book.getSearchVector());
                    ps.executeUpdate();
                }
                return book;
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error saving book", e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error deleting book", e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        String sql = "SELECT 1 FROM books WHERE id = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error checking book existence", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM books";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error counting books", e);
        }
        return 0;
    }

    @Override
    public PageResult<Book> search(BookSearchCriteria criteria) {
        // Minimal parity: same as in-memory, filter by title contains
        List<Book> list = findByCriteria(criteria);
        return new PageResult<>(list, null, null, false, false, list.size());
    }

    @Override
    public List<Book> findByTitleOrAuthorContaining(String query) {
    String sql = "SELECT id, title, title_sort, has_cover, created_at, updated_at, publication_date, language_code " +
             "FROM books WHERE LOWER(title) LIKE ? OR LOWER(path) LIKE ? OR LOWER(isbn) LIKE ? ORDER BY created_at";
        List<Book> items = new ArrayList<>();
        String like = "%" + query.toLowerCase() + "%";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapRowToBookLight(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error searching books", e);
        }
        return items;
    }

    @Override
    public List<Book> findByCriteria(BookSearchCriteria criteria) {
        // Minimal: title contains
        if (criteria != null && criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) {
            return findByTitleOrAuthorContaining(criteria.getTitle().trim());
        }
        // Fallback to all
    return findAll(null, 0).getItems();
    }

    @Override
    public void linkBookToSeries(UUID bookId, UUID seriesId, Double seriesIndex) {
        String upsert = "INSERT INTO book_series (book_id, series_id, series_index) VALUES (?, ?, ?) " +
                        "ON CONFLICT (book_id, series_id) DO UPDATE SET series_index = EXCLUDED.series_index";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(upsert)) {
            ps.setObject(1, bookId);
            ps.setObject(2, seriesId);
            if (seriesIndex != null) {
                ps.setBigDecimal(3, java.math.BigDecimal.valueOf(seriesIndex));
            } else {
                ps.setNull(3, Types.NUMERIC);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error linking book to series", e);
        }
    }

    private Book mapRowToBook(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId((UUID) rs.getObject("id"));
        b.setTitle(rs.getString("title"));
        b.setTitleSort(rs.getString("title_sort"));
        b.setIsbn(rs.getString("isbn"));
        b.setPath(rs.getString("path"));
        Long fileSize = rs.getLong("file_size"); if (!rs.wasNull()) b.setFileSize(fileSize);
        b.setFileHash(rs.getString("file_hash"));
        boolean hc = rs.getBoolean("has_cover"); if (!rs.wasNull()) b.setHasCover(hc);
        Timestamp created = rs.getTimestamp("created_at"); if (created != null) b.setCreatedAt(created.toInstant().atOffset(java.time.ZoneOffset.UTC));
        Timestamp updated = rs.getTimestamp("updated_at"); if (updated != null) b.setUpdatedAt(updated.toInstant().atOffset(java.time.ZoneOffset.UTC));
        java.sql.Date pub = rs.getDate("publication_date"); if (pub != null) b.setPublicationDate(pub.toLocalDate());
        b.setLanguage(rs.getString("language_code"));
        
        // Parse metadata JSON
        String metadataJson = rs.getString("metadata");
        if (metadataJson != null && !metadataJson.trim().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = objectMapper.readValue(metadataJson, Map.class);
                b.setMetadata(metadata);
            } catch (Exception e) {
                // If metadata parsing fails, set empty map
                b.setMetadata(new HashMap<>());
            }
        }
        
        b.setSearchVector(rs.getString("search_vector"));
        return b;
    }

    // Light-weight row mapper for list/search endpoints
    private Book mapRowToBookLight(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId((UUID) rs.getObject("id"));
        b.setTitle(rs.getString("title"));
        b.setTitleSort(rs.getString("title_sort"));
        boolean hc = rs.getBoolean("has_cover"); if (!rs.wasNull()) b.setHasCover(hc);
        Timestamp created = rs.getTimestamp("created_at"); if (created != null) b.setCreatedAt(created.toInstant().atOffset(java.time.ZoneOffset.UTC));
        Timestamp updated = rs.getTimestamp("updated_at"); if (updated != null) b.setUpdatedAt(updated.toInstant().atOffset(java.time.ZoneOffset.UTC));
        java.sql.Date pub = rs.getDate("publication_date"); if (pub != null) b.setPublicationDate(pub.toLocalDate());
        b.setLanguage(rs.getString("language_code"));
        return b;
    }

    private void hydratePublisher(Connection conn, Book b) {
        if (b.getPublisher() != null) return; // already set
        String sql = "SELECT p.id, p.name FROM publishers p WHERE p.id = (SELECT publisher_id FROM books WHERE id = ? AND publisher_id IS NOT NULL)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, b.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    org.motpassants.domain.core.model.Publisher pub = new org.motpassants.domain.core.model.Publisher();
                    pub.setId((java.util.UUID) rs.getObject("id"));
                    pub.setName(rs.getString("name"));
                    b.setPublisher(pub);
                }
            }
        } catch (SQLException ignored) { }
    }

    private void hydrateFormats(Connection conn, Book b) {
        String sql = "SELECT id, format_type, file_path, file_size FROM formats WHERE book_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, b.getId());
            try (ResultSet rs = ps.executeQuery()) {
                java.util.Set<org.motpassants.domain.core.model.Format> set = new java.util.HashSet<>();
                while (rs.next()) {
                    org.motpassants.domain.core.model.Format f = new org.motpassants.domain.core.model.Format();
                    f.setId((java.util.UUID) rs.getObject("id"));
                    f.setFormatType(rs.getString("format_type"));
                    f.setFilePath(rs.getString("file_path"));
                    long sz = rs.getLong("file_size"); if (!rs.wasNull()) f.setFileSize(sz);
                    set.add(f);
                }
                if (!set.isEmpty()) b.setFormats(set);
            }
        } catch (SQLException ignored) { }
    }

    private void hydrateFirstSeries(Connection conn, Book b) {
        String sql = "SELECT bs.series_id, bs.series_index, s.name, s.sort_name FROM book_series bs JOIN series s ON bs.series_id = s.id WHERE bs.book_id = ? ORDER BY bs.series_index NULLS LAST, s.sort_name LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, b.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    org.motpassants.domain.core.model.Series s = new org.motpassants.domain.core.model.Series();
                    s.setId((java.util.UUID) rs.getObject("series_id"));
                    s.setName(rs.getString("name"));
                    s.setSortName(rs.getString("sort_name"));
                    org.motpassants.domain.core.model.BookSeries link = new org.motpassants.domain.core.model.BookSeries();
                    link.setBook(b);
                    link.setSeries(s);
                    java.math.BigDecimal idx = rs.getBigDecimal("series_index");
                    if (idx != null) link.setSeriesIndex(idx.doubleValue());
                    java.util.Set<org.motpassants.domain.core.model.BookSeries> set = new java.util.HashSet<>();
                    set.add(link);
                    b.setSeries(set);
                }
            }
        } catch (SQLException ignored) { }
    }

    private Timestamp toTimestamp(OffsetDateTime odt) {
        return odt != null ? Timestamp.from(odt.toInstant()) : null;
    }

    private String serializeJson(Map<String, Object> map) {
        try {
            return map != null ? objectMapper.writeValueAsString(map) : "{}";
        } catch (Exception e) {
            return "{}";
        }
    }

    private boolean existsById(Connection conn, UUID id) throws SQLException {
        String sql = "SELECT 1 FROM books WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public java.util.Map<String, java.util.List<org.motpassants.domain.core.model.Author>> findContributorsByBook(java.util.UUID bookId) {
        if (bookId == null) return java.util.Map.of();
        String sql = "SELECT a.id, a.name, a.sort_name, owa.role " +
                "FROM book_original_works bow " +
                "JOIN original_work_authors owa ON bow.original_work_id = owa.original_work_id " +
                "JOIN authors a ON a.id = owa.author_id " +
                "WHERE bow.book_id = ? " +
                "ORDER BY CASE LOWER(owa.role) WHEN 'author' THEN 0 ELSE 1 END, a.sort_name";
        java.util.Map<String, java.util.List<org.motpassants.domain.core.model.Author>> map = new java.util.LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String role = rs.getString("role");
                    java.util.UUID id = (java.util.UUID) rs.getObject("id");
                    String name = rs.getString("name");
                    String sortName = rs.getString("sort_name");
                    org.motpassants.domain.core.model.Author a = org.motpassants.domain.core.model.Author.builder()
                            .id(id)
                            .name(name)
                            .sortName(sortName)
                            .build();
                    map.computeIfAbsent(role != null ? role.toLowerCase() : "author", k -> new java.util.ArrayList<>()).add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error fetching contributors for book", e);
        }
        return map;
    }
}