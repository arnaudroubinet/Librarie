package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Settings management functionality in hexagonal architecture.
 * Tests the complete flow from REST API to database with DDD approach.
 * Based on backend-copy SettingsController endpoints.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SettingsIntegrationTest {

    @Test
    @Order(1)
    public void testGetSystemSettings() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("version", notNullValue())
            .body("entityCounts", notNullValue())
            .body("supportedFormats", notNullValue());
    }

    @Test
    @Order(2)
    public void testGetSystemSettingsEntityCounts() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("entityCounts.books", greaterThanOrEqualTo(0))
            .body("entityCounts.series", greaterThanOrEqualTo(0))
            .body("entityCounts.authors", greaterThanOrEqualTo(0))
            .body("entityCounts.publishers", greaterThanOrEqualTo(0))
            .body("entityCounts.languages", greaterThanOrEqualTo(0))
            .body("entityCounts.formats", greaterThanOrEqualTo(0))
            .body("entityCounts.tags", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(3)
    public void testGetSystemSettingsSupportedFormats() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("supportedFormats", hasItems("PDF", "EPUB", "MOBI", "AZW3", "TXT"))
            .body("supportedFormats", hasSize(greaterThan(0)));
    }

    @Test
    @Order(4)
    public void testGetSystemSettingsVersion() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("version", matchesPattern("\\d+\\.\\d+\\.\\d+.*")); // Semantic versioning pattern
    }

    @Test
    @Order(5)
    public void testGetSystemSettingsApplicationName() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("applicationName", equalTo("Librarie"));
    }

    @Test
    @Order(6)
    public void testGetSystemSettingsFeatureFlags() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("featureFlags", notNullValue())
            .body("featureFlags.enableIngest", notNullValue())
            .body("featureFlags.enableExport", notNullValue())
            .body("featureFlags.enableSync", notNullValue());
    }

    @Test
    @Order(7)
    public void testGetSystemSettingsDefaultPagination() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("defaultPageSize", greaterThan(0))
            .body("maxPageSize", greaterThanOrEqualTo(100));
    }

    @Test
    @Order(8)
    public void testGetSystemSettingsStorageConfiguration() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("storageConfiguration", notNullValue())
            .body("storageConfiguration.baseDirectory", notNullValue())
            .body("storageConfiguration.allowedFileTypes", notNullValue());
    }

    @Test
    @Order(9)
    public void testGetSystemSettingsResponseStructure() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("$", hasKey("version"))
            .body("$", hasKey("applicationName"))
            .body("$", hasKey("entityCounts"))
            .body("$", hasKey("supportedFormats"))
            .body("$", hasKey("featureFlags"))
            .body("$", hasKey("defaultPageSize"))
            .body("$", hasKey("maxPageSize"))
            .body("$", hasKey("storageConfiguration"));
    }

    @Test
    @Order(10)
    public void testGetSystemSettingsIsReadOnly() {
        // Settings endpoint should be read-only (no POST, PUT, DELETE)
        given()
            .when().post("/v1/settings")
            .then()
            .statusCode(anyOf(equalTo(405), equalTo(404))); // Method Not Allowed or Not Found

        given()
            .when().put("/v1/settings")
            .then()
            .statusCode(anyOf(equalTo(405), equalTo(404))); // Method Not Allowed or Not Found

        given()
            .when().delete("/v1/settings")
            .then()
            .statusCode(anyOf(equalTo(405), equalTo(404))); // Method Not Allowed or Not Found
    }
}