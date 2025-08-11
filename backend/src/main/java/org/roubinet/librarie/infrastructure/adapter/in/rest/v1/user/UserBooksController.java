package org.roubinet.librarie.infrastructure.adapter.in.rest.v1.user;

import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.domain.entity.BookSeries;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.BookResponseDto;
import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;
import org.roubinet.librarie.infrastructure.security.InputSanitizationService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;
import java.util.UUID;

/**
 * User REST controller for book-related user operations.
 * Implements user-specific book actions like reading progress, favorites, etc.
 * Version 1 API following REST best practices.
 */
@Path("/v1/user/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User Books", description = "User-specific book operations")
public class UserBooksController {
    
    private final BookUseCase bookUseCase;
    private final InputSanitizationService sanitizationService;
    private final LibrarieConfigProperties config;
    
    @Inject
    public UserBooksController(BookUseCase bookUseCase,
                              InputSanitizationService sanitizationService,
                              LibrarieConfigProperties config) {
        this.bookUseCase = bookUseCase;
        this.sanitizationService = sanitizationService;
        this.config = config;
    }
    
    @POST
    @Path("/{id}/completion")
    @Operation(summary = "Update reading completion", description = "Update the reading progress for a specific book")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Reading progress updated successfully"),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid progress data")
    })
    public Response updateReadingCompletion(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id,
            @Parameter(description = "Reading progress (0-100)", required = true, example = "75")
            CompletionRequest request) {
        
        try {
            UUID bookId = UUID.fromString(id);
            
            if (request == null || request.getProgress() < 0 || request.getProgress() > 100) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid progress value. Must be between 0 and 100.")
                    .build();
            }
            
            Optional<Book> book = bookUseCase.getBookById(bookId);
            if (book.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            // In a real implementation, this would update user-specific reading progress
            // For now, just acknowledge the operation
            
            return Response.ok()
                .entity("{\"message\": \"Reading progress updated to " + request.getProgress() + "%\"}")
                .build();
                
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid book ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @POST
    @Path("/{id}/favorite")
    @Operation(summary = "Add book to favorites", description = "Add a book to user's favorites list")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Book added to favorites"),
        @APIResponse(responseCode = "404", description = "Book not found")
    })
    public Response addToFavorites(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = UUID.fromString(id);
            
            Optional<Book> book = bookUseCase.getBookById(bookId);
            if (book.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            // In a real implementation, this would add to user's favorites
            // For now, just acknowledge the operation
            
            return Response.ok()
                .entity("{\"message\": \"Book added to favorites\"}")
                .build();
                
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid book ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @DELETE
    @Path("/{id}/favorite")
    @Operation(summary = "Remove book from favorites", description = "Remove a book from user's favorites list")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Book removed from favorites"),
        @APIResponse(responseCode = "404", description = "Book not found")
    })
    public Response removeFromFavorites(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = UUID.fromString(id);
            
            Optional<Book> book = bookUseCase.getBookById(bookId);
            if (book.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            // In a real implementation, this would remove from user's favorites
            // For now, just acknowledge the operation
            
            return Response.noContent().build();
                
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid book ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @GET
    @Path("/{id}")
    @Operation(summary = "Get book details for user", description = "Get book details with user-specific information")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Book details retrieved successfully",
            content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Book not found")
    })
    public Response getBookDetailsForUser(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = UUID.fromString(id);
            Optional<Book> book = bookUseCase.getBookById(bookId);
            
            if (book.isPresent()) {
                BookResponseDto dto = toDto(book.get());
                // In a real implementation, this would include user-specific data like reading progress
                return Response.ok(dto).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid book ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Convert Book entity to DTO.
     */
    private BookResponseDto toDto(Book book) {
        if (book == null) {
            return null;
        }
        
        BookResponseDto dto = new BookResponseDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setTitleSort(book.getTitleSort());
        dto.setIsbn(book.getIsbn());
        dto.setPath(book.getPath());
        dto.setFileSize(book.getFileSize());
        dto.setFileHash(book.getFileHash());
        dto.setHasCover(book.getHasCover());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        dto.setPublicationDate(book.getPublicationDate());
        dto.setMetadata(book.getMetadata());
        
        // Handle language
        if (book.getLanguage() != null) {
            dto.setLanguage(book.getLanguage().getName());
        }
        
        // Handle publisher
        if (book.getPublisher() != null) {
            dto.setPublisher(book.getPublisher().getName());
        }
        
        // Handle author - extract from relationships or metadata
        String authorName = extractAuthorFromBook(book);
        dto.setAuthor(authorName);
        
        // Handle series - extract from relationships
        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
            BookSeries firstSeries = book.getSeries().iterator().next();
            if (firstSeries.series() != null) {
                dto.setSeries(firstSeries.series().getName());
                dto.setSeriesIndex(firstSeries.seriesIndex().intValue());
            }
        }
        
        // Handle description - extract from metadata if available
        String description = extractDescriptionFromBook(book);
        dto.setDescription(description);
        
        return dto;
    }
    
    /**
     * Extract author name from book relationships or metadata.
     */
    private String extractAuthorFromBook(Book book) {
        // In a real implementation, this would extract from author relationships
        // For now, try to get from metadata or return default
        if (book.getMetadata() != null && book.getMetadata().containsKey("author")) {
            return (String) book.getMetadata().get("author");
        }
        return "Unknown Author";
    }
    
    /**
     * Extract description from book metadata.
     */
    private String extractDescriptionFromBook(Book book) {
        if (book.getMetadata() != null && book.getMetadata().containsKey("description")) {
            return (String) book.getMetadata().get("description");
        }
        return null;
    }
    
    /**
     * Request DTO for completion updates.
     */
    public static class CompletionRequest {
        @Schema(description = "Reading progress percentage (0-100)", example = "75")
        private int progress;
        
        public int getProgress() {
            return progress;
        }
        
        public void setProgress(int progress) {
            this.progress = progress;
        }
    }
}