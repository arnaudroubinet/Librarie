package org.roubinet.librarie.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * OriginalWorkExternalId record for storing external identifiers for original works.
 * Immutable entity representing external IDs like ISBN, OCLC, etc.
 */
@Entity
@Table(name = "original_work_external_ids")
public record OriginalWorkExternalId(
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid")
    UUID id,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_work_id", nullable = false)
    OriginalWork originalWork,
    
    @Column(name = "identifier_type", nullable = false)
    String identifierType,
    
    @Column(name = "identifier_value", nullable = false)
    String identifierValue,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    OffsetDateTime createdAt
) {
    
    // Default constructor for JPA
    public OriginalWorkExternalId() {
        this(null, null, null, null, null);
    }
    
    // Constructor for creating new instances
    public OriginalWorkExternalId(OriginalWork originalWork, String identifierType, String identifierValue) {
        this(null, originalWork, identifierType, identifierValue, null);
    }
}