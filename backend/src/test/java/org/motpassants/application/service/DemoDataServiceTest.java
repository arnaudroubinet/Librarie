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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DemoDataService.
 * Tests the production safeguard and demo data population logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DemoDataService Tests")
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
        demoDataService = new DemoDataService(
            configurationPort,
            bookRepository,
            log,
            demoDataPort
        );
    }
    
    @Test
    @DisplayName("Should skip demo data when disabled")
    void shouldSkipDemoDataWhenDisabled() {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(false);
        
        // When
        demoDataService.populateDemoData();
        
        // Then
        verify(log).debug("Demo disabled; skipping demo data population");
        verify(demoDataPort, never()).seed();
        verify(bookRepository, never()).count();
    }
    
    @Test
    @DisplayName("Should throw exception when demo enabled in production profile")
    void shouldThrowExceptionWhenDemoEnabledInProduction() {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(configurationPort.getActiveProfile()).thenReturn("prod");
        
        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> demoDataService.populateDemoData(),
            "Should throw IllegalStateException when demo enabled in prod"
        );
        
        // Verify error message
        assertTrue(exception.getMessage().contains("SECURITY"));
        assertTrue(exception.getMessage().contains("production profile"));
        assertTrue(exception.getMessage().contains("librarie.demo.enabled=false"));
        
        // Verify error was logged
        verify(log).error(anyString(), any(IllegalStateException.class));
        
        // Verify demo data was not populated
        verify(demoDataPort, never()).seed();
        verify(bookRepository, never()).count();
    }
    
    @Test
    @DisplayName("Should throw exception when demo enabled in PROD profile (uppercase)")
    void shouldThrowExceptionWhenDemoEnabledInProductionUppercase() {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(configurationPort.getActiveProfile()).thenReturn("PROD");
        
        // When/Then
        assertThrows(
            IllegalStateException.class,
            () -> demoDataService.populateDemoData(),
            "Should throw exception for uppercase PROD profile"
        );
        
        verify(demoDataPort, never()).seed();
    }
    
    @Test
    @DisplayName("Should populate demo data in dev profile when enabled and database empty")
    void shouldPopulateDemoDataInDevProfile() throws Exception {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(configurationPort.getActiveProfile()).thenReturn("dev");
        when(bookRepository.count()).thenReturn(0L);
        
        // When
        demoDataService.populateDemoData();
        
        // Then
        verify(log).info(contains("Starting demo data population"));
        verify(log).info(contains("profile: dev"));
        verify(demoDataPort).seed();
        verify(log).info("Demo data population finished");
    }
    
    @Test
    @DisplayName("Should populate demo data in test profile when enabled and database empty")
    void shouldPopulateDemoDataInTestProfile() throws Exception {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(configurationPort.getActiveProfile()).thenReturn("test");
        when(bookRepository.count()).thenReturn(0L);
        
        // When
        demoDataService.populateDemoData();
        
        // Then
        verify(demoDataPort).seed();
    }
    
    @Test
    @DisplayName("Should skip demo data when books already exist")
    void shouldSkipDemoDataWhenBooksExist() {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(configurationPort.getActiveProfile()).thenReturn("dev");
        when(bookRepository.count()).thenReturn(10L);
        
        // When
        demoDataService.populateDemoData();
        
        // Then
        verify(log).debug("Books already present; skipping demo data population");
        verify(demoDataPort, never()).seed();
    }
    
    @Test
    @DisplayName("Should handle demo data population failure gracefully")
    void shouldHandleDemoDataPopulationFailure() throws Exception {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(configurationPort.getActiveProfile()).thenReturn("dev");
        when(bookRepository.count()).thenReturn(0L);
        doThrow(new RuntimeException("Database error")).when(demoDataPort).seed();
        
        // When
        assertDoesNotThrow(() -> demoDataService.populateDemoData());
        
        // Then
        verify(log).warn("Demo data population failed; continuing without demo seed");
    }
    
    @Test
    @DisplayName("Should allow custom profiles that are not prod")
    void shouldAllowCustomProfiles() throws Exception {
        // Given
        when(configurationPort.isDemoEnabled()).thenReturn(true);
        when(configurationPort.getActiveProfile()).thenReturn("staging");
        when(bookRepository.count()).thenReturn(0L);
        
        // When
        demoDataService.populateDemoData();
        
        // Then
        verify(demoDataPort).seed();
    }
}
