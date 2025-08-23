package org.motpassants.infrastructure.adapter.in.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.jboss.resteasy.reactive.RestForm;
import org.motpassants.domain.port.in.UploadUseCase;
import org.motpassants.domain.core.model.UploadModels;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

/**
 * REST controller for file upload operations.
 * Implements DATA-001: Upload & Automated Ingest Pipeline.
 */
@Path("/api/upload")
@Tag(name = "Upload", description = "File upload operations")
public class UploadController {
    
    private static final Logger LOG = Logger.getLogger(UploadController.class);
    
    @Inject
    UploadUseCase uploadUseCase;
    
    /**
     * Upload a single book file.
     */
    @POST
    @Path("/book")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Upload a book file",
        description = "Upload a single book file for processing through the ingestion pipeline"
    )
    @APIResponse(
        responseCode = "200",
        description = "Upload processed successfully",
        content = @Content(schema = @Schema(implementation = UploadModels.UploadResult.class))
    )
    @APIResponse(
        responseCode = "400",
        description = "Bad request - validation failed"
    )
    @APIResponse(
        responseCode = "413",
        description = "File too large"
    )
    @APIResponse(
        responseCode = "415",
        description = "Unsupported file type"
    )
    @APIResponse(
        responseCode = "500",
        description = "Internal server error"
    )
    public Response uploadBook(@RestForm("file") FileUpload file) {
        LOG.info("Received file upload request: " + (file != null ? file.fileName() : "null"));
        
        try {
            // Validate form data
            if (file == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("No file provided"))
                    .build();
            }
            
            String filename = file.fileName();
            if (filename == null || filename.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Filename is required"))
                    .build();
            }
            
            // Process the upload using the file input stream
            try (InputStream inputStream = Files.newInputStream(file.uploadedFile())) {
                UploadModels.UploadResult result = uploadUseCase.processUploadedFile(
                    inputStream, filename, file.contentType());
                
                // Determine response status based on result
                if (result.success()) {
                    return Response.ok(result).build();
                } else {
                    // Map status to appropriate HTTP status codes
                    return switch (result.status()) {
                        case "VALIDATION_FAILED" -> Response.status(Response.Status.BAD_REQUEST).entity(result).build();
                        case "DUPLICATE" -> Response.status(Response.Status.CONFLICT).entity(result).build();
                        case "PROCESSING_FAILED" -> Response.status(422).entity(result).build(); // 422 Unprocessable Entity
                        default -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
                    };
                }
            }
            
        } catch (Exception e) {
            LOG.error("Error processing file upload", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Validate a file without processing it.
     */
    @POST
    @Path("/validate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Validate a file for upload",
        description = "Validate a file without processing it through the ingestion pipeline"
    )
    @APIResponse(
        responseCode = "200",
        description = "Validation completed",
        content = @Content(schema = @Schema(implementation = UploadModels.ValidationResult.class))
    )
    public Response validateFile(@RestForm("file") FileUpload file) {
        LOG.info("Received file validation request: " + (file != null ? file.fileName() : "null"));
        
        try {
            if (file == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("File is required"))
                    .build();
            }
            
            String filename = file.fileName();
            if (filename == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Filename is required"))
                    .build();
            }
            
            try (InputStream inputStream = Files.newInputStream(file.uploadedFile())) {
                UploadModels.ValidationResult result = uploadUseCase.validateUploadedFile(
                    inputStream, filename, file.contentType());
                
                return Response.ok(result).build();
            }
            
        } catch (Exception e) {
            LOG.error("Error validating file", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Get upload configuration information.
     */
    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get upload configuration",
        description = "Get maximum file size and allowed file extensions"
    )
    @APIResponse(
        responseCode = "200",
        description = "Configuration retrieved successfully",
        content = @Content(schema = @Schema(implementation = UploadConfig.class))
    )
    public Response getUploadConfig() {
        try {
            long maxSize = uploadUseCase.getMaxUploadSize();
            List<String> allowedExtensions = uploadUseCase.getAllowedExtensions();
            
            UploadConfig config = new UploadConfig(maxSize, allowedExtensions);
            return Response.ok(config).build();
            
        } catch (Exception e) {
            LOG.error("Error getting upload configuration", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("Internal server error: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * Upload configuration response.
     */
    public record UploadConfig(
        long maxFileSize,
        List<String> allowedExtensions
    ) {}
    
    /**
     * Error response.
     */
    public record ErrorResponse(String message) {}
}