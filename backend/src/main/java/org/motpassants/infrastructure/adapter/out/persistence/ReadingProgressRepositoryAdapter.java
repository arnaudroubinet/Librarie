package org.motpassants.infrastructure.adapter.out.persistence;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.ReadingProgress;
import org.motpassants.domain.port.out.ReadingProgressRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class ReadingProgressRepositoryAdapter implements ReadingProgressRepository {

    @Inject
    AgroalDataSource dataSource;

    @Override
    public ReadingProgress save(ReadingProgress readingProgress) {
        if (readingProgress.getId() == null) {
            return insert(readingProgress);
        } else {
            return update(readingProgress);
        }
    }

    private ReadingProgress insert(ReadingProgress readingProgress) {
        String sql = """
                INSERT INTO reading_progress (id, book_id, user_id, device_id, progress_cfi, progress_percent, last_read_at, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            UUID id = UUID.randomUUID();
            readingProgress.setId(id);

            stmt.setObject(1, id);
            stmt.setObject(2, readingProgress.getBookId());
            stmt.setObject(3, readingProgress.getUserId());
            stmt.setString(4, "web-reader"); // Default device ID for web reader
            stmt.setString(5, buildCfi(readingProgress)); // Convert pages to CFI-like format
            stmt.setBigDecimal(6, java.math.BigDecimal.valueOf(
                readingProgress.getProgress() != null ? readingProgress.getProgress() * 100 : 0.0));
            stmt.setTimestamp(7, readingProgress.getLastReadAt() != null ? 
                Timestamp.valueOf(readingProgress.getLastReadAt()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(8, readingProgress.getCreatedAt() != null ? 
                Timestamp.valueOf(readingProgress.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(9, readingProgress.getUpdatedAt() != null ? 
                Timestamp.valueOf(readingProgress.getUpdatedAt()) : Timestamp.valueOf(LocalDateTime.now()));

            stmt.executeUpdate();
            return readingProgress;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert reading progress", e);
        }
    }

    private ReadingProgress update(ReadingProgress readingProgress) {
        String sql = """
                UPDATE reading_progress 
                SET progress_cfi = ?, progress_percent = ?, last_read_at = ?, updated_at = ?
                WHERE id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, buildCfi(readingProgress));
            stmt.setBigDecimal(2, java.math.BigDecimal.valueOf(
                readingProgress.getProgress() != null ? readingProgress.getProgress() * 100 : 0.0));
            stmt.setTimestamp(3, readingProgress.getLastReadAt() != null ? 
                Timestamp.valueOf(readingProgress.getLastReadAt()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setObject(5, readingProgress.getId());

            stmt.executeUpdate();
            return readingProgress;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reading progress", e);
        }
    }

    @Override
    public Optional<ReadingProgress> findByUserIdAndBookId(UUID userId, UUID bookId) {
        String sql = """
                SELECT id, book_id, user_id, device_id, progress_cfi, progress_percent, last_read_at, created_at, updated_at
                FROM reading_progress 
                WHERE user_id = ? AND book_id = ? AND device_id = 'web-reader'
                ORDER BY updated_at DESC
                LIMIT 1
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            stmt.setObject(2, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReadingProgress(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reading progress", e);
        }
    }

    @Override
    public List<ReadingProgress> findByUserId(UUID userId) {
        String sql = """
                SELECT id, book_id, user_id, device_id, progress_cfi, progress_percent, last_read_at, created_at, updated_at
                FROM reading_progress 
                WHERE user_id = ? AND device_id = 'web-reader'
                ORDER BY last_read_at DESC
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                List<ReadingProgress> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapResultSetToReadingProgress(rs));
                }
                return results;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find reading progress for user", e);
        }
    }

    @Override
    public void deleteByUserIdAndBookId(UUID userId, UUID bookId) {
        String sql = "DELETE FROM reading_progress WHERE user_id = ? AND book_id = ? AND device_id = 'web-reader'";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            stmt.setObject(2, bookId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete reading progress", e);
        }
    }

    @Override
    public boolean existsByUserIdAndBookId(UUID userId, UUID bookId) {
        String sql = "SELECT 1 FROM reading_progress WHERE user_id = ? AND book_id = ? AND device_id = 'web-reader' LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            stmt.setObject(2, bookId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check reading progress existence", e);
        }
    }

    private ReadingProgress mapResultSetToReadingProgress(ResultSet rs) throws SQLException {
        ReadingProgress progress = new ReadingProgress();
        
        progress.setId((UUID) rs.getObject("id"));
        progress.setBookId((UUID) rs.getObject("book_id"));
        progress.setUserId((UUID) rs.getObject("user_id"));
        
        // Convert progress_percent (0-100) to progress (0.0-1.0)
        java.math.BigDecimal percent = rs.getBigDecimal("progress_percent");
        if (percent != null) {
            progress.setProgress(percent.doubleValue() / 100.0);
        }
        
        // Parse CFI to extract page information
        String cfi = rs.getString("progress_cfi");
        parseCfi(cfi, progress);
        
        progress.setIsCompleted(progress.getProgress() != null && progress.getProgress() >= 1.0);
        
        Timestamp lastReadAt = rs.getTimestamp("last_read_at");
        if (lastReadAt != null) {
            progress.setLastReadAt(lastReadAt.toLocalDateTime());
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            progress.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            progress.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return progress;
    }

    private String buildCfi(ReadingProgress progress) {
        // Build a simple CFI-like string from page information
        if (progress.getCurrentPage() != null) {
            return String.format("epubcfi(/6/%d!)", progress.getCurrentPage() * 2);
        }
        return null;
    }

    private void parseCfi(String cfi, ReadingProgress progress) {
        // Parse simple CFI-like string to extract page information
        if (cfi != null && cfi.contains("/6/") && cfi.contains("!")) {
            try {
                String pageStr = cfi.substring(cfi.indexOf("/6/") + 3, cfi.indexOf("!"));
                int pageValue = Integer.parseInt(pageStr) / 2;
                progress.setCurrentPage(pageValue);
            } catch (Exception e) {
                // Ignore parsing errors for now
            }
        }
    }
}