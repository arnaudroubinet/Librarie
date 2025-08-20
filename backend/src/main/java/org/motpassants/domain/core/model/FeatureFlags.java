package org.motpassants.domain.core.model;

/**
 * Feature flags domain model representing system feature toggles.
 * Converted to record for immutability and reduced boilerplate.
 */
public record FeatureFlags(
    boolean enableIngest,
    boolean enableExport,
    boolean enableSync
) {}