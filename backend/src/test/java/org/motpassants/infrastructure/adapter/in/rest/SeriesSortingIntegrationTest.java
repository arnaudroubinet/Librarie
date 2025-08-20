package org.motpassants.infrastructure.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for series sorting functionality.
 */
@QuarkusTest
public class SeriesSortingIntegrationTest {

    @Test
    public void testDefaultSorting() {
        given()
            .when().get("/v1/books/series?limit=3")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }

    @Test
    public void testTitleSortAscending() {
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "SORT_NAME")
            .queryParam("sortDirection", "ASC")
            .when().get("/v1/books/series")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)))
            .body("content[0].name", notNullValue())
            .body("content[0].sortName", notNullValue());
    }

    @Test
    public void testTitleSortDescending() {
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "SORT_NAME")
            .queryParam("sortDirection", "DESC")
            .when().get("/v1/books/series")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)))
            .body("content[0].name", notNullValue())
            .body("content[0].sortName", notNullValue());
    }

    @Test
    public void testUpdatedAtAscending() {
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "UPDATED_AT")
            .queryParam("sortDirection", "ASC")
            .when().get("/v1/books/series")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }

    @Test
    public void testUpdatedAtDescending() {
        given()
            .queryParam("limit", 5)
            .queryParam("sortField", "UPDATED_AT")
            .queryParam("sortDirection", "DESC")
            .when().get("/v1/books/series")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }

    @Test
    public void testInvalidSortField() {
        given()
            .queryParam("sortField", "INVALID_FIELD")
            .when().get("/v1/books/series")
            .then()
            .statusCode(400)
            .body("error", containsString("Invalid sort field"));
    }

    @Test
    public void testInvalidSortDirection() {
        given()
            .queryParam("sortField", "SORT_NAME")
            .queryParam("sortDirection", "INVALID")
            .when().get("/v1/books/series")
            .then()
            .statusCode(400)
            .body("error", containsString("Invalid sort direction"));
    }

    @Test
    public void testSortFieldWithoutDirection() {
        given()
            .queryParam("limit", 3)
            .queryParam("sortField", "SORT_NAME")
            .when().get("/v1/books/series")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }

    @Test
    public void testCaseInsensitiveSortParameters() {
        given()
            .queryParam("limit", 3)
            .queryParam("sortField", "sort_name")
            .queryParam("sortDirection", "asc")
            .when().get("/v1/books/series")
            .then()
            .statusCode(200)
            .body("content", hasSize(greaterThan(0)));
    }
}
