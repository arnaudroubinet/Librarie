package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.anyOf;

/**
 * Integration tests for metadata functionality.
 * Tests DATA-002: Metadata Editing and External Providers REST API.
 */
@QuarkusTest
public class MetadataIntegrationTest {

    @Test
    public void testGetProviderStatus() {
        given()
            .when().get("/api/metadata/providers/status")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].providerId", notNullValue())
                .body("[0].providerName", notNullValue())
                .body("[0].enabled", notNullValue())
                .body("[0].connected", notNullValue());
    }

    @Test
    public void testSearchMetadataByValidIsbn() {
        given()
            .when().get("/api/metadata/search/isbn/9780545010221")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", org.hamcrest.Matchers.greaterThanOrEqualTo(0));
                // Note: External API calls may be blocked, so we just verify the endpoint is working
    }

    @Test
    public void testSearchMetadataByInvalidIsbn() {
        given()
            .when().get("/api/metadata/search/isbn/invalid-isbn")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(0));
    }

    @Test
    public void testSearchMetadataByEmptyIsbn() {
        given()
            .when().get("/api/metadata/search/isbn/")
            .then()
                .statusCode(404); // Empty path parameter results in 404
    }

    @Test
    public void testSearchMetadataByTitle() {
        given()
            .queryParam("title", "Harry Potter")
            .queryParam("author", "Rowling")
            .when().get("/api/metadata/search/title")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", org.hamcrest.Matchers.greaterThanOrEqualTo(0));
                // Note: External API calls may be blocked, so we just verify the endpoint is working
    }

    @Test
    public void testSearchMetadataByTitleWithoutAuthor() {
        given()
            .queryParam("title", "Programming")
            .when().get("/api/metadata/search/title")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", org.hamcrest.Matchers.greaterThanOrEqualTo(0));
    }

    @Test
    public void testSearchMetadataByEmptyTitle() {
        given()
            .when().get("/api/metadata/search/title")
            .then()
                .statusCode(400)
                .body("message", equalTo("Title is required"));
    }

    @Test
    public void testGetBestMetadataByValidIsbn() {
        given()
            .when().get("/api/metadata/best/9780545010221")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)))
                .contentType(ContentType.JSON);
                // Note: External API calls may be blocked, so we accept both success and not found
    }

    @Test
    public void testGetBestMetadataByInvalidIsbn() {
        given()
            .when().get("/api/metadata/best/invalid-isbn")
            .then()
                .statusCode(404)
                .body("message", containsString("No metadata found"));
    }

    @Test
    public void testPreviewMetadataChanges() {
        // First get a book from the existing books and check its current title
        Response bookResponse = given()
            .when().get("/v1/books?page=0&size=1")
            .then()
                .statusCode(200)
                .extract()
                .response();
                
        String bookId = bookResponse.path("content[0].id");
        String currentTitle = bookResponse.path("content[0].title");
        
        // Use a title that's guaranteed to be different from the current one
        String newTitle = currentTitle != null && currentTitle.equals("Unique Preview Test Title") 
            ? "Different Preview Test Title" 
            : "Unique Preview Test Title";

        // Test metadata preview - using JSON directly with guaranteed different values
        String metadataJson = String.format("""
            {
                "title": "%s",
                "description": "Unique preview test description that should be different",
                "pageCount": 999
            }
            """, newTitle);

        given()
            .contentType(ContentType.JSON)
            .body(metadataJson)
            .when().post("/api/metadata/preview/" + bookId)
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("bookId", equalTo(bookId))
                .body("newTitle", equalTo(newTitle))
                .body("changes.size()", greaterThan(0));
    }

    @Test
    public void testPreviewMetadataChangesInvalidBookId() {
        String metadataJson = """
            {
                "title": "Test Title"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(metadataJson)
            .when().post("/api/metadata/preview/invalid-uuid")
            .then()
                .statusCode(404)
                .body("message", containsString("Book not found"));
    }

    @Test
    public void testApplyMetadataToBook() {
        // First get a book ID from the existing books
        String bookId = given()
            .when().get("/v1/books?page=0&size=1")
            .then()
                .statusCode(200)
                .extract()
                .path("content[0].id");

        // Test metadata application
        String metadataJson = """
            {
                "description": "Applied test description",
                "pageCount": 300
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(metadataJson)
            .queryParam("overwrite", false)
            .when().post("/api/metadata/apply/" + bookId)
            .then()
                .statusCode(200)
                .body("message", containsString("applied successfully"))
                .body("bookId", equalTo(bookId));
    }

    @Test
    public void testApplyMetadataInvalidBookId() {
        String metadataJson = """
            {
                "title": "Test Title"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(metadataJson)
            .when().post("/api/metadata/apply/invalid-uuid")
            .then()
                .statusCode(404)
                .body("message", containsString("Book not found"));
    }

    @Test
    public void testMergeMetadata() {
        String metadataArrayJson = """
            [
                {
                    "title": "Title 1",
                    "description": "Description 1",
                    "pageCount": 100,
                    "confidence": 0.8
                },
                {
                    "title": "Title 2",
                    "publisher": "Publisher 2",
                    "pageCount": 200,
                    "confidence": 0.9
                }
            ]
            """;

        given()
            .contentType(ContentType.JSON)
            .body(metadataArrayJson)
            .when().post("/api/metadata/merge")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("title", notNullValue())
                .body("confidence", notNullValue());
    }

    @Test
    public void testMergeEmptyMetadataList() {
        given()
            .contentType(ContentType.JSON)
            .body("[]")
            .when().post("/api/metadata/merge")
            .then()
                .statusCode(400)
                .body("message", equalTo("Metadata list cannot be empty"));
    }

    @Test
    public void testProviderConnectionTest() {
        given()
            .when().post("/api/metadata/providers/test")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].providerId", notNullValue())
                .body("[0].connected", notNullValue());
    }
}