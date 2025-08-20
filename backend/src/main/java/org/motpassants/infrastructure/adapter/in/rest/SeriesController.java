package org.motpassants.infrastructure.adapter.in.rest;

import org.motpassants.domain.core.model.Page;
import org.motpassants.domain.core.model.Series;
import org.motpassants.domain.port.in.SeriesUseCase;
import org.motpassants.infrastructure.adapter.in.rest.dto.SeriesRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.SeriesResponseDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.SeriesListItemDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.SeriesDetailsDto;
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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Context;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for series operations.
 * Provides the adapter for incoming HTTP requests in the hexagonal architecture.
 */
@Path("/v1/books/series")
@Tag(name = "Series", description = "Book series management operations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SeriesController {
    
    private final SeriesUseCase seriesUseCase;
    private final org.motpassants.application.service.BookService bookService;
    private final org.motpassants.infrastructure.media.ImageCachingService imageCachingService;
    private final org.motpassants.infrastructure.config.LibrarieConfigProperties config;

    @Context
    Request httpRequest;
    
    @Inject
    public SeriesController(SeriesUseCase seriesUseCase, org.motpassants.application.service.BookService bookService, org.motpassants.infrastructure.media.ImageCachingService imageCachingService, org.motpassants.infrastructure.config.LibrarieConfigProperties config) {
        this.seriesUseCase = seriesUseCase;
        this.bookService = bookService;
        this.imageCachingService = imageCachingService;
        this.config = config;
    }
    
    @GET
    @Operation(summary = "Get all series with pagination", description = "Retrieve all series using cursor-based pagination (preferred) or legacy offset-based")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Series retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getAllSeries(
            @Parameter(description = "Page number (0-based)", example = "0")
            @DefaultValue("0") @QueryParam("page") int page,
            @Parameter(description = "Number of items per page", example = "20")
            @DefaultValue("20") @QueryParam("size") int size,
            @Parameter(description = "Max items per page", example = "20")
            @QueryParam("limit") Integer limit,
            @Parameter(description = "Sort field (UPDATED_AT, TITLE_SORT, PUBLICATION_DATE, SORT_NAME)") @QueryParam("sortField") String sortField,
            @Parameter(description = "Sort direction (ASC, DESC)") @QueryParam("sortDirection") @DefaultValue("DESC") String sortDirection,
            @Parameter(description = "Cursor for next page") @QueryParam("cursor") String cursor) {

        try {
        // Parse sort params if present
        org.motpassants.domain.core.model.SeriesSortCriteria seriesSort = null;
        if (sortField != null && !sortField.trim().isEmpty()) {
            try {
                seriesSort = org.motpassants.domain.core.model.SeriesSortCriteria.of(sortField, sortDirection);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid sort parameters: " + e.getMessage()))
                    .build();
            }
        }

        // Prefer cursor-based when cursor or limit is provided explicitly
        if ((cursor != null && !cursor.isBlank()) || limit != null) {
        int pageSize = (limit != null) ? limit : size;
    org.motpassants.domain.core.model.PageResult<Series> result = (seriesSort != null) ? seriesUseCase.getAllSeries(cursor, pageSize, seriesSort) : seriesUseCase.getAllSeries(cursor, pageSize);
    List<SeriesListItemDto> items = result.getItems().stream().map(this::toListItemDto).collect(Collectors.toList());
    PageResponseDto<SeriesListItemDto> response = new PageResponseDto<>(
            items,
            result.getNextCursor(),
            result.getPreviousCursor(),
            pageSize,
            result.hasNext(),
            result.hasPrevious(),
            (long) result.getTotalCount()
        );
        return Response.ok(response).build();
        }

        // Fallback legacy offset-based
        int pageSize = (limit != null) ? limit : size;
    Page<Series> pageResult = seriesUseCase.getAllSeries(page, pageSize);
    List<SeriesListItemDto> seriesDtos = pageResult.getContent().stream().map(this::toListItemDto).collect(Collectors.toList());
    PageResponseDto<SeriesListItemDto> response = new PageResponseDto<>(
            seriesDtos,
            null,
            null,
            pageSize,
            pageResult.getMetadata().getPage() < pageResult.getMetadata().getTotalPages() - 1,
            pageResult.getMetadata().getPage() > 0,
            pageResult.getMetadata().getTotalElements()
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
    @Operation(summary = "Get series by ID", description = "Retrieve a specific series by its UUID")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Series found",
            content = @Content(schema = @Schema(implementation = SeriesDetailsDto.class))),
        @APIResponse(responseCode = "404", description = "Series not found")
    })
    public Response getSeriesById(
            @Parameter(description = "Series UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID seriesId = UUID.fromString(id);
            Optional<Series> seriesOpt = seriesUseCase.getSeriesById(seriesId);
            
            if (seriesOpt.isPresent()) {
                return Response.ok(toDetailsDto(seriesOpt.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Series not found")
                    .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid series ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @POST
    @Operation(summary = "Create a new series", description = "Create a new series with the provided details")
    @APIResponses(value = {
        @APIResponse(responseCode = "201", description = "Series created successfully",
            content = @Content(schema = @Schema(implementation = SeriesDetailsDto.class))),
        @APIResponse(responseCode = "400", description = "Invalid input"),
        @APIResponse(responseCode = "409", description = "Series already exists")
    })
    public Response createSeries(SeriesRequestDto request) {
        try {
            // Basic validation
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Series name is required")
                    .build();
            }
            
            Series series = seriesUseCase.createSeries(
                request.getName(),
                request.getSortName(),
                request.getDescription(),
                request.getImagePath(),
                request.getTotalBooks(),
                request.getIsCompleted(),
                request.getMetadata()
            );
            
            return Response.status(Response.Status.CREATED)
                .entity(toDto(series))
                .build();
                
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an existing series", description = "Update a series with the provided details")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Series updated successfully",
            content = @Content(schema = @Schema(implementation = SeriesResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Series not found"),
        @APIResponse(responseCode = "400", description = "Invalid input"),
        @APIResponse(responseCode = "409", description = "Series name already exists")
    })
    public Response updateSeries(
            @Parameter(description = "Series UUID", required = true)
            @PathParam("id") String id,
            SeriesRequestDto request) {
        
        try {
            UUID seriesId = UUID.fromString(id);
            Optional<Series> updatedSeries = seriesUseCase.updateSeries(
                seriesId,
                request.getName(),
                request.getSortName(),
                request.getDescription(),
                request.getImagePath(),
                request.getTotalBooks(),
                request.getIsCompleted(),
                request.getMetadata()
            );
            
            if (updatedSeries.isPresent()) {
                return Response.ok(toDetailsDto(updatedSeries.get())).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Series not found")
                    .build();
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already exists")) {
                return Response.status(Response.Status.CONFLICT)
                    .entity(e.getMessage())
                    .build();
            }
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid series ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a series", description = "Delete a series by its UUID")
    @APIResponses(value = {
        @APIResponse(responseCode = "204", description = "Series deleted successfully"),
        @APIResponse(responseCode = "404", description = "Series not found")
    })
    public Response deleteSeries(
            @Parameter(description = "Series UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID seriesId = UUID.fromString(id);
            boolean deleted = seriesUseCase.deleteSeries(seriesId);
            
            if (deleted) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Series not found")
                    .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid series ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @GET
    @Path("/search")
    @Operation(summary = "Search series", description = "Search series by name")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully"),
        @APIResponse(responseCode = "400", description = "Invalid query parameter")
    })
    public Response searchSeries(
            @Parameter(description = "Search query", required = true)
            @QueryParam("q") String query,
            @Parameter(description = "Number of items per page", example = "20")
            @DefaultValue("20") @QueryParam("size") int size,
            @Parameter(description = "Number of items per page (alternative to size)", example = "20")
            @QueryParam("limit") Integer limit) {
        
        try {
            // Validate query parameter
            if (query == null || query.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Query parameter 'q' is required and cannot be empty")
                    .build();
            }
            
            List<Series> series = seriesUseCase.searchSeries(query);
            List<SeriesListItemDto> seriesDtos = series.stream()
                .map(this::toListItemDto)
                .collect(Collectors.toList());
            
            // Apply pagination if limit/size is specified
            int pageSize = (limit != null) ? limit : size;
            if (pageSize < seriesDtos.size()) {
                seriesDtos = seriesDtos.subList(0, Math.min(pageSize, seriesDtos.size()));
            }
            
            // Create paginated response to match test expectations
            PageResponseDto<SeriesListItemDto> response = new PageResponseDto<SeriesListItemDto>(
                seriesDtos,
                null, // nextCursor
                null, // previousCursor
                pageSize, // limit
                false, // hasNext (single page search result)
                false, // hasPrevious (single page search result)
                (long) series.size() // totalElements
            );
            
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    @GET
    @Path("/{id}/picture")
    @Produces("image/*")
    @Operation(summary = "Get series picture", description = "Get the series cover image")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Image returned"),
        @APIResponse(responseCode = "404", description = "Series or image not found")
    })
    public Response getSeriesPicture(
            @Parameter(description = "Series UUID", required = true)
            @PathParam("id") String id) {
        try {
            UUID seriesId = UUID.fromString(id);
            Optional<Series> seriesOpt = seriesUseCase.getSeriesById(seriesId);
            if (seriesOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Series not found")
                    .build();
            }

            Series series = seriesOpt.get();
            String remoteUrl = null;
            boolean remoteUrlFromImagePath = false;
            String remoteUrlMetadataKey = null;

            // 1) If imagePath is a remote URL, prefer it
            String imagePath = series.getImagePath();
            if (imagePath != null && !imagePath.isBlank()) {
                String trimmed = imagePath.trim();
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    remoteUrl = trimmed;
                    remoteUrlFromImagePath = true;
                } else {
                    // 2) If imagePath is a local relative path, try serving it directly for backward compatibility
                    byte[] bytes = imageCachingService.getImage(trimmed);
                    if (bytes != null) {
                        String mime = imageCachingService.getImageMimeType(trimmed);
                        return Response.ok(bytes)
                            .type(mime)
                            .header("Cache-Control", "max-age=3600")
                            .build();
                    }
                }
            }

            // 3) Otherwise check metadata for a remote URL (support both keys: seriesImageUrl and imageUrl)
            if (remoteUrl == null && series.getMetadata() != null) {
                Object urlObj = series.getMetadata().get("seriesImageUrl");
                if (!(urlObj instanceof String) || ((String) urlObj).isBlank()) {
                    urlObj = series.getMetadata().get("imageUrl");
                }
                if (urlObj instanceof String s) {
                    String trimmed = s.trim();
                    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                        remoteUrl = trimmed;
                        // track which key we used
                        if (series.getMetadata().get("seriesImageUrl") instanceof String && ((String) series.getMetadata().get("seriesImageUrl")).trim().equals(trimmed)) {
                            remoteUrlMetadataKey = "seriesImageUrl";
                        } else if (series.getMetadata().get("imageUrl") instanceof String && ((String) series.getMetadata().get("imageUrl")).trim().equals(trimmed)) {
                            remoteUrlMetadataKey = "imageUrl";
                        }
                    }
                }
            }

            // Validate remote URL if present. If broken, clear it from the series and metadata, then fall back to books.
            if (remoteUrl != null && !isRemoteImageReachable(remoteUrl)) {
                try {
                    String newImagePath = remoteUrlFromImagePath ? null : series.getImagePath();
                    java.util.Map<String, Object> newMeta = series.getMetadata();
                    if (newMeta != null && remoteUrlMetadataKey != null) {
                        newMeta = new java.util.HashMap<>(newMeta);
                        newMeta.remove(remoteUrlMetadataKey);
                    }
                    // Important: preserve current description to avoid nulling it (updateDetails sets description directly)
                    seriesUseCase.updateSeries(
                        seriesId,
                        null, // name unchanged
                        null, // sortName unchanged
                        series.getDescription(),
                        newImagePath,
                        null, // totalBooks unchanged
                        null, // isCompleted unchanged
                        newMeta
                    );
                } catch (Exception ignored) { /* non-blocking cleanup */ }
                remoteUrl = null; // force fallback path
            }

            // 4) If still nothing, try series-cover fallback from first book with a cover
            if (remoteUrl == null) {
                java.util.List<org.motpassants.domain.core.model.Book> ordered = bookService.getBooksBySeriesOrderedByIndex(seriesId, 20);
                for (org.motpassants.domain.core.model.Book b : ordered) {
                    if (Boolean.TRUE.equals(b.getHasCover())) {
                        // Attempt to serve local cover directly from books/covers/<bookId>
                        java.nio.file.Path baseDir = java.nio.file.Paths.get(config.storage().baseDir());
                        // Reuse the same serving helper but pointing to books folder; no remote URL
                        return imageCachingService.serveLocalFirstStrongETag(
                            httpRequest,
                            baseDir,
                            "books",
                            "covers",
                            b.getId().toString(),
                            null,
                            getSeriesFailoverSvg()
                        );
                    }
                }
            }

            java.nio.file.Path baseDir = java.nio.file.Paths.get(config.storage().baseDir());
            // If no series image and no fallback from books, return 404 (no covers)
            return imageCachingService.serveLocalFirstStrongETag(
                httpRequest,
                baseDir,
                "series",
                "covers",
                id,
                remoteUrl,
                null
            );
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid series ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }

    private boolean isRemoteImageReachable(String url) {
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .build();
            // First try HEAD
            java.net.http.HttpRequest head = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                .timeout(java.time.Duration.ofSeconds(6))
                .method("HEAD", java.net.http.HttpRequest.BodyPublishers.noBody())
                .header("User-Agent", "Librarie/1.0")
                .build();
            java.net.http.HttpResponse<Void> hr = client.send(head, java.net.http.HttpResponse.BodyHandlers.discarding());
            int sc = hr.statusCode();
            String ct = hr.headers().firstValue("content-type").orElse("").toLowerCase();
            if (sc >= 200 && sc < 300 && ct.startsWith("image/")) return true;
        } catch (Exception ignore) {
            // fall through to GET
        }
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .build();
            java.net.http.HttpRequest get = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                .timeout(java.time.Duration.ofSeconds(8))
                .header("User-Agent", "Librarie/1.0")
                .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                .GET()
                .build();
            java.net.http.HttpResponse<byte[]> gr = client.send(get, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
            int sc = gr.statusCode();
            String ct = gr.headers().firstValue("content-type").orElse("").toLowerCase();
            return sc >= 200 && sc < 300 && ct.startsWith("image/") && gr.body() != null && gr.body().length > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] getSeriesFailoverSvg() {
        String svg = """
                <svg xmlns='http://www.w3.org/2000/svg' width='320' height='320' viewBox='0 0 320 320'>
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
                    </g>
                </svg>
                """;
        return svg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    
    @GET
    @Path("/{id}/books")
    @Operation(summary = "Get all books in series (no pagination)", description = "Returns all books for a given series, ordered by series index when available, with minimal fields required for the page")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Books retrieved successfully"),
        @APIResponse(responseCode = "404", description = "Series not found")
    })
    public Response getSeriesBooks(
            @Parameter(description = "Series UUID", required = true)
            @PathParam("id") String id) {
        try {
            UUID seriesId = UUID.fromString(id);

            // Ensure series exists
            Optional<Series> seriesOpt = seriesUseCase.getSeriesById(seriesId);
            if (seriesOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Series not found")
                    .build();
            }

            // Use ordered-by-index for UX; fetch up to 100, then if more, append rest by created_at DESC
        java.util.List<org.motpassants.domain.core.model.Book> first = bookService.getBooksBySeriesOrderedByIndex(seriesId, 100);
            java.util.Set<java.util.UUID> seen = new java.util.HashSet<>();
            java.util.List<org.motpassants.infrastructure.adapter.in.rest.dto.SeriesBookItemDto> items = new java.util.ArrayList<>();
            for (var b : first) {
                seen.add(b.getId());
                items.add(org.motpassants.infrastructure.adapter.in.rest.dto.SeriesBookItemDto.builder()
                    .id(b.getId())
                    .title(b.getTitle())
                    .hasCover(Boolean.TRUE.equals(b.getHasCover()))
            .seriesIndex(extractSeriesIndex(b, seriesId))
                    .publicationDate(b.getPublicationDate())
                    .build());
            }

            // If there might be more, fall back to cursor pages to fetch all remaining
            String cursor = null;
            int pageLimit = 100;
            while (true) {
                var page = bookService.getBooksBySeries(seriesId, cursor, pageLimit);
                if (page.getItems() == null || page.getItems().isEmpty()) break;
                for (var b : page.getItems()) {
                    if (seen.add(b.getId())) {
                        items.add(org.motpassants.infrastructure.adapter.in.rest.dto.SeriesBookItemDto.builder()
                            .id(b.getId())
                            .title(b.getTitle())
                            .hasCover(Boolean.TRUE.equals(b.getHasCover()))
                            .seriesIndex(extractSeriesIndex(b, seriesId))
                            .publicationDate(b.getPublicationDate())
                            .build());
                    }
                }
                if (!page.hasNext()) break;
                cursor = page.getNextCursor();
            }

            // Sort by series index asc (nulls last); then title
            items.sort((a, b) -> {
                Double ai = a.getSeriesIndex();
                Double bi = b.getSeriesIndex();
                if (ai == null && bi == null) return a.getTitle().compareToIgnoreCase(b.getTitle());
                if (ai == null) return 1;
                if (bi == null) return -1;
                int cmp = Double.compare(ai, bi);
                if (cmp != 0) return cmp;
                return a.getTitle().compareToIgnoreCase(b.getTitle());
            });

            return Response.ok(items).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid series ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }

    private static Double extractSeriesIndex(org.motpassants.domain.core.model.Book book, java.util.UUID seriesId) {
        if (book == null || book.getSeries() == null || book.getSeries().isEmpty()) return null;
        for (org.motpassants.domain.core.model.BookSeries bs : book.getSeries()) {
            if (bs != null && bs.getSeries() != null && seriesId.equals(bs.getSeries().getId())) {
                return bs.getSeriesIndex();
            }
        }
        // fallback: if only one relation present, return its index
        org.motpassants.domain.core.model.BookSeries first = book.getSeries().iterator().next();
        return first != null ? first.getSeriesIndex() : null;
    }
    
    /**
     * Convert Series domain model to DTO.
     */
    private SeriesResponseDto toDto(Series series) {
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
            .metadata(series.getMetadata())
            .createdAt(series.getCreatedAt())
            .updatedAt(series.getUpdatedAt())
            .hasPicture(series.getHasPicture())
            .build();
    }

    // New minimal mapper for list pages
    private SeriesListItemDto toListItemDto(Series series) {
        if (series == null) return null;
        return SeriesListItemDto.builder()
            .id(series.getId())
            .name(series.getName())
            .bookCount(series.getTotalBooks())
            .hasPicture(series.getHasPicture())
            .build();
    }

    // New detailed mapper for details page
    private SeriesDetailsDto toDetailsDto(Series series) {
        if (series == null) return null;
        return SeriesDetailsDto.builder()
            .id(series.getId())
            .name(series.getName())
            .sortName(series.getSortName())
            .description(series.getDescription())
            .imagePath(series.getImagePath())
            .bookCount(series.getTotalBooks())
            .isCompleted(series.getIsCompleted())
            .hasPicture(series.getHasPicture())
            .metadata(series.getMetadata())
            .createdAt(series.getCreatedAt())
            .updatedAt(series.getUpdatedAt())
            .build();
    }
}