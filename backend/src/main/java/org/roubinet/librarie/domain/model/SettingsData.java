package org.roubinet.librarie.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Domain model representing system settings and statistics.
 */
public class SettingsData {
    
    private String version;
    private List<String> supportedFormats;
    private Map<String, Long> entityCounts;
    
    public SettingsData() {}
    
    public SettingsData(String version, List<String> supportedFormats, Map<String, Long> entityCounts) {
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
    
    public Map<String, Long> getEntityCounts() {
        return entityCounts;
    }
    
    public void setEntityCounts(Map<String, Long> entityCounts) {
        this.entityCounts = entityCounts;
    }
}