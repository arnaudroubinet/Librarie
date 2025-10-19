package org.motpassants.infrastructure.adapter.out.metadata.googlebooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Root response from Google Books API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBooksResponse {
    
    private String kind;
    private Integer totalItems;
    private List<GoogleBooksVolume> items;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public List<GoogleBooksVolume> getItems() {
        return items;
    }

    public void setItems(List<GoogleBooksVolume> items) {
        this.items = items;
    }
}
