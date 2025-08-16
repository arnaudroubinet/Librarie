package org.roubinet.librarie.application.port.out;

import org.roubinet.librarie.domain.entity.Publisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Secondary port (driven port) for publisher persistence operations.
 * This interface will be implemented by JPA repository adapters.
 */
public interface PublisherRepository {
    
    /**
     * Find all publishers.
     * 
     * @return list of all publishers
     */
    List<Publisher> findAll();
    
    /**
     * Find a publisher by its ID.
     * 
     * @param id the publisher's UUID
     * @return optional containing the publisher if found
     */
    Optional<Publisher> findById(UUID id);
    
    /**
     * Find publishers by name containing the search term (case-insensitive).
     * 
     * @param name the name search term
     * @return list of matching publishers
     */
    List<Publisher> findByNameContainingIgnoreCase(String name);
    
    /**
     * Save a publisher (create or update).
     * 
     * @param publisher the publisher to save
     * @return the saved publisher
     */
    Publisher save(Publisher publisher);
    
    /**
     * Delete a publisher by ID.
     * 
     * @param id the publisher's UUID
     */
    void deleteById(UUID id);
    
    /**
     * Check if a publisher exists by ID.
     * 
     * @param id the publisher's UUID
     * @return true if the publisher exists
     */
    boolean existsById(UUID id);
    
    /**
     * Count total number of publishers.
     * 
     * @return total count
     */
    long count();
}