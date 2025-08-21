package org.motpassants.infrastructure.adapter.in.rest;

import org.motpassants.domain.core.model.Settings;
import org.motpassants.domain.core.model.EntityCounts;
import org.motpassants.domain.port.in.SettingsUseCase;
import org.motpassants.infrastructure.adapter.in.rest.dto.SettingsResponseDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.EntityCountsDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.FeatureFlagsDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.StorageConfigurationDto;

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
 * Provides the adapter for incoming HTTP requests in the hexagonal architecture.
 */
@Path("/v1/settings")
@Tag(name = "Settings", description = "System settings and statistics")
@Produces(MediaType.APPLICATION_JSON)
public class SettingsController {
    
    private final SettingsUseCase settingsUseCase;
    
    @Inject
    public SettingsController(SettingsUseCase settingsUseCase) {
        this.settingsUseCase = settingsUseCase;
    }
    
    @GET
    @Operation(summary = "Get system settings", description = "Retrieve system settings including version, supported formats, and entity counts")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Settings retrieved successfully",
            content = @Content(schema = @Schema(implementation = SettingsResponseDto.class)))
    })
    public Response getSettings() {
        try {
            Settings settings = settingsUseCase.getSystemSettings();
            
            // Convert domain models to DTOs
            EntityCounts entityCounts = settings.getEntityCounts();
            EntityCountsDto entityCountsDto = new EntityCountsDto(
                entityCounts.books(),
                entityCounts.series(),
                entityCounts.authors(),
                entityCounts.publishers(),
                entityCounts.languages(),
                entityCounts.formats(),
                entityCounts.tags()
            );
            
            FeatureFlagsDto featureFlagsDto = new FeatureFlagsDto(
                settings.getFeatureFlags().enableIngest(),
                settings.getFeatureFlags().enableExport(),
                settings.getFeatureFlags().enableSync()
            );
            
            StorageConfigurationDto storageConfigurationDto = new StorageConfigurationDto(
                settings.getStorageConfiguration().baseDirectory(),
                settings.getStorageConfiguration().allowedFileTypes()
            );
            
            SettingsResponseDto response = new SettingsResponseDto(
                settings.getVersion(),
                settings.getApplicationName(),
                settings.getSupportedFormats(),
                entityCountsDto,
                featureFlagsDto,
                settings.getDefaultPageSize(),
                settings.getMaxPageSize(),
                storageConfigurationDto
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }
}