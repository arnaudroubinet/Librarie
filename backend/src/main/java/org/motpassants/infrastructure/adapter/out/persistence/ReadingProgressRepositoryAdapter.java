package org.motpassants.infrastructure.adapter.out.persistence;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.ReadingProgress;
import org.motpassants.domain.core.model.ReadingStatus;
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
        INSERT INTO reading_progress (id, book_id, user_id, device_id, progress_cfi, progress_percent, 
                                      progress_locator, status, started_at, finished_at, last_read_at, 
                                      created_at, updated_at, sync_version, notes)
        VALUES (?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?)
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
            // Optional locator JSON string from domain
            String locator = readingProgress.getProgressLocator();
            stmt.setString(7, locator);
            stmt.setString(8, readingProgress.getStatus() != null ? 
                readingProgress.getStatus().name() : ReadingStatus.READING.name());
            stmt.setTimestamp(9, readingProgress.getStartedAt() != null ? 
                Timestamp.valueOf(readingProgress.getStartedAt()) : null);
            stmt.setTimestamp(10, readingProgress.getFinishedAt() != null ? 
                Timestamp.valueOf(readingProgress.getFinishedAt()) : null);
            stmt.setTimestamp(11, readingProgress.getLastReadAt() != null ? 
                Timestamp.valueOf(readingProgress.getLastReadAt()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(12, readingProgress.getCreatedAt() != null ? 
                Timestamp.valueOf(readingProgress.getCreatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(13, readingProgress.getUpdatedAt() != null ? 
                Timestamp.valueOf(readingProgress.getUpdatedAt()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(14, readingProgress.getSyncVersion() != null ? readingProgress.getSyncVersion() : 1L);
            stmt.setString(15, readingProgress.getNotes());

            stmt.executeUpdate();
            return readingProgress;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert reading progress", e);
        }
    }

    private ReadingProgress update(ReadingProgress readingProgress) {
    String sql = """
        UPDATE reading_progress 
        SET progress_cfi = ?, progress_percent = ?, progress_locator = ?::jsonb, status = ?, 
            started_at = ?, finished_at = ?, last_read_at = ?, updated_at = ?, sync_version = ?, notes = ?
        WHERE id = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, buildCfi(readingProgress));
            stmt.setBigDecimal(2, java.math.BigDecimal.valueOf(
                readingProgress.getProgress() != null ? readingProgress.getProgress() * 100 : 0.0));
            String locator = readingProgress.getProgressLocator();
            stmt.setString(3, locator);
            stmt.setString(4, readingProgress.getStatus() != null ? 
                readingProgress.getStatus().name() : ReadingStatus.READING.name());
            stmt.setTimestamp(5, readingProgress.getStartedAt() != null ? 
                Timestamp.valueOf(readingProgress.getStartedAt()) : null);
            stmt.setTimestamp(6, readingProgress.getFinishedAt() != null ? 
                Timestamp.valueOf(readingProgress.getFinishedAt()) : null);
            stmt.setTimestamp(7, readingProgress.getLastReadAt() != null ? 
                Timestamp.valueOf(readingProgress.getLastReadAt()) : Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(9, readingProgress.getSyncVersion() != null ? readingProgress.getSyncVersion() : 1L);
            stmt.setString(10, readingProgress.getNotes());
            stmt.setObject(11, readingProgress.getId());

            stmt.executeUpdate();
            return readingProgress;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reading progress", e);
        }
    }

    @Override
    public Optional<ReadingProgress> findByUserIdAndBookId(UUID userId, UUID bookId) {
        String sql = """
                SELECT id, book_id, user_id, device_id, progress_cfi, progress_percent, progress_locator, 
                       status, started_at, finished_at, last_read_at, created_at, updated_at, sync_version, notes
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
                SELECT id, book_id, user_id, device_id, progress_cfi, progress_percent, progress_locator, 
                       status, started_at, finished_at, last_read_at, created_at, updated_at, sync_version, notes
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

        // Attach raw locator if present (store directly on domain model)
        String locator = rs.getString("progress_locator");
        if (locator != null) {
            progress.setProgressLocator(locator);
        }
        
        // Parse status
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            progress.setStatus(ReadingStatus.valueOf(statusStr));
        }
        
        progress.setIsCompleted(progress.getProgress() != null && progress.getProgress() >= 1.0);
        
        Timestamp startedAt = rs.getTimestamp("started_at");
        if (startedAt != null) {
            progress.setStartedAt(startedAt.toLocalDateTime());
        }
        
        Timestamp finishedAt = rs.getTimestamp("finished_at");
        if (finishedAt != null) {
            progress.setFinishedAt(finishedAt.toLocalDateTime());
        }
        
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
        
        Long syncVersion = rs.getLong("sync_version");
        if (!rs.wasNull()) {
            progress.setSyncVersion(syncVersion);
        }
        
        String notes = rs.getString("notes");
        if (notes != null) {
            progress.setNotes(notes);
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