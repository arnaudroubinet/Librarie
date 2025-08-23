package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * DTO for batch edit preview responses.
 */
public record BatchEditPreviewDto(
    @JsonProperty("bookId") UUID bookId,
    @JsonProperty("currentTitle") String currentTitle,
    @JsonProperty("newTitle") String newTitle,
    @JsonProperty("currentAuthors") List<String> currentAuthors,
    @JsonProperty("newAuthors") List<String> newAuthors,
    @JsonProperty("currentTags") List<String> currentTags,
    @JsonProperty("newTags") List<String> newTags,
    @JsonProperty("currentLanguage") String currentLanguage,
    @JsonProperty("newLanguage") String newLanguage,
    @JsonProperty("currentPublisher") String currentPublisher,
    @JsonProperty("newPublisher") String newPublisher,
    @JsonProperty("currentIsbn") String currentIsbn,
    @JsonProperty("newIsbn") String newIsbn,
    @JsonProperty("currentDescription") String currentDescription,
    @JsonProperty("newDescription") String newDescription,
    @JsonProperty("hasChanges") boolean hasChanges
) {}