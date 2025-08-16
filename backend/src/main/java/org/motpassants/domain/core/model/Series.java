package org.motpassants.domain.core.model;

import java.util.UUID;

/**
 * Series domain model.
 * Placeholder for now - will be detailed later.
 */
public class Series {
    private UUID id;
    private String name;
    
    public Series() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}