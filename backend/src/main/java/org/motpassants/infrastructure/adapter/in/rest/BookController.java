package org.motpassants.infrastructure.adapter.in.rest;

import org.motpassants.application.service.BookService;
import org.motpassants.application.service.DemoDataService;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.BookSearchCriteria;
import org.motpassants.domain.core.model.BookSortCriteria;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.core.model.SortField;
import org.motpassants.domain.core.model.SortDirection;
import org.motpassants.infrastructure.adapter.in.rest.dto.BookRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.BookResponseDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.BookListItemDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.BookDetailsDto;
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

import java.nio.charset.StandardCharsets;
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
    private final org.motpassants.infrastructure.media.ImageCachingService imageCachingService;
    private final org.motpassants.infrastructure.config.LibrarieConfigProperties config;
    private final DemoDataService demoDataService;

    @Context
    Request httpRequest;

    @Inject
    public BookController(BookService bookService, org.motpassants.infrastructure.media.ImageCachingService imageCachingService, org.motpassants.infrastructure.config.LibrarieConfigProperties config, DemoDataService demoDataService) {
        this.bookService = bookService;
        this.imageCachingService = imageCachingService;
        this.config = config;
        this.demoDataService = demoDataService;
    }

    @GET
    @Operation(summary = "Get all books", description = "Retrieve all books with pagination and sorting (lightweight items)")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid pagination or sorting parameters")
    })
    public Response getAllBooks(
            @Parameter(description = "Pagination cursor") @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items per page") @QueryParam("limit") @DefaultValue("20") int limit,
            @Parameter(description = "Sort field (UPDATED_AT, TITLE_SORT, PUBLICATION_DATE)") @QueryParam("sortField") String sortField,
            @Parameter(description = "Sort direction (ASC, DESC)") @QueryParam("sortDirection") @DefaultValue("DESC") String sortDirection) {
        
        try {
            // Parse and validate sorting parameters using service business logic
            BookSortCriteria sortCriteria = bookService.parseAndValidateSortCriteria(sortField, sortDirection);

            // Ensure demo data is present in dev/demo mode to avoid empty first load due to async seeding
            demoDataService.populateDemoData();
            PageResult<Book> result = bookService.getAllBooks(cursor, limit, sortCriteria);

            List<BookListItemDto> bookDtos = result.getItems().stream()
                .map(this::toListItemDto)
                .collect(Collectors.toList());

            PageResponseDto<BookListItemDto> response = new PageResponseDto<BookListItemDto>(
                bookDtos,
                result.getNextCursor(),
                result.getPreviousCursor(),
                limit,
                result.hasNext(),
                result.hasPrevious(),
                (long) result.getTotalCount()
            );
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
            UUID bookId = bookService.validateAndParseId(id);
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
                    .entity(Map.of("error", e.getMessage()))
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
            UUID bookId = bookService.validateAndParseId(id);
            
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
            UUID bookId = bookService.validateAndParseId(id);
            bookService.deleteBook(bookId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}/details")
    @Operation(summary = "Get book details by ID", description = "Retrieve rich book details by its UUID")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Book details found",
                    content = @Content(schema = @Schema(implementation = BookDetailsDto.class))),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid UUID format")
    })
    public Response getBookDetailsById(
            @Parameter(description = "Book UUID") @PathParam("id") String id) {
        try {
            UUID bookId = bookService.validateAndParseId(id);
            Optional<Book> book = bookService.getBookById(bookId);
            if (book.isPresent()) {
                return Response.ok(toDetailsDto(book.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Book not found"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/search")
    @Operation(summary = "Search books", description = "Search books by query string (lightweight items)")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Search completed successfully"),
        @APIResponse(responseCode = "400", description = "Invalid search parameters")
    })
    public Response searchBooks(
            @Parameter(description = "Search query") @QueryParam("q") String q,
            @Parameter(description = "Search query (alias)") @QueryParam("query") String query) {
        // Support both 'q' and 'query' params (frontend uses 'q')
        String effective = (q != null && !q.isBlank()) ? q : query;
        
        try {
            bookService.validateSearchQuery(effective);
            List<Book> books = bookService.searchBooks(effective);
            List<BookListItemDto> bookDtos = books.stream()
                .map(this::toListItemDto)
                .collect(Collectors.toList());

            PageResponseDto<BookListItemDto> response = new PageResponseDto<BookListItemDto>(
                bookDtos,
                null, // nextCursor
                null, // previousCursor  
                bookDtos.size(), // limit
                false, // hasNext
                false, // hasPrevious
                (long) books.size() // totalElements
            );
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/criteria")
    @Operation(summary = "Search books by criteria", description = "Search books using detailed criteria (lightweight items)")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Search completed successfully"),
        @APIResponse(responseCode = "400", description = "Invalid search criteria")
    })
    public Response searchBooksByCriteria(BookSearchCriteria criteria) {
        try {
            List<Book> books = bookService.searchBooksByCriteria(criteria);
            List<BookListItemDto> bookDtos = books.stream()
                .map(this::toListItemDto)
                .collect(Collectors.toList());

            PageResponseDto<BookListItemDto> response = new PageResponseDto<BookListItemDto>(
                bookDtos,
                null, // nextCursor
                null, // previousCursor
                bookDtos.size(), // limit
                false, // hasNext
                false, // hasPrevious
                (long) books.size() // totalElements
            );
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
            UUID bookId = bookService.validateAndParseId(id);
            
            Optional<Book> existingBook = bookService.getBookById(bookId);
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
            
            // For now, we just return a success response without actually storing the progress
            // This maintains API compatibility with backend-copy
            Map<String, Object> response = Map.of(
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

    @GET
    @Path("/{id}/cover")
    @Produces("image/*")
    @Operation(summary = "Get book cover image", description = "Streams the book cover image from local assets with strong ETag; demo seeding hydrates files")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Image bytes returned"),
        @APIResponse(responseCode = "404", description = "Cover not found")
    })
    public Response getBookCover(@PathParam("id") String id) {
        try {
            UUID bookId = bookService.validateAndParseId(id);
            Optional<Book> bookOpt = bookService.getBookById(bookId);
            if (bookOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Book not found").build();
            }
            // We only need existence check; no further book usage required here

            // Remote URLs are no longer used; covers are hydrated into local assets during seeding/ingest
            String storedUrl = null;

            java.nio.file.Path baseDir = java.nio.file.Paths.get(config.storage().baseDir());
            // Prefer using injected config bean rather than static; but we can derive from image service's config through its sanitize call by passing baseDir string used in app config
            // Call the local-first service (it will hydrate from remote if needed and set ETag/Last-Modified)
            return imageCachingService.serveLocalFirstStrongETag(
                httpRequest,
                baseDir,
                "books",
                "covers",
                id,
                storedUrl,
                getFailoverSvg()
            );
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid book ID format").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error: " + e.getMessage()).build();
        }
    }

    private static byte[] getFailoverSvg() {
        String svg = """
                <svg xmlns='http://www.w3.org/2000/svg' width='320' height='480' viewBox='0 0 320 480'>
                    <defs>
                        <linearGradient id='g' x1='0' y1='0' x2='1' y2='1'>
                            <stop offset='0%' stop-color='#f0f0f0'/>
                            <stop offset='100%' stop-color='#d8d8d8'/>
                        </linearGradient>
                    </defs>
                    <rect width='100%' height='100%' fill='url(#g)'/>
                    <circle cx='160' cy='240' r='60' fill='#ccc'/>
                    <text x='160' y='320' text-anchor='middle' font-family='Arial, sans-serif' font-size='18' fill='#999'>No Cover</text>
                </svg>
                """;
        return svg.getBytes(StandardCharsets.UTF_8);
    }
    
    
    /**
     * Convert domain entity to response DTO.
     */
    private BookResponseDto toResponseDto(Book book) {
    BookResponseDto.Builder builder = BookResponseDto.builder()
            .id(book.getId())
            .title(book.getTitle())
            .titleSort(book.getTitleSort())
            .isbn(book.getIsbn())
            .description(book.getDescription())
            .pageCount(book.getPageCount())
            .publicationYear(book.getPublicationYear())
            .language(book.getLanguage())
            .path(book.getPath())
            .fileSize(book.getFileSize())
            .fileHash(book.getFileHash())
            .hasCover(book.getHasCover())
            .publicationDate(book.getPublicationDate())
            .metadata(book.getMetadata())
            .createdAt(book.getCreatedAt())
            .updatedAt(book.getUpdatedAt());

    String publisherName = (book.getPublisher() != null && book.getPublisher().getName() != null)
        ? book.getPublisher().getName()
        : null;
    if (publisherName != null && !"null".equalsIgnoreCase(publisherName)) builder.publisher(publisherName);

    if (book.getSeries() != null && !book.getSeries().isEmpty()) {
            var first = book.getSeries().stream().findFirst().orElse(null);
            if (first != null && first.getSeries() != null) {
                builder.series(first.getSeries().getName())
               .seriesId(first.getSeries().getId())
               .seriesIndex(first.getSeriesIndex());
            }
        }

        if (book.getFormats() != null && !book.getFormats().isEmpty()) {
            builder.formats(book.getFormats().stream()
                .map(f -> f.getFormatType())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()));
        }

        return builder.build();
    }

    private BookDetailsDto toDetailsDto(Book book) {
    BookDetailsDto.Builder builder = BookDetailsDto.builder()
            .id(book.getId())
            .title(book.getTitle())
            .titleSort(book.getTitleSort())
            .isbn(book.getIsbn())
            .description(book.getDescription())
            .pageCount(book.getPageCount())
            .publicationYear(book.getPublicationYear())
            .language(book.getLanguage())
            .path(book.getPath())
            .fileSize(book.getFileSize())
            .fileHash(book.getFileHash())
            .hasCover(book.getHasCover())
            .publicationDate(book.getPublicationDate())
            .metadata(book.getMetadata())
            .createdAt(book.getCreatedAt())
            .updatedAt(book.getUpdatedAt());

    String publisherName = (book.getPublisher() != null && book.getPublisher().getName() != null)
        ? book.getPublisher().getName()
        : null;
    if (publisherName != null && !"null".equalsIgnoreCase(publisherName)) builder.publisher(publisherName);

        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
            var first = book.getSeries().stream().findFirst().orElse(null);
            if (first != null && first.getSeries() != null) {
                builder.series(first.getSeries().getName())
                       .seriesId(first.getSeries().getId())
                       .seriesIndex(first.getSeriesIndex());
            }
        }

        if (book.getFormats() != null && !book.getFormats().isEmpty()) {
            builder.formats(book.getFormats().stream()
                .map(f -> f.getFormatType())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList()));
        }
        // Contributors grouped by role
        try {
            java.util.Map<String, java.util.List<org.motpassants.domain.core.model.Author>> contribs = bookService.getContributors(book.getId());
            if (contribs != null && !contribs.isEmpty()) {
                java.util.Map<String, java.util.List<java.util.Map<String, String>>> api = new java.util.LinkedHashMap<>();
                for (var e : contribs.entrySet()) {
                    java.util.List<java.util.Map<String, String>> list = new java.util.ArrayList<>();
                    for (var a : e.getValue()) {
                        if (a != null && a.getId() != null && a.getName() != null) {
                            list.add(java.util.Map.of(
                                "id", a.getId().toString(),
                                "name", a.getName()
                            ));
                        }
                    }
                    if (!list.isEmpty()) api.put(e.getKey(), list);
                }
                if (!api.isEmpty()) builder.contributorsDetailed(api);
            }
        } catch (Exception ignored) { }
        return builder.build();
    }

    private BookListItemDto toListItemDto(Book book) {
        return BookListItemDto.builder()
            .id(book.getId())
            .title(book.getTitle())
            .titleSort(book.getTitleSort())
            .hasCover(book.getHasCover())
            .publicationDate(book.getPublicationDate())
            .createdAt(book.getCreatedAt())
            .build();
    }

    // Removed metadata-derived fallbacks: description/pages/publicationYear and publisher now reflect only core fields.
    
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
    // Cover hydration is file-based; no coverUrl in API
        book.setCreatedAt(OffsetDateTime.now());
        book.setUpdatedAt(OffsetDateTime.now());
        return book;
    }
}