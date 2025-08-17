package org.motpassants.infrastructure.adapter.in.rest.dto;

import java.util.List;

/**
 * Data Transfer Object for storage configuration.
 */
public class StorageConfigurationDto {
    
    private String baseDirectory;
    private List<String> allowedFileTypes;
    
    // Default constructor
    public StorageConfigurationDto() {}
    
    // Constructor
    public StorageConfigurationDto(String baseDirectory, List<String> allowedFileTypes) {
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
        return "StorageConfigurationDto{" +
                "baseDirectory='" + baseDirectory + '\'' +
                ", allowedFileTypes=" + allowedFileTypes +
                '}';
    }
}