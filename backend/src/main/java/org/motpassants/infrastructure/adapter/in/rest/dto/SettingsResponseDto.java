package org.motpassants.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * Data Transfer Object for Settings responses.
 */
public class SettingsResponseDto {
    
    private String version;
    private String applicationName;
    private List<String> supportedFormats;
    private EntityCountsDto entityCounts;
    private FeatureFlagsDto featureFlags;
    private int defaultPageSize;
    private int maxPageSize;
    private StorageConfigurationDto storageConfiguration;
    
    // Default constructor
    public SettingsResponseDto() {}
    
    // Constructor
    public SettingsResponseDto(String version, String applicationName, 
                              List<String> supportedFormats, EntityCountsDto entityCounts,
                              FeatureFlagsDto featureFlags, int defaultPageSize, int maxPageSize,
                              StorageConfigurationDto storageConfiguration) {
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
    
    public EntityCountsDto getEntityCounts() {
        return entityCounts;
    }
    
    public void setEntityCounts(EntityCountsDto entityCounts) {
        this.entityCounts = entityCounts;
    }
    
    public FeatureFlagsDto getFeatureFlags() {
        return featureFlags;
    }
    
    public void setFeatureFlags(FeatureFlagsDto featureFlags) {
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
    
    public StorageConfigurationDto getStorageConfiguration() {
        return storageConfiguration;
    }
    
    public void setStorageConfiguration(StorageConfigurationDto storageConfiguration) {
        this.storageConfiguration = storageConfiguration;
    }
    
    @Override
    public String toString() {
        return "SettingsResponseDto{" +
                "version='" + version + '\'' +
                ", applicationName='" + applicationName + '\'' +
                '}';
    }
}