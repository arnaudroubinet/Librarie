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
            .body("content", notNullValue())
            .body("size", greaterThanOrEqualTo(0))
            .body("limit", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    public void testGetAllAuthorsWithPagination() {
        given()
            .queryParam("limit", 5)
            .when().get("/v1/authors")
            .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("limit", equalTo(5));
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
            .body("content", notNullValue())
            .body("content", hasSize(greaterThanOrEqualTo(0)));
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
            .body("content", notNullValue())
            .body("limit", equalTo(5));
    }

    @Test
    @Order(8)
    public void testGetAuthorPicture() {
        // Test getting author picture (should return 200 with fallback SVG for author without picture)
        given()
            .when().get("/v1/authors/{id}/picture", createdAuthorId)
            .then()
            .statusCode(200)
            .contentType(anyOf(containsString("image/"), containsString("svg")));
    }

    @Test
    @Order(9)
    public void testGetAuthorPictureWithAcceptHeaders() {
        // Test with different Accept headers to ensure no 406 errors
        
        // Browser typical Accept header
        given()
            .header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
            .when().get("/v1/authors/{id}/picture", createdAuthorId)
            .then()
            .statusCode(200)
            .contentType(anyOf(containsString("image/"), containsString("svg")));
        
        // Generic Accept header
        given()
            .header("Accept", "*/*")
            .when().get("/v1/authors/{id}/picture", createdAuthorId)
            .then()
            .statusCode(200)
            .contentType(anyOf(containsString("image/"), containsString("svg")));
        
        // Image-specific Accept header
        given()
            .header("Accept", "image/*")
            .when().get("/v1/authors/{id}/picture", createdAuthorId)
            .then()
            .statusCode(200)
            .contentType(anyOf(containsString("image/"), containsString("svg")));
        
        // JSON Accept header (should still work with image endpoint)
        given()
            .header("Accept", "application/json")
            .when().get("/v1/authors/{id}/picture", createdAuthorId)
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(406))); // May return 406 if strict content negotiation
    }

    @Test
    @Order(10)
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
    @Order(11)
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
    @Order(12)
    public void testGetNonExistentAuthor() {
        given()
            .when().get("/v1/authors/00000000-0000-0000-0000-000000000000")
            .then()
            .statusCode(404);
    }

    @Test
    @Order(12)
    public void testGetNonExistentAuthorPicture() {
        // Verify that requesting a picture for a non-existent author returns 404, not 406
        given()
            .header("Accept", "image/*")
            .when().get("/v1/authors/00000000-0000-0000-0000-000000000000/picture")
            .then()
            .statusCode(404)
            .contentType(containsString("text/plain"));
    }

    @Test
    @Order(13)
    public void testSearchAuthorsWithInvalidQuery() {
        given()
            .queryParam("q", "")
            .when().get("/v1/authors/search")
            .then()
            .statusCode(400);
    }
}