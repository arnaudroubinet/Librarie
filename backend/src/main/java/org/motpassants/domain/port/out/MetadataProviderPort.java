package org.motpassants.domain.port.out;

import org.motpassants.domain.core.model.MetadataSearchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Output port for metadata provider implementations.
 * Defines the contract for external metadata sources (Google Books, Open Library, etc).
 * Implementations should be in the infrastructure layer.
 */
public interface MetadataProviderPort {

    /**
     * Search for book metadata by ISBN.
     *
     * @param isbn The ISBN to search for (can be ISBN-10 or ISBN-13)
     * @return Future containing list of metadata search results
     */
    CompletableFuture<List<MetadataSearchResult>> searchByIsbn(String isbn);

    /**
     * Search for book metadata by title and optional author.
     *
     * @param title  The book title to search for
     * @param author Optional author name to refine search
     * @return Future containing list of metadata search results
     */
    CompletableFuture<List<MetadataSearchResult>> searchByTitleAndAuthor(String title, String author);

    /**
     * Get the provider name for identification.
     *
     * @return The provider name (e.g., "Google Books", "Open Library")
     */
    String getProviderName();

    /**
     * Check if this provider is currently enabled.
     *
     * @return true if the provider is enabled and can be used
     */
    boolean isEnabled();

    /**
     * Get the priority of this provider (lower number = higher priority).
     * Used for ordering providers when multiple are available.
     *
     * @return Priority value (0-100, where 0 is highest priority)
     */
    int getPriority();
}
