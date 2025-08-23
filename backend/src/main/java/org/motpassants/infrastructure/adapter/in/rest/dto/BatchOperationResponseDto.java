package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for batch operation responses.
 */
public record BatchOperationResponseDto(
    @JsonProperty("operationId") UUID operationId,
    @JsonProperty("type") String type,
    @JsonProperty("bookIds") List<UUID> bookIds,
    @JsonProperty("editRequest") BatchEditRequestDto editRequest,
    @JsonProperty("userId") UUID userId,
    @JsonProperty("createdAt") OffsetDateTime createdAt,
    @JsonProperty("status") String status,
    @JsonProperty("results") List<BatchOperationResultDto> results,
    @JsonProperty("errorMessage") String errorMessage,
    @JsonProperty("successCount") int successCount,
    @JsonProperty("failureCount") int failureCount,
    @JsonProperty("totalCount") int totalCount
) {}