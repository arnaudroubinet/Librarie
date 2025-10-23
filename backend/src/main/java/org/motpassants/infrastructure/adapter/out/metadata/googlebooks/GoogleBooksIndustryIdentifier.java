package org.motpassants.infrastructure.adapter.out.metadata.googlebooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Industry identifier (ISBN, etc.) from Google Books API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksIndustryIdentifier {
    
    private String type; // "ISBN_10" or "ISBN_13"
    private String identifier;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
