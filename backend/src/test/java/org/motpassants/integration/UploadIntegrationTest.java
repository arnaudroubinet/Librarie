package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

/**
 * Integration tests for upload functionality.
 * Tests DATA-001: Upload & Automated Ingest Pipeline REST API.
 */
@QuarkusTest
public class UploadIntegrationTest {

    @Test
    public void testGetUploadConfig() {
        given()
            .when().get("/api/upload/config")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("maxFileSize", equalTo(104857600))
                .body("allowedExtensions", hasItems("pdf", "epub", "mobi", "azw", "azw3", "fb2", "txt", "rtf", "doc", "docx"));
    }

    @Test
    public void testUploadBookWithoutFile() {
        given()
            .contentType(ContentType.MULTIPART)
            .when().post("/api/upload/book")
            .then()
                .statusCode(400)
                .body("message", containsString("file"));
    }

    @Test
    public void testValidateFileWithoutFile() {
        given()
            .contentType(ContentType.MULTIPART)
            .when().post("/api/upload/validate")
            .then()
                .statusCode(400)
                .body("message", containsString("File is required"));
    }

    @Test
    public void testUploadConfigEndpointReturnsCorrectConfiguration() {
        given()
            .when().get("/api/upload/config")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("maxFileSize", notNullValue())
                .body("allowedExtensions", notNullValue())
                .body("allowedExtensions.size()", org.hamcrest.Matchers.greaterThan(0));
    }

    // Note: Testing actual file uploads would require creating test files
    // and handling multipart uploads properly. These tests focus on the
    // API structure and error handling for now.
}