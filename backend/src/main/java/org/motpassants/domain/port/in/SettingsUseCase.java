package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.Settings;

/**
 * Use case interface for Settings operations.
 * Defines the business capabilities for settings and system information.
 */
public interface SettingsUseCase {
    
    /**
     * Get complete system settings including version, supported formats,
     * and entity counts.
     * 
     * @return System settings
     */
    Settings getSystemSettings();
}