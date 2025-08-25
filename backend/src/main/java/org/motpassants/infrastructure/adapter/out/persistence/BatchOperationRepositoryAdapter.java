package org.motpassants.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.*;
import org.motpassants.domain.port.out.BatchOperationRepository;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Database adapter implementing batch operation repository using JDBC.
 */
@ApplicationScoped
public class BatchOperationRepositoryAdapter implements BatchOperationRepository {
    
    @Inject
    AgroalDataSource dataSource;
    
    @Inject
    ObjectMapper objectMapper;
    
    @Override
    public BatchOperation save(BatchOperation operation) {
        String sql = """
            INSERT INTO batch_operations (
                id, type, book_ids, edit_request, user_id, created_at, status, results, error_message
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setObject(1, operation.operationId());
            stmt.setString(2, operation.type().name());
            stmt.setString(3, objectMapper.writeValueAsString(operation.bookIds()));
            stmt.setString(4, operation.editRequest() != null ? 
                objectMapper.writeValueAsString(operation.editRequest()) : null);
            stmt.setObject(5, operation.userId());
            stmt.setTimestamp(6, Timestamp.from(operation.createdAt().toInstant()));
            stmt.setString(7, operation.status().name());
            stmt.setString(8, objectMapper.writeValueAsString(operation.results()));
            stmt.setString(9, operation.errorMessage());
            
            stmt.executeUpdate();
            return operation;
            
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Failed to save batch operation", e);
        }
    }
    
    @Override
    public Optional<BatchOperation> findById(UUID operationId) {
        String sql = """
            SELECT id, type, book_ids, edit_request, user_id, created_at, status, results, error_message
            FROM batch_operations WHERE id = ?
        """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setObject(1, operationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBatchOperation(rs));
                }
                return Optional.empty();
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find batch operation", e);
        }
    }
    
    @Override
    public List<BatchOperation> findRecentByUserId(UUID userId, int limit) {
        String sql = """
            SELECT id, type, book_ids, edit_request, user_id, created_at, status, results, error_message
            FROM batch_operations WHERE user_id = ?
            ORDER BY created_at DESC LIMIT ?
        """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setObject(1, userId);
            stmt.setInt(2, limit);
            
            List<BatchOperation> operations = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    operations.add(mapResultSetToBatchOperation(rs));
                }
            }
            return operations;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find recent batch operations", e);
        }
    }
    
    @Override
    public BatchOperation update(UUID operationId, BatchOperation operation) {
        String sql = """
            UPDATE batch_operations SET
                type = ?, book_ids = ?, edit_request = ?, user_id = ?, 
                created_at = ?, status = ?, results = ?, error_message = ?
            WHERE id = ?
        """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, operation.type().name());
            stmt.setString(2, objectMapper.writeValueAsString(operation.bookIds()));
            stmt.setString(3, operation.editRequest() != null ? 
                objectMapper.writeValueAsString(operation.editRequest()) : null);
            stmt.setObject(4, operation.userId());
            stmt.setTimestamp(5, Timestamp.from(operation.createdAt().toInstant()));
            stmt.setString(6, operation.status().name());
            stmt.setString(7, objectMapper.writeValueAsString(operation.results()));
            stmt.setString(8, operation.errorMessage());
            stmt.setObject(9, operationId);
            
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Batch operation not found for update: " + operationId);
            }
            
            return operation;
            
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Failed to update batch operation", e);
        }
    }
    
    @Override
    public int deleteOldOperations(int olderThanDays) {
        String sql = "DELETE FROM batch_operations WHERE created_at < ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            Timestamp cutoffDate = Timestamp.from(
                OffsetDateTime.now().minusDays(olderThanDays).toInstant()
            );
            stmt.setTimestamp(1, cutoffDate);
            
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete old batch operations", e);
        }
    }
    
    private BatchOperation mapResultSetToBatchOperation(ResultSet rs) throws SQLException {
        try {
            UUID operationId = (UUID) rs.getObject("id");
            BatchOperationType type = BatchOperationType.valueOf(rs.getString("type"));
            
            // Use TypeReference for proper generic type deserialization
            List<UUID> bookIds = objectMapper.readValue(
                rs.getString("book_ids"), 
                objectMapper.getTypeFactory().constructCollectionType(List.class, UUID.class)
            );
            
            BatchEditRequest editRequest = null;
            String editRequestJson = rs.getString("edit_request");
            if (editRequestJson != null) {
                editRequest = objectMapper.readValue(editRequestJson, BatchEditRequest.class);
            }
            
            UUID userId = (UUID) rs.getObject("user_id");
            OffsetDateTime createdAt = rs.getTimestamp("created_at").toInstant().atOffset(java.time.ZoneOffset.UTC);
            BatchOperationStatus status = BatchOperationStatus.valueOf(rs.getString("status"));
            
            // Use TypeReference for proper generic type deserialization
            List<BatchOperationResult> results = objectMapper.readValue(
                rs.getString("results"),
                objectMapper.getTypeFactory().constructCollectionType(List.class, BatchOperationResult.class)
            );
            
            String errorMessage = rs.getString("error_message");
            
            return new BatchOperation(
                operationId, type, bookIds, editRequest, userId, 
                createdAt, status, results, errorMessage
            );
            
        } catch (JsonProcessingException e) {
            throw new SQLException("Failed to parse JSON fields", e);
        }
    }
}