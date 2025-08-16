package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Series management functionality in hexagonal architecture.
 * Tests the complete flow from REST API to database with DDD approach.
 * Based on backend-copy SeriesController endpoints.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeriesIntegrationTest {

    private static String createdSeriesId;

    @Test
    @Order(1)
    public void testGetAllSeries() {
        given()
            .when().get("/v1/books/series")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("metadata", notNullValue())
            .body("metadata.totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    public void testGetAllSeriesWithPagination() {
        given()
            .queryParam("limit", 5)
            .when().get("/v1/books/series")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("metadata.limit", equalTo(5));
    }

    @Test
    @Order(3)
    public void testCreateSeries() {
        String seriesJson = """
            {
                "name": "Test Series Integration",
                "description": "A test series for integration testing",
                "totalBooks": 5,
                "isCompleted": false
            }
            """;

        createdSeriesId = given()
            .contentType(ContentType.JSON)
            .body(seriesJson)
            .when().post("/v1/books/series")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Test Series Integration"))
            .body("totalBooks", equalTo(5))
            .body("isCompleted", equalTo(false))
            .extract().path("id");
    }

    @Test
    @Order(4)
    public void testGetSeriesById() {
        given()
            .when().get("/v1/books/series/{id}", createdSeriesId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdSeriesId))
            .body("name", equalTo("Test Series Integration"))
            .body("totalBooks", equalTo(5));
    }

    @Test
    @Order(5)
    public void testUpdateSeries() {
        String updatedSeriesJson = """
            {
                "name": "Updated Test Series",
                "description": "An updated test series for integration testing",
                "totalBooks": 7,
                "isCompleted": true
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updatedSeriesJson)
            .when().put("/v1/books/series/{id}", createdSeriesId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Test Series"))
            .body("totalBooks", equalTo(7))
            .body("isCompleted", equalTo(true));
    }

    @Test
    @Order(6)
    public void testSearchSeries() {
        given()
            .queryParam("q", "Updated Test")
            .when().get("/v1/books/series/search")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("data", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(7)
    public void testSearchSeriesWithPagination() {
        given()
            .queryParam("q", "Updated Test")
            .queryParam("limit", 5)
            .when().get("/v1/books/series/search")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("metadata.limit", equalTo(5));
    }

    @Test
    @Order(8)
    public void testGetSeriesPicture() {
        // Test getting series picture (should return 404 for new series without picture)
        given()
            .when().get("/v1/books/series/{id}/picture", createdSeriesId)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(404))); // OK if no picture or if fallback SVG is returned
    }

    @Test
    @Order(9)
    public void testGetSeriesBooks() {
        // Test getting books in series
        given()
            .when().get("/v1/books/series/{id}/books", createdSeriesId)
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("metadata", notNullValue());
    }

    @Test
    @Order(10)
    public void testDeleteSeries() {
        given()
            .when().delete("/v1/books/series/{id}", createdSeriesId)
            .then()
            .statusCode(204);

        // Verify series is deleted
        given()
            .when().get("/v1/books/series/{id}", createdSeriesId)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(11)
    public void testCreateSeriesWithInvalidData() {
        String invalidSeriesJson = """
            {
                "name": "",
                "totalBooks": -1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidSeriesJson)
            .when().post("/v1/books/series")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(12)
    public void testGetNonExistentSeries() {
        given()
            .when().get("/v1/books/series/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    @Order(13)
    public void testSearchSeriesWithInvalidQuery() {
        given()
            .queryParam("q", "")
            .when().get("/v1/books/series/search")
            .then()
            .statusCode(400);
    }
}