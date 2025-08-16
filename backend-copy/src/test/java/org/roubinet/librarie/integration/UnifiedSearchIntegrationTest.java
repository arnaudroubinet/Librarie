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
 * Integration tests for Unified Search functionality.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnifiedSearchIntegrationTest {

    @Test
    @Order(1)
    public void testUnifiedSearchEmpty() {
        given()
            .queryParam("query", "")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue());
    }

    @Test
    @Order(2)
    public void testUnifiedSearchWithQuery() {
        given()
            .queryParam("query", "test")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue())
            .body("metadata", notNullValue())
            .body("metadata.totalResults", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(3)
    public void testUnifiedSearchWithFilters() {
        given()
            .queryParam("query", "test")
            .queryParam("entityTypes", "books,authors")
            .queryParam("limit", "5")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue());
    }

    @Test
    @Order(4)
    public void testUnifiedSearchBooksOnly() {
        given()
            .queryParam("query", "test")
            .queryParam("entityTypes", "books")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", emptyOrNullString())
            .body("series", emptyOrNullString());
    }

    @Test
    @Order(5)
    public void testUnifiedSearchAuthorsOnly() {
        given()
            .queryParam("query", "test")
            .queryParam("entityTypes", "authors")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", emptyOrNullString())
            .body("authors", notNullValue())
            .body("series", emptyOrNullString());
    }

    @Test
    @Order(6)
    public void testUnifiedSearchSeriesOnly() {
        given()
            .queryParam("query", "test")
            .queryParam("entityTypes", "series")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", emptyOrNullString())
            .body("authors", emptyOrNullString())
            .body("series", notNullValue());
    }

    @Test
    @Order(7)
    public void testUnifiedSearchWithPagination() {
        given()
            .queryParam("query", "test")
            .queryParam("limit", "2")
            .queryParam("offset", "0")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("metadata", notNullValue())
            .body("metadata.hasMore", notNullValue());
    }

    @Test
    @Order(8)
    public void testUnifiedSearchWithInvalidEntityType() {
        given()
            .queryParam("query", "test")
            .queryParam("entityTypes", "invalid")
            .when().get("/v1/search")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(9)
    public void testUnifiedSearchWithSpecialCharacters() {
        given()
            .queryParam("query", "test@#$%")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue());
    }

    @Test
    @Order(10)
    public void testUnifiedSearchSuggestions() {
        given()
            .queryParam("query", "tes")
            .queryParam("suggestions", "true")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("suggestions", notNullValue());
    }
}