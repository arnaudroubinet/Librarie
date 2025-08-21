package org.motpassants.domain.core.model;

import java.util.List;

/**
 * Storage configuration domain model representing system storage settings.
 * Converted to record for immutability and reduced boilerplate.
 */
public record StorageConfiguration(
    String baseDirectory,
    List<String> allowedFileTypes
) {}