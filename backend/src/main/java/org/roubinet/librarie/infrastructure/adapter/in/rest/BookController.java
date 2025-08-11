package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.BookResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.PageResponseDto;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for book operations.
 * This adapter translates HTTP requests to domain use cases.
 */
@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Books", description = "Book management operations")
public class BookController {
    
    private final BookUseCase bookUseCase;
    
    @Inject
    public BookController(BookUseCase bookUseCase) {
        this.bookUseCase = bookUseCase;
    }
    
    @GET
    @Operation(summary = "Get all books", description = "Retrieve a paginated list of all books")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Books retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getAllBooks(
            @Parameter(description = "Page number (0-based)", example = "0")
            @DefaultValue("0") @QueryParam("page") int page,
            @Parameter(description = "Page size", example = "20")
            @DefaultValue("20") @QueryParam("size") int size) {
        
        List<Book> books = bookUseCase.getAllBooks(page, size);
        long totalCount = bookUseCase.getTotalBooksCount();
        
        List<BookResponseDto> bookDtos = books.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        
        PageResponseDto<BookResponseDto> response = new PageResponseDto<BookResponseDto>(
            bookDtos, null, null, size, totalCount
        );
        
        return Response.ok(response).build();
    }
    
    @GET
    @Path("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its UUID")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Book found",
            content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Book not found")
    })
    public Response getBookById(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = UUID.fromString(id);
            Optional<Book> book = bookUseCase.getBookById(bookId);
            
            if (book.isPresent()) {
                return Response.ok(toDto(book.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found with ID: " + id)
                    .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid UUID format: " + id)
                .build();
        }
    }
    
    @GET
    @Path("/search")
    @Operation(summary = "Search books", description = "Search books by title, author, series, or ISBN")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response searchBooks(
            @Parameter(description = "Search query", required = true)
            @QueryParam("q") String query,
            @Parameter(description = "Page number (0-based)", example = "0")
            @DefaultValue("0") @QueryParam("page") int page,
            @Parameter(description = "Page size", example = "20")
            @DefaultValue("20") @QueryParam("size") int size) {
        
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Search query is required")
                .build();
        }
        
        List<Book> books = bookUseCase.searchBooks(query, page, size);
        
        List<BookResponseDto> bookDtos = books.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        
        // For search results, we don't have an accurate total count
        // In a real implementation, you might want to add a separate count query
        PageResponseDto<BookResponseDto> response = new PageResponseDto<BookResponseDto>(
            bookDtos, null, null, size, (long) bookDtos.size()
        );
        
        return Response.ok(response).build();
    }
    
    @GET
    @Path("/by-author")
    @Operation(summary = "Get books by author", description = "Retrieve books by a specific author")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Books retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getBooksByAuthor(
            @Parameter(description = "Author name", required = true)
            @QueryParam("author") String authorName,
            @Parameter(description = "Page number (0-based)", example = "0")
            @DefaultValue("0") @QueryParam("page") int page,
            @Parameter(description = "Page size", example = "20")
            @DefaultValue("20") @QueryParam("size") int size) {
        
        if (authorName == null || authorName.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Author name is required")
                .build();
        }
        
        List<Book> books = bookUseCase.getBooksByAuthor(authorName, page, size);
        
        List<BookResponseDto> bookDtos = books.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        
        PageResponseDto<BookResponseDto> response = new PageResponseDto<BookResponseDto>(
            bookDtos, null, null, size, (long) bookDtos.size()
        );
        
        return Response.ok(response).build();
    }
    
    @GET
    @Path("/by-series")
    @Operation(summary = "Get books by series", description = "Retrieve books in a specific series")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Books retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getBooksBySeries(
            @Parameter(description = "Series name", required = true)
            @QueryParam("series") String seriesName,
            @Parameter(description = "Page number (0-based)", example = "0")
            @DefaultValue("0") @QueryParam("page") int page,
            @Parameter(description = "Page size", example = "20")
            @DefaultValue("20") @QueryParam("size") int size) {
        
        if (seriesName == null || seriesName.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Series name is required")
                .build();
        }
        
        List<Book> books = bookUseCase.getBooksBySeries(seriesName, page, size);
        
        List<BookResponseDto> bookDtos = books.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        
        PageResponseDto<BookResponseDto> response = new PageResponseDto<BookResponseDto>(
            bookDtos, null, null, size, (long) bookDtos.size()
        );
        
        return Response.ok(response).build();
    }
    
    /**
     * Convert Book entity to DTO for API response.
     */
    private BookResponseDto toDto(Book book) {
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
        
        // Set language name if available
        if (book.getLanguage() != null) {
            dto.setLanguage(book.getLanguage().getName());
        }
        
        // Set publisher name if available
        if (book.getPublisher() != null) {
            dto.setPublisher(book.getPublisher().getName());
        }
        
        return dto;
    }
}