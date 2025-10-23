package org.motpassants.domain.core.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MetadataFetchStatus enum.
 */
@DisplayName("MetadataFetchStatus Unit Tests")
class MetadataFetchStatusTest {

    @Test
    @DisplayName("Should identify terminal states correctly")
    void shouldIdentifyTerminalStates() {
        assertTrue(MetadataFetchStatus.COMPLETED.isTerminal());
        assertTrue(MetadataFetchStatus.FAILED.isTerminal());
        assertTrue(MetadataFetchStatus.APPROVED.isTerminal());
        assertTrue(MetadataFetchStatus.REJECTED.isTerminal());
        
        assertFalse(MetadataFetchStatus.PENDING.isTerminal());
        assertFalse(MetadataFetchStatus.IN_PROGRESS.isTerminal());
        assertFalse(MetadataFetchStatus.PARTIAL.isTerminal());
    }

    @Test
    @DisplayName("Should identify successful states correctly")
    void shouldIdentifySuccessfulStates() {
        assertTrue(MetadataFetchStatus.COMPLETED.isSuccessful());
        assertTrue(MetadataFetchStatus.PARTIAL.isSuccessful());
        assertTrue(MetadataFetchStatus.APPROVED.isSuccessful());
        
        assertFalse(MetadataFetchStatus.FAILED.isSuccessful());
        assertFalse(MetadataFetchStatus.REJECTED.isSuccessful());
        assertFalse(MetadataFetchStatus.PENDING.isSuccessful());
        assertFalse(MetadataFetchStatus.IN_PROGRESS.isSuccessful());
    }

    @Test
    @DisplayName("Should have descriptions for all statuses")
    void shouldHaveDescriptionsForAllStatuses() {
        for (MetadataFetchStatus status : MetadataFetchStatus.values()) {
            assertNotNull(status.getDescription());
            assertFalse(status.getDescription().isEmpty());
        }
    }

    @Test
    @DisplayName("ToString should return description")
    void toStringShouldReturnDescription() {
        assertEquals("Pending fetch", MetadataFetchStatus.PENDING.toString());
        assertEquals("Fetch completed successfully", MetadataFetchStatus.COMPLETED.toString());
        assertEquals("Fetch failed", MetadataFetchStatus.FAILED.toString());
    }
}
