package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.motpassants.domain.port.out.LoggingPort;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for logging adapters.
 * Tests the complete flow from domain to logging infrastructure.
 */
@QuarkusTest
@DisplayName("Logging Adapter Integration Tests")
public class LoggingIntegrationTest {

    @Inject
    LoggingPort loggingPort;

    @Test
    @DisplayName("Should log debug messages without throwing exceptions")
    void shouldLogDebugMessagesWithoutThrowing() {
        assertDoesNotThrow(() -> {
            loggingPort.debug("Test debug message");
        });
    }

    @Test
    @DisplayName("Should log info messages without throwing exceptions")
    void shouldLogInfoMessagesWithoutThrowing() {
        assertDoesNotThrow(() -> {
            loggingPort.info("Test info message");
        });
    }

    @Test
    @DisplayName("Should log formatted info messages without throwing exceptions")
    void shouldLogFormattedInfoMessagesWithoutThrowing() {
        assertDoesNotThrow(() -> {
            loggingPort.infof("Test formatted message with %s and %d", "string", 42);
        });
    }

    @Test
    @DisplayName("Should log warn messages without throwing exceptions")
    void shouldLogWarnMessagesWithoutThrowing() {
        assertDoesNotThrow(() -> {
            loggingPort.warn("Test warning message");
        });
    }

    @Test
    @DisplayName("Should log error messages without throwing exceptions")
    void shouldLogErrorMessagesWithoutThrowing() {
        assertDoesNotThrow(() -> {
            loggingPort.error("Test error message", new RuntimeException("Test exception"));
        });
    }

    @Test
    @DisplayName("Should handle null messages gracefully")
    void shouldHandleNullMessagesGracefully() {
        assertDoesNotThrow(() -> {
            loggingPort.debug(null);
            loggingPort.info(null);
            loggingPort.warn(null);
            loggingPort.error(null, new RuntimeException("Test"));
        });
    }

    @Test
    @DisplayName("Should handle empty messages gracefully")
    void shouldHandleEmptyMessagesGracefully() {
        assertDoesNotThrow(() -> {
            loggingPort.debug("");
            loggingPort.info("");
            loggingPort.warn("");
            loggingPort.error("", new RuntimeException("Test"));
        });
    }

    @Test
    @DisplayName("Should handle formatted messages with null parameters")
    void shouldHandleFormattedMessagesWithNullParameters() {
        assertDoesNotThrow(() -> {
            loggingPort.infof("Test message with null: %s", (Object) null);
            loggingPort.infof(null, "param1", "param2");
        });
    }
}