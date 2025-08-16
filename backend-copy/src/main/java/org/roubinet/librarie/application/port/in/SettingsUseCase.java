package org.roubinet.librarie.application.port.in;

import org.roubinet.librarie.domain.model.SettingsData;

/**
 * Primary port (use case) for settings operations.
 * This interface defines the business operations related to system settings.
 */
public interface SettingsUseCase {
    
    /**
     * Get system settings including version, stats, and supported formats.
     * 
     * @return SettingsData containing all system information
     */
    SettingsData getSystemSettings();
}