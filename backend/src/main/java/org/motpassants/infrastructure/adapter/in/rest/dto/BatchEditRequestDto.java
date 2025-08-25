package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

/**
 * DTO for batch edit requests.
 */
public record BatchEditRequestDto(
    @JsonProperty("title") String title,
    @JsonProperty("subtitle") String subtitle,
    @JsonProperty("description") String description,
    @JsonProperty("authors") List<String> authors,
    @JsonProperty("seriesId") UUID seriesId,
    @JsonProperty("seriesPosition") Integer seriesPosition,
    @JsonProperty("tags") List<String> tags,
    @JsonProperty("language") String language,
    @JsonProperty("publisher") String publisher,
    @JsonProperty("isbn") String isbn,
    @JsonProperty("addTags") Boolean addTags,
    @JsonProperty("addAuthors") Boolean addAuthors
) {}