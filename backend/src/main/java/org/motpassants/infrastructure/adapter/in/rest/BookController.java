package org.motpassants.infrastructure.adapter.in.rest;

import org.motpassants.application.service.BookService;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.BookSearchCriteria;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.infrastructure.adapter.in.rest.dto.BookRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.BookResponseDto;
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

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for book operations.
 * Inbound adapter that translates HTTP requests to use case calls.
 * Infrastructure layer component.
 */
@Path("/v1/books")
@Tag(name = "Books", description = "Book management operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookController {

    private final BookService bookService;

    @Inject
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GET
    @Operation(summary = "Get all books", description = "Retrieve all books with pagination")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public Response getAllBooks(
            @Parameter(description = "Pagination cursor") @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items per page") @QueryParam("limit") @DefaultValue("20") int limit) {
        
        try {
            PageResult<Book> result = bookService.getAllBooks(cursor, limit);
            
            List<BookResponseDto> bookDtos = result.getItems().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
            
            PageResponseDto.PaginationMetadata metadata = new PageResponseDto.PaginationMetadata();
            metadata.setTotalElements(result.getTotalCount());
            metadata.setPageSize(limit);
            metadata.setNextCursor(result.getNextCursor());
            metadata.setPreviousCursor(result.getPreviousCursor());
            
            PageResponseDto<BookResponseDto> response = new PageResponseDto<>(bookDtos, metadata);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its UUID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid UUID format")
    })
    public Response getBookById(
            @Parameter(description = "Book UUID") @PathParam("id") String id) {
        
        try {
            UUID bookId = UUID.fromString(id);
            Optional<Book> book = bookService.getBookById(bookId);
            
            if (book.isPresent()) {
                return Response.ok(toResponseDto(book.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Book not found"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid UUID format"))
                    .build();
        }
    }

    @POST
    @Operation(summary = "Create book", description = "Create a new book")
    @APIResponses({
        @APIResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid book data"),
        @APIResponse(responseCode = "409", description = "Book already exists")
    })
    public Response createBook(BookRequestDto bookRequest) {
        try {
            // Validate required fields
            if (bookRequest.getTitle() == null || bookRequest.getTitle().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Book title is required"))
                        .build();
            }
            
            Book book = toEntity(bookRequest);
            Book createdBook = bookService.createBook(book);
            return Response.status(Response.Status.CREATED)
                    .entity(toResponseDto(createdBook))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update book", description = "Update an existing book")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid book data")
    })
    public Response updateBook(
            @Parameter(description = "Book UUID") @PathParam("id") String id,
            BookRequestDto bookRequest) {
        
        try {
            UUID bookId = UUID.fromString(id);
            
            Book book = toEntity(bookRequest);
            book.setId(bookId);
            
            Book updatedBook = bookService.updateBook(book);
            return Response.ok(toResponseDto(updatedBook)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete book", description = "Delete a book by its UUID")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Book deleted successfully"),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid UUID format")
    })
    public Response deleteBook(
            @Parameter(description = "Book UUID") @PathParam("id") String id) {
        
        try {
            UUID bookId = UUID.fromString(id);
            bookService.deleteBook(bookId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/search")
    @Operation(summary = "Search books", description = "Search books by query string")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Search completed successfully"),
        @APIResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public Response searchBooks(
            @Parameter(description = "Search query") @QueryParam("query") String query) {
        
        if (query == null || query.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Search query cannot be empty"))
                    .build();
        }
        
        List<Book> books = bookService.searchBooks(query);
        List<BookResponseDto> bookDtos = books.stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
            
        PageResponseDto.PaginationMetadata metadata = new PageResponseDto.PaginationMetadata();
        metadata.setTotalElements(books.size());
        
        PageResponseDto<BookResponseDto> response = new PageResponseDto<>(bookDtos, metadata);
        return Response.ok(response).build();
    }

    @POST
    @Path("/criteria")
    @Operation(summary = "Search books by criteria", description = "Search books using detailed criteria")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Search completed successfully"),
        @APIResponse(responseCode = "400", description = "Invalid search criteria")
    })
    public Response searchBooksByCriteria(BookSearchCriteria criteria) {
        try {
            List<Book> books = bookService.searchBooksByCriteria(criteria);
            List<BookResponseDto> bookDtos = books.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
                
            PageResponseDto.PaginationMetadata metadata = new PageResponseDto.PaginationMetadata();
            metadata.setTotalElements(books.size());
            
            PageResponseDto<BookResponseDto> response = new PageResponseDto<>(bookDtos, metadata);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/count")
    @Operation(summary = "Get book count", description = "Get total number of books")
    @APIResponse(responseCode = "200", description = "Count retrieved successfully")
    public Response getBookCount() {
        long count = bookService.getTotalBooksCount();
        return Response.ok(Map.of("count", count)).build();
    }
    
    /**
     * Convert domain entity to response DTO.
     */
    private BookResponseDto toResponseDto(Book book) {
        BookResponseDto dto = new BookResponseDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setTitleSort(book.getTitleSort());
        dto.setIsbn(book.getIsbn());
        dto.setDescription(book.getDescription());
        dto.setPageCount(book.getPageCount());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setLanguage(book.getLanguage());
        dto.setCoverUrl(book.getCoverUrl());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        return dto;
    }
    
    /**
     * Convert request DTO to domain entity.
     */
    private Book toEntity(BookRequestDto dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setDescription(dto.getDescription());
        book.setPageCount(dto.getPageCount());
        book.setPublicationYear(dto.getPublicationYear());
        book.setLanguage(dto.getLanguage());
        book.setCoverUrl(dto.getCoverUrl());
        book.setCreatedAt(OffsetDateTime.now());
        book.setUpdatedAt(OffsetDateTime.now());
        return book;
    }
}