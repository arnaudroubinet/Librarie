package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.domain.entity.BookSeries;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.BookRequestDto;
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
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * REST controller for book operations.
 * Feature-based API with version 1 path structure.
 * OIDC token will control access rights.
 */
@Path("/v1/books")
@Tag(name = "Books", description = "Book management operations")
public class BookController {
    
    private final BookUseCase bookUseCase;
    private final InputSanitizationService sanitizationService;
    private final LibrarieConfigProperties config;
    private final CursorUtils cursorUtils;
    
    @Inject
    public BookController(BookUseCase bookUseCase,
                         InputSanitizationService sanitizationService,
                         LibrarieConfigProperties config,
                         CursorUtils cursorUtils) {
        this.bookUseCase = bookUseCase;
        this.sanitizationService = sanitizationService;
        this.config = config;
        this.cursorUtils = cursorUtils;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all books with cursor-based pagination", description = "Retrieve all books using keyset pagination")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Books retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getAllBooks(
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
            
            // For now, fallback to offset-based pagination until repository implements cursor pagination
            int page = 0; // Will be enhanced to use cursor
            List<Book> books = bookUseCase.getAllBooks(page, limit);
            
            List<BookResponseDto> bookDtos = books.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            // Generate cursors for pagination
            String nextCursor = null;
            String previousCursor = null;
            
            if (!books.isEmpty()) {
                Book lastBook = books.get(books.size() - 1);
                if (lastBook.getCreatedAt() != null && lastBook.getId() != null) {
                    nextCursor = cursorUtils.createCursor(lastBook.getId(), lastBook.getCreatedAt());
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
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
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
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new book", description = "Create a new book in the library")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Book created successfully",
            content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid book data")
    })
    public Response createBook(
            @Parameter(description = "Book data", required = true)
            BookRequestDto bookRequest) {
        
        try {
            // Validate and sanitize input
            validateBookRequest(bookRequest);
            
            Book book = toEntity(bookRequest);
            Book savedBook = bookUseCase.createBook(book);
            
            return Response.status(Response.Status.CREATED)
                .entity(toDto(savedBook))
                .build();
                
        } catch (SecurityException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Security validation failed: " + e.getMessage())
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid book data: " + e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update book", description = "Update an existing book")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Book updated successfully",
            content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid book data")
    })
    public Response updateBook(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id,
            @Parameter(description = "Updated book data", required = true)
            BookRequestDto bookRequest) {
        
        try {
            UUID bookId = UUID.fromString(id);
            
            // Validate and sanitize input
            validateBookRequest(bookRequest);
            
            Optional<Book> existingBook = bookUseCase.getBookById(bookId);
            if (existingBook.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            Book book = toEntity(bookRequest);
            book.setId(bookId);
            Book updatedBook = bookUseCase.updateBook(book);
            
            return Response.ok(toDto(updatedBook)).build();
            
        } catch (SecurityException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Security validation failed: " + e.getMessage())
                .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid book data: " + e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete book", description = "Delete a book from the library")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Book deleted successfully"),
        @APIResponse(responseCode = "404", description = "Book not found")
    })
    public Response deleteBook(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = UUID.fromString(id);
            
            Optional<Book> existingBook = bookUseCase.getBookById(bookId);
            if (existingBook.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            bookUseCase.deleteBook(bookId);
            
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
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search books", description = "Search books by title, author, series, or ISBN with cursor pagination")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response searchBooks(
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
            String sanitizedQuery = sanitizationService.sanitizeSearchInput(query);
            
            // Validate limit
            if (limit <= 0) {
                limit = config.pagination().defaultPageSize();
            }
            if (limit > config.pagination().maxPageSize()) {
                limit = config.pagination().maxPageSize();
            }
            
            // For now, fallback to offset-based pagination
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
                    nextCursor = cursorUtils.createCursor(lastBook.getId(), lastBook.getCreatedAt());
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
    
    @POST
    @Path("/{id}/completion")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update reading completion", description = "Update the reading completion progress for a book")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Reading completion updated successfully"),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid completion data")
    })
    public Response updateReadingCompletion(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id,
            @Parameter(description = "Completion data", required = true)
            Map<String, Object> completionData) {
        
        try {
            UUID bookId = UUID.fromString(id);
            
            Optional<Book> existingBook = bookUseCase.getBookById(bookId);
            if (existingBook.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            if (completionData == null || !completionData.containsKey("progress")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Progress value is required")
                    .build();
            }
            
            Object progressObj = completionData.get("progress");
            if (!(progressObj instanceof Number)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Progress must be a number")
                    .build();
            }
            
            double progress = ((Number) progressObj).doubleValue();
            if (progress < 0 || progress > 100) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Progress must be between 0 and 100")
                    .build();
            }
            
            // In a real implementation, this would update user's reading progress
            Map<String, Object> response = Map.of(
                "bookId", bookId,
                "progress", progress,
                "status", "updated",
                "message", "Reading completion updated successfully"
            );
            
            return Response.ok(response).build();
            
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
     * Validate book request data with security checks.
     */
    private void validateBookRequest(BookRequestDto bookRequest) {
        if (bookRequest == null) {
            throw new IllegalArgumentException("Book data is required");
        }
        
        if (bookRequest.getTitle() == null || bookRequest.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title is required");
        }
        
        // Sanitize inputs
        String sanitizedTitle = sanitizationService.sanitizeTextInput(bookRequest.getTitle());
        if (!sanitizedTitle.equals(bookRequest.getTitle())) {
            throw new SecurityException("Book title contains unsafe characters");
        }
        
        if (bookRequest.getAuthor() != null) {
            String sanitizedAuthor = sanitizationService.sanitizeTextInput(bookRequest.getAuthor());
            if (!sanitizedAuthor.equals(bookRequest.getAuthor())) {
                throw new SecurityException("Author name contains unsafe characters");
            }
        }
        
        if (bookRequest.getDescription() != null) {
            String sanitizedDescription = sanitizationService.sanitizeTextInput(bookRequest.getDescription());
            if (!sanitizedDescription.equals(bookRequest.getDescription())) {
                throw new SecurityException("Description contains unsafe characters");
            }
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
     * Convert DTO to Book entity.
     */
    private Book toEntity(BookRequestDto dto) {
        if (dto == null) {
            return null;
        }
        
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        
        // Store author and other fields in metadata for now
        // In a real implementation, these would be handled through proper relationships
        Map<String, Object> metadata = new HashMap<>();
        if (dto.getAuthor() != null) {
            metadata.put("author", dto.getAuthor());
        }
        if (dto.getDescription() != null) {
            metadata.put("description", dto.getDescription());
        }
        if (dto.getSeries() != null) {
            metadata.put("series", dto.getSeries());
        }
        if (dto.getSeriesIndex() != null) {
            metadata.put("seriesIndex", dto.getSeriesIndex());
        }
        book.setMetadata(metadata);
        
        return book;
    }
}