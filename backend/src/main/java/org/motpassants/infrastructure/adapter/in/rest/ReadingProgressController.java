package org.motpassants.infrastructure.adapter.in.rest;

import org.motpassants.application.service.ReadingProgressService;
import org.motpassants.domain.core.model.ReadingProgress;
import org.motpassants.infrastructure.adapter.in.rest.dto.ReadingProgressRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.ReadingProgressResponseDto;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.*;

/**
 * REST controller for reading progress operations.
 * Inbound adapter that translates HTTP requests to use case calls.
 */
@Path("/v1/books")
@Tag(name = "Reading Progress", description = "Reading progress tracking operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReadingProgressController {

    private final ReadingProgressService readingProgressService;

    @Inject
    public ReadingProgressController(ReadingProgressService readingProgressService) {
        this.readingProgressService = readingProgressService;
    }

    @PUT
    @Path("/{bookId}/progress")
    @Operation(summary = "Update reading progress", description = "Update or create reading progress for a book")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Progress updated successfully",
                    content = @Content(schema = @Schema(implementation = ReadingProgressResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid request data"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response updateProgress(
            @Parameter(description = "Book UUID") @PathParam("bookId") String bookId,
            ReadingProgressRequestDto request) {
        
        try {
            // TODO: Get actual user ID from security context
            // For now, using a mock user ID
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID bookUuid = UUID.fromString(bookId);
            
            // Validate progress value
            if (request.getProgress() != null && (request.getProgress() < 0.0 || request.getProgress() > 1.0)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Progress must be between 0.0 and 1.0"))
                        .build();
            }
            
            ReadingProgress progress = readingProgressService.updateReadingProgress(
                userId, 
                bookUuid, 
                request.getProgress(), 
                request.getCurrentPage(), 
                request.getTotalPages(),
                request.getProgressLocator()
            );
            
            // Update notes if provided
            if (request.getNotes() != null) {
                progress.setNotes(request.getNotes());
            }
            
            return Response.ok(toResponseDto(progress)).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{bookId}/progress")
    @Operation(summary = "Get reading progress", description = "Retrieve reading progress for a book")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Progress retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ReadingProgressResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Progress not found"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response getProgress(
            @Parameter(description = "Book UUID") @PathParam("bookId") String bookId) {
        
        try {
            // TODO: Get actual user ID from security context
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID bookUuid = UUID.fromString(bookId);
            
            Optional<ReadingProgress> progress = readingProgressService.getReadingProgress(userId, bookUuid);
            
            if (progress.isPresent()) {
                return Response.ok(toResponseDto(progress.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Reading progress not found"))
                        .build();
            }
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{bookId}/progress/finish")
    @Operation(summary = "Mark book as finished", description = "Mark a book as finished/completed")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Book marked as finished",
                    content = @Content(schema = @Schema(implementation = ReadingProgressResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid request"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response markAsFinished(
            @Parameter(description = "Book UUID") @PathParam("bookId") String bookId) {
        
        try {
            // TODO: Get actual user ID from security context
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID bookUuid = UUID.fromString(bookId);
            
            ReadingProgress progress = readingProgressService.markAsCompleted(userId, bookUuid);
            
            return Response.ok(toResponseDto(progress)).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{bookId}/progress/start")
    @Operation(summary = "Mark book as started", description = "Mark a book as started reading")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Book marked as started",
                    content = @Content(schema = @Schema(implementation = ReadingProgressResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid request"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response markAsStarted(
            @Parameter(description = "Book UUID") @PathParam("bookId") String bookId) {
        
        try {
            // TODO: Get actual user ID from security context
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID bookUuid = UUID.fromString(bookId);
            
            ReadingProgress progress = readingProgressService.markAsStarted(userId, bookUuid);
            
            return Response.ok(toResponseDto(progress)).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/{bookId}/progress/dnf")
    @Operation(summary = "Mark book as DNF", description = "Mark a book as Did Not Finish")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Book marked as DNF",
                    content = @Content(schema = @Schema(implementation = ReadingProgressResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid request"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response markAsDNF(
            @Parameter(description = "Book UUID") @PathParam("bookId") String bookId) {
        
        try {
            // TODO: Get actual user ID from security context
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID bookUuid = UUID.fromString(bookId);
            
            ReadingProgress progress = readingProgressService.markAsDNF(userId, bookUuid);
            
            return Response.ok(toResponseDto(progress)).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{bookId}/progress")
    @Operation(summary = "Delete reading progress", description = "Delete reading progress for a book")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Progress deleted successfully"),
        @APIResponse(responseCode = "400", description = "Invalid request"),
        @APIResponse(responseCode = "401", description = "User not authenticated")
    })
    public Response deleteProgress(
            @Parameter(description = "Book UUID") @PathParam("bookId") String bookId) {
        
        try {
            // TODO: Get actual user ID from security context
            UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID bookUuid = UUID.fromString(bookId);
            
            readingProgressService.deleteReadingProgress(userId, bookUuid);
            
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Convert domain model to DTO.
     */
    private ReadingProgressResponseDto toResponseDto(ReadingProgress progress) {
        ReadingProgressResponseDto dto = new ReadingProgressResponseDto();
        dto.setId(progress.getId());
        dto.setBookId(progress.getBookId());
        dto.setUserId(progress.getUserId());
        dto.setCurrentPage(progress.getCurrentPage());
        dto.setTotalPages(progress.getTotalPages());
        dto.setProgress(progress.getProgress());
        dto.setProgressPercentage(progress.getProgressPercentage());
        dto.setStatus(progress.getStatus() != null ? progress.getStatus().name() : null);
        dto.setIsCompleted(progress.getIsCompleted());
        dto.setStartedAt(progress.getStartedAt());
        dto.setFinishedAt(progress.getFinishedAt());
        dto.setLastReadAt(progress.getLastReadAt());
        dto.setCreatedAt(progress.getCreatedAt());
        dto.setUpdatedAt(progress.getUpdatedAt());
        dto.setNotes(progress.getNotes());
        return dto;
    }
}
