package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.metadata.BookMetadata;
import org.motpassants.domain.core.model.metadata.FieldChange;
import org.motpassants.domain.core.model.metadata.MetadataPreview;
import org.motpassants.domain.core.model.metadata.ProviderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case for metadata operations and external provider integration.
 * Implements DATA-002 requirements for Metadata Editing and External Providers.
 */
public interface MetadataUseCase {
    
    /**
     * Search for metadata by ISBN from external providers.
     * @param isbn the ISBN to search for
     * @return list of metadata from all providers
     */
    List<BookMetadata> searchMetadataByIsbn(String isbn);
    
    /**
     * Search for metadata by title and author from external providers.
     * @param title the book title
     * @param author optional author name
     * @return list of metadata from all providers
     */
    List<BookMetadata> searchMetadataByTitle(String title, String author);
    
    /**
     * Get the best metadata for a book by selecting highest confidence result.
     * @param isbn the ISBN to search for
     * @return the best metadata found, or empty if none found
     */
    Optional<BookMetadata> getBestMetadata(String isbn);
    
    /**
     * Apply metadata from external provider to an existing book.
     * @param bookId the book to update
     * @param metadata the metadata to apply
     * @param overwriteExisting whether to overwrite existing non-null fields
     * @return the updated book ID
     */
    UUID applyMetadataToBook(UUID bookId, BookMetadata metadata, boolean overwriteExisting);
    
    /**
     * Preview what changes would be made when applying metadata to a book.
     * @param bookId the book to preview changes for
     * @param metadata the metadata to apply
     * @param overwriteExisting whether to overwrite existing non-null fields
     * @return preview of changes
     */
    MetadataPreview previewMetadataChanges(UUID bookId, BookMetadata metadata, boolean overwriteExisting);
    
    /**
     * Merge multiple metadata results using configured merge strategy.
     * @param metadataList list of metadata from different providers
     * @return merged metadata
     */
    BookMetadata mergeMetadata(List<BookMetadata> metadataList);
    
    /**
     * Get status of all metadata providers.
     * @return list of provider statuses
     */
    List<ProviderStatus> getProviderStatuses();
    
    /**
     * Test connection to all metadata providers.
     * @return list of provider statuses with connection test results
     */
    List<ProviderStatus> testProviderConnections();
}