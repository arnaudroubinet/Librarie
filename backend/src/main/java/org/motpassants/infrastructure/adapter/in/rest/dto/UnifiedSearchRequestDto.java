package org.motpassants.infrastructure.adapter.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Data Transfer Object for unified search requests.
 */
public class UnifiedSearchRequestDto {
    
    @JsonProperty("q")
    private String q; // Search query (for GET parameter compatibility)
    
    @JsonProperty("query")
    private String query; // Search query (for POST body compatibility)
    
    private Integer limit; // Limit per entity type
    private List<String> types; // Entity types to search
    
    // Default constructor
    public UnifiedSearchRequestDto() {}
    
    // Constructor
    public UnifiedSearchRequestDto(String q, Integer limit, List<String> types) {
        this.q = q;
        this.query = q; // Set both fields to the same value
        this.limit = limit;
        this.types = types;
    }
    
    // Getters and setters
    public String getQ() {
        // Return query if q is null (for POST requests)
        return q != null ? q : query;
    }
    
    public void setQ(String q) {
        this.q = q;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public List<String> getTypes() {
        return types;
    }
    
    public void setTypes(List<String> types) {
        this.types = types;
    }
    
    @Override
    public String toString() {
        return "UnifiedSearchRequestDto{" +
                "q='" + getQ() + '\'' +
                ", limit=" + limit +
                ", types=" + types +
                '}';
    }
}