package org.roubinet.librarie.infrastructure.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
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
        given()
          .when().get("/api/library/stats")
          .then()
             .statusCode(200)
             .body("supportedFormats", is(28))
             .body("version", is("1.0.0-SNAPSHOT"))
             .body("features", notNullValue());
    }
    
    @Test
    public void testSupportedFormats() {
        given()
          .when().get("/api/library/supported-formats")
          .then()
             .statusCode(200)
             .body("count", is(28))
             .body("supportedFormats", notNullValue());
    }
    
    @Test
    public void testGetAllBooks() {
        given()
          .when().get("/api/books")
          .then()
             .statusCode(200)
             .body("content", notNullValue())
             .body("page", is(0))
             .body("size", is(20));
    }
    
    @Test
    public void testSearchBooksWithQuery() {
        given()
          .queryParam("q", "test")
          .when().get("/api/books/search")
          .then()
             .statusCode(200)
             .body("content", notNullValue());
    }
    
    @Test
    public void testSearchBooksWithoutQuery() {
        given()
          .when().get("/api/books/search")
          .then()
             .statusCode(400);
    }
    
    @Test
    public void testGetBooksByAuthor() {
        given()
          .queryParam("author", "test author")
          .when().get("/api/books/by-author")
          .then()
             .statusCode(200)
             .body("content", notNullValue());
    }
    
    @Test
    public void testGetBooksBySeries() {
        given()
          .queryParam("series", "test series")
          .when().get("/api/books/by-series")
          .then()
             .statusCode(200)
             .body("content", notNullValue());
    }
    
    @Test
    public void testOpenApiDocumentation() {
        given()
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