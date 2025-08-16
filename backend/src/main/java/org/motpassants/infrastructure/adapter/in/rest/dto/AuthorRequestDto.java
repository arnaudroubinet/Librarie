package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Map;

/**
 * Data Transfer Object for Author creation and update requests.
 * Validates input data and provides clean API contract.
 */
@Schema(description = "Author request data")
public class AuthorRequestDto {

    @Schema(description = "Author name", required = true, example = "J.K. Rowling")
    @JsonProperty("name")
    private String name;

    @Schema(description = "Sort name for alphabetical listing", required = true, example = "Rowling, J.K.")
    @JsonProperty("sortName")
    private String sortName;

    @Schema(description = "Author biography in multiple languages")
    @JsonProperty("bio")
    private Map<String, String> bio;

    @Schema(description = "Birth date", example = "1965-07-31")
    @JsonProperty("birthDate")
    private LocalDate birthDate;

    @Schema(description = "Death date", example = "2023-01-01")
    @JsonProperty("deathDate")
    private LocalDate deathDate;

    @Schema(description = "Official website URL", example = "https://example.com")
    @JsonProperty("websiteUrl")
    private String websiteUrl;

    @Schema(description = "Additional metadata")
    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    // Default constructor
    public AuthorRequestDto() {}

    // Constructor for testing
    public AuthorRequestDto(String name, String sortName, Map<String, String> bio,
                           LocalDate birthDate, LocalDate deathDate, String websiteUrl,
                           Map<String, Object> metadata) {
        this.name = name;
        this.sortName = sortName;
        this.bio = bio;
        this.birthDate = birthDate;
        this.deathDate = deathDate;
        this.websiteUrl = websiteUrl;
        this.metadata = metadata;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public Map<String, String> getBio() {
        return bio;
    }

    public void setBio(Map<String, String> bio) {
        this.bio = bio;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public LocalDate getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(LocalDate deathDate) {
        this.deathDate = deathDate;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "AuthorRequestDto{" +
                "name='" + name + '\'' +
                ", sortName='" + sortName + '\'' +
                '}';
    }
}