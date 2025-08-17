package org.motpassants.domain.core.model;

import java.util.UUID;

/**
 * OriginalWork domain model.
 */
public class OriginalWork {
    private UUID id;
    private String title;
    
    public OriginalWork() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}