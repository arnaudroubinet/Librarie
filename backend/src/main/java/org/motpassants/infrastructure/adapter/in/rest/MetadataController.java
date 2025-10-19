package org.motpassants.infrastructure.adapter.in.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.MetadataSearchResult;
import org.motpassants.domain.port.in.MetadataUseCase;
import org.motpassants.infrastructure.adapter.in.rest.dto.metadata.ApplyMetadataRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.metadata.MetadataSearchResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for metadata operations.
 * Provides endpoints for searching and applying book metadata.
 */
@Path("/api/metadata")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Metadata", description = "Book metadata operations")
public class MetadataController {

    private static final Logger log = LoggerFactory.getLogger(MetadataController.class);

    @Inject
    MetadataUseCase metadataUseCase;

    @GET
    @Path("/search/isbn/{isbn}")
    @Operation(summary = "Search metadata by ISBN", 
               description = "Search for book metadata across all providers using ISBN")
    @APIResponse(responseCode = "200", description = "Metadata search results")
    @APIResponse(responseCode = "400", description = "Invalid ISBN")
    public CompletableFuture<Response> searchByIsbn(
            @Parameter(description = "ISBN to search for (ISBN-10 or ISBN-13)", required = true)
            @PathParam("isbn") String isbn) {
        
        log.info("REST: Searching metadata by ISBN: {}", isbn);
        
        if (isbn == null || isbn.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\": \"ISBN cannot be empty\"}")
                            .build());
        }

        return metadataUseCase.searchMetadataByIsbn(isbn)
                .thenApply(results -> {
                    List<MetadataSearchResponseDto> dtos = MetadataSearchResponseDto.fromDomainList(results);
                    log.info("REST: Returning {} metadata results for ISBN {}", dtos.size(), isbn);
                    return Response.ok(dtos).build();
                })
                .exceptionally(throwable -> {
                    log.error("Error searching metadata by ISBN: {}", isbn, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"error\": \"Failed to search metadata: " + throwable.getMessage() + "\"}")
                            .build();
                });
    }

    @GET
    @Path("/search/title")
    @Operation(summary = "Search metadata by title and author", 
               description = "Search for book metadata across all providers using title and optional author")
    @APIResponse(responseCode = "200", description = "Metadata search results")
    @APIResponse(responseCode = "400", description = "Invalid parameters")
    public CompletableFuture<Response> searchByTitleAndAuthor(
            @Parameter(description = "Book title to search for", required = true)
            @QueryParam("title") String title,
            @Parameter(description = "Book author to refine search (optional)")
            @QueryParam("author") String author) {
        
        log.info("REST: Searching metadata by title: {}, author: {}", title, author);
        
        if (title == null || title.trim().isEmpty()) {
            return CompletableFuture.completedFuture(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\": \"Title cannot be empty\"}")
                            .build());
        }

        return metadataUseCase.searchMetadataByTitleAndAuthor(title, author)
                .thenApply(results -> {
                    List<MetadataSearchResponseDto> dtos = MetadataSearchResponseDto.fromDomainList(results);
                    log.info("REST: Returning {} metadata results for title/author search", dtos.size());
                    return Response.ok(dtos).build();
                })
                .exceptionally(throwable -> {
                    log.error("Error searching metadata by title/author", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"error\": \"Failed to search metadata: " + throwable.getMessage() + "\"}")
                            .build();
                });
    }

    @GET
    @Path("/books/{bookId}/fetch")
    @Operation(summary = "Fetch metadata for a book", 
               description = "Automatically fetch the best matching metadata for a book by its ID")
    @APIResponse(responseCode = "200", description = "Best metadata match found")
    @APIResponse(responseCode = "404", description = "Book not found or no metadata found")
    public CompletableFuture<Response> fetchMetadataForBook(
            @Parameter(description = "UUID of the book", required = true)
            @PathParam("bookId") String bookIdStr) {
        
        log.info("REST: Fetching metadata for book: {}", bookIdStr);
        
        UUID bookId;
        try {
            bookId = UUID.fromString(bookIdStr);
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity("{\"error\": \"Invalid book ID format\"}")
                            .build());
        }

        return metadataUseCase.fetchMetadataForBook(bookId)
                .thenApply(result -> {
                    MetadataSearchResponseDto dto = MetadataSearchResponseDto.fromDomain(result);
                    log.info("REST: Found metadata for book {} from {}", bookId, result.getSource());
                    return Response.ok(dto).build();
                })
                .exceptionally(throwable -> {
                    log.error("Error fetching metadata for book: {}", bookId, throwable);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\": \"No metadata found: " + throwable.getMessage() + "\"}")
                            .build();
                });
    }

    @POST
    @Path("/books/{bookId}/apply")
    @Operation(summary = "Apply metadata to a book", 
               description = "Apply selected metadata result to a book")
    @APIResponse(responseCode = "200", description = "Metadata applied successfully")
    @APIResponse(responseCode = "400", description = "Invalid request")
    @APIResponse(responseCode = "404", description = "Book not found")
    public Response applyMetadata(
            @Parameter(description = "UUID of the book", required = true)
            @PathParam("bookId") String bookIdStr,
            @Parameter(description = "Metadata to apply", required = true)
            MetadataSearchResponseDto metadata,
            @Parameter(description = "Whether to download cover image")
            @QueryParam("downloadCover") @DefaultValue("false") boolean downloadCover) {
        
        log.info("REST: Applying metadata to book: {}", bookIdStr);
        
        UUID bookId;
        try {
            bookId = UUID.fromString(bookIdStr);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Invalid book ID format\"}")
                    .build();
        }

        if (metadata == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Metadata cannot be null\"}")
                    .build();
        }

        try {
            // Convert DTO to domain model
            MetadataSearchResult domainMetadata = convertToDomain(metadata);
            
            // Apply metadata
            Book updatedBook = metadataUseCase.applyMetadataToBook(bookId, domainMetadata, downloadCover);
            
            log.info("REST: Successfully applied metadata to book {}", bookId);
            return Response.ok("{\"success\": true, \"message\": \"Metadata applied successfully\"}").build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument applying metadata: {}", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            log.error("Error applying metadata to book: {}", bookId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Failed to apply metadata: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Convert DTO back to domain model.
     */
    private MetadataSearchResult convertToDomain(MetadataSearchResponseDto dto) {
        return MetadataSearchResult.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .authors(dto.getAuthors())
                .isbn10(dto.getIsbn10())
                .isbn13(dto.getIsbn13())
                .description(dto.getDescription())
                .pageCount(dto.getPageCount())
                .publisher(dto.getPublisher())
                .publishedDate(dto.getPublishedDate())
                .language(dto.getLanguage())
                .categories(dto.getCategories())
                .coverImageUrl(dto.getCoverImageUrl())
                .averageRating(dto.getAverageRating())
                .ratingsCount(dto.getRatingsCount())
                .providerBookId(dto.getProviderBookId())
                .confidenceScore(dto.getConfidenceScore())
                .build();
    }
}
