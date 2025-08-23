package org.motpassants.domain.core.model.metadata;

import java.util.List;
import java.util.UUID;

/**
 * Preview of metadata changes to be applied to a book.
 */
public record MetadataPreview(
    UUID bookId,
    String currentTitle,
    String newTitle,
    String currentDescription,
    String newDescription,
    String currentIsbn,
    String newIsbn,
    String currentPublisher,
    String newPublisher,
    List<String> currentAuthors,
    List<String> newAuthors,
    List<String> currentTags,
    List<String> newTags,
    String currentCoverUrl,
    String newCoverUrl,
    int totalChanges,
    List<FieldChange> changes
) {}