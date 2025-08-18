package org.motpassants.infrastructure.adapter.in.rest;

import org.motpassants.domain.core.model.Page;
import org.motpassants.domain.core.model.Series;
import org.motpassants.domain.port.in.SeriesUseCase;
import org.motpassants.infrastructure.adapter.in.rest.dto.SeriesRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.SeriesResponseDto;
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
            @Parameter(description = "Cursor for next page")
            @QueryParam("cursor") String cursor) {
        
        try {
        // Prefer cursor-based when cursor or limit is provided explicitly
        if ((cursor != null && !cursor.isBlank()) || limit != null) {
        int pageSize = (limit != null) ? limit : size;
        org.motpassants.domain.core.model.PageResult<Series> result = seriesUseCase.getAllSeries(cursor, pageSize);
        List<SeriesResponseDto> items = result.getItems().stream().map(this::toDto).collect(Collectors.toList());
        PageResponseDto<SeriesResponseDto> response = new PageResponseDto<>(
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
        List<SeriesResponseDto> seriesDtos = pageResult.getContent().stream().map(this::toDto).collect(Collectors.toList());
        PageResponseDto<SeriesResponseDto> response = new PageResponseDto<>(
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
            content = @Content(schema = @Schema(implementation = SeriesResponseDto.class))),
        @APIResponse(responseCode = "404", description = "Series not found")
    })
    public Response getSeriesById(
            @Parameter(description = "Series UUID", required = true)
            @PathParam("id") String id) {
        
        try {
            UUID seriesId = UUID.fromString(id);
            Optional<Series> seriesOpt = seriesUseCase.getSeriesById(seriesId);
            
            if (seriesOpt.isPresent()) {
                return Response.ok(toDto(seriesOpt.get())).build();
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
            content = @Content(schema = @Schema(implementation = SeriesResponseDto.class))),
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
                return Response.ok(toDto(updatedSeries.get())).build();
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
            List<SeriesResponseDto> seriesDtos = series.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            // Apply pagination if limit/size is specified
            int pageSize = (limit != null) ? limit : size;
            if (pageSize < seriesDtos.size()) {
                seriesDtos = seriesDtos.subList(0, Math.min(pageSize, seriesDtos.size()));
            }
            
            // Create paginated response to match test expectations
            PageResponseDto<SeriesResponseDto> response = new PageResponseDto<SeriesResponseDto>(
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

            // 1) If imagePath is a remote URL, prefer it
            String imagePath = series.getImagePath();
            if (imagePath != null && !imagePath.isBlank()) {
                String trimmed = imagePath.trim();
                if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
                    remoteUrl = trimmed;
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
                    }
                }
            }

            java.nio.file.Path baseDir = java.nio.file.Paths.get(config.storage().baseDir());
            return imageCachingService.serveLocalFirstStrongETag(
                httpRequest,
                baseDir,
                "series",
                "covers",
                id,
                remoteUrl,
                getSeriesFailoverSvg()
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
    @Operation(summary = "Get books in series", description = "Get all books in a specific series")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Books retrieved successfully"),
        @APIResponse(responseCode = "404", description = "Series not found")
    })
    public Response getSeriesBooks(
            @Parameter(description = "Series UUID", required = true)
            @PathParam("id") String id,
            @Parameter(description = "Cursor for next page") @QueryParam("cursor") String cursor,
            @Parameter(description = "Max items per page", example = "20") @DefaultValue("20") @QueryParam("limit") int limit) {
        
        try {
            UUID seriesId = UUID.fromString(id);
            
            // Check if series exists
            Optional<Series> seriesOpt = seriesUseCase.getSeriesById(seriesId);
            if (seriesOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                    .entity("Series not found")
                    .build();
            }
            
            org.motpassants.domain.core.model.PageResult<org.motpassants.domain.core.model.Book> result =
                bookService.getBooksBySeries(seriesId, cursor, limit);

            java.util.List<org.motpassants.infrastructure.adapter.in.rest.dto.BookResponseDto> items = result.getItems().stream()
                .map(this::toBookDto)
                .collect(java.util.stream.Collectors.toList());

            PageResponseDto<org.motpassants.infrastructure.adapter.in.rest.dto.BookResponseDto> response = new PageResponseDto<>(
                items,
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
                .entity("Invalid series ID format")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
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
            .fallbackImagePath(series.getEffectiveImagePath())
            .hasPicture(series.getHasPicture())
            .build();
    }

    private org.motpassants.infrastructure.adapter.in.rest.dto.BookResponseDto toBookDto(org.motpassants.domain.core.model.Book book) {
        // Reuse mapping from BookController to ensure consistency
        org.motpassants.infrastructure.adapter.in.rest.dto.BookResponseDto.Builder builder = org.motpassants.infrastructure.adapter.in.rest.dto.BookResponseDto.builder()
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

        if (book.getPublisher() != null) {
            builder.publisher(book.getPublisher().getName());
        }
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
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(java.util.stream.Collectors.toList()));
        }
        return builder.build();
    }
}