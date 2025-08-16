package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.application.port.in.BookSearchCriteria;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.domain.entity.BookSeries;
import org.roubinet.librarie.domain.entity.BookOriginalWork;
import org.roubinet.librarie.domain.entity.OriginalWork;
import org.roubinet.librarie.domain.entity.OriginalWorkAuthor;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.BookRequestDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.BookResponseDto;
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
import jakarta.ws.rs.core.*;

import java.util.*;
import java.util.stream.Collectors;
import org.roubinet.librarie.infrastructure.media.ImageCachingService;

/**
 * REST controller for book operations.
 */
@Path("/v1/books")
@Tag(name = "Books", description = "Book management operations")
public class BookController {

    private final BookUseCase bookUseCase;
    private final InputSanitizationService sanitizationService;
    private final LibrarieConfigProperties config;
    private final ImageCachingService imageCachingService;

    @Context
    Request httpRequest;

    @Inject
    public BookController(BookUseCase bookUseCase,
                          InputSanitizationService sanitizationService,
                          LibrarieConfigProperties config,
                          ImageCachingService imageCachingService) {
        this.bookUseCase = bookUseCase;
        this.sanitizationService = sanitizationService;
        this.config = config;
        this.imageCachingService = imageCachingService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all books with cursor-based pagination", description = "Retrieve all books using keyset pagination")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Books retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getAllBooks(
            @Parameter(description = "Cursor for pagination")
            @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items to return", example = "20")
            @DefaultValue("20") @QueryParam("limit") int limit) {

        try {
            if (limit <= 0) {
                limit = config.pagination().defaultPageSize();
            }
            if (limit > config.pagination().maxPageSize()) {
                limit = config.pagination().maxPageSize();
            }

            CursorPageResult<Book> pageResult = bookUseCase.getAllBooks(cursor, limit);

            List<BookResponseDto> bookDtos = pageResult.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

            PageResponseDto<BookResponseDto> response = new PageResponseDto<>(
                bookDtos,
                pageResult.getNextCursor(),
                pageResult.getPreviousCursor(),
                pageResult.getLimit(),
                pageResult.isHasNext(),
                pageResult.isHasPrevious(),
                pageResult.getTotalCount()
            );

            return Response.ok(response).build();
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
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid book ID format")
    })
    public Response getBookById(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        try {
            UUID bookId = UUID.fromString(id);
            Optional<Book> book = bookUseCase.getBookById(bookId);
            if (book.isPresent()) {
                return Response.ok(toDto(book.get())).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity("Book not found").build();
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
            String sanitizedQuery = sanitizationService.sanitizeSearchQuery(query);
            
            // Validate limit
            if (limit <= 0) {
                limit = config.pagination().defaultPageSize();
            }
            if (limit > config.pagination().maxPageSize()) {
                limit = config.pagination().maxPageSize();
            }
            
            // Use cursor pagination
            CursorPageResult<Book> pageResult = bookUseCase.searchBooks(sanitizedQuery, cursor, limit);
            
            List<BookResponseDto> bookDtos = pageResult.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            PageResponseDto<BookResponseDto> response = new PageResponseDto<>(
                bookDtos, 
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
    
    @POST
    @Path("/criteria")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search books by criteria", description = "Search books using complex criteria with cursor pagination")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid search criteria")
    })
    public Response searchBooksByCriteria(
            @Parameter(description = "Search criteria", required = true)
            BookSearchCriteria criteria,
            @Parameter(description = "Cursor for pagination")
            @QueryParam("cursor") String cursor,
            @Parameter(description = "Number of items to return", example = "20")
            @DefaultValue("20") @QueryParam("limit") int limit) {
        
        try {
            if (criteria == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Search criteria is required")
                    .build();
            }
            
            // Validate and sanitize criteria inputs
            validateSearchCriteria(criteria);
            
            // Validate limit
            if (limit <= 0) {
                limit = config.pagination().defaultPageSize();
            }
            if (limit > config.pagination().maxPageSize()) {
                limit = config.pagination().maxPageSize();
            }
            
            // Use cursor pagination with criteria
            CursorPageResult<Book> pageResult = bookUseCase.getBooksByCriteria(criteria, cursor, limit);
            
            List<BookResponseDto> bookDtos = pageResult.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            PageResponseDto<BookResponseDto> response = new PageResponseDto<>(
                bookDtos, 
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
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid search criteria: " + e.getMessage())
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
     * Validate search criteria with security checks.
     */
    private void validateSearchCriteria(BookSearchCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria is required");
        }
        
        // Sanitize text inputs
        if (criteria.getTitleContains() != null) {
            String sanitized = sanitizationService.sanitizeSearchQuery(criteria.getTitleContains());
            if (!sanitized.equals(criteria.getTitleContains())) {
                throw new SecurityException("Title search contains unsafe characters");
            }
        }
        
        if (criteria.getSeriesContains() != null) {
            String sanitized = sanitizationService.sanitizeTextInput(criteria.getSeriesContains());
            if (!sanitized.equals(criteria.getSeriesContains())) {
                throw new SecurityException("Series search contains unsafe characters");
            }
        }
        
        if (criteria.getLanguageEquals() != null) {
            String sanitized = sanitizationService.sanitizeTextInput(criteria.getLanguageEquals());
            if (!sanitized.equals(criteria.getLanguageEquals())) {
                throw new SecurityException("Language search contains unsafe characters");
            }
        }
        
        if (criteria.getPublisherContains() != null) {
            String sanitized = sanitizationService.sanitizeTextInput(criteria.getPublisherContains());
            if (!sanitized.equals(criteria.getPublisherContains())) {
                throw new SecurityException("Publisher search contains unsafe characters");
            }
        }
        
        if (criteria.getDescriptionContains() != null) {
            String sanitized = sanitizationService.sanitizeTextInput(criteria.getDescriptionContains());
            if (!sanitized.equals(criteria.getDescriptionContains())) {
                throw new SecurityException("Description search contains unsafe characters");
            }
        }
        
        if (criteria.getIsbnEquals() != null) {
            String sanitized = sanitizationService.sanitizeTextInput(criteria.getIsbnEquals());
            if (!sanitized.equals(criteria.getIsbnEquals())) {
                throw new SecurityException("ISBN search contains unsafe characters");
            }
        }
        
        // Validate contributors list
        if (criteria.getContributorsContain() != null) {
            for (String contributor : criteria.getContributorsContain()) {
                if (contributor != null) {
                    String sanitized = sanitizationService.sanitizeTextInput(contributor);
                    if (!sanitized.equals(contributor)) {
                        throw new SecurityException("Contributor search contains unsafe characters");
                    }
                }
            }
        }
        
        // Validate formats list
        if (criteria.getFormatsIn() != null) {
            for (String format : criteria.getFormatsIn()) {
                if (format != null) {
                    String sanitized = sanitizationService.sanitizeTextInput(format);
                    if (!sanitized.equals(format)) {
                        throw new SecurityException("Format search contains unsafe characters");
                    }
                }
            }
        }
        
        // Validate sort parameters
        if (criteria.getSortBy() != null) {
            String sanitized = sanitizationService.sanitizeTextInput(criteria.getSortBy());
            if (!sanitized.equals(criteria.getSortBy())) {
                throw new SecurityException("Sort field contains unsafe characters");
            }
        }
        
        if (criteria.getSortDirection() != null) {
            String sortDir = criteria.getSortDirection().toLowerCase();
            if (!sortDir.equals("asc") && !sortDir.equals("desc")) {
                throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
            }
        }
        
        // Validate date ranges
        if (criteria.getPublishedAfter() != null && criteria.getPublishedBefore() != null) {
            if (criteria.getPublishedAfter().isAfter(criteria.getPublishedBefore())) {
                throw new IllegalArgumentException("Published after date cannot be later than published before date");
            }
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
        if (book.getMetadata() != null && book.getMetadata().get("coverUrl") instanceof String url) {
            dto.setCoverUrl(url);
        }
        
        // Handle language
        if (book.getLanguage() != null) {
            dto.setLanguage(book.getLanguage().getName());
        }
        
        // Handle publisher
        if (book.getPublisher() != null) {
            dto.setPublisher(book.getPublisher().getName());
        }
        
        // Handle contributors - extract from relationships or metadata
        Map<String, List<String>> contributors = extractContributorsFromBook(book);
        dto.setContributors(contributors);

        // Detailed contributors with UUIDs for author role (and extendable)
        Map<String, List<org.roubinet.librarie.infrastructure.adapter.in.rest.dto.ContributorRefDto>> contributorsDetailed = new HashMap<>();
        List<org.roubinet.librarie.infrastructure.adapter.in.rest.dto.ContributorRefDto> authorRefs = new ArrayList<>();
        if (book.getOriginalWorks() != null) {
            for (BookOriginalWork bow : book.getOriginalWorks()) {
                OriginalWork ow = bow.getOriginalWork();
                if (ow != null && ow.getAuthors() != null) {
                    for (OriginalWorkAuthor owa : ow.getAuthors()) {
                        if (owa.getAuthor() != null) {
                            var auth = owa.getAuthor();
                            authorRefs.add(new org.roubinet.librarie.infrastructure.adapter.in.rest.dto.ContributorRefDto(auth.getId(), auth.getName()));
                        }
                    }
                }
            }
        }
        if (!authorRefs.isEmpty()) {
            contributorsDetailed.put("author", authorRefs);
        }
        if (!contributorsDetailed.isEmpty()) {
            dto.setContributorsDetailed(contributorsDetailed);
        }
        
        // Handle series - extract from relationships
        if (book.getSeries() != null && !book.getSeries().isEmpty()) {
            BookSeries firstSeries = book.getSeries().iterator().next();
            if (firstSeries.getSeries() != null) {
                dto.setSeries(firstSeries.getSeries().getName());
                dto.setSeriesId(firstSeries.getSeries().getId());
                dto.setSeriesIndex(firstSeries.getSeriesIndex().intValue());
            }
        }
        
        // Handle description - extract from metadata if available
        String description = extractDescriptionFromBook(book);
        dto.setDescription(description);
        
        return dto;
    }
    
    /**
     * Extract contributors from book relationships or metadata.
     */
    private Map<String, List<String>> extractContributorsFromBook(Book book) {
        Map<String, List<String>> contributors = new HashMap<>();
        
        // Extract authors from OriginalWorkAuthor relationships through BookOriginalWork
        List<String> authors = new ArrayList<>();
        
        if (book.getOriginalWorks() != null) {
            for (BookOriginalWork bookOriginalWork : book.getOriginalWorks()) {
                OriginalWork originalWork = bookOriginalWork.getOriginalWork();
                if (originalWork != null && originalWork.getAuthors() != null) {
                    for (OriginalWorkAuthor originalWorkAuthor : originalWork.getAuthors()) {
                        if (originalWorkAuthor.getAuthor() != null) {
                            authors.add(originalWorkAuthor.getAuthor().getName());
                        }
                    }
                }
            }
        }
        
        // Fallback to metadata if no relationship authors found
        if (authors.isEmpty() && book.getMetadata() != null) {
            if (book.getMetadata().containsKey("author")) {
                authors.add((String) book.getMetadata().get("author"));
            }
        }
        
        // Set authors in contributors
        if (!authors.isEmpty()) {
            contributors.put("author", authors);
        }
        
        // Handle other contributor types from metadata
        if (book.getMetadata() != null) {
            if (book.getMetadata().containsKey("illustrator")) {
                contributors.put("illustrator", List.of((String) book.getMetadata().get("illustrator")));
            }
            if (book.getMetadata().containsKey("translator")) {
                contributors.put("translator", List.of((String) book.getMetadata().get("translator")));
            }
        }
        
        // Default if no contributors found at all
        if (contributors.isEmpty()) {
            contributors.put("author", List.of("Unknown Author"));
        }
        
        return contributors;
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
        if (dto.getCoverUrl() != null && !dto.getCoverUrl().trim().isEmpty()) {
            // Basic allowlist: only http/https
            String url = dto.getCoverUrl().trim();
            if (url.startsWith("http://") || url.startsWith("https://")) {
                metadata.put("coverUrl", url);
                book.setHasCover(true);
            }
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

    @GET
    @Path("/{id}/cover")
    @Operation(summary = "Get book cover image", description = "Streams the cover image bytes resolved from stored coverUrl (supports direct images and Amazon product pages)")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Image bytes returned"),
        @APIResponse(responseCode = "404", description = "Cover not found")
    })
    public Response getBookCover(@PathParam("id") String id) {
        try {
            UUID bookId = UUID.fromString(id);
            Optional<Book> bookOpt = bookUseCase.getBookById(bookId);
            if (bookOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Book not found").build();
            }
            Book book = bookOpt.get();

            String storedUrl = null;
            if (Boolean.TRUE.equals(book.getHasCover()) && book.getMetadata() != null) {
                Object urlObj = book.getMetadata().get("coverUrl");
                if (urlObj instanceof String s && (s.startsWith("http://") || s.startsWith("https://"))) {
                    storedUrl = s.trim();
                }
            }

            java.nio.file.Path baseDir = java.nio.file.Paths.get(config.storage().baseDir());
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
        // Simple inline SVG placeholder for missing covers
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
                            <g fill='#9a9a9a'>
                                <rect x='60' y='120' width='200' height='20' rx='4'/>
                                <rect x='80' y='160' width='160' height='14' rx='4'/>
                                <rect x='80' y='200' width='160' height='14' rx='4'/>
                                <rect x='80' y='240' width='160' height='14' rx='4'/>
                            </g>
                        </svg>
                """;
                return svg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

}