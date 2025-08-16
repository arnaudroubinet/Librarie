package org.roubinet.librarie.application.port.out;

import org.roubinet.librarie.domain.entity.Format;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary port (driven port) for format persistence operations.
 * This interface will be implemented by JPA repository adapters.
 */
public interface FormatRepository {
    
    /**
     * Find all formats.
     * 
     * @return list of all formats
     */
    List<Format> findAll();
    
    /**
     * Find a format by its ID.
     * 
     * @param id the format's UUID
     * @return optional containing the format if found
     */
    Optional<Format> findById(UUID id);
    
    /**
     * Find formats by type.
     * 
     * @param type the format type
     * @return list of formats with the specified type
     */
    List<Format> findByType(String type);
    
    /**
     * Find distinct format types.
     * 
     * @return list of distinct format types
     */
    List<String> findDistinctTypes();
    
    /**
     * Save a format (create or update).
     * 
     * @param format the format to save
     * @return the saved format
     */
    Format save(Format format);
    
    /**
     * Delete a format by ID.
     * 
     * @param id the format's UUID
     */
    void deleteById(UUID id);
    
    /**
     * Check if a format exists by ID.
     * 
     * @param id the format's UUID
     * @return true if the format exists
     */
    boolean existsById(UUID id);
    
    /**
     * Count total number of formats.
     * 
     * @return total count
     */
    long count();
}