package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * Integration tests for Reading Progress functionality.
 * Tests the complete flow from REST API to database.
 */
@QuarkusTest
public class ReadingProgressIntegrationTest {

    private String createTestBook(String title, String isbn) {
        String bookJson = String.format("""
            {
                "title": "%s",
                "isbn": "%s",
                "description": "A test book for reading progress integration testing"
            }
            """, title, isbn);

        return given()
            .contentType(ContentType.JSON)
            .body(bookJson)
            .when().post("/v1/books")
            .then()
            .statusCode(201)
            .extract().path("id");
    }

    @Test
    @DisplayName("Should update reading progress successfully")
    public void testUpdateReadingProgress() {
        String bookId = createTestBook("Test Book for Progress Tracking", "978-1234567890");
        
        String progressJson = """
            {
                "currentPage": 50,
                "totalPages": 300,
                "progress": 0.16666667
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(progressJson)
            .when().put("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(200)
            .body("currentPage", equalTo(50))
            .body("totalPages", equalTo(300))
            .body("progress", notNullValue())
            .body("progressPercentage", notNullValue())
            .body("status", equalTo("READING"))
            .body("isCompleted", equalTo(false))
            .body("startedAt", notNullValue())
            .body("lastReadAt", notNullValue());
    }

    @Test
    @DisplayName("Should get reading progress successfully")
    public void testGetReadingProgress() {
        String bookId = createTestBook("Test Book for Get Progress", "978-1234567891");
        
        // First create progress
        String progressJson = """
            {
                "currentPage": 50,
                "totalPages": 300,
                "progress": 0.16666667
            }
            """;
        given()
            .contentType(ContentType.JSON)
            .body(progressJson)
            .when().put("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(200);
        
        // Then get it
        given()
            .when().get("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(200)
            .body("currentPage", equalTo(50))
            .body("totalPages", equalTo(300))
            .body("status", equalTo("READING"));
    }

    @Test
    @DisplayName("Should mark book as started")
    public void testMarkAsStarted() {
        String bookId = createTestBook("Book to Start", "978-9876543210");

        given()
            .contentType(ContentType.JSON)
            .when().post("/v1/books/" + bookId + "/progress/start")
            .then()
            .statusCode(200)
            .body("status", equalTo("READING"))
            .body("startedAt", notNullValue())
            .body("isCompleted", equalTo(false));
    }

    @Test
    @DisplayName("Should mark book as finished")
    public void testMarkAsFinished() {
        String bookId = createTestBook("Book to Finish", "978-1234567892");
        
        // First add some progress
        String progressJson = """
            {
                "currentPage": 250,
                "totalPages": 300,
                "progress": 0.83
            }
            """;
        given()
            .contentType(ContentType.JSON)
            .body(progressJson)
            .when().put("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(200);
        
        // Now mark as finished
        given()
            .contentType(ContentType.JSON)
            .when().post("/v1/books/" + bookId + "/progress/finish")
            .then()
            .statusCode(200)
            .body("status", equalTo("FINISHED"))
            .body("progress", equalTo(1.0f))
            .body("progressPercentage", equalTo(100.0f))
            .body("isCompleted", equalTo(true))
            .body("finishedAt", notNullValue());
    }

    @Test
    @DisplayName("Should mark book as DNF")
    public void testMarkAsDNF() {
        String bookId = createTestBook("Book to DNF", "978-1111111111");

        // Start reading
        given()
            .contentType(ContentType.JSON)
            .body("{\"progress\": 0.3}")
            .when().put("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(200);

        // Mark as DNF
        given()
            .contentType(ContentType.JSON)
            .when().post("/v1/books/" + bookId + "/progress/dnf")
            .then()
            .statusCode(200)
            .body("status", equalTo("DNF"))
            .body("isCompleted", equalTo(false));
    }

    @Test
    @DisplayName("Should validate progress range")
    public void testValidateProgressRange() {
        String bookId = createTestBook("Book for validation", "978-1111111112");
        
        String invalidProgressJson = """
            {
                "progress": 1.5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidProgressJson)
            .when().put("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(400)
            .body("error", containsString("Progress must be between 0.0 and 1.0"));
    }

    @Test
    @DisplayName("Should delete reading progress")
    public void testDeleteReadingProgress() {
        String bookId = createTestBook("Book to Delete Progress", "978-2222222222");

        // Add progress
        given()
            .contentType(ContentType.JSON)
            .body("{\"progress\": 0.5}")
            .when().put("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(200);

        // Delete progress
        given()
            .when().delete("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(204);

        // Verify deletion
        given()
            .when().get("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("Should return 404 for non-existent progress")
    public void testGetNonExistentProgress() {
        String bookId = createTestBook("Book Without Progress", "978-3333333333");

        given()
            .when().get("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(404)
            .body("error", containsString("Reading progress not found"));
    }

    @Test
    @DisplayName("Should update progress with notes")
    public void testUpdateProgressWithNotes() {
        String bookId = createTestBook("Book With Notes", "978-4444444444");
        
        String progressWithNotesJson = """
            {
                "progress": 0.75,
                "notes": "Really enjoying this book! The plot twist was unexpected."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(progressWithNotesJson)
            .when().put("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(200)
            .body("notes", equalTo("Really enjoying this book! The plot twist was unexpected."))
            .body("progress", notNullValue());
    }

    @Test
    @DisplayName("Should auto-mark as finished when progress reaches 1.0")
    public void testAutoMarkAsFinished() {
        String bookId = createTestBook("Book to Auto-Finish", "978-5555555555");

        // Update progress to 100%
        String progressJson = """
            {
                "progress": 1.0,
                "currentPage": 200,
                "totalPages": 200
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(progressJson)
            .when().put("/v1/books/" + bookId + "/progress")
            .then()
            .statusCode(200)
            .body("status", equalTo("FINISHED"))
            .body("isCompleted", equalTo(true))
            .body("finishedAt", notNullValue());
    }
}
