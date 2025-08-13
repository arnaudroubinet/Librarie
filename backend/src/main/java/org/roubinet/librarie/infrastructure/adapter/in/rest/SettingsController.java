package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.roubinet.librarie.application.port.in.SettingsUseCase;
import org.roubinet.librarie.domain.model.SettingsData;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.SettingsResponseDto;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST controller for settings operations.
 */
@Path("/v1/settings")
@Tag(name = "Settings", description = "System settings and statistics")
public class SettingsController {
    
    private final SettingsUseCase settingsUseCase;
    
    @Inject
    public SettingsController(SettingsUseCase settingsUseCase) {
        this.settingsUseCase = settingsUseCase;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get system settings", description = "Retrieve system settings including version, supported formats, and entity counts")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Settings retrieved successfully",
            content = @Content(schema = @Schema(implementation = SettingsResponseDto.class)))
    })
    public Response getSettings() {
        try {
            SettingsData settingsData = settingsUseCase.getSystemSettings();
            
            SettingsResponseDto response = new SettingsResponseDto(
                settingsData.getVersion(),
                settingsData.getSupportedFormats(),
                settingsData.getEntityCounts()
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
}