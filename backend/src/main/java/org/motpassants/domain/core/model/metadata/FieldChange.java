package org.motpassants.domain.core.model.metadata;

/**
 * Individual field change preview.
 */
public record FieldChange(
    String fieldName,
    String currentValue,
    String newValue,
    String changeType  // "add", "update", "remove"
) {}