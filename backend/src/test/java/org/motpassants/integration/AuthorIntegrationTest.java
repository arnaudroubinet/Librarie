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
 * Integration tests for Author management functionality in hexagonal architecture.
 * Tests the complete flow from REST API to database with DDD approach.
 * Based on backend-copy AuthorController endpoints.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorIntegrationTest {

    private static String createdAuthorId;

    @Test
    @Order(1)
    public void testGetAllAuthors() {
        given()
            .when().get("/v1/authors")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("metadata", notNullValue())
            .body("metadata.totalElements", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    public void testGetAllAuthorsWithPagination() {
        given()
            .queryParam("limit", 5)
            .when().get("/v1/authors")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("metadata.limit", equalTo(5));
    }

    @Test
    @Order(3)
    public void testCreateAuthor() {
        String authorJson = """
            {
                "name": "Test Author Integration",
                "sortName": "Author, Test Integration",
                "bio": {"en": "A test author for integration testing"},
                "birthDate": "1970-01-01",
                "websiteUrl": "https://testauthor.com"
            }
            """;

        createdAuthorId = given()
            .contentType(ContentType.JSON)
            .body(authorJson)
            .when().post("/v1/authors")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Test Author Integration"))
            .body("sortName", equalTo("Author, Test Integration"))
            .extract().path("id");
    }

    @Test
    @Order(4)
    public void testGetAuthorById() {
        given()
            .when().get("/v1/authors/{id}", createdAuthorId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdAuthorId))
            .body("name", equalTo("Test Author Integration"))
            .body("sortName", equalTo("Author, Test Integration"));
    }

    @Test
    @Order(5)
    public void testUpdateAuthor() {
        String updatedAuthorJson = """
            {
                "name": "Updated Test Author",
                "sortName": "Author, Updated Test",
                "bio": {"en": "An updated test author for integration testing"},
                "birthDate": "1970-01-01",
                "websiteUrl": "https://updatedtestauthor.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updatedAuthorJson)
            .when().put("/v1/authors/{id}", createdAuthorId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Test Author"))
            .body("sortName", equalTo("Author, Updated Test"));
    }

    @Test
    @Order(6)
    public void testSearchAuthors() {
        given()
            .queryParam("q", "Updated Test")
            .when().get("/v1/authors/search")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("data", hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(7)
    public void testSearchAuthorsWithPagination() {
        given()
            .queryParam("q", "Updated Test")
            .queryParam("limit", 5)
            .when().get("/v1/authors/search")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("metadata.limit", equalTo(5));
    }

    @Test
    @Order(8)
    public void testGetAuthorPicture() {
        // Test getting author picture (should return 404 for new author without picture)
        given()
            .when().get("/v1/authors/{id}/picture", createdAuthorId)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(404))); // OK if no picture or if fallback SVG is returned
    }

    @Test
    @Order(9)
    public void testDeleteAuthor() {
        given()
            .when().delete("/v1/authors/{id}", createdAuthorId)
            .then()
            .statusCode(204);

        // Verify author is deleted
        given()
            .when().get("/v1/authors/{id}", createdAuthorId)
            .then()
            .statusCode(404);
    }

    @Test
    @Order(10)
    public void testCreateAuthorWithInvalidData() {
        String invalidAuthorJson = """
            {
                "name": "",
                "sortName": ""
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidAuthorJson)
            .when().post("/v1/authors")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(11)
    public void testGetNonExistentAuthor() {
        given()
            .when().get("/v1/authors/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    @Order(12)
    public void testSearchAuthorsWithInvalidQuery() {
        given()
            .queryParam("q", "")
            .when().get("/v1/authors/search")
            .then()
            .statusCode(400);
    }
}