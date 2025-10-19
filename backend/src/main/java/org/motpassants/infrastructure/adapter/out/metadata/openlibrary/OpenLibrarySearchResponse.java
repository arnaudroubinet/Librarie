package org.motpassants.infrastructure.adapter.out.metadata.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response from Open Library Search API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibrarySearchResponse {
    
    private Integer numFound;
    private Integer start;
    @JsonProperty("numFoundExact")
    private Boolean numFoundExact;
    private List<OpenLibraryDoc> docs;

    public Integer getNumFound() {
        return numFound;
    }

    public void setNumFound(Integer numFound) {
        this.numFound = numFound;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Boolean getNumFoundExact() {
        return numFoundExact;
    }

    public void setNumFoundExact(Boolean numFoundExact) {
        this.numFoundExact = numFoundExact;
    }

    public List<OpenLibraryDoc> getDocs() {
        return docs;
    }

    public void setDocs(List<OpenLibraryDoc> docs) {
        this.docs = docs;
    }
}
