package org.motpassants.domain.core.model.metadata;

/**
 * Provider status information.
 */
public record ProviderStatus(
    String providerId,
    String providerName,
    boolean enabled,
    boolean connected,
    String error
) {}