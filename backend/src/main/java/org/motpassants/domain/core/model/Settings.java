package org.motpassants.domain.core.model;

import java.util.List;

/**
 * Settings domain model representing system configuration and statistics.
 */
public class Settings {
    
    private String version;
    private String applicationName;
    private List<String> supportedFormats;
    private EntityCounts entityCounts;
    private FeatureFlags featureFlags;
    private int defaultPageSize;
    private int maxPageSize;
    private StorageConfiguration storageConfiguration;
    
    public Settings() {}
    
    public Settings(String version, String applicationName, List<String> supportedFormats, 
                   EntityCounts entityCounts, FeatureFlags featureFlags, 
                   int defaultPageSize, int maxPageSize, StorageConfiguration storageConfiguration) {
        this.version = version;
        this.applicationName = applicationName;
        this.supportedFormats = supportedFormats;
        this.entityCounts = entityCounts;
        this.featureFlags = featureFlags;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
        this.storageConfiguration = storageConfiguration;
    }
    
    // Getters and setters
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getApplicationName() {
        return applicationName;
    }
    
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
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
    
    public FeatureFlags getFeatureFlags() {
        return featureFlags;
    }
    
    public void setFeatureFlags(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }
    
    public int getDefaultPageSize() {
        return defaultPageSize;
    }
    
    public void setDefaultPageSize(int defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }
    
    public int getMaxPageSize() {
        return maxPageSize;
    }
    
    public void setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
    }
    
    public StorageConfiguration getStorageConfiguration() {
        return storageConfiguration;
    }
    
    public void setStorageConfiguration(StorageConfiguration storageConfiguration) {
        this.storageConfiguration = storageConfiguration;
    }
    
    @Override
    public String toString() {
        return "Settings{" +
                "version='" + version + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }
}