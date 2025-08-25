package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * DTO for batch operation requests.
 */
public record BatchOperationRequestDto(
    @JsonProperty("bookIds") List<UUID> bookIds,
    @JsonProperty("editRequest") BatchEditRequestDto editRequest
) {}