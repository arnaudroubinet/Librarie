package org.motpassants.infrastructure.adapter.in.rest;

import org.motpassants.application.service.BookService;
import org.motpassants.application.service.DemoDataService;
import org.motpassants.application.service.ReadingProgressService;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.BookSearchCriteria;
import org.motpassants.domain.core.model.BookSortCriteria;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.core.model.ReadingProgress;
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

import java.io.InputStream;
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
    private final ReadingProgressService readingProgressService;
    private final org.motpassants.infrastructure.media.ImageCachingService imageCachingService;
    private final org.motpassants.infrastructure.config.LibrarieConfigProperties config;
    private final DemoDataService demoDataService;
    private final org.motpassants.infrastructure.readium.EpubPublicationService epubService;

    @Context
    Request httpRequest;

    @Inject
    public BookController(BookService bookService, ReadingProgressService readingProgressService, org.motpassants.infrastructure.media.ImageCachingService imageCachingService, org.motpassants.infrastructure.config.LibrarieConfigProperties config, DemoDataService demoDataService, org.motpassants.infrastructure.readium.EpubPublicationService epubService) {
        this.bookService = bookService;
        this.readingProgressService = readingProgressService;
        this.imageCachingService = imageCachingService;
        this.config = config;
        this.demoDataService = demoDataService;
        this.epubService = epubService;
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

    // ========= Readium-style web publication endpoints =========

    @GET
    @Path("/{id}/manifest.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Readium Web Publication manifest", description = "Returns a minimal Readium Web Publication Manifest for this EPUB")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Manifest returned"),
        @APIResponse(responseCode = "404", description = "Book or file not found"),
        @APIResponse(responseCode = "400", description = "Invalid book ID")
    })
    public Response getReadiumManifest(@PathParam("id") String id, @Context UriInfo uriInfo) {
        try {
            UUID bookId = bookService.validateAndParseId(id);
            Optional<Book> bookOpt = bookService.getBookById(bookId);
            if (bookOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Book not found").build();
            }
            Book book = bookOpt.get();
            var pubOpt = epubService.openPublication(book);
            if (pubOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("EPUB not available").build();
            }
            var pub = pubOpt.get();

            String selfHref = uriInfo.getAbsolutePath().toString();
            String base = selfHref.substring(0, selfHref.lastIndexOf('/'));
            String resourcesBase = base + "/resources";

            // Build a minimal manifest compliant enough for Readium Web navigator
            Map<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("@context", List.of("https://readium.org/webpub-manifest/context.jsonld"));
            manifest.put("metadata", Map.of(
                "title", pub.getTitle() != null ? pub.getTitle() : (book.getTitle() != null ? book.getTitle() : id),
                "language", pub.getLanguage() != null ? pub.getLanguage() : "en"
            ));

            // readingOrder (spine)
            List<Map<String, Object>> readingOrder = new ArrayList<>();
            for (String href : pub.getSpineResourceHrefs()) {
                String abs = resourcesBase + "/" + normalizeForUrl(epubService.buildZipPath(pub.getOpfDir(), href));
                String type = Optional.ofNullable(pub.getManifestHrefToMediaType().get(href))
                        .orElse(epubService.guessContentType(href));
                Map<String, Object> link = new LinkedHashMap<>();
                link.put("href", abs);
                link.put("type", type);
                readingOrder.add(link);
            }
            manifest.put("readingOrder", readingOrder);

            // resources (all manifest items we know content-type for)
            List<Map<String, Object>> resources = new ArrayList<>();
            for (var e : pub.getManifestHrefToMediaType().entrySet()) {
                String href = e.getKey();
                String type = e.getValue();
                if (href == null) continue;
                String abs = resourcesBase + "/" + normalizeForUrl(epubService.buildZipPath(pub.getOpfDir(), href));
                Map<String, Object> link = new LinkedHashMap<>();
                link.put("href", abs);
                if (type != null) link.put("type", type);
                resources.add(link);
            }
            manifest.put("resources", resources);

            // TOC if present in OPF (nav doc or ncx)
            try {
                List<Map<String, Object>> toc = epubService.extractTocLinks(pub).stream()
                    .map(href -> {
                        String abs = resourcesBase + "/" + normalizeForUrl(href);
                        Map<String, Object> link = new LinkedHashMap<>();
                        link.put("href", abs);
                        return link;
                    })
                    .toList();
                if (!toc.isEmpty()) {
                    manifest.put("toc", toc);
                }
            } catch (Exception ignore) {
                // Optional: ignore TOC extraction failures
            }

            // self link
            manifest.put("links", List.of(Map.of("rel", List.of("self"), "href", selfHref, "type", "application/webpub+json")));

            return Response.ok(manifest).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid book ID format").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/{id}/resources/{path: .+}")
    @Operation(summary = "Readium resource proxy", description = "Streams a resource from inside the EPUB zip using a stable URL")
    public Response getReadiumResource(@PathParam("id") String id, @PathParam("path") String path) {
        try {
            UUID bookId = bookService.validateAndParseId(id);
            Optional<Book> bookOpt = bookService.getBookById(bookId);
            if (bookOpt.isEmpty()) return Response.status(Response.Status.NOT_FOUND).entity("Book not found").build();
            var pubOpt = epubService.openPublication(bookOpt.get());
            if (pubOpt.isEmpty()) return Response.status(Response.Status.NOT_FOUND).entity("EPUB not available").build();
            var pub = pubOpt.get();

            // Prevent path traversal: path is already relative to zip root; enforce no .. segments
            String safe = path.replace("\\", "/");
            if (safe.contains("..")) {
                return Response.status(Response.Status.FORBIDDEN).entity("Invalid path").build();
            }

            // Compute caching headers (ETag + Last-Modified) using EPUB file and entry metadata
            java.util.Date lastModifiedDate = null;
            String etagValue = null;
            try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(pub.getEpubFile().toFile())) {
                java.util.zip.ZipEntry entry = zip.getEntry(safe);
                if (entry == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity("Resource not found").build();
                }
                long fileMtime = java.nio.file.Files.getLastModifiedTime(pub.getEpubFile()).toMillis();
                long entryTime = entry.getTime();
                long lm = Math.max(fileMtime, entryTime > 0 ? entryTime : 0);
                if (lm > 0) lastModifiedDate = new java.util.Date(lm);
                long crc = entry.getCrc();
                long size = entry.getSize();
                etagValue = String.format("W/\"%s-%s-%s-%s\"", pub.getEpubFile().getFileName(), Long.toHexString(fileMtime), Long.toHexString(crc), Long.toHexString(size));
            } catch (Exception ignore) {
                // Best-effort; if metadata fails, continue without precondition short-circuit
            }

            // Short-circuit with 304 if ETag matches
            Response.ResponseBuilder precond = null;
            if (etagValue != null) {
                precond = httpRequest.evaluatePreconditions(new EntityTag(etagValue));
            }
            jakarta.ws.rs.core.CacheControl cc = new jakarta.ws.rs.core.CacheControl();
            cc.setPrivate(false);
            cc.setMaxAge(86400); // 1 day - static EPUB resources don't change

            if (precond != null) {
                return precond.cacheControl(cc).tag(new EntityTag(etagValue))
                    .lastModified(lastModifiedDate)
                    .build();
            }

            // Stream entry with headers
            final String contentType = epubService.guessContentType(safe);
            InputStream is = epubService.openEntryStream(pub, safe);
            Response.ResponseBuilder builder = Response.ok(is).type(contentType);
            if (etagValue != null) builder.tag(new EntityTag(etagValue));
            if (lastModifiedDate != null) builder.lastModified(lastModifiedDate);
            builder.cacheControl(cc);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid book ID format").build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity("Resource not found").build();
        }
    }

    private static String normalizeForUrl(String in) {
        if (in == null) return null;
        return in.replace("\\", "/");
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
    @Operation(summary = "Update reading completion", description = "Update the reading completion progress for a book with optional status")
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
            if (!(progressObj instanceof Number progressNumber)) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Progress must be a number")
                    .build();
            }
            
            double progress = progressNumber.doubleValue();
            if (progress < 0 || progress > 100) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Progress must be between 0 and 100")
                    .build();
            }
            
            // Get additional parameters
            Integer currentPage = null;
            Integer totalPages = null;
            
            if (completionData.containsKey("currentPage") && completionData.get("currentPage") instanceof Number) {
                currentPage = ((Number) completionData.get("currentPage")).intValue();
            }
            
            if (completionData.containsKey("totalPages") && completionData.get("totalPages") instanceof Number) {
                totalPages = ((Number) completionData.get("totalPages")).intValue();
            }
            
            // Parse status if provided
            org.motpassants.domain.core.model.ReadingStatus status = null;
            if (completionData.containsKey("status")) {
                Object statusObj = completionData.get("status");
                if (statusObj instanceof String statusStr) {
                    try {
                        status = org.motpassants.domain.core.model.ReadingStatus.fromString(statusStr);
                    } catch (IllegalArgumentException e) {
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Invalid status value: " + statusStr)
                            .build();
                    }
                }
            }
            
            // For now, use a mock user ID - in real implementation, get from security context
            UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"); // Mock user ID
            
            // Convert percentage to decimal (0-100 -> 0.0-1.0)
            double progressDecimal = progress / 100.0;
            
            // Optional raw Readium locator from client
            String locatorJson = null;
            if (completionData.containsKey("locator")) {
                Object locatorObj = completionData.get("locator");
                if (locatorObj instanceof String s) {
                    locatorJson = s;
                } else if (locatorObj instanceof Map<?,?> m) {
                    // Serialize map to JSON string for storage
                    try {
                        locatorJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(m);
                    } catch (Exception ignore) { /* ignore serialization error */ }
                }
            }

            // Update reading progress with or without status
            ReadingProgress readingProgress;
            if (status != null) {
                readingProgress = readingProgressService.updateReadingProgressWithStatus(
                    userId, bookId, progressDecimal, currentPage, totalPages, locatorJson, status);
            } else {
                readingProgress = readingProgressService.updateReadingProgress(
                    userId, bookId, progressDecimal, currentPage, totalPages, locatorJson);
            }
            
            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("progress", progress);
            response.put("currentPage", readingProgress.getCurrentPage() != null ? readingProgress.getCurrentPage() : 0);
            response.put("totalPages", readingProgress.getTotalPages() != null ? readingProgress.getTotalPages() : 0);
            response.put("isCompleted", readingProgress.getIsCompleted() != null ? readingProgress.getIsCompleted() : false);
            response.put("status", readingProgress.getStatus() != null ? readingProgress.getStatus().name() : "READING");
            if (readingProgress.getStartedAt() != null) {
                response.put("startedAt", readingProgress.getStartedAt().toString());
            }
            if (readingProgress.getFinishedAt() != null) {
                response.put("finishedAt", readingProgress.getFinishedAt().toString());
            }
            if (readingProgress.getProgressLocator() != null) {
                response.put("locator", readingProgress.getProgressLocator());
            }
            response.put("syncVersion", readingProgress.getSyncVersion() != null ? readingProgress.getSyncVersion() : 1L);
            response.put("message", "Reading completion updated successfully");
            
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
    @Path("/{id}/progress")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get reading progress", description = "Get the current reading progress for a book")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Reading progress retrieved successfully"),
        @APIResponse(responseCode = "404", description = "Book not found or no reading progress"),
        @APIResponse(responseCode = "400", description = "Invalid book ID")
    })
    public Response getReadingProgress(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = bookService.validateAndParseId(id);
            
            Optional<Book> existingBook = bookService.getBookById(bookId);
            if (existingBook.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            // For now, use a mock user ID - in real implementation, get from security context
            UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"); // Mock user ID
            
            Optional<ReadingProgress> progress = readingProgressService.getReadingProgress(userId, bookId);
            
            Map<String, Object> response;
            if (progress.isEmpty()) {
                // Default to start-of-book when no progress exists yet
                Map<String, Object> resp = new java.util.LinkedHashMap<>();
                resp.put("progress", 0.0);
                resp.put("currentPage", 0);
                resp.put("totalPages", 0);
                resp.put("isCompleted", false);
                resp.put("status", "UNREAD");
                resp.put("lastReadAt", null);
                resp.put("startedAt", null);
                resp.put("finishedAt", null);
                resp.put("syncVersion", 1L);
                response = resp;
            } else {
                ReadingProgress readingProgress = progress.get();
                Map<String, Object> resp = new java.util.LinkedHashMap<>();
                resp.put("progress", readingProgress.getProgress() != null ? readingProgress.getProgress() * 100 : 0.0);
                resp.put("currentPage", readingProgress.getCurrentPage() != null ? readingProgress.getCurrentPage() : 0);
                resp.put("totalPages", readingProgress.getTotalPages() != null ? readingProgress.getTotalPages() : 0);
                resp.put("isCompleted", readingProgress.getIsCompleted() != null ? readingProgress.getIsCompleted() : false);
                resp.put("status", readingProgress.getStatus() != null ? readingProgress.getStatus().name() : "READING");
                resp.put("lastReadAt", readingProgress.getLastReadAt());
                if (readingProgress.getStartedAt() != null) {
                    resp.put("startedAt", readingProgress.getStartedAt());
                }
                if (readingProgress.getFinishedAt() != null) {
                    resp.put("finishedAt", readingProgress.getFinishedAt());
                }
                if (readingProgress.getProgressLocator() != null) {
                    resp.put("locator", readingProgress.getProgressLocator());
                }
                if (readingProgress.getNotes() != null) {
                    resp.put("notes", readingProgress.getNotes());
                }
                resp.put("syncVersion", readingProgress.getSyncVersion() != null ? readingProgress.getSyncVersion() : 1L);
                response = resp;
            }
            
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
    @Path("/{id}/file")
    @Produces("application/epub+zip")
    @Operation(summary = "Get book file", description = "Streams the book file for reading")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Book file returned"),
        @APIResponse(responseCode = "404", description = "Book or file not found"),
        @APIResponse(responseCode = "403", description = "Access denied")
    })
    public Response getBookFile(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = bookService.validateAndParseId(id);
            
            Optional<Book> bookOpt = bookService.getBookById(bookId);
            if (bookOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            Book book = bookOpt.get();
            
            // For security, validate that the file is an EPUB
            if (book.getPath() == null || !book.getPath().toLowerCase().endsWith(".epub")) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Only EPUB files are supported for reading")
                    .build();
            }
            
            // Build the full file path (using the assets directory from config)
            java.nio.file.Path basePath = java.nio.file.Paths.get(config.storage().baseDir()).normalize();
            // Ensure stored path is treated as a relative path inside baseDir (demo seed stores paths with leading '/')
            String stored = book.getPath();
            if (stored == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book file not found")
                    .build();
            }
            String relative = stored.startsWith("/") || stored.startsWith("\\") ? stored.substring(1) : stored;
            java.nio.file.Path bookPath = basePath.resolve(relative).normalize();
            
            // Security check - ensure the path is within the base directory
            if (!bookPath.startsWith(basePath)) {
                return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied")
                    .build();
            }
            
            // Check if file exists
            if (!java.nio.file.Files.exists(bookPath)) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book file not found")
                    .build();
            }
            
            // Stream the file
            java.io.File file = bookPath.toFile();
            return Response.ok(file)
                .header("Content-Disposition", "inline; filename=\"" + book.getTitle() + ".epub\"")
                .header("Content-Type", "application/epub+zip")
                .header("Content-Length", file.length())
                .build();
            
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

    @POST
    @Path("/{id}/mark-started")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Mark book as started", description = "Mark a book as started reading")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Book marked as started successfully"),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid book ID")
    })
    public Response markAsStarted(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = bookService.validateAndParseId(id);
            
            Optional<Book> existingBook = bookService.getBookById(bookId);
            if (existingBook.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            // For now, use a mock user ID - in real implementation, get from security context
            UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            
            ReadingProgress readingProgress = readingProgressService.markAsStarted(userId, bookId);
            
            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("status", readingProgress.getStatus().name());
            response.put("startedAt", readingProgress.getStartedAt());
            response.put("message", "Book marked as started successfully");
            
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

    @POST
    @Path("/{id}/mark-finished")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Mark book as finished", description = "Mark a book as completed/finished reading")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Book marked as finished successfully"),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid book ID")
    })
    public Response markAsFinished(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = bookService.validateAndParseId(id);
            
            Optional<Book> existingBook = bookService.getBookById(bookId);
            if (existingBook.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            // For now, use a mock user ID - in real implementation, get from security context
            UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            
            ReadingProgress readingProgress = readingProgressService.markAsCompleted(userId, bookId);
            
            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("status", readingProgress.getStatus().name());
            response.put("finishedAt", readingProgress.getFinishedAt());
            response.put("progress", 100.0);
            response.put("isCompleted", true);
            response.put("message", "Book marked as finished successfully");
            
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

    @POST
    @Path("/{id}/mark-dnf")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Mark book as DNF", description = "Mark a book as DNF (Did Not Finish)")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Book marked as DNF successfully"),
        @APIResponse(responseCode = "404", description = "Book not found"),
        @APIResponse(responseCode = "400", description = "Invalid book ID or no prior reading progress"),
        @APIResponse(responseCode = "409", description = "Cannot mark as DNF without starting the book")
    })
    public Response markAsDnf(
            @Parameter(description = "Book UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID bookId = bookService.validateAndParseId(id);
            
            Optional<Book> existingBook = bookService.getBookById(bookId);
            if (existingBook.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Book not found")
                    .build();
            }
            
            // For now, use a mock user ID - in real implementation, get from security context
            UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
            
            ReadingProgress readingProgress = readingProgressService.markAsDnf(userId, bookId);
            
            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("status", readingProgress.getStatus().name());
            response.put("progress", readingProgress.getProgress() != null ? readingProgress.getProgress() * 100 : 0.0);
            response.put("isCompleted", false);
            response.put("message", "Book marked as DNF successfully");
            
            return Response.ok(response).build();
            
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(e.getMessage())
                .build();
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