package org.motpassants.domain.core.model;

import java.util.List;

/**
 * Storage configuration domain model representing system storage settings.
 */
public class StorageConfiguration {
    
    private String baseDirectory;
    private List<String> allowedFileTypes;
    
    public StorageConfiguration() {}
    
    public StorageConfiguration(String baseDirectory, List<String> allowedFileTypes) {
        this.baseDirectory = baseDirectory;
        this.allowedFileTypes = allowedFileTypes;
    }
    
    // Getters and setters
    public String getBaseDirectory() {
        return baseDirectory;
    }
    
    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }
    
    public List<String> getAllowedFileTypes() {
        return allowedFileTypes;
    }
    
    public void setAllowedFileTypes(List<String> allowedFileTypes) {
        this.allowedFileTypes = allowedFileTypes;
    }
    
    @Override
    public String toString() {
        return "StorageConfiguration{" +
                "baseDirectory='" + baseDirectory + '\'' +
                ", allowedFileTypes=" + allowedFileTypes +
                '}';
    }
}