package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.MetadataSearchResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Input port (use case) for metadata operations.
 * Defines the business operations related to book metadata fetching.
 * Implemented by the application service layer.
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

    /**
     * Apply selected metadata to a book.
     * Updates the book with information from the metadata result.
     *
     * @param bookId           The UUID of the book to update
     * @param metadataResult   The metadata to apply
     * @param downloadCover    Whether to download the cover image
     * @return The updated book
     */
    Book applyMetadataToBook(UUID bookId, MetadataSearchResult metadataResult, boolean downloadCover);

    /**
     * Fetch metadata for a book automatically.
     * Attempts to find the best matching metadata and returns it for review.
     *
     * @param bookId The UUID of the book
     * @return Future containing the best metadata match (if found)
     */
    CompletableFuture<MetadataSearchResult> fetchMetadataForBook(UUID bookId);

    /**
     * Batch fetch metadata for multiple books.
     * Used for background processing of library imports.
     *
     * @param bookIds List of book UUIDs to fetch metadata for
     * @return Future that completes when batch processing is done
     */
    CompletableFuture<Void> batchFetchMetadata(List<UUID> bookIds);
}
