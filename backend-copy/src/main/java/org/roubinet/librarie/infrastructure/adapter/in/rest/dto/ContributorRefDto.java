package org.roubinet.librarie.infrastructure.adapter.in.rest.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Contributor reference (id and display name)")
public class ContributorRefDto {

    @Schema(description = "Contributor UUID", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
    private UUID id;

    @Schema(description = "Contributor display name", example = "J.R.R. Tolkien")
    private String name;

    public ContributorRefDto() {}

    public ContributorRefDto(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
