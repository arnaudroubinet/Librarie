package org.motpassants.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.Author;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.port.out.AuthorRepositoryPort;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

@ApplicationScoped
public class AuthorRepositoryAdapter implements AuthorRepositoryPort {

    @Inject
    AgroalDataSource dataSource;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Author save(Author author) {
        String sql = "INSERT INTO authors (id, name, sort_name, bio, birth_date, death_date, website_url, has_picture, metadata, created_at, updated_at) VALUES (?, ?, ?, CAST(? AS JSONB), ?, ?, ?, ?, CAST(? AS JSONB), ?, ?)";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, author.getId());
            ps.setString(2, author.getName());
            ps.setString(3, author.getSortName());
            ps.setString(4, writeJson(author.getBio()));
            if (author.getBirthDate() != null) ps.setDate(5, java.sql.Date.valueOf(author.getBirthDate())); else ps.setNull(5, Types.DATE);
            if (author.getDeathDate() != null) ps.setDate(6, java.sql.Date.valueOf(author.getDeathDate())); else ps.setNull(6, Types.DATE);
            ps.setString(7, author.getWebsiteUrl());
            // has_picture is NOT NULL DEFAULT FALSE; never write NULL
            ps.setBoolean(8, author.getHasPicture() != null ? author.getHasPicture() : false);
            ps.setString(9, writeJson(author.getMetadata()));
            ps.setObject(10, toTs(author.getCreatedAt()));
            ps.setObject(11, toTs(author.getUpdatedAt()));
            ps.executeUpdate();
            return author;
        } catch (SQLException e) {
            throw new RuntimeException("DB error saving author", e);
        }
    }

    @Override
    public Author update(Author author) {
        String sql = "UPDATE authors SET name=?, sort_name=?, bio=CAST(? AS JSONB), birth_date=?, death_date=?, website_url=?, has_picture=?, metadata=CAST(? AS JSONB), updated_at=? WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, author.getName());
            ps.setString(2, author.getSortName());
            ps.setString(3, writeJson(author.getBio()));
            if (author.getBirthDate() != null) ps.setDate(4, java.sql.Date.valueOf(author.getBirthDate())); else ps.setNull(4, Types.DATE);
            if (author.getDeathDate() != null) ps.setDate(5, java.sql.Date.valueOf(author.getDeathDate())); else ps.setNull(5, Types.DATE);
            ps.setString(6, author.getWebsiteUrl());
            // has_picture is NOT NULL DEFAULT FALSE; never write NULL
            ps.setBoolean(7, author.getHasPicture() != null ? author.getHasPicture() : false);
            ps.setString(8, writeJson(author.getMetadata()));
            ps.setObject(9, toTs(OffsetDateTime.now()));
            ps.setObject(10, author.getId());
            ps.executeUpdate();
            return author;
        } catch (SQLException e) {
            throw new RuntimeException("DB error updating author", e);
        }
    }

    @Override
    public Optional<Author> findById(UUID id) {
    String sql = "SELECT id, name, sort_name, bio, birth_date, death_date, website_url, has_picture, metadata, created_at, updated_at FROM authors WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error finding author by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Author> findByName(String name) {
    String sql = "SELECT id, name, sort_name, bio, birth_date, death_date, website_url, has_picture, metadata, created_at, updated_at FROM authors WHERE name=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error finding author by name", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM authors WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("DB error deleting author", e);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        String sql = "SELECT 1 FROM authors WHERE id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("DB error checking author existence", e);
        }
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM authors WHERE name=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("DB error checking author name existence", e);
        }
    }

    @Override
    public PageResult<Author> findAll(String cursor, int limit) {
    String sql = "SELECT id, name, sort_name, bio, birth_date, death_date, website_url, has_picture, metadata, created_at, updated_at FROM authors ORDER BY created_at";
        List<Author> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error listing authors", e);
        }
        return new PageResult<>(list, null, null, false, false, list.size());
    }

    @Override
    public PageResult<Author> searchByName(String query, String cursor, int limit) {
    String sql = "SELECT id, name, sort_name, bio, birth_date, death_date, website_url, has_picture, metadata, created_at, updated_at FROM authors WHERE LOWER(name) LIKE ? OR LOWER(sort_name) LIKE ? ORDER BY created_at";
        List<Author> list = new ArrayList<>();
        String like = "%" + query.toLowerCase() + "%";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB error searching authors", e);
        }
        return new PageResult<>(list, null, null, false, false, list.size());
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM authors";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getLong(1) : 0L; }
        } catch (SQLException e) {
            throw new RuntimeException("DB error counting authors", e);
        }
    }

    private Author map(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String name = rs.getString("name");
        String sortName = rs.getString("sort_name");
        String bioJson = rs.getString("bio");
        Map<String, String> bio = null;
        try {
            if (bioJson != null) {
                MapType bioType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);
                bio = objectMapper.readValue(bioJson, bioType);
            }
        } catch (Exception ignore) {}
        java.sql.Date birth = rs.getDate("birth_date");
        java.sql.Date death = rs.getDate("death_date");
        String website = rs.getString("website_url");
        // Optional boolean flag for picture presence
        Boolean hasPicture = null;
        boolean hp = rs.getBoolean("has_picture");
        if (!rs.wasNull()) hasPicture = hp;
        String metaJson = rs.getString("metadata");
        Map<String, Object> meta = null;
        try {
            if (metaJson != null) {
                MapType metaType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
                meta = objectMapper.readValue(metaJson, metaType);
            }
        } catch (Exception ignore) {}
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        Author a = Author.reconstitute(
            id, name, sortName, bio,
            birth != null ? birth.toLocalDate() : null,
            death != null ? death.toLocalDate() : null,
            website, meta,
            created != null ? created.toInstant().atOffset(java.time.ZoneOffset.UTC) : null,
            updated != null ? updated.toInstant().atOffset(java.time.ZoneOffset.UTC) : null
        );
        a.setHasPicture(hasPicture);
        return a;
    }

    private Object toTs(OffsetDateTime odt) { return odt != null ? Timestamp.from(odt.toInstant()) : null; }
    private String writeJson(Object obj) { try { return obj != null ? objectMapper.writeValueAsString(obj) : "{}"; } catch (Exception e) { return "{}"; } }
}