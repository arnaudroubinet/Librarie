package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.AuthorUseCase;
import org.roubinet.librarie.domain.entity.Author;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.AuthorResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.PageResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;
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
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Context;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.roubinet.librarie.infrastructure.media.ImageCachingService;

/**
 * REST controller for author operations.
 * Feature-based API with version 1 path structure.
 */
@Path("/v1/authors")
@Tag(name = "Authors", description = "Author management operations")
public class AuthorController {
    
    private final AuthorUseCase authorUseCase;
    private final InputSanitizationService sanitizationService;
    private final LibrarieConfigProperties config;
    private final ImageCachingService imageCachingService;
    @Context
    Request httpRequest;
    
    @Inject
    public AuthorController(AuthorUseCase authorUseCase,
                           InputSanitizationService sanitizationService,
                           LibrarieConfigProperties config,
                           ImageCachingService imageCachingService) {
        this.authorUseCase = authorUseCase;
        this.sanitizationService = sanitizationService;
        this.config = config;
        this.imageCachingService = imageCachingService;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all authors with cursor-based pagination", description = "Retrieve all authors using keyset pagination")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Authors retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getAllAuthors(
            @Parameter(description = "Cursor for pagination", example = "eyJpZCI6IjEyMyIsInRpbWVzdGFtcCI6IjIwMjQtMDEtMDFUMTA6MDA6MDBaIn0=")
            @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items to return", example = "20")
            @DefaultValue("20") @QueryParam("limit") int limit) {
        
        try {
            // Validate and sanitize inputs
            if (limit <= 0) {
                limit = config.pagination().defaultPageSize();
            }
            if (limit > config.pagination().maxPageSize()) {
                limit = config.pagination().maxPageSize();
            }
            
            // Use cursor pagination
            CursorPageResult<Author> pageResult = authorUseCase.getAllAuthors(cursor, limit);
            
            List<AuthorResponseDto> authorDtos = pageResult.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            PageResponseDto<AuthorResponseDto> response = new PageResponseDto<>(
                authorDtos, 
                pageResult.getNextCursor(), 
                pageResult.getPreviousCursor(), 
                pageResult.getLimit(),
                pageResult.isHasNext(),
                pageResult.isHasPrevious(),
                pageResult.getTotalCount()
            );
            
            return Response.ok(response).build();
            
        } catch (SecurityException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Security validation failed: " + e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/{id}/picture")
    @Operation(summary = "Get author picture", description = "Serves the author's picture using local-first strong ETag caching; populates from metadata.imageUrl if missing")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Image bytes returned"),
        @APIResponse(responseCode = "304", description = "Not Modified"),
        @APIResponse(responseCode = "404", description = "Author or image not found")
    })
    public Response getAuthorPicture(@PathParam("id") String id) {
        try {
            UUID authorId = UUID.fromString(id);
            Optional<Author> authorOpt = authorUseCase.getAuthorById(authorId);
            if (authorOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Author not found").build();
            }
            String storedUrl = null;
            Author author = authorOpt.get();
            if (author.getMetadata() != null) {
                Object urlObj = author.getMetadata().get("imageUrl");
                if (urlObj instanceof String s && (s.startsWith("http://") || s.startsWith("https://"))) {
                    storedUrl = s.trim();
                }
            }

            java.nio.file.Path baseDir = java.nio.file.Paths.get(config.storage().baseDir());
            return imageCachingService.serveLocalFirstStrongETag(
                httpRequest,
                baseDir,
                "autors",
                "covers",
                id,
                storedUrl,
                getFailoverSvg()
            );
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid author ID format").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error: " + e.getMessage()).build();
        }
    }

    private static byte[] getFailoverSvg() {
        String svg = """
                <svg xmlns='http://www.w3.org/2000/svg' width='320' height='320' viewBox='0 0 320 320'>
                    <defs>
                        <linearGradient id='g' x1='0' y1='0' x2='1' y2='1'>
                            <stop offset='0%' stop-color='#f0f0f0'/>
                            <stop offset='100%' stop-color='#d8d8d8'/>
                        </linearGradient>
                    </defs>
                    <rect width='100%' height='100%' fill='url(#g)'/>
                    <g fill='#9a9a9a'>
                        <circle cx='160' cy='120' r='60'/>
                        <rect x='80' y='210' width='160' height='20' rx='10'/>
                    </g>
                </svg>
        """;
        return svg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get author by ID", description = "Retrieve a specific author by their UUID")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Author found",
            content = @Content(schema = @Schema(implementation = AuthorResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Author not found")
    })
    public Response getAuthorById(
            @Parameter(description = "Author UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID authorId = UUID.fromString(id);
            Optional<Author> author = authorUseCase.getAuthorById(authorId);
            
            if (author.isPresent()) {
                return Response.ok(toDto(author.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Author not found")
                    .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid author ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search authors", description = "Search authors by name with cursor pagination")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response searchAuthors(
            @Parameter(description = "Search query", required = true)
            @QueryParam("q") String query,
            @Parameter(description = "Cursor for pagination")
            @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items to return", example = "20")
            @DefaultValue("20") @QueryParam("limit") int limit) {
        
        try {
            if (query == null || query.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Search query is required")
                    .build();
            }
            
            // Sanitize search query
            String sanitizedQuery = sanitizationService.sanitizeSearchQuery(query);
            
            // Validate limit
            if (limit <= 0) {
                limit = config.pagination().defaultPageSize();
            }
            if (limit > config.pagination().maxPageSize()) {
                limit = config.pagination().maxPageSize();
            }
            
            // Use cursor pagination
            CursorPageResult<Author> pageResult = authorUseCase.searchAuthors(sanitizedQuery, cursor, limit);
            
            List<AuthorResponseDto> authorDtos = pageResult.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            PageResponseDto<AuthorResponseDto> response = new PageResponseDto<>(
                authorDtos, 
                pageResult.getNextCursor(), 
                pageResult.getPreviousCursor(), 
                pageResult.getLimit(),
                pageResult.isHasNext(),
                pageResult.isHasPrevious(),
                pageResult.getTotalCount()
            );
            
            return Response.ok(response).build();
            
        } catch (SecurityException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Security validation failed: " + e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Convert Author entity to DTO.
     */
    private AuthorResponseDto toDto(Author author) {
        if (author == null) {
            return null;
        }
        
        AuthorResponseDto dto = new AuthorResponseDto();
        dto.setId(author.getId());
        dto.setName(author.getName());
        dto.setSortName(author.getSortName());
        dto.setBio(author.getBio());
        dto.setBirthDate(author.getBirthDate());
        dto.setDeathDate(author.getDeathDate());
        dto.setWebsiteUrl(author.getWebsiteUrl());
        dto.setMetadata(author.getMetadata());
        dto.setCreatedAt(author.getCreatedAt());
        dto.setUpdatedAt(author.getUpdatedAt());
        
        return dto;
    }
}