package org.motpassants.domain.port.out.metadata;

import org.motpassants.domain.core.model.metadata.BookMetadata;
import org.motpassants.domain.core.model.metadata.ProviderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Port for managing multiple metadata providers and aggregating results.
 * Outbound port for metadata aggregation and provider management.
 */
public interface MetadataAggregatorPort {
    
    /**
     * Search for metadata across all enabled providers by ISBN.
     * Results are ordered by provider priority and confidence.
     * @param isbn the ISBN to search for
     * @return list of metadata from all providers that found results
     */
    List<BookMetadata> findByIsbnFromAllProviders(String isbn);
    
    /**
     * Get the best metadata result for an ISBN by selecting the highest confidence result.
     * @param isbn the ISBN to search for
     * @return the best metadata found, or empty if none found
     */
    Optional<BookMetadata> getBestMetadataByIsbn(String isbn);
    
    /**
     * Search for metadata by title and author across all providers.
     * @param title the book title
     * @param author optional author name
     * @return list of metadata from all providers, ordered by relevance
     */
    List<BookMetadata> searchByTitleFromAllProviders(String title, String author);
    
    /**
     * Merge metadata from multiple sources using configured merge strategy.
     * @param metadataList list of metadata from different providers
     * @return merged metadata with best fields from each source
     */
    BookMetadata mergeMetadata(List<BookMetadata> metadataList);
    
    /**
     * Get list of all registered metadata providers.
     * @return list of all providers (enabled and disabled)
     */
    List<MetadataProviderPort> getAllProviders();
    
    /**
     * Get list of enabled metadata providers, ordered by priority.
     * @return list of enabled providers
     */
    List<MetadataProviderPort> getEnabledProviders();
    
    /**
     * Register a new metadata provider.
     * @param provider the provider to register
     */
    void registerProvider(MetadataProviderPort provider);
    
    /**
     * Test all provider connections.
     * @return list of provider statuses
     */
    List<ProviderStatus> testAllProviders();
}