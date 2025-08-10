package org.roubinet.librarie.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * OriginalWorkExternalId entity for storing external identifiers for original works.
 */
@Entity
@Table(name = "original_work_external_ids")
public class OriginalWorkExternalId extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "original_work_id", nullable = false)
    private OriginalWork originalWork;

    @Column(name = "identifier_type", nullable = false)
    private String identifierType;

    @Column(name = "identifier_value", nullable = false)
    private String identifierValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Default constructor
    public OriginalWorkExternalId() {}

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OriginalWork getOriginalWork() {
        return originalWork;
    }

    public void setOriginalWork(OriginalWork originalWork) {
        this.originalWork = originalWork;
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OriginalWorkExternalId)) return false;
        OriginalWorkExternalId that = (OriginalWorkExternalId) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}