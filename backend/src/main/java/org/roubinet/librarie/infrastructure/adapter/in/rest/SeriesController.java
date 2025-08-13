package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.SeriesUseCase;
import org.roubinet.librarie.domain.entity.Series;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for series operations.
 * Feature-based API with version 1 path structure.
 */
@Path("/v1/books/series")
@Tag(name = "Series", description = "Book series management operations")
public class SeriesController {
    
    private final SeriesUseCase seriesUseCase;
    private final LibrarieConfigProperties config;
    
    @Inject
    public SeriesController(SeriesUseCase seriesUseCase,
                           LibrarieConfigProperties config) {
        this.seriesUseCase = seriesUseCase;
        this.config = config;
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
            CursorPageResult<Series> pageResult = seriesUseCase.getAllSeries(cursor, limit);
            
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
            Optional<Series> series = seriesUseCase.getSeriesById(seriesId);
            
            if (series.isPresent()) {
                return Response.ok(toDto(series.get())).build();
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
    
    /**
     * Convert Series entity to DTO with book count and fallback image.
     */
    private SeriesResponseDto toDto(Series series) {
        if (series == null) {
            return null;
        }
        
        SeriesResponseDto dto = new SeriesResponseDto();
        dto.setId(series.getId());
        dto.setName(series.getName());
        dto.setSortName(series.getSortName());
        dto.setDescription(series.getDescription());
        dto.setImagePath(series.getImagePath());
        dto.setMetadata(series.getMetadata());
        dto.setCreatedAt(series.getCreatedAt());
        dto.setUpdatedAt(series.getUpdatedAt());
        
        // Calculate book count and get fallback image
        int bookCount = getBookCountForSeries(series.getId());
        dto.setBookCount(bookCount);
        
        // If series doesn't have its own image, get the image from the book with lowest index
        if (series.getImagePath() == null || series.getImagePath().trim().isEmpty()) {
            String fallbackImage = getFallbackImageForSeries(series.getId());
            dto.setFallbackImagePath(fallbackImage);
        }
        
        return dto;
    }
    
    /**
     * Get the number of books in a series.
     */
    private int getBookCountForSeries(UUID seriesId) {
        return seriesUseCase.getBookCountForSeries(seriesId);
    }
    
    /**
     * Get fallback image for a series from its books.
     */
    private String getFallbackImageForSeries(UUID seriesId) {
        return seriesUseCase.getFallbackImageForSeries(seriesId).orElse(null);
    }
}