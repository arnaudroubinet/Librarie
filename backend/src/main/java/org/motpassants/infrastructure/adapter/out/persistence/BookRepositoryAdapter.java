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
    String sql = "SELECT id, title, title_sort, isbn, path, file_size, file_hash, has_cover, created_at, updated_at, publication_date, language_code, publisher_id, metadata, search_vector FROM books ORDER BY created_at";
        List<Book> items = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error fetching books", e);
        }
        return new PageResult<>(items, null, null, false, false, items.size());
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