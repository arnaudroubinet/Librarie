package org.motpassants.domain.port.out.metadata;

import org.motpassants.domain.core.model.metadata.BookMetadata;

import java.util.List;
import java.util.Optional;

/**
 * Port for external metadata providers.
 * Outbound port defining the contract for metadata retrieval from external sources.
 */
public interface MetadataProviderPort {
    
    /**
     * Gets the unique identifier for this provider.
     * @return provider identifier (e.g., "google-books", "openlibrary")
     */
    String getProviderId();
    
    /**
     * Gets the human-readable name for this provider.
     * @return provider name (e.g., "Google Books", "Open Library")
     */
    String getProviderName();
    
    /**
     * Indicates if this provider is currently enabled and configured.
     * @return true if provider is available for use
     */
    boolean isEnabled();
    
    /**
     * Search for book metadata by ISBN.
     * @param isbn the ISBN to search for (can be ISBN-10 or ISBN-13)
     * @return metadata if found, empty if not found
     */
    Optional<BookMetadata> findByIsbn(String isbn);
    
    /**
     * Search for book metadata by title and optional author.
     * @param title the book title
     * @param author optional author name
     * @return list of matching metadata, empty if none found
     */
    List<BookMetadata> searchByTitle(String title, String author);
    
    /**
     * Get metadata by provider-specific ID.
     * @param providerSpecificId the ID from this provider's system
     * @return metadata if found, empty if not found
     */
    Optional<BookMetadata> findByProviderId(String providerSpecificId);
    
    /**
     * Test the connection and configuration of this provider.
     * @return true if provider is accessible and properly configured
     */
    boolean testConnection();
    
    /**
     * Get the priority/preference order for this provider.
     * Lower numbers indicate higher priority.
     * @return priority order (0 = highest priority)
     */
    int getPriority();
}