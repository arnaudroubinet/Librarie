package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * DTO for batch delete requests.
 */
public record BatchDeleteRequestDto(
    @JsonProperty("bookIds") List<UUID> bookIds
) {}