package org.motpassants.domain.core.model;

/**
 * Feature flags domain model representing system feature toggles.
 */
public class FeatureFlags {
    
    private boolean enableIngest;
    private boolean enableExport;
    private boolean enableSync;
    
    public FeatureFlags() {}
    
    public FeatureFlags(boolean enableIngest, boolean enableExport, boolean enableSync) {
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
        return "FeatureFlags{" +
                "enableIngest=" + enableIngest +
                ", enableExport=" + enableExport +
                ", enableSync=" + enableSync +
                '}';
    }
}