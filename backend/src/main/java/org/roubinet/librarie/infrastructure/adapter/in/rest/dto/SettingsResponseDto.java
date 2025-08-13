package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * DTO for settings response containing system information.
 */
public class SettingsResponseDto {
    
    private String version;
    private List<String> supportedFormats;
    private EntityCountsDto entityCounts;
    
    public SettingsResponseDto() {}
    
    public SettingsResponseDto(String version, List<String> supportedFormats, EntityCountsDto entityCounts) {
        this.version = version;
        this.supportedFormats = supportedFormats;
        this.entityCounts = entityCounts;
    }
    
    // Getters and Setters
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public List<String> getSupportedFormats() {
        return supportedFormats;
    }
    
    public void setSupportedFormats(List<String> supportedFormats) {
        this.supportedFormats = supportedFormats;
    }
    
    public EntityCountsDto getEntityCounts() {
        return entityCounts;
    }
    
    public void setEntityCounts(EntityCountsDto entityCounts) {
        this.entityCounts = entityCounts;
    }
}