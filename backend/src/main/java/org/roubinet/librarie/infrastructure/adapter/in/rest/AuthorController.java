package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.AuthorUseCase;
import org.roubinet.librarie.domain.entity.Author;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.AuthorResponseDto;

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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for author operations.
 * Feature-based API with version 1 path structure.
 */
@Path("/v1/authors")
@Tag(name = "Authors", description = "Author management operations")
public class AuthorController {
    
    private final AuthorUseCase authorUseCase;
    
    @Inject
    public AuthorController(AuthorUseCase authorUseCase) {
        this.authorUseCase = authorUseCase;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all authors", description = "Retrieve all authors")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Authors retrieved successfully",
            content = @Content(schema = @Schema(implementation = AuthorResponseDto.class)))
    })
    public Response getAllAuthors() {
        try {
            List<Author> authors = authorUseCase.getAllAuthors();
            List<AuthorResponseDto> authorDtos = authors.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return Response.ok(authorDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get author by ID", description = "Retrieve a specific author by its UUID")
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
    @Operation(summary = "Search authors", description = "Search authors by name")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = AuthorResponseDto.class)))
    })
    public Response searchAuthors(
            @Parameter(description = "Search query", required = true)
            @QueryParam("q") String query) {
        
        try {
            List<Author> authors = authorUseCase.searchAuthors(query);
            List<AuthorResponseDto> authorDtos = authors.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return Response.ok(authorDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    private AuthorResponseDto toDto(Author author) {
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