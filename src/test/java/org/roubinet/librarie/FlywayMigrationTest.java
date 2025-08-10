package org.roubinet.librarie;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for database migration and schema validation using Flyway.
 * This test validates that the Flyway migration succeeds.
 */
@QuarkusTest
@TestProfile(FlywayTestProfile.class)
public class FlywayMigrationTest {

    @Inject
    EntityManager entityManager;

    @Test
    public void testFlywayMigrationSucceeds() {
        // Test that the EntityManager is available and working
        assertNotNull(entityManager, "EntityManager should be injected and available");
        
        // Test that we can execute a simple query to verify the schema exists
        var result = entityManager.createNativeQuery("SELECT COUNT(*) FROM languages").getSingleResult();
        assertNotNull(result, "Should be able to query the languages table");
        
        // The languages table should have at least the reference data we inserted
        assertTrue(((Number) result).longValue() >= 12, "Languages table should contain at least 12 reference languages");
    }

    @Test
    public void testLanguagesReferenceData() {
        // Test that the reference data was properly inserted
        var englishCount = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM languages WHERE code = 'en' AND name = 'English'"
        ).getSingleResult();
        
        assertEquals(1L, ((Number) englishCount).longValue(), "English language should be present");
        
        var arabicCount = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM languages WHERE code = 'ar' AND rtl = true"
        ).getSingleResult();
        
        assertEquals(1L, ((Number) arabicCount).longValue(), "Arabic should be present and marked as RTL");
    }

    @Test
    public void testSystemConfigReferenceData() {
        // Test that system configuration was properly inserted
        var configCount = entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM system_config WHERE key = 'library_path'"
        ).getSingleResult();
        
        assertEquals(1L, ((Number) configCount).longValue(), "Library path configuration should be present");
    }

    @Test 
    public void testCoreTablesExist() {
        // Test that all core tables exist by running simple queries
        String[] tables = {
            "books", "authors", "original_works", "series", "tags", "publishers", 
            "formats", "ratings", "reading_progress", "user_preferences", 
            "download_history", "import_jobs"
        };
        
        for (String table : tables) {
            assertDoesNotThrow(() -> {
                entityManager.createNativeQuery("SELECT COUNT(*) FROM " + table).getSingleResult();
            }, "Table " + table + " should exist and be queryable");
        }
    }

    @Test
    public void testRelationshipTablesExist() {
        // Test that all relationship tables exist
        String[] relationshipTables = {
            "original_work_authors", "book_original_works", "book_series", 
            "book_tags", "book_publishers", "original_work_external_ids"
        };
        
        for (String table : relationshipTables) {
            assertDoesNotThrow(() -> {
                entityManager.createNativeQuery("SELECT COUNT(*) FROM " + table).getSingleResult();
            }, "Relationship table " + table + " should exist and be queryable");
        }
    }
}