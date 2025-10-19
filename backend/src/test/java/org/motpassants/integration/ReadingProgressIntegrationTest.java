package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Reading Progress functionality.
 * Tests the complete flow from REST API to database.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReadingProgressIntegrationTest {

    private static String testBookId;

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
    @Order(1)
    @DisplayName("Should update reading progress successfully")
    public void testUpdateReadingProgress() {
        testBookId = createTestBook("Test Book for Progress Tracking", "978-1234567890");
        
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
            .when().put("/v1/books/" + testBookId + "/progress")
            .then()
            .statusCode(200)
            .body("bookId", equalTo(testBookId))
            .body("currentPage", equalTo(50))
            .body("totalPages", equalTo(300))
            .body("progress", closeTo(0.167, 0.01))
            .body("progressPercentage", closeTo(16.7, 0.1))
            .body("status", equalTo("READING"))
            .body("isCompleted", equalTo(false))
            .body("startedAt", notNullValue())
            .body("lastReadAt", notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("Should get reading progress successfully")
    public void testGetReadingProgress() {
        given()
            .when().get("/v1/books/" + testBookId + "/progress")
            .then()
            .statusCode(200)
            .body("bookId", equalTo(testBookId))
            .body("currentPage", equalTo(50))
            .body("totalPages", equalTo(300))
            .body("status", equalTo("READING"));
    }

    @Test
    @Order(3)
    @DisplayName("Should mark book as started")
    public void testMarkAsStarted() {
        String newBookId = createTestBook("Book to Start", "978-9876543210");

        given()
            .when().post("/v1/books/" + newBookId + "/progress/start")
            .then()
            .statusCode(200)
            .body("status", equalTo("READING"))
            .body("startedAt", notNullValue())
            .body("isCompleted", equalTo(false));
    }

    @Test
    @Order(4)
    @DisplayName("Should mark book as finished")
    public void testMarkAsFinished() {
        given()
            .when().post("/v1/books/" + testBookId + "/progress/finish")
            .then()
            .statusCode(200)
            .body("status", equalTo("FINISHED"))
            .body("progress", equalTo(1.0f))
            .body("progressPercentage", equalTo(100.0f))
            .body("isCompleted", equalTo(true))
            .body("finishedAt", notNullValue())
            .body("currentPage", equalTo(300)); // Should be set to totalPages
    }

    @Test
    @Order(5)
    @DisplayName("Should mark book as DNF")
    public void testMarkAsDNF() {
        String dnfBookId = createTestBook("Book to DNF", "978-1111111111");

        // Start reading
        given()
            .contentType(ContentType.JSON)
            .body("{\"progress\": 0.3}")
            .when().put("/v1/books/" + dnfBookId + "/progress")
            .then()
            .statusCode(200);

        // Mark as DNF
        given()
            .when().post("/v1/books/" + dnfBookId + "/progress/dnf")
            .then()
            .statusCode(200)
            .body("status", equalTo("DNF"))
            .body("isCompleted", equalTo(false));
    }

    @Test
    @Order(6)
    @DisplayName("Should validate progress range")
    public void testValidateProgressRange() {
        String invalidProgressJson = """
            {
                "progress": 1.5
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(invalidProgressJson)
            .when().put("/v1/books/" + testBookId + "/progress")
            .then()
            .statusCode(400)
            .body("error", containsString("Progress must be between 0.0 and 1.0"));
    }

    @Test
    @Order(7)
    @DisplayName("Should delete reading progress")
    public void testDeleteReadingProgress() {
        String deleteBookId = createTestBook("Book to Delete Progress", "978-2222222222");

        // Add progress
        given()
            .contentType(ContentType.JSON)
            .body("{\"progress\": 0.5}")
            .when().put("/v1/books/" + deleteBookId + "/progress")
            .then()
            .statusCode(200);

        // Delete progress
        given()
            .when().delete("/v1/books/" + deleteBookId + "/progress")
            .then()
            .statusCode(204);

        // Verify deletion
        given()
            .when().get("/v1/books/" + deleteBookId + "/progress")
            .then()
            .statusCode(404);
    }

    @Test
    @Order(8)
    @DisplayName("Should return 404 for non-existent progress")
    public void testGetNonExistentProgress() {
        String noProgressBookId = createTestBook("Book Without Progress", "978-3333333333");

        given()
            .when().get("/v1/books/" + noProgressBookId + "/progress")
            .then()
            .statusCode(404)
            .body("error", containsString("Reading progress not found"));
    }

    @Test
    @Order(9)
    @DisplayName("Should update progress with notes")
    public void testUpdateProgressWithNotes() {
        String notesBookId = createTestBook("Book With Notes", "978-4444444444");
        
        String progressWithNotesJson = """
            {
                "progress": 0.75,
                "notes": "Really enjoying this book! The plot twist was unexpected."
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(progressWithNotesJson)
            .when().put("/v1/books/" + notesBookId + "/progress")
            .then()
            .statusCode(200)
            .body("notes", equalTo("Really enjoying this book! The plot twist was unexpected."))
            .body("progress", closeTo(0.75, 0.01));
    }

    @Test
    @Order(10)
    @DisplayName("Should auto-mark as finished when progress reaches 1.0")
    public void testAutoMarkAsFinished() {
        String autoFinishBookId = createTestBook("Book to Auto-Finish", "978-5555555555");

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
            .when().put("/v1/books/" + autoFinishBookId + "/progress")
            .then()
            .statusCode(200)
            .body("status", equalTo("FINISHED"))
            .body("isCompleted", equalTo(true))
            .body("finishedAt", notNullValue());
    }
}
