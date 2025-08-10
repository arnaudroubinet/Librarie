package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.IngestUseCase;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * REST controller for library management operations.
 * Handles ingest, conversion, and library maintenance functions.
 */
@Path("/api/library")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Library Management", description = "Library automation and management operations")
public class LibraryController {
    
    private final IngestUseCase ingestUseCase;
    
    @Inject
    public LibraryController(IngestUseCase ingestUseCase) {
        this.ingestUseCase = ingestUseCase;
    }
    
    @POST
    @Path("/refresh")
    @Operation(summary = "Refresh library", 
               description = "Manually trigger a refresh of the library from the ingest directory")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Library refresh completed",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public Response refreshLibrary() {
        try {
            int processedFiles = ingestUseCase.refreshLibrary();
            
            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Library refresh completed",
                "processedFiles", processedFiles
            );
            
            return Response.ok(response).build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Failed to refresh library: " + e.getMessage()
            );
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
        }
    }
    
    @GET
    @Path("/supported-formats")
    @Operation(summary = "Get supported formats", 
               description = "Get list of all supported ebook formats for ingestion")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Supported formats retrieved",
            content = @Content(schema = @Schema(implementation = List.class)))
    })
    public Response getSupportedFormats() {
        List<String> formats = ingestUseCase.getSupportedFormats();
        
        Map<String, Object> response = Map.of(
            "supportedFormats", formats,
            "count", formats.size()
        );
        
        return Response.ok(response).build();
    }
    
    @POST
    @Path("/scan")
    @Operation(summary = "Scan ingest directory", 
               description = "Scan the configured ingest directory for new files and process them")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Scan completed",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public Response scanIngestDirectory() {
        try {
            List<String> ingestedBooks = ingestUseCase.scanIngestDirectory();
            
            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Ingest directory scan completed",
                "ingestedBooks", ingestedBooks,
                "count", ingestedBooks.size()
            );
            
            return Response.ok(response).build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "status", "error",
                "message", "Failed to scan ingest directory: " + e.getMessage()
            );
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(errorResponse)
                .build();
        }
    }
    
    @GET
    @Path("/stats")
    @Operation(summary = "Get library statistics", 
               description = "Get basic statistics about the library")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Statistics retrieved",
            content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public Response getLibraryStats() {
        // Basic stats - in a full implementation this would include more detailed metrics
        Map<String, Object> stats = Map.of(
            "supportedFormats", ingestUseCase.getSupportedFormats().size(),
            "version", "1.0.0-SNAPSHOT",
            "features", List.of(
                "Automatic Ingest (27+ formats)",
                "Hexagonal Architecture",
                "REST API with OpenAPI",
                "Book Management",
                "Search Functionality"
            )
        );
        
        return Response.ok(stats).build();
    }
}