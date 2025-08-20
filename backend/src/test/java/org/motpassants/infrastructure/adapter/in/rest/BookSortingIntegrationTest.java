package org.motpassants.infrastructure.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for book sorting functionality.
 * Tests the new sorting parameters in the BookController.
 */
@QuarkusTest
public class BookSortingIntegrationTest {

    @Test
    public void testDefaultSorting() {
        // Test that default sorting (no sort parameters) works
        given()
            .when().get("/v1/books?limit=3")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)))
            .body("totalElements", greaterThan(0));
    }

    @Test
    public void testTitleSortAscending() {
        // Test title_sort ASC
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "TITLE_SORT")
            .queryParam("sortDirection", "ASC")
            .when().get("/v1/books")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)))
            .body("content[0].title", notNullValue())
            .body("content[0].titleSort", notNullValue());
    }

    @Test
    public void testTitleSortDescending() {
        // Test title_sort DESC
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "TITLE_SORT")
            .queryParam("sortDirection", "DESC")
            .when().get("/v1/books")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)))
            .body("content[0].title", notNullValue())
            .body("content[0].titleSort", notNullValue());
    }

    @Test
    public void testPublicationDateAscending() {
        // Test publication_date ASC
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "PUBLICATION_DATE")
            .queryParam("sortDirection", "ASC")
            .when().get("/v1/books")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }

    @Test
    public void testPublicationDateDescending() {
        // Test publication_date DESC
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "PUBLICATION_DATE")
            .queryParam("sortDirection", "DESC")
            .when().get("/v1/books")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }

    @Test
    public void testUpdatedAtAscending() {
        // Test updated_at ASC
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "UPDATED_AT")
            .queryParam("sortDirection", "ASC")
            .when().get("/v1/books")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }

    @Test
    public void testUpdatedAtDescending() {
        // Test updated_at DESC (default)
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "UPDATED_AT")
            .queryParam("sortDirection", "DESC")
            .when().get("/v1/books")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }

    @Test
    public void testInvalidSortField() {
        // Test invalid sort field returns proper error
        given()
            .queryParam("sortField", "INVALID_FIELD")
            .when().get("/v1/books")
            .then()
            .statusCode(400)
            .body("error", containsString("Invalid sort field: INVALID_FIELD"))
            .body("error", containsString("UPDATED_AT, TITLE_SORT, PUBLICATION_DATE"));
    }

    @Test
    public void testInvalidSortDirection() {
        // Test invalid sort direction returns proper error
        given()
            .queryParam("sortField", "TITLE_SORT")
            .queryParam("sortDirection", "INVALID")
            .when().get("/v1/books")
            .then()
            .statusCode(400)
            .body("error", containsString("Invalid sort direction: INVALID"))
            .body("error", containsString("ASC, DESC"));
    }

    @Test
    public void testSortFieldWithoutDirection() {
        // Test sort field without direction (should default to DESC)
        given()
            .queryParam("limit", 3)
            .queryParam("sortField", "TITLE_SORT")
            .when().get("/v1/books")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }

    @Test
    public void testCaseInsensitiveSortParameters() {
        // Test that sort parameters are case insensitive
        given()
            .queryParam("limit", 3)
            .queryParam("sortField", "title_sort")
            .queryParam("sortDirection", "asc")
            .when().get("/v1/books")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }
}