package org.roubinet.librarie.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Settings management functionality.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SettingsIntegrationTest {

    @Test
    @Order(1)
    public void testGetSettings() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("libraryName", notNullValue())
            .body("enableDemo", notNullValue())
            .body("maxPageSize", notNullValue());
    }

    @Test
    @Order(2)
    public void testUpdateSettings() {
        String settingsJson = """
            {
                "libraryName": "Test Library Updated",
                "enableDemo": false,
                "maxPageSize": 100,
                "defaultLanguage": "en"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(settingsJson)
            .when().put("/v1/settings")
            .then()
            .statusCode(200)
            .body("libraryName", equalTo("Test Library Updated"))
            .body("enableDemo", equalTo(false))
            .body("maxPageSize", equalTo(100));
    }

    @Test
    @Order(3)
    public void testGetUpdatedSettings() {
        given()
            .when().get("/v1/settings")
            .then()
            .statusCode(200)
            .body("libraryName", equalTo("Test Library Updated"))
            .body("enableDemo", equalTo(false))
            .body("maxPageSize", equalTo(100));
    }

    @Test
    @Order(4)
    public void testUpdateSettingsWithInvalidData() {
        String invalidSettingsJson = """
            {
                "libraryName": "",
                "maxPageSize": -1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidSettingsJson)
            .when().put("/v1/settings")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(5)
    public void testGetEntityCounts() {
        given()
            .when().get("/v1/settings/entity-counts")
            .then()
            .statusCode(200)
            .body("books", greaterThanOrEqualTo(0))
            .body("authors", greaterThanOrEqualTo(0))
            .body("series", greaterThanOrEqualTo(0));
    }
}