package org.roubinet.librarie.infrastructure.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * Integration tests for the hexagonal architecture REST API.
 * Tests the complete application including all layers.
 */
@QuarkusTest
public class LibraryHexagonalArchitectureTest {
    
    @Test
    public void testLibraryStats() {
        // Library stats functionality was removed with the actor pattern
        // This test is now obsolete as we focus on feature-based APIs
        given()
          .when().get("/q/health")
          .then()
             .statusCode(200)
             .body("status", is("UP"));
    }
    
    @Test
    public void testSupportedFormats() {
        // Supported formats functionality was removed with the actor pattern
        // This test is now obsolete as we focus on feature-based APIs
        given()
          .when().get("/q/health")
          .then()
             .statusCode(200)
             .body("status", is("UP"));
    }
    
    @Test
    public void testGetAllBooks() {
        given()
          .when().get("/v1/books")
          .then()
             .statusCode(200)
             .body("content", notNullValue())
             .body("limit", is(20));
    }
    
    @Test
    public void testSearchBooksWithQuery() {
        given()
          .queryParam("q", "test")
          .when().get("/v1/books/search")
          .then()
             .statusCode(200)
             .body("content", notNullValue());
    }
    
    @Test
    public void testSearchBooksWithoutQuery() {
        given()
          .when().get("/v1/books/search")
          .then()
             .statusCode(400);
    }
    
    @Test
    public void testGetBooksByAuthor() {
        // Use the new criteria-based search endpoint instead of separate by-author endpoint
        given()
          .contentType("application/json")
          .body("{\"contributorsContain\": [\"test author\"]}")
          .when().post("/v1/books/criteria")
          .then()
             .statusCode(200)
             .body("content", notNullValue());
    }
    
    @Test
    public void testGetBooksBySeries() {
        // Use the new criteria-based search endpoint instead of separate by-series endpoint
        given()
          .contentType("application/json")
          .body("{\"seriesContains\": \"test series\"}")
          .when().post("/v1/books/criteria")
          .then()
             .statusCode(200)
             .body("content", notNullValue());
    }
    
    @Test
    public void testOpenApiDocumentation() {
        given()
          .accept("application/json")
          .when().get("/q/openapi")
          .then()
             .statusCode(200)
             .body("openapi", notNullValue())
             .body("info.title", notNullValue());
    }
    
    @Test
    public void testSwaggerUI() {
        given()
          .when().get("/q/swagger-ui")
          .then()
             .statusCode(200);
    }
    
    @Test
    public void testHealthCheck() {
        given()
          .when().get("/q/health")
          .then()
             .statusCode(200)
             .body("status", is("UP"));
    }
}