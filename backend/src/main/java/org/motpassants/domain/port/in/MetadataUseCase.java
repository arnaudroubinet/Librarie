package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.MetadataSearchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Input port (use case) for metadata operations.
 * Defines the business operations related to book metadata fetching.
 * Implemented by the application service layer.
 * 
 * Note: Applying metadata to books is handled by the frontend,
 * which fetches metadata here and then updates the book via BookService.
 */
public interface MetadataUseCase {

    /**
     * Search for metadata by ISBN across all enabled providers.
     *
     * @param isbn The ISBN to search for
     * @return Future containing aggregated metadata results from all providers
     */
    CompletableFuture<List<MetadataSearchResult>> searchMetadataByIsbn(String isbn);

    /**
     * Search for metadata by title and optional author across all enabled providers.
     *
     * @param title  The book title
     * @param author Optional author name
     * @return Future containing aggregated metadata results from all providers
     */
    CompletableFuture<List<MetadataSearchResult>> searchMetadataByTitleAndAuthor(String title, String author);
}
