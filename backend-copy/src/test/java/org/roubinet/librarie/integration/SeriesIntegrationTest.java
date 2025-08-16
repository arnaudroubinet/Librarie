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
 * Integration tests for Series management functionality.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeriesIntegrationTest {

    private static String createdSeriesId;

    @Test
    @Order(1)
    public void testGetAllSeries() {
        given()
            .when().get("/v1/series")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("metadata", notNullValue())
            .body("metadata.totalCount", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    public void testCreateSeries() {
        String seriesJson = """
            {
                "name": "Test Series",
                "description": "A test series for integration testing",
                "totalBooks": 5,
                "isCompleted": false
            }
            """;

        createdSeriesId = given()
            .contentType(ContentType.JSON)
            .body(seriesJson)
            .when().post("/v1/series")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Test Series"))
            .body("description", equalTo("A test series for integration testing"))
            .body("totalBooks", equalTo(5))
            .extract().path("id");
    }

    @Test
    @Order(3)
    public void testGetSeriesById() {
        given()
            .when().get("/v1/series/{id}", createdSeriesId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdSeriesId))
            .body("name", equalTo("Test Series"))
            .body("description", equalTo("A test series for integration testing"));
    }

    @Test
    @Order(4)
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
            .when().put("/v1/series/{id}", createdSeriesId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Test Series"))
            .body("totalBooks", equalTo(7))
            .body("isCompleted", equalTo(true));
    }

    @Test
    @Order(5)
    public void testSearchSeries() {
        given()
            .queryParam("query", "Updated Test")
            .when().get("/v1/series/search")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("data", hasSize(greaterThanOrEqualTo(1)))
            .body("data[0].name", containsString("Updated Test"));
    }

    @Test
    @Order(6)
    public void testGetSeriesBooks() {
        given()
            .when().get("/v1/series/{id}/books", createdSeriesId)
            .then()
            .statusCode(200)
            .body("data", notNullValue());
    }

    @Test
    @Order(7)
    public void testDeleteSeries() {
        given()
            .when().delete("/v1/series/{id}", createdSeriesId)
            .then()
            .statusCode(204);

        // Verify series is deleted
        given()
            .when().get("/v1/series/{id}", createdSeriesId)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(8)
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
            .when().post("/v1/series")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(9)
    public void testGetNonExistentSeries() {
        given()
            .when().get("/v1/series/non-existent-id")
            .then()
            .statusCode(404);
    }
}