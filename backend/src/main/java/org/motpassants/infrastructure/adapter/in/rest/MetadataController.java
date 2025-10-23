package org.motpassants.infrastructure.adapter.in.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.motpassants.domain.port.in.MetadataUseCase;
import org.motpassants.infrastructure.adapter.in.rest.dto.metadata.MetadataSearchResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for metadata operations.
 * Provides endpoints for searching book metadata.
 * 
 * Note: Applying metadata to books is handled by the frontend,
 * which uses these search endpoints and then updates books via BookService.
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


}
