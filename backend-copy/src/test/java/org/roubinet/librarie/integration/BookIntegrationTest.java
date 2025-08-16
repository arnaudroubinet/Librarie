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
 * Integration tests for Book management functionality.
 * Tests the complete flow from REST API to database.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookIntegrationTest {

    private static String createdBookId;

    @Test
    @Order(1)
    public void testGetAllBooks() {
        given()
            .when().get("/v1/books")
            .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("size", greaterThanOrEqualTo(0))
            .body("limit", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    public void testCreateBook() {
        String bookJson = """
            {
                "title": "Test Book Integration",
                "author": "Test Author",
                "isbn": "978-0123456789",
                "description": "A test book for integration testing"
            }
            """;

        createdBookId = given()
            .contentType(ContentType.JSON)
            .body(bookJson)
            .when().post("/v1/books")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("title", equalTo("Test Book Integration"))
            .body("isbn", equalTo("978-0123456789"))
            .extract().path("id");
    }

    @Test
    @Order(3)
    public void testGetBookById() {
        given()
            .when().get("/v1/books/{id}", createdBookId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdBookId))
            .body("title", equalTo("Test Book Integration"))
            .body("isbn", equalTo("978-0123456789"));
    }

    @Test
    @Order(4)
    public void testUpdateBook() {
        String updatedBookJson = """
            {
                "title": "Updated Test Book",
                "author": "Test Author Updated",
                "isbn": "978-0123456789",
                "description": "An updated test book for integration testing"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updatedBookJson)
            .when().put("/v1/books/{id}", createdBookId)
            .then()
            .statusCode(200)
            .body("title", equalTo("Updated Test Book"));
    }

    @Test
    @Order(5)
    public void testSearchBooks() {
        given()
            .queryParam("query", "Updated Test")
            .when().get("/v1/books/search")
            .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("content", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(6)
    public void testSearchBooksByCriteria() {
        String criteriaJson = """
            {
                "title": "Updated Test",
                "pageSize": 10
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(criteriaJson)
            .when().post("/v1/books/criteria")
            .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("content", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(7)
    public void testMarkBookAsCompleted() {
        String completionJson = """
            {
                "completed": true,
                "rating": 4
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(completionJson)
            .when().post("/v1/books/{id}/completion", createdBookId)
            .then()
            .statusCode(200);
    }

    @Test
    @Order(8)
    public void testDeleteBook() {
        given()
            .when().delete("/v1/books/{id}", createdBookId)
            .then()
            .statusCode(204);

        // Verify book is deleted
        given()
            .when().get("/v1/books/{id}", createdBookId)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(9)
    public void testCreateBookWithInvalidData() {
        String invalidBookJson = """
            {
                "title": "",
                "isbn": "invalid-isbn"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidBookJson)
            .when().post("/v1/books")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(10)
    public void testGetNonExistentBook() {
        given()
            .when().get("/v1/books/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }
}