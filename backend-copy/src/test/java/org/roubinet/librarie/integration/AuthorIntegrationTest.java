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
 * Integration tests for Author management functionality.
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
            .body("metadata.totalCount", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    public void testCreateAuthor() {
        String authorJson = """
            {
                "name": "Test Author",
                "biography": "A test author for integration testing",
                "birthDate": "1970-01-01",
                "nationality": "Test Country"
            }
            """;

        createdAuthorId = given()
            .contentType(ContentType.JSON)
            .body(authorJson)
            .when().post("/v1/authors")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Test Author"))
            .body("biography", equalTo("A test author for integration testing"))
            .extract().path("id");
    }

    @Test
    @Order(3)
    public void testGetAuthorById() {
        given()
            .when().get("/v1/authors/{id}", createdAuthorId)
            .then()
            .statusCode(200)
            .body("id", equalTo(createdAuthorId))
            .body("name", equalTo("Test Author"))
            .body("biography", equalTo("A test author for integration testing"));
    }

    @Test
    @Order(4)
    public void testUpdateAuthor() {
        String updatedAuthorJson = """
            {
                "name": "Updated Test Author",
                "biography": "An updated test author for integration testing",
                "birthDate": "1970-01-01",
                "nationality": "Updated Country"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(updatedAuthorJson)
            .when().put("/v1/authors/{id}", createdAuthorId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Test Author"))
            .body("nationality", equalTo("Updated Country"));
    }

    @Test
    @Order(5)
    public void testSearchAuthors() {
        given()
            .queryParam("query", "Updated Test")
            .when().get("/v1/authors/search")
            .then()
            .statusCode(200)
            .body("data", notNullValue())
            .body("data", hasSize(greaterThanOrEqualTo(1)))
            .body("data[0].name", containsString("Updated Test"));
    }

    @Test
    @Order(6)
    public void testGetAuthorBooks() {
        given()
            .when().get("/v1/authors/{id}/books", createdAuthorId)
            .then()
            .statusCode(200)
            .body("data", notNullValue());
    }

    @Test
    @Order(7)
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
    @Order(8)
    public void testCreateAuthorWithInvalidData() {
        String invalidAuthorJson = """
            {
                "name": "",
                "biography": null
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
    @Order(9)
    public void testGetNonExistentAuthor() {
        given()
            .when().get("/v1/authors/non-existent-id")
            .then()
            .statusCode(404);
    }
}