package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * DTO for batch operation result items.
 */
public record BatchOperationResultDto(
    @JsonProperty("bookId") UUID bookId,
    @JsonProperty("bookTitle") String bookTitle,
    @JsonProperty("success") boolean success,
    @JsonProperty("errorMessage") String errorMessage,
    @JsonProperty("changesSummary") String changesSummary
) {}