package org.motpassants.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.Series;
import org.motpassants.domain.port.out.SeriesRepositoryPort;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class SeriesRepositoryAdapter implements SeriesRepositoryPort {

    @Inject
    AgroalDataSource dataSource;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public List<Series> findAll(int offset, int limit) {
    String sql = "SELECT id, name, sort_name, description, book_count, has_picture, metadata, created_at, updated_at FROM series ORDER BY name OFFSET ? LIMIT ?";
        List<Series> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(0, offset));
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error listing series", e);
        }
        return list;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM series";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0L; }
        } catch (SQLException e) {
            throw new RuntimeException("DB error counting series", e);
        }
    }

    @Override
    public Optional<Series> findById(UUID id) {
    String sql = "SELECT id, name, sort_name, description, book_count, has_picture, metadata, created_at, updated_at FROM series WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error finding series by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Series save(Series series) {
    // Ensure ID and defaults
    if (series.getId() == null) series.setId(UUID.randomUUID());
    if (series.getSortName() == null || series.getSortName().isBlank()) series.setSortName(series.getName());
    if (series.getCreatedAt() == null) series.setCreatedAt(OffsetDateTime.now());
    series.setUpdatedAt(series.getUpdatedAt() != null ? series.getUpdatedAt() : OffsetDateTime.now());

    // Upsert by ID
    if (existsById(series.getId())) return update(series);

    String sql = "INSERT INTO series (id, name, sort_name, description, book_count, has_picture, metadata, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, CAST(? AS JSONB), ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, series.getId());
            ps.setString(2, series.getName());
            ps.setString(3, series.getSortName());
            ps.setString(4, series.getDescription());
            ps.setInt(5, series.getBookCount());
    // has_picture is NOT NULL DEFAULT FALSE; never write NULL
    ps.setBoolean(6, series.getHasPicture() != null ? series.getHasPicture() : false);
        ps.setString(7, writeJson(series.getMetadata()));
        ps.setObject(8, toTs(series.getCreatedAt()));
        ps.setObject(9, toTs(series.getUpdatedAt()));
            ps.executeUpdate();
            return series;
        } catch (SQLException e) {
            throw new RuntimeException("DB error saving series", e);
        }
    }

    private Series update(Series series) {
    String sql = "UPDATE series SET name=?, sort_name=?, description=?, book_count=?, has_picture=?, metadata=CAST(? AS JSONB), updated_at=? WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, series.getName());
            ps.setString(2, series.getSortName());
            ps.setString(3, series.getDescription());
            ps.setInt(4, series.getBookCount());
    // has_picture is NOT NULL DEFAULT FALSE; never write NULL
    ps.setBoolean(5, series.getHasPicture() != null ? series.getHasPicture() : false);
        ps.setString(6, writeJson(series.getMetadata()));
        ps.setObject(7, toTs(OffsetDateTime.now()));
        ps.setObject(8, series.getId());
            ps.executeUpdate();
            return series;
        } catch (SQLException e) {
            throw new RuntimeException("DB error updating series", e);
        }
    }

    @Override
    public boolean deleteById(UUID id) {
        String sql = "DELETE FROM series WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("DB error deleting series", e);
        }
    }

    @Override
    public List<Series> searchByName(String query) {
    String sql = "SELECT id, name, sort_name, description, book_count, has_picture, metadata, created_at, updated_at FROM series WHERE LOWER(name) LIKE ? OR LOWER(sort_name) LIKE ? ORDER BY name";
        List<Series> list = new ArrayList<>();
        String like = "%" + query.toLowerCase() + "%";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error searching series", e);
        }
        return list;
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM series WHERE LOWER(name)=LOWER(?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("DB error checking series name existence", e);
        }
    }

    @Override
    public boolean existsByNameAndIdNot(String name, UUID excludeId) {
        String sql = "SELECT 1 FROM series WHERE LOWER(name)=LOWER(?) AND id<>?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setObject(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("DB error checking series name uniqueness", e);
        }
    }

    private Series map(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String name = rs.getString("name");
        String sortName = rs.getString("sort_name");
        String description = rs.getString("description");
    String imagePath = null; // image_path removed from DB; use assets/metadata instead
    int bookCount = rs.getInt("book_count");
    Boolean hasPicture = null; boolean hp = rs.getBoolean("has_picture"); if (!rs.wasNull()) hasPicture = hp;
        String metaJson = rs.getString("metadata");
        Map<String, Object> meta = null;
        try {
            if (metaJson != null) {
                MapType type = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
                meta = objectMapper.readValue(metaJson, type);
            }
        } catch (Exception ignore) {}
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        Series s = new Series();
        s.setId(id);
        s.setName(name);
        s.setSortName(sortName);
        s.setDescription(description);
    s.setImagePath(imagePath);
        s.setHasPicture(hasPicture);
        s.setBookCount(bookCount);
        s.setMetadata(meta);
        s.setCreatedAt(created != null ? created.toInstant().atOffset(java.time.ZoneOffset.UTC) : null);
        s.setUpdatedAt(updated != null ? updated.toInstant().atOffset(java.time.ZoneOffset.UTC) : null);
        return s;
    }

    private boolean existsById(UUID id) {
        String sql = "SELECT 1 FROM series WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("DB error checking series existence", e);
        }
    }

    private Object toTs(OffsetDateTime odt) { return odt != null ? Timestamp.from(odt.toInstant()) : null; }
    private String writeJson(Object obj) { try { return obj != null ? objectMapper.writeValueAsString(obj) : "{}"; } catch (Exception e) { return "{}"; } }
}