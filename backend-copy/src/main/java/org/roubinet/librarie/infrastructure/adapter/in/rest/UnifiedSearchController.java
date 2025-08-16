package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.AuthorUseCase;
import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.application.port.in.SeriesUseCase;
import org.roubinet.librarie.domain.entity.Author;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.domain.model.SeriesData;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.AuthorResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.BookResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.SeriesResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.UnifiedSearchResultDto;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for unified search operations.
 * Searches across books, series, and authors transparently.
 */
@Path("/v1/search")
@Tag(name = "Search", description = "Unified search operations")
public class UnifiedSearchController {
    
    private final BookUseCase bookUseCase;
    private final SeriesUseCase seriesUseCase;
    private final AuthorUseCase authorUseCase;
    private final InputSanitizationService sanitizationService;
    
    @Inject
    public UnifiedSearchController(BookUseCase bookUseCase, 
                                   SeriesUseCase seriesUseCase,
                                   AuthorUseCase authorUseCase,
                                   InputSanitizationService sanitizationService) {
        this.bookUseCase = bookUseCase;
        this.seriesUseCase = seriesUseCase;
        this.authorUseCase = authorUseCase;
        this.sanitizationService = sanitizationService;

    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Unified search", description = "Search across books, series, and authors")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = UnifiedSearchResultDto.class)))
    })
    public Response unifiedSearch(
            @Parameter(description = "Search query", required = true)
            @QueryParam("q") String query,
            @Parameter(description = "Maximum number of items per category", example = "10")
            @DefaultValue("10") @QueryParam("limit") int limit) {
        
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
                limit = 10; // Default limit for unified search
            }
            if (limit > 50) {
                limit = 50; // Max limit for unified search to avoid performance issues
            }
            
            // Search in parallel across all types
            // For simplicity, we'll limit results to avoid pagination complexity in unified search
            List<Book> books = bookUseCase.searchBooks(sanitizedQuery, null, limit).getItems();
            List<SeriesData> series = seriesUseCase.searchSeries(sanitizedQuery);
            List<Author> authors = authorUseCase.searchAuthors(sanitizedQuery);
            
            // Limit series and authors to the specified limit
            if (series.size() > limit) {
                series = series.subList(0, limit);
            }
            if (authors.size() > limit) {
                authors = authors.subList(0, limit);
            }
            
            // Convert to DTOs
            List<BookResponseDto> bookDtos = books.stream()
                .map(this::bookToDto)
                .collect(Collectors.toList());
            
            List<SeriesResponseDto> seriesDtos = series.stream()
                .map(this::seriesToDto)
                .collect(Collectors.toList());
            
            List<AuthorResponseDto> authorDtos = authors.stream()
                .map(this::authorToDto)
                .collect(Collectors.toList());
            
            UnifiedSearchResultDto result = new UnifiedSearchResultDto(bookDtos, seriesDtos, authorDtos);
            
            return Response.ok(result).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    private BookResponseDto bookToDto(Book book) {
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
        
        // For unified search, we'll use a simplified approach for contributors, series, etc.
        // This avoids the complex relationship extraction needed for full book details
        
        return dto;
    }
    
    private SeriesResponseDto seriesToDto(SeriesData seriesData) {
        SeriesResponseDto dto = new SeriesResponseDto();
        dto.setId(seriesData.getId());
        dto.setName(seriesData.getName());
        dto.setSortName(seriesData.getSortName());
        dto.setDescription(seriesData.getDescription());
        dto.setImagePath(seriesData.getImagePath());
        dto.setMetadata(seriesData.getMetadata());
        dto.setCreatedAt(seriesData.getCreatedAt());
        dto.setUpdatedAt(seriesData.getUpdatedAt());
        dto.setBookCount(seriesData.getBookCount());
        
        // Set fallback image if the series doesn't have its own image
        if (seriesData.getImagePath() == null || seriesData.getImagePath().trim().isEmpty()) {
            dto.setFallbackImagePath(seriesData.getEffectiveImagePath());
        }
        
        return dto;
    }
    
    private AuthorResponseDto authorToDto(Author author) {
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