package org.motpassants.infrastructure.adapter.in.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.motpassants.domain.core.model.*;
import org.motpassants.domain.port.in.BatchOperationUseCase;
import org.motpassants.infrastructure.adapter.in.rest.dto.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for batch operations on books.
 * Handles bulk edit and delete operations with proper error handling.
 */
@Path("/api/batch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BatchOperationController {
    
    private final BatchOperationUseCase batchOperationUseCase;
    
    @Inject
    public BatchOperationController(BatchOperationUseCase batchOperationUseCase) {
        this.batchOperationUseCase = batchOperationUseCase;
    }
    
    /**
     * Execute a batch edit operation on multiple books.
     */
    @POST
    @Path("/edit")
    public Response executeBatchEdit(BatchOperationRequestDto request) {
        try {
            // Validate request
            if (request.bookIds() == null || request.bookIds().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Book IDs list cannot be empty"))
                    .build();
            }
            
            if (request.editRequest() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Edit request cannot be null"))
                    .build();
            }
            
            // Convert DTO to domain model
            var editRequest = mapToDomainEditRequest(request.editRequest());
            
            if (!editRequest.hasChanges()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Edit request must contain at least one change"))
                    .build();
            }
            
            // For now, use a dummy user ID (will be replaced with actual authentication)
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            
            // Execute batch edit
            var operation = batchOperationUseCase.executeBatchEdit(
                request.bookIds(), editRequest, userId);
            
            // Convert to response DTO
            var responseDto = mapToResponseDto(operation);
            
            return Response.ok(responseDto).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Execute a batch delete operation on multiple books.
     */
    @POST
    @Path("/delete")
    public Response executeBatchDelete(BatchDeleteRequestDto request) {
        try {
            // Validate request
            if (request.bookIds() == null || request.bookIds().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Book IDs list cannot be empty"))
                    .build();
            }
            
            // For now, use a dummy user ID (will be replaced with actual authentication)
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            
            // Execute batch delete
            var operation = batchOperationUseCase.executeBatchDelete(
                request.bookIds(), userId);
            
            // Convert to response DTO
            var responseDto = mapToResponseDto(operation);
            
            return Response.ok(responseDto).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Preview the changes that would be made by a batch edit operation.
     */
    @POST
    @Path("/preview")
    public Response previewBatchEdit(BatchOperationRequestDto request) {
        try {
            // Validate request
            if (request.bookIds() == null || request.bookIds().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Book IDs list cannot be empty"))
                    .build();
            }
            
            if (request.editRequest() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Edit request cannot be null"))
                    .build();
            }
            
            // Convert DTO to domain model
            var editRequest = mapToDomainEditRequest(request.editRequest());
            
            // Generate preview
            var previews = batchOperationUseCase.previewBatchEdit(
                request.bookIds(), editRequest);
            
            // Convert to response DTOs
            var previewDtos = previews.stream()
                .map(this::mapToPreviewDto)
                .collect(Collectors.toList());
            
            return Response.ok(previewDtos).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Get the status and results of a batch operation.
     */
    @GET
    @Path("/operations/{operationId}")
    public Response getBatchOperation(@PathParam("operationId") UUID operationId) {
        try {
            var operationOpt = batchOperationUseCase.getBatchOperation(operationId);
            
            if (operationOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Batch operation not found"))
                    .build();
            }
            
            var responseDto = mapToResponseDto(operationOpt.get());
            return Response.ok(responseDto).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Cancel a running batch operation.
     */
    @POST
    @Path("/operations/{operationId}/cancel")
    public Response cancelBatchOperation(@PathParam("operationId") UUID operationId) {
        try {
            // For now, use a dummy user ID (will be replaced with actual authentication)
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            
            boolean cancelled = batchOperationUseCase.cancelBatchOperation(operationId, userId);
            
            if (!cancelled) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("Batch operation not found or cannot be cancelled"))
                    .build();
            }
            
            return Response.ok(new SuccessResponse("Batch operation cancelled successfully")).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Get recent batch operations for the current user.
     */
    @GET
    @Path("/operations")
    public Response getRecentBatchOperations(@QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            // For now, use a dummy user ID (will be replaced with actual authentication)
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            
            var operations = batchOperationUseCase.getRecentBatchOperations(userId, limit);
            
            var responseDtos = operations.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
            
            return Response.ok(responseDtos).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Maps DTO to domain model for batch edit request.
     */
    private BatchEditRequest mapToDomainEditRequest(BatchEditRequestDto dto) {
        return BatchEditRequest.builder()
            .title(dto.title())
            .subtitle(dto.subtitle())
            .description(dto.description())
            .authors(dto.authors())
            .seriesId(dto.seriesId())
            .seriesPosition(dto.seriesPosition())
            .tags(dto.tags())
            .language(dto.language())
            .publisher(dto.publisher())
            .isbn(dto.isbn())
            .addTags(dto.addTags())
            .addAuthors(dto.addAuthors())
            .build();
    }
    
    /**
     * Maps domain model to response DTO for batch operation.
     */
    private BatchOperationResponseDto mapToResponseDto(BatchOperation operation) {
        var resultDtos = operation.results().stream()
            .map(result -> new BatchOperationResultDto(
                result.bookId(),
                result.bookTitle(),
                result.success(),
                result.errorMessage(),
                result.changesSummary()
            ))
            .collect(Collectors.toList());
        
        int successCount = (int) operation.results().stream().filter(BatchOperationResult::success).count();
        int failureCount = operation.results().size() - successCount;
        
        BatchEditRequestDto editRequestDto = null;
        if (operation.editRequest() != null) {
            var editRequest = operation.editRequest();
            editRequestDto = new BatchEditRequestDto(
                editRequest.title().orElse(null),
                editRequest.subtitle().orElse(null),
                editRequest.description().orElse(null),
                editRequest.authors().orElse(null),
                editRequest.seriesId().orElse(null),
                editRequest.seriesPosition().orElse(null),
                editRequest.tags().orElse(null),
                editRequest.language().orElse(null),
                editRequest.publisher().orElse(null),
                editRequest.isbn().orElse(null),
                editRequest.addTags().orElse(null),
                editRequest.addAuthors().orElse(null)
            );
        }
        
        return new BatchOperationResponseDto(
            operation.operationId(),
            operation.type().name(),
            operation.bookIds(),
            editRequestDto,
            operation.userId(),
            operation.createdAt(),
            operation.status().name(),
            resultDtos,
            operation.errorMessage(),
            successCount,
            failureCount,
            operation.bookIds().size()
        );
    }
    
    /**
     * Maps domain model to DTO for batch edit preview.
     */
    private BatchEditPreviewDto mapToPreviewDto(BatchEditPreview preview) {
        return new BatchEditPreviewDto(
            preview.bookId(),
            preview.currentTitle(),
            preview.newTitle(),
            preview.currentAuthors(),
            preview.newAuthors(),
            preview.currentTags(),
            preview.newTags(),
            preview.currentLanguage(),
            preview.newLanguage(),
            preview.currentPublisher(),
            preview.newPublisher(),
            preview.currentIsbn(),
            preview.newIsbn(),
            preview.currentDescription(),
            preview.newDescription(),
            preview.hasChanges()
        );
    }
    
    /**
     * Simple error response record.
     */
    private record ErrorResponse(String message) {}
    
    /**
     * Simple success response record.
     */
    private record SuccessResponse(String message) {}
}