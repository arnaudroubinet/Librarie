package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.motpassants.application.service.DemoDataService;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.AuthorRepositoryPort;
import org.motpassants.domain.port.out.SeriesRepositoryPort;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Demo Data idempotency.
 * Verifies that running demo data population multiple times doesn't duplicate data.
 */
@QuarkusTest
@DisplayName("Demo Data Idempotency Integration Tests")
public class DemoDataIdempotencyIntegrationTest {

    @Inject
    DemoDataService demoDataService;

    @Inject
    BookRepository bookRepository;

    @Inject
    AuthorRepositoryPort authorRepository;

    @Inject
    SeriesRepositoryPort seriesRepository;

    @Test
    @DisplayName("Should maintain stable counts when demo data population runs multiple times")
    void shouldMaintainStableCountsOnMultipleRuns() {
        // Given - capture initial counts
        long initialBookCount = bookRepository.count();
        long initialAuthorCount = authorRepository.count();
        long initialSeriesCount = seriesRepository.count();

        // When - run demo data population again
        demoDataService.populateDemoData();

        // Then - counts should remain stable
        long afterFirstRunBookCount = bookRepository.count();
        long afterFirstRunAuthorCount = authorRepository.count();
        long afterFirstRunSeriesCount = seriesRepository.count();

        assertEquals(initialBookCount, afterFirstRunBookCount, 
            "Book count should not change after second run");
        assertEquals(initialAuthorCount, afterFirstRunAuthorCount, 
            "Author count should not change after second run");
        assertEquals(initialSeriesCount, afterFirstRunSeriesCount, 
            "Series count should not change after second run");

        // When - run demo data population a third time to further verify idempotency
        demoDataService.populateDemoData();

        // Then - counts should still remain stable
        long afterSecondRunBookCount = bookRepository.count();
        long afterSecondRunAuthorCount = authorRepository.count();
        long afterSecondRunSeriesCount = seriesRepository.count();

        assertEquals(afterFirstRunBookCount, afterSecondRunBookCount, 
            "Book count should not change after third run");
        assertEquals(afterFirstRunAuthorCount, afterSecondRunAuthorCount, 
            "Author count should not change after third run");
        assertEquals(afterFirstRunSeriesCount, afterSecondRunSeriesCount, 
            "Series count should not change after third run");
    }

    @Test
    @DisplayName("Should skip population gracefully when books already exist")
    void shouldSkipPopulationWhenBooksExist() {
        // Given - ensure there are books in the database
        long initialCount = bookRepository.count();
        assertTrue(initialCount > 0, "Test requires existing books in database");

        // When
        demoDataService.populateDemoData();

        // Then - count should remain unchanged
        assertEquals(initialCount, bookRepository.count(), 
            "Book count should remain unchanged when books already exist");
    }
}
