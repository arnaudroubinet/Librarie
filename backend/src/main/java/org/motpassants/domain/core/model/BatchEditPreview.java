package org.motpassants.domain.core.model;

import java.util.List;
import java.util.UUID;

/**
 * Represents a preview of changes that would be made to a book in a batch edit operation.
 */
public record BatchEditPreview(
    UUID bookId,
    String currentTitle,
    String newTitle,
    List<String> currentAuthors,
    List<String> newAuthors,
    List<String> currentTags,
    List<String> newTags,
    String currentLanguage,
    String newLanguage,
    String currentPublisher,
    String newPublisher,
    String currentIsbn,
    String newIsbn,
    String currentDescription,
    String newDescription,
    boolean hasChanges
) {
    
    /**
     * Creates a preview showing no changes.
     */
    public static BatchEditPreview noChanges(UUID bookId, String title) {
        return new BatchEditPreview(
            bookId, title, title, List.of(), List.of(), List.of(), List.of(),
            null, null, null, null, null, null, null, null, false
        );
    }
}