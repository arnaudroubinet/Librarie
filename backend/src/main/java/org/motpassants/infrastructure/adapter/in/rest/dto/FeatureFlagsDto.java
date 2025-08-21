package org.motpassants.infrastructure.adapter.in.rest.dto;

/**
 * Data Transfer Object for feature flags.
 * Converted to record for immutability and reduced boilerplate.
 */
public record FeatureFlagsDto(
    boolean enableIngest,
    boolean enableExport, 
    boolean enableSync
) {}