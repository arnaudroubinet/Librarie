package org.motpassants.domain.core.model;

import java.util.UUID;

/**
 * Format domain model representing book file formats.
 * Placeholder for now - will be detailed later.
 */
public class Format {
    private UUID id;
    private String formatType;
    private String filePath;
    private Long fileSize;
    
    public Format() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFormatType() { return formatType; }
    public void setFormatType(String formatType) { this.formatType = formatType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
}