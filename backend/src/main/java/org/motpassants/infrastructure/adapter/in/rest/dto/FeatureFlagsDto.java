package org.motpassants.infrastructure.adapter.in.rest.dto;

/**
 * Data Transfer Object for feature flags.
 */
public class FeatureFlagsDto {
    
    private boolean enableIngest;
    private boolean enableExport;
    private boolean enableSync;
    
    // Default constructor
    public FeatureFlagsDto() {}
    
    // Constructor
    public FeatureFlagsDto(boolean enableIngest, boolean enableExport, boolean enableSync) {
        this.enableIngest = enableIngest;
        this.enableExport = enableExport;
        this.enableSync = enableSync;
    }
    
    // Getters and setters
    public boolean isEnableIngest() {
        return enableIngest;
    }
    
    public void setEnableIngest(boolean enableIngest) {
        this.enableIngest = enableIngest;
    }
    
    public boolean isEnableExport() {
        return enableExport;
    }
    
    public void setEnableExport(boolean enableExport) {
        this.enableExport = enableExport;
    }
    
    public boolean isEnableSync() {
        return enableSync;
    }
    
    public void setEnableSync(boolean enableSync) {
        this.enableSync = enableSync;
    }
    
    @Override
    public String toString() {
        return "FeatureFlagsDto{" +
                "enableIngest=" + enableIngest +
                ", enableExport=" + enableExport +
                ", enableSync=" + enableSync +
                '}';
    }
}