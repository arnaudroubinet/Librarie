package org.motpassants.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.BookSearchCriteria;
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
    public PageResult<Book> findAll(String cursor, int limit) {
        // Cursor-based pagination ordered by newest first (created_at DESC, id DESC)
        // Cursor format: base64("<epochMicros>|<uuid>")
        // Backward-compatible: also accepts legacy epochMillis cursors.
    String baseSql = "SELECT id, title, title_sort, isbn, path, file_size, file_hash, has_cover, created_at, updated_at, publication_date, language_code, publisher_id, metadata, search_vector " +
        "FROM books ";

        String orderClause = " ORDER BY created_at DESC, id DESC";

        // We'll bind exact Timestamp + UUID for stable keyset pagination
        java.sql.Timestamp cursorTimestamp = null;
        UUID cursorUuid = null;
        if (cursor != null && !cursor.isBlank()) {
            try {
                String decoded = new String(java.util.Base64.getUrlDecoder().decode(cursor));
                String[] parts = decoded.split("\\|");
                if (parts.length == 2) {
                    long epochNumber = Long.parseLong(parts[0]);
                    // Detect unit: micros (>= 10^15) vs millis (<= 10^14)
                    if (epochNumber >= 1_000_000_000_000_000L) {
                        long seconds = epochNumber / 1_000_000L;
                        long microsRemainder = epochNumber % 1_000_000L;
                        long nanos = microsRemainder * 1_000L;
                        cursorTimestamp = java.sql.Timestamp.from(java.time.Instant.ofEpochSecond(seconds, nanos));
                    } else {
                        // Legacy millis
                        cursorTimestamp = new java.sql.Timestamp(epochNumber);
                    }
                    cursorUuid = java.util.UUID.fromString(parts[1]);
                }
            } catch (Exception ignore) {
                // If cursor is invalid, treat as no cursor
                cursorTimestamp = null;
                cursorUuid = null;
            }
        }

        StringBuilder sql = new StringBuilder(baseSql);
        if (cursorTimestamp != null && cursorUuid != null) {
            // For DESC, fetch strictly older rows, or same timestamp with smaller UUID
            sql.append("WHERE (created_at < ? OR (created_at = ? AND id < ?))");
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
                if (cursorTimestamp != null && cursorUuid != null) {
                    // Bind exact timestamp twice (for < and =) and the UUID as tiebreaker
                    ps.setTimestamp(idx++, cursorTimestamp);
                    ps.setTimestamp(idx++, cursorTimestamp);
                    ps.setObject(idx++, cursorUuid);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Book book = mapRowToBook(rs);
                        // Enrich minimal associated data required by frontend list
                        hydratePublisher(conn, book);
                        hydrateFormats(conn, book);
                        hydrateFirstSeries(conn, book);
                        items.add(book);
                    }
                }
            }

            if (items.size() > limit) {
                hasNext = true;
                // The (limit+1)th row indicates more pages; derive next cursor from the limit-th item
                Book lastOfPage = items.get(limit - 1);
                items = new ArrayList<>(items.subList(0, limit));
                // Build nextCursor from lastOfPage created_at and id
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
            } else if (!items.isEmpty()) {
                // No more pages
                hasNext = false;
                nextCursor = null;
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB error fetching books", e);
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
    String sql = "SELECT id, title, title_sort, isbn, path, file_size, file_hash, has_cover, created_at, updated_at, publication_date, language_code, publisher_id, metadata, search_vector " +
                     "FROM books WHERE LOWER(title) LIKE ? OR LOWER(path) LIKE ? OR LOWER(isbn) LIKE ? ORDER BY created_at";
        List<Book> items = new ArrayList<>();
        String like = "%" + query.toLowerCase() + "%";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) items.add(mapRowToBook(rs));
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
        long fs = rs.getLong("file_size"); if (!rs.wasNull()) b.setFileSize(fs);
        b.setFileHash(rs.getString("file_hash"));
    boolean hc = rs.getBoolean("has_cover"); if (!rs.wasNull()) b.setHasCover(hc);
        Timestamp created = rs.getTimestamp("created_at"); if (created != null) b.setCreatedAt(created.toInstant().atOffset(java.time.ZoneOffset.UTC));
        Timestamp updated = rs.getTimestamp("updated_at"); if (updated != null) b.setUpdatedAt(updated.toInstant().atOffset(java.time.ZoneOffset.UTC));
    java.sql.Date pub = rs.getDate("publication_date"); if (pub != null) b.setPublicationDate(pub.toLocalDate());
        b.setLanguage(rs.getString("language_code"));
        String metadataJson = rs.getString("metadata");
        if (metadataJson != null && !metadataJson.isBlank()) {
            try {
                MapType type = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
                Map<String, Object> meta = objectMapper.readValue(metadataJson, type);
                b.setMetadata(meta);
            } catch (Exception ignore) { /* leave null */ }
        }
        b.setSearchVector(rs.getString("search_vector"));
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

}