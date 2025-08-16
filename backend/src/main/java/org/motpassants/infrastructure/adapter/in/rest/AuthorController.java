package org.motpassants.infrastructure.adapter.in.rest;

import org.motpassants.application.service.AuthorService;
import org.motpassants.domain.core.model.Author;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.infrastructure.adapter.in.rest.dto.AuthorRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.AuthorResponseDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.PageResponseDto;

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
import java.util.stream.Collectors;

/**
 * REST controller for author operations.
 * Inbound adapter that translates HTTP requests to use case calls.
 * Infrastructure layer component.
 */
@Path("/v1/authors")
@Tag(name = "Authors", description = "Author management operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorController {

    private final AuthorService authorService;

    @Inject
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GET
    @Operation(summary = "Get all authors", description = "Retrieve all authors with pagination")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Authors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public Response getAllAuthors(
            @Parameter(description = "Cursor for pagination")
            @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items to return (max 100)", example = "20")
            @DefaultValue("20") @QueryParam("limit") int limit) {
        
        try {
            PageResult<Author> pageResult = authorService.getAllAuthors(cursor, limit);
            
            List<AuthorResponseDto> authorDtos = pageResult.getItems().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
                
            PageResponseDto.PaginationMetadata metadata = new PageResponseDto.PaginationMetadata();
            metadata.setTotalElements(pageResult.getTotalCount());
            metadata.setPageSize(limit);
            metadata.setNextCursor(pageResult.getNextCursor());
            metadata.setPreviousCursor(pageResult.getPreviousCursor());
            
            PageResponseDto<AuthorResponseDto> response = new PageResponseDto<>(authorDtos, metadata);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Internal server error"))
                .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get author by ID", description = "Retrieve a specific author by their UUID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Author found",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Author not found"),
        @APIResponse(responseCode = "400", description = "Invalid author ID")
    })
    public Response getAuthorById(
            @Parameter(description = "Author UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID authorId = UUID.fromString(id);
            Optional<Author> author = authorService.getAuthorById(authorId);
            
            if (author.isPresent()) {
                return Response.ok(toResponseDto(author.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Author not found"))
                    .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Invalid author ID format"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Internal server error"))
                .build();
        }
    }

    @POST
    @Operation(summary = "Create new author", description = "Create a new author with the provided information")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Author created successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid input data"),
        @APIResponse(responseCode = "409", description = "Author with same name already exists")
    })
    public Response createAuthor(AuthorRequestDto requestDto) {
        try {
            Author author = authorService.createAuthor(
                requestDto.getName(),
                requestDto.getSortName(),
                requestDto.getBio(),
                requestDto.getBirthDate(),
                requestDto.getDeathDate(),
                requestDto.getWebsiteUrl(),
                requestDto.getMetadata()
            );
            
            return Response.status(Response.Status.CREATED)
                .entity(toResponseDto(author))
                .build();
                
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Internal server error"))
                .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update author", description = "Update an existing author with the provided information")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Author updated successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid input data"),
        @APIResponse(responseCode = "404", description = "Author not found"),
        @APIResponse(responseCode = "409", description = "Author with same name already exists")
    })
    public Response updateAuthor(
            @Parameter(description = "Author UUID", required = true)
            @PathParam("id") String id,
            AuthorRequestDto requestDto) {
        
        try {
            UUID authorId = UUID.fromString(id);
            Author author = authorService.updateAuthor(
                authorId,
                requestDto.getName(),
                requestDto.getSortName(),
                requestDto.getBio(),
                requestDto.getBirthDate(),
                requestDto.getDeathDate(),
                requestDto.getWebsiteUrl(),
                requestDto.getMetadata()
            );
            
            return Response.ok(toResponseDto(author)).build();
            
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Internal server error"))
                .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete author", description = "Delete an existing author by their UUID")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Author deleted successfully"),
        @APIResponse(responseCode = "404", description = "Author not found"),
        @APIResponse(responseCode = "400", description = "Invalid author ID")
    })
    public Response deleteAuthor(
            @Parameter(description = "Author UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID authorId = UUID.fromString(id);
            authorService.deleteAuthor(authorId);
            
            return Response.noContent().build();
            
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Internal server error"))
                .build();
        }
    }

    @GET
    @Path("/search")
    @Operation(summary = "Search authors", description = "Search authors by name with pagination")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = PageResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public Response searchAuthors(
            @Parameter(description = "Search query", required = true)
            @QueryParam("q") String query,
            @Parameter(description = "Cursor for pagination")
            @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items to return (max 100)", example = "20")
            @DefaultValue("20") @QueryParam("limit") int limit) {
        
        try {
            if (query == null || query.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Search query is required"))
                    .build();
            }
            
            PageResult<Author> pageResult = authorService.searchAuthors(query, cursor, limit);
            
            List<AuthorResponseDto> authorDtos = pageResult.getItems().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
                
            PageResponseDto.PaginationMetadata metadata = new PageResponseDto.PaginationMetadata();
            metadata.setTotalElements(pageResult.getTotalCount());
            metadata.setPageSize(limit);
            metadata.setNextCursor(pageResult.getNextCursor());
            metadata.setPreviousCursor(pageResult.getPreviousCursor());
            
            PageResponseDto<AuthorResponseDto> response = new PageResponseDto<>(authorDtos, metadata);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Internal server error"))
                .build();
        }
    }

    @GET
    @Path("/{id}/picture")
    @Operation(summary = "Get author picture", description = "Retrieve author's picture image")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Image returned"),
        @APIResponse(responseCode = "404", description = "Author or image not found"),
        @APIResponse(responseCode = "400", description = "Invalid author ID")
    })
    public Response getAuthorPicture(
            @Parameter(description = "Author UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID authorId = UUID.fromString(id);
            Optional<Author> author = authorService.getAuthorById(authorId);
            
            if (author.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Author not found"))
                    .build();
            }
            
            // For now, return a simple SVG placeholder
            // TODO: Implement proper image handling
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
            
            return Response.ok(svg.getBytes())
                .type("image/svg+xml")
                .build();
                
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "Invalid author ID format"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Internal server error"))
                .build();
        }
    }

    /**
     * Convert Author domain object to response DTO.
     */
    private AuthorResponseDto toResponseDto(Author author) {
        return new AuthorResponseDto(
            author.getId(),
            author.getName(),
            author.getSortName(),
            author.getBio(),
            author.getBirthDate(),
            author.getDeathDate(),
            author.getWebsiteUrl(),
            author.getMetadata(),
            author.getCreatedAt(),
            author.getUpdatedAt()
        );
    }
}