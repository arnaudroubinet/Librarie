package org.roubinet.librarie.infrastructure.adapter.in.rest.v1.pub;

import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.domain.entity.BookSeries;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.BookResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.PageResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorUtils;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Public REST controller for book query operations.
 * Implements CQRS pattern for complex read operations.
 * Version 1 API following REST best practices.
 */
@Path("/v1/pub/books")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Public Books", description = "Public book search and query operations")
public class PublicBooksQueryController {
    
    private final BookUseCase bookUseCase;
    private final InputSanitizationService sanitizationService;
    private final LibrarieConfigProperties config;
    
    @Inject
    public PublicBooksQueryController(BookUseCase bookUseCase,
                                     InputSanitizationService sanitizationService,
                                     LibrarieConfigProperties config) {
        this.bookUseCase = bookUseCase;
        this.sanitizationService = sanitizationService;
        this.config = config;
    }
    
    @GET
    @Path("/search")
    @Operation(summary = "Search books", description = "Search books using query string with cursor-based pagination")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid search query")
    })
    public Response searchBooks(
            @Parameter(description = "Search query", required = true, example = "harry potter")
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
            
            // Validate pagination parameters
            if (limit <= 0) {
                limit = config.pagination().defaultPageSize();
            }
            if (limit > config.pagination().maxPageSize()) {
                limit = config.pagination().maxPageSize();
            }
            
            // For now, fallback to offset-based until repository implements cursor search
            int page = 0; // Will be enhanced to use cursor
            List<Book> books = bookUseCase.searchBooks(sanitizedQuery, page, limit);
            
            List<BookResponseDto> bookDtos = books.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            // Generate cursors for pagination
            String nextCursor = null;
            String previousCursor = null;
            
            if (!books.isEmpty()) {
                Book lastBook = books.get(books.size() - 1);
                if (lastBook.getCreatedAt() != null && lastBook.getId() != null) {
                    nextCursor = CursorUtils.createCursor(lastBook.getId(), lastBook.getCreatedAt());
                }
            }
            
            PageResponseDto<BookResponseDto> response = new PageResponseDto<>(
                bookDtos, nextCursor, previousCursor, limit
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
    @Path("/by-author")
    @Operation(summary = "Get books by author", description = "Retrieve books filtered by author name")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Books retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getBooksByAuthor(
            @Parameter(description = "Author name", required = true, example = "J.K. Rowling")
            @QueryParam("author") String authorName,
            @Parameter(description = "Cursor for pagination")
            @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items to return", example = "20")
            @DefaultValue("20") @QueryParam("limit") int limit) {
        
        try {
            if (authorName == null || authorName.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Author name is required")
                    .build();
            }
            
            // Sanitize author name
            String sanitizedAuthor = sanitizationService.sanitizeTextInput(authorName);
            
            // Validate pagination parameters
            if (limit <= 0) {
                limit = config.pagination().defaultPageSize();
            }
            if (limit > config.pagination().maxPageSize()) {
                limit = config.pagination().maxPageSize();
            }
            
            // For now, fallback to offset-based until repository implements cursor pagination
            int page = 0; // Will be enhanced to use cursor
            List<Book> books = bookUseCase.getBooksByAuthor(sanitizedAuthor, page, limit);
            
            List<BookResponseDto> bookDtos = books.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            // Generate cursors for pagination
            String nextCursor = null;
            String previousCursor = null;
            
            if (!books.isEmpty()) {
                Book lastBook = books.get(books.size() - 1);
                if (lastBook.getCreatedAt() != null && lastBook.getId() != null) {
                    nextCursor = CursorUtils.createCursor(lastBook.getId(), lastBook.getCreatedAt());
                }
            }
            
            PageResponseDto<BookResponseDto> response = new PageResponseDto<>(
                bookDtos, nextCursor, previousCursor, limit
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
    @Path("/by-series")
    @Operation(summary = "Get books by series", description = "Retrieve books filtered by series name")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Books retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getBooksBySeries(
            @Parameter(description = "Series name", required = true, example = "Harry Potter")
            @QueryParam("series") String seriesName,
            @Parameter(description = "Cursor for pagination")
            @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items to return", example = "20")
            @DefaultValue("20") @QueryParam("limit") int limit) {
        
        try {
            if (seriesName == null || seriesName.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Series name is required")
                    .build();
            }
            
            // Sanitize series name
            String sanitizedSeries = sanitizationService.sanitizeTextInput(seriesName);
            
            // Validate pagination parameters
            if (limit <= 0) {
                limit = config.pagination().defaultPageSize();
            }
            if (limit > config.pagination().maxPageSize()) {
                limit = config.pagination().maxPageSize();
            }
            
            // For now, fallback to offset-based until repository implements cursor pagination
            int page = 0; // Will be enhanced to use cursor
            List<Book> books = bookUseCase.getBooksBySeries(sanitizedSeries, page, limit);
            
            List<BookResponseDto> bookDtos = books.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            // Generate cursors for pagination
            String nextCursor = null;
            String previousCursor = null;
            
            if (!books.isEmpty()) {
                Book lastBook = books.get(books.size() - 1);
                if (lastBook.getCreatedAt() != null && lastBook.getId() != null) {
                    nextCursor = CursorUtils.createCursor(lastBook.getId(), lastBook.getCreatedAt());
                }
            }
            
            PageResponseDto<BookResponseDto> response = new PageResponseDto<>(
                bookDtos, nextCursor, previousCursor, limit
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
}