package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.SeriesUseCase;
import org.roubinet.librarie.domain.model.SeriesData;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.SeriesResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.PageResponseDto;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;
import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;

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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.roubinet.librarie.infrastructure.media.ImageCachingService;

/**
 * REST controller for series operations.
 * Feature-based API with version 1 path structure.
 */
@Path("/v1/books/series")
@Tag(name = "Series", description = "Book series management operations")
public class SeriesController {
    
    private final SeriesUseCase seriesUseCase;
    private final LibrarieConfigProperties config;
    private final ImageCachingService imageCachingService;
    @Context
    Request httpRequest;
    
    @Inject
    public SeriesController(SeriesUseCase seriesUseCase,
                           LibrarieConfigProperties config,
                           ImageCachingService imageCachingService) {
        this.seriesUseCase = seriesUseCase;
        this.config = config;
        this.imageCachingService = imageCachingService;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all series with cursor-based pagination", description = "Retrieve all series using keyset pagination")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Series retrieved successfully",
            content = @Content(schema = @Schema(implementation = PageResponseDto.class)))
    })
    public Response getAllSeries(
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
            
            // Use cursor pagination
            CursorPageResult<SeriesData> pageResult = seriesUseCase.getAllSeries(cursor, limit);
            
            List<SeriesResponseDto> seriesDtos = pageResult.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            PageResponseDto<SeriesResponseDto> response = new PageResponseDto<>(
                seriesDtos, 
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
    @Path("/{id}/picture")
    @Operation(summary = "Get series picture", description = "Serves the series picture using local-first strong ETag caching; populates from metadata.seriesImageUrl if missing")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Image bytes returned"),
        @APIResponse(responseCode = "304", description = "Not Modified"),
        @APIResponse(responseCode = "404", description = "Series or image not found")
    })
    public Response getSeriesPicture(@PathParam("id") String id) {
        try {
            UUID seriesId = UUID.fromString(id);
            Optional<SeriesData> seriesOpt = seriesUseCase.getSeriesById(seriesId);
            if (seriesOpt.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Series not found").build();
            }
            SeriesData series = seriesOpt.get();
            String storedUrl = null;
            if (series.getMetadata() != null) {
                Object urlObj = series.getMetadata().get("seriesImageUrl");
                if (urlObj instanceof String s && (s.startsWith("http://") || s.startsWith("https://"))) {
                    storedUrl = s.trim();
                }
            }

            java.nio.file.Path baseDir = java.nio.file.Paths.get(config.storage().baseDir());
            return imageCachingService.serveLocalFirstStrongETag(
                httpRequest,
                baseDir,
                "series",
                "covers",
                id,
                storedUrl,
                getFailoverSvg()
            );
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid series ID format").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error: " + e.getMessage()).build();
        }
    }
    private static byte[] getFailoverSvg() {
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
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
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
            Optional<SeriesData> seriesData = seriesUseCase.getSeriesById(seriesId);
            
            if (seriesData.isPresent()) {
                return Response.ok(toDto(seriesData.get())).build();
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
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Search series", description = "Search series by name")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = SeriesResponseDto.class)))
    })
    public Response searchSeries(
            @Parameter(description = "Search query", required = true)
            @QueryParam("q") String query) {
        
        try {
            List<SeriesData> series = seriesUseCase.searchSeries(query);
            List<SeriesResponseDto> seriesDtos = series.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
            
            return Response.ok(seriesDtos).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
    
    /**
     * Convert SeriesData domain model to DTO.
     */
    private SeriesResponseDto toDto(SeriesData seriesData) {
        if (seriesData == null) {
            return null;
        }
        
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
}