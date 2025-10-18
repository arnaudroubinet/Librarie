package org.motpassants.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.DemoDataPort;
import org.motpassants.domain.port.out.LoggingPort;

import static org.mockito.Mockito.*;

/**
 * Unit tests for DemoDataService.
 * Tests the demo data population logic and idempotency behavior.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DemoDataService Unit Tests")
class DemoDataServiceTest {

    @Mock
    private ConfigurationPort configurationPort;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoggingPort log;

    @Mock
    private DemoDataPort demoDataPort;

    private DemoDataService demoDataService;

    @BeforeEach
    void setUp() {
        demoDataService = new DemoDataService(configurationPort, bookRepository, log, demoDataPort);
    }

    @Test
    @DisplayName("Should skip demo data population when demo mode is disabled")
    void shouldSkipWhenDemoDisabled() {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(false);

        // When
        demoDataService.populateDemoData();

        // Then
        verify(configurationPort).isDemoEnabled();
        verify(log).debug("Demo disabled; skipping demo data population");
        verify(bookRepository, never()).count();
        verify(demoDataPort, never()).seed();
    }

    @Test
    @DisplayName("Should skip demo data population when books already exist")
    void shouldSkipWhenBooksAlreadyExist() {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(bookRepository.count()).thenReturn(10L);

        // When
        demoDataService.populateDemoData();

        // Then
        verify(configurationPort).isDemoEnabled();
        verify(bookRepository).count();
        verify(log).debug("Books already present; skipping demo data population");
        verify(demoDataPort, never()).seed();
    }

    @Test
    @DisplayName("Should populate demo data when demo enabled and no books exist")
    void shouldPopulateWhenDemoEnabledAndNoBooksExist() {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(bookRepository.count()).thenReturn(0L);

        // When
        demoDataService.populateDemoData();

        // Then
        verify(configurationPort).isDemoEnabled();
        verify(bookRepository).count();
        verify(log).info("Starting demo data population (async)");
        verify(demoDataPort).seed();
        verify(log).info("Demo data population finished");
    }

    @Test
    @DisplayName("Should handle exceptions gracefully during demo data population")
    void shouldHandleExceptionsGracefully() {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(bookRepository.count()).thenReturn(0L);
        doThrow(new RuntimeException("Database error")).when(demoDataPort).seed();

        // When
        demoDataService.populateDemoData();

        // Then
        verify(configurationPort).isDemoEnabled();
        verify(bookRepository).count();
        verify(log).info("Starting demo data population (async)");
        verify(demoDataPort).seed();
        verify(log).warn("Demo data population failed; continuing without demo seed");
    }

    @Test
    @DisplayName("Should be idempotent when called multiple times")
    void shouldBeIdempotentWhenCalledMultipleTimes() {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(bookRepository.count()).thenReturn(0L, 10L);

        // When - first call should populate
        demoDataService.populateDemoData();

        // Then - verify first call populated
        verify(demoDataPort, times(1)).seed();

        // When - second call should skip because books exist
        demoDataService.populateDemoData();

        // Then - verify second call did not populate again
        verify(demoDataPort, times(1)).seed(); // Still only called once
        verify(log, times(1)).debug("Books already present; skipping demo data population");
    }
}
