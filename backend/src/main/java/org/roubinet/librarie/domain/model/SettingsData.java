package org.roubinet.librarie.domain.model;

import java.util.List;

/**
 * Domain model representing system settings and statistics.
 */
public class SettingsData {
    
    private String version;
    private List<String> supportedFormats;
    private EntityCounts entityCounts;
    
    public SettingsData() {}
    
    public SettingsData(String version, List<String> supportedFormats, EntityCounts entityCounts) {
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
    
    public EntityCounts getEntityCounts() {
        return entityCounts;
    }
    
    public void setEntityCounts(EntityCounts entityCounts) {
        this.entityCounts = entityCounts;
    }
}