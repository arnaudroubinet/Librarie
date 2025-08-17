package org.motpassants.infrastructure.adapter.in.rest;

import org.motpassants.domain.core.model.UnifiedSearchResult;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.Author;
import org.motpassants.domain.core.model.Series;
import org.motpassants.domain.port.in.UnifiedSearchUseCase;
import org.motpassants.infrastructure.adapter.in.rest.dto.UnifiedSearchResultDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.UnifiedSearchRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.BookResponseDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.AuthorResponseDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.SeriesResponseDto;

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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for unified search operations.
 * Provides the adapter for cross-entity search in the hexagonal architecture.
 */
@Path("/v1/search")
@Tag(name = "Search", description = "Unified search operations across all entities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UnifiedSearchController {
    
    private final UnifiedSearchUseCase unifiedSearchUseCase;
    
    @Inject
    public UnifiedSearchController(UnifiedSearchUseCase unifiedSearchUseCase) {
        this.unifiedSearchUseCase = unifiedSearchUseCase;
    }
    
    @GET
    @Operation(summary = "Unified search", description = "Search across books, authors, and series")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = UnifiedSearchResultDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public Response unifiedSearch(
            @Parameter(description = "Search query", required = true)
            @QueryParam("q") String query,
            @Parameter(description = "Maximum number of results per entity type", example = "10")
            @DefaultValue("10") @QueryParam("limit") int limit,
            @Parameter(description = "Entity types to search (comma-separated)", example = "books,authors")
            @QueryParam("types") String types) {
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Validate query parameter
            if (query == null || query.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Search query 'q' is required and cannot be empty"))
                    .build();
            }
            
            // Parse and validate entity types
            List<String> entityTypes = null;
            if (types != null && !types.trim().isEmpty()) {
                entityTypes = Arrays.stream(types.split(","))
                    .map(String::trim)
                    .filter(type -> !type.isEmpty())
                    .collect(Collectors.toList());
                
                // Validate entity types
                List<String> validTypes = Arrays.asList("books", "authors", "series");
                for (String type : entityTypes) {
                    if (!validTypes.contains(type)) {
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("message", "Invalid entity type: " + type + ". Valid types are: books, authors, series"))
                            .build();
                    }
                }
            }
            
            // Perform unified search
            UnifiedSearchResult result = unifiedSearchUseCase.unifiedSearch(query, limit, entityTypes);
            
            // Convert to DTOs
            List<BookResponseDto> bookDtos = result.getBooks().stream()
                .map(this::bookToDto)
                .collect(Collectors.toList());
            
            List<AuthorResponseDto> authorDtos = result.getAuthors().stream()
                .map(this::authorToDto)
                .collect(Collectors.toList());
            
            List<SeriesResponseDto> seriesDtos = result.getSeries().stream()
                .map(this::seriesToDto)
                .collect(Collectors.toList());
            
            long searchTime = System.currentTimeMillis() - startTime;
            
            UnifiedSearchResultDto responseDto = new UnifiedSearchResultDto(
                bookDtos, authorDtos, seriesDtos, query, searchTime, limit
            );
            
            return Response.ok(responseDto).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @POST
    @Operation(summary = "Unified search (POST)", description = "Search across books, authors, and series using POST request")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = UnifiedSearchResultDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public Response unifiedSearchPost(UnifiedSearchRequestDto request) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Validate request
            if (request == null || request.getQ() == null || request.getQ().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Search query 'q' is required and cannot be empty"))
                    .build();
            }
            
            int limit = (request.getLimit() != null) ? request.getLimit() : 10;
            
            // Validate entity types if provided
            if (request.getTypes() != null && !request.getTypes().isEmpty()) {
                List<String> validTypes = Arrays.asList("books", "authors", "series");
                for (String type : request.getTypes()) {
                    if (!validTypes.contains(type)) {
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity(Map.of("message", "Invalid entity type: " + type + ". Valid types are: books, authors, series"))
                            .build();
                    }
                }
            }
            
            // Perform unified search
            UnifiedSearchResult result = unifiedSearchUseCase.unifiedSearch(
                request.getQ(), 
                limit, 
                request.getTypes()
            );
            
            // Convert to DTOs
            List<BookResponseDto> bookDtos = result.getBooks().stream()
                .map(this::bookToDto)
                .collect(Collectors.toList());
            
            List<AuthorResponseDto> authorDtos = result.getAuthors().stream()
                .map(this::authorToDto)
                .collect(Collectors.toList());
            
            List<SeriesResponseDto> seriesDtos = result.getSeries().stream()
                .map(this::seriesToDto)
                .collect(Collectors.toList());
            
            long searchTime = System.currentTimeMillis() - startTime;
            
            UnifiedSearchResultDto responseDto = new UnifiedSearchResultDto(
                bookDtos, authorDtos, seriesDtos, request.getQ(), searchTime, limit
            );
            
            return Response.ok(responseDto).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Convert Book domain model to DTO.
     */
    private BookResponseDto bookToDto(Book book) {
        if (book == null) {
            return null;
        }
        
    return BookResponseDto.builder()
        .id(book.getId())
        .title(book.getTitle())
        .titleSort(book.getTitleSort())
        .isbn(book.getIsbn())
        .description(book.getDescription())
        .pageCount(book.getPageCount())
        .publicationYear(book.getPublicationYear())
        .language(book.getLanguage())
        .hasCover(book.getHasCover())
        .createdAt(book.getCreatedAt())
        .updatedAt(book.getUpdatedAt())
        .build();
    }
    
    /**
     * Convert Author domain model to DTO.
     */
    private AuthorResponseDto authorToDto(Author author) {
        if (author == null) {
            return null;
        }
        
        return AuthorResponseDto.builder()
                .id(author.getId())
                .name(author.getName())
                .sortName(author.getSortName())
                .bio(author.getBio())
                .birthDate(author.getBirthDate())
                .deathDate(author.getDeathDate())
                .websiteUrl(author.getWebsiteUrl())
                .metadata(author.getMetadata())
                .createdAt(author.getCreatedAt())
                .updatedAt(author.getUpdatedAt())
                .hasPicture(author.getHasPicture())
                .build();
    }
    
    /**
     * Convert Series domain model to DTO.
     */
    private SeriesResponseDto seriesToDto(Series series) {
        if (series == null) {
            return null;
        }
        
        return SeriesResponseDto.builder()
                .id(series.getId())
                .name(series.getName())
                .sortName(series.getSortName())
                .description(series.getDescription())
                .imagePath(series.getImagePath())
                .totalBooks(series.getTotalBooks())
                .isCompleted(series.getIsCompleted())
                .hasPicture(series.getHasPicture())
                .metadata(series.getMetadata())
                .createdAt(series.getCreatedAt())
                .updatedAt(series.getUpdatedAt())
                .fallbackImagePath(series.getEffectiveImagePath())
                .build();
    }
}