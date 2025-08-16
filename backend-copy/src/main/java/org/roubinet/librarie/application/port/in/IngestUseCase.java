package org.roubinet.librarie.application.port.in;

import java.nio.file.Path;
import java.util.List;

/**
 * Port interface for automated book ingest functionality.
 * Based on Calibre-Web-Automated ingest service capabilities.
 */
public interface IngestUseCase {
    
    /**
     * Process a single file for ingestion into the library.
     * Supports 27+ ebook formats as per CWA specifications.
     * 
     * @param filePath path to the file to ingest
     * @return the UUID of the created book entry
     */
    String ingestFile(Path filePath);
    
    /**
     * Process all files in a directory for ingestion.
     * 
     * @param directoryPath path to directory containing files to ingest
     * @return list of UUIDs for successfully created book entries
     */
    List<String> ingestDirectory(Path directoryPath);
    
    /**
     * Scan the configured ingest directory for new files.
     * This is typically called by scheduled jobs.
     * 
     * @return list of UUIDs for newly ingested books
     */
    List<String> scanIngestDirectory();
    
    /**
     * Check if a file format is supported for ingestion.
     * 
     * @param fileExtension the file extension (e.g., "epub", "mobi")
     * @return true if the format is supported
     */
    boolean isSupportedFormat(String fileExtension);
    
    /**
     * Get list of all supported ingest formats.
     * 
     * @return list of supported file extensions
     */
    List<String> getSupportedFormats();
    
    /**
     * Manually trigger a refresh of the library from the ingest directory.
     * Equivalent to CWA's "Library Refresh" functionality.
     * 
     * @return number of files processed
     */
    int refreshLibrary();
}