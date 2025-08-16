package org.motpassants.domain.port.in;

import java.nio.file.Path;
import java.util.List;

/**
 * Use case for automated book ingestion functionality.
 */
public interface IngestUseCase {
    
    /**
     * Scans a directory for books and imports them.
     */
    List<String> ingestFromDirectory(String directoryPath);
    
    /**
     * Ingests a single book file.
     */
    String ingestSingleBook(Path bookPath);
    
    /**
     * Gets supported file formats for ingestion.
     */
    List<String> getSupportedFormats();
    
    /**
     * Validates if a file can be ingested.
     */
    boolean canIngest(Path filePath);
}