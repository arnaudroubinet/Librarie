package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.roubinet.librarie.application.port.in.SettingsUseCase;
import org.roubinet.librarie.domain.model.SettingsData;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.SettingsResponseDto;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SettingsController.
 */
public class SettingsControllerTest {

    @Mock
    private SettingsUseCase settingsUseCase;

    private SettingsController settingsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        settingsController = new SettingsController(settingsUseCase);
    }

    @Test
    void testGetSettings_Success() {
        // Given
        SettingsData mockSettingsData = new SettingsData(
            "1.0.0-SNAPSHOT",
            List.of("epub", "pdf", "mobi"),
            Map.of("books", 5L, "authors", 3L, "series", 2L)
        );
        when(settingsUseCase.getSystemSettings()).thenReturn(mockSettingsData);

        // When
        Response response = settingsController.getSettings();

        // Then
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        
        SettingsResponseDto dto = (SettingsResponseDto) response.getEntity();
        assertEquals("1.0.0-SNAPSHOT", dto.getVersion());
        assertEquals(3, dto.getSupportedFormats().size());
        assertEquals(3, dto.getEntityCounts().size());
    }

    @Test
    void testGetSettings_Exception() {
        // Given
        when(settingsUseCase.getSystemSettings()).thenThrow(new RuntimeException("Test exception"));

        // When
        Response response = settingsController.getSettings();

        // Then
        assertEquals(500, response.getStatus());
    }
}