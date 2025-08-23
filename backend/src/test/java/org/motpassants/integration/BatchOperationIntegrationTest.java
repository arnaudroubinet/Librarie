package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.infrastructure.adapter.in.rest.dto.BatchDeleteRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.BatchEditRequestDto;
import org.motpassants.infrastructure.adapter.in.rest.dto.BatchOperationRequestDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for batch operations API endpoints.
 */
//@Test
class BatchOperationIntegrationTest {
    
    @Inject
    BookRepository bookRepository;
    
    private UUID testBookId1;
    private UUID testBookId2;
    
    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        bookRepository.findAll(null, 100, null).getItems()
            .forEach(book -> bookRepository.deleteById(book.getId()));
        
        // Create test books
        testBookId1 = createTestBook("Test Book 1", "Description 1", "en");
        testBookId2 = createTestBook("Test Book 2", "Description 2", "fr");
    }
    
    @Test
    void executeBatchEdit_WithValidRequest_ShouldUpdateBooks() {
        // Arrange
        var editRequest = new BatchEditRequestDto(
            "New Title",        // title
            null,               // subtitle
            "New Description",  // description
            null,               // authors
            null,               // seriesId
            null,               // seriesPosition
            null,               // tags
            "es",               // language
            null,               // publisher
            null,               // isbn
            null,               // addTags
            null                // addAuthors
        );
        
        var batchRequest = new BatchOperationRequestDto(
            List.of(testBookId1, testBookId2),
            editRequest
        );
        
        // Act
        Response response = given()
            .contentType("application/json")
            .body(batchRequest)
            .when()
            .post("/api/batch/edit")
            .then()
            .statusCode(200)
            .body("operationId", notNullValue())
            .body("type", equalTo("EDIT"))
            .body("status", equalTo("COMPLETED"))
            .body("totalCount", equalTo(2))
            .body("successCount", equalTo(2))
            .body("failureCount", equalTo(0))
            .body("results", hasSize(2))
            .extract().response();
        
        // Assert - Verify books were actually updated
        var updatedBook1 = bookRepository.findById(testBookId1);
        assertTrue(updatedBook1.isPresent());
        assertEquals("New Title", updatedBook1.get().getTitle());
        assertEquals("New Description", updatedBook1.get().getDescription());
        assertEquals("es", updatedBook1.get().getLanguage());
        
        var updatedBook2 = bookRepository.findById(testBookId2);
        assertTrue(updatedBook2.isPresent());
        assertEquals("New Title", updatedBook2.get().getTitle());
        assertEquals("New Description", updatedBook2.get().getDescription());
        assertEquals("es", updatedBook2.get().getLanguage());
    }
    
    @Test
    void executeBatchEdit_WithEmptyBookIds_ShouldReturnBadRequest() {
        // Arrange
        var editRequest = new BatchEditRequestDto(
            "New Title", null, null, null, null, null, null, null, null, null, null, null
        );
        
        var batchRequest = new BatchOperationRequestDto(
            List.of(), // Empty book IDs
            editRequest
        );
        
        // Act & Assert
        given()
            .contentType("application/json")
            .body(batchRequest)
            .when()
            .post("/api/batch/edit")
            .then()
            .statusCode(400)
            .body("message", containsString("Book IDs list cannot be empty"));
    }
    
    @Test
    void executeBatchEdit_WithNonExistentBook_ShouldReturnPartialSuccess() {
        // Arrange
        var nonExistentBookId = UUID.randomUUID();
        var editRequest = new BatchEditRequestDto(
            "New Title", null, null, null, null, null, null, null, null, null, null, null
        );
        
        var batchRequest = new BatchOperationRequestDto(
            List.of(testBookId1, nonExistentBookId),
            editRequest
        );
        
        // Act & Assert
        given()
            .contentType("application/json")
            .body(batchRequest)
            .when()
            .post("/api/batch/edit")
            .then()
            .statusCode(200)
            .body("status", equalTo("PARTIAL"))
            .body("totalCount", equalTo(2))
            .body("successCount", equalTo(1))
            .body("failureCount", equalTo(1))
            .body("results", hasSize(2))
            .body("results[0].success", equalTo(true))
            .body("results[1].success", equalTo(false))
            .body("results[1].errorMessage", equalTo("Book not found"));
    }
    
    @Test
    void executeBatchDelete_WithValidRequest_ShouldDeleteBooks() {
        // Arrange
        var deleteRequest = new BatchDeleteRequestDto(
            List.of(testBookId1, testBookId2)
        );
        
        // Act
        given()
            .contentType("application/json")
            .body(deleteRequest)
            .when()
            .post("/api/batch/delete")
            .then()
            .statusCode(200)
            .body("operationId", notNullValue())
            .body("type", equalTo("DELETE"))
            .body("status", equalTo("COMPLETED"))
            .body("totalCount", equalTo(2))
            .body("successCount", equalTo(2))
            .body("failureCount", equalTo(0))
            .body("results", hasSize(2));
        
        // Assert - Verify books were actually deleted
        var deletedBook1 = bookRepository.findById(testBookId1);
        assertTrue(deletedBook1.isEmpty());
        
        var deletedBook2 = bookRepository.findById(testBookId2);
        assertTrue(deletedBook2.isEmpty());
    }
    
    @Test
    void previewBatchEdit_WithValidRequest_ShouldReturnPreview() {
        // Arrange
        var editRequest = new BatchEditRequestDto(
            "New Title", null, "New Description", null, null, null, null, "es", null, null, null, null
        );
        
        var batchRequest = new BatchOperationRequestDto(
            List.of(testBookId1, testBookId2),
            editRequest
        );
        
        // Act & Assert
        given()
            .contentType("application/json")
            .body(batchRequest)
            .when()
            .post("/api/batch/preview")
            .then()
            .statusCode(200)
            .body("$", hasSize(2))
            .body("[0].bookId", equalTo(testBookId1.toString()))
            .body("[0].currentTitle", equalTo("Test Book 1"))
            .body("[0].newTitle", equalTo("New Title"))
            .body("[0].currentDescription", equalTo("Description 1"))
            .body("[0].newDescription", equalTo("New Description"))
            .body("[0].currentLanguage", equalTo("en"))
            .body("[0].newLanguage", equalTo("es"))
            .body("[0].hasChanges", equalTo(true))
            .body("[1].bookId", equalTo(testBookId2.toString()))
            .body("[1].currentTitle", equalTo("Test Book 2"))
            .body("[1].newTitle", equalTo("New Title"))
            .body("[1].hasChanges", equalTo(true));
    }
    
    @Test
    void getBatchOperation_WithValidOperationId_ShouldReturnOperation() {
        // Arrange - First create a batch operation
        var editRequest = new BatchEditRequestDto(
            "Test Title", null, null, null, null, null, null, null, null, null, null, null
        );
        
        var batchRequest = new BatchOperationRequestDto(
            List.of(testBookId1),
            editRequest
        );
        
        Response createResponse = given()
            .contentType("application/json")
            .body(batchRequest)
            .when()
            .post("/api/batch/edit")
            .then()
            .statusCode(200)
            .extract().response();
        
        String operationId = createResponse.jsonPath().getString("operationId");
        
        // Act & Assert
        given()
            .when()
            .get("/api/batch/operations/{operationId}", operationId)
            .then()
            .statusCode(200)
            .body("operationId", equalTo(operationId))
            .body("type", equalTo("EDIT"))
            .body("status", equalTo("COMPLETED"));
    }
    
    @Test
    void getBatchOperation_WithInvalidOperationId_ShouldReturnNotFound() {
        // Arrange
        var invalidOperationId = UUID.randomUUID();
        
        // Act & Assert
        given()
            .when()
            .get("/api/batch/operations/{operationId}", invalidOperationId)
            .then()
            .statusCode(404)
            .body("message", equalTo("Batch operation not found"));
    }
    
    @Test
    void getRecentBatchOperations_ShouldReturnOperations() {
        // Arrange - Create a batch operation first
        var editRequest = new BatchEditRequestDto(
            "Test Title", null, null, null, null, null, null, null, null, null, null, null
        );
        
        var batchRequest = new BatchOperationRequestDto(
            List.of(testBookId1),
            editRequest
        );
        
        given()
            .contentType("application/json")
            .body(batchRequest)
            .when()
            .post("/api/batch/edit");
        
        // Act & Assert
        given()
            .when()
            .get("/api/batch/operations")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].operationId", notNullValue())
            .body("[0].type", equalTo("EDIT"));
    }
    
    @Transactional
    UUID createTestBook(String title, String description, String language) {
        var book = new Book();
        book.setId(UUID.randomUUID());
        book.setTitle(title);
        book.setDescription(description);
        book.setLanguage(language);
        book.setPath("/books/" + book.getId() + ".epub");
        book.setCreatedAt(OffsetDateTime.now());
        book.setUpdatedAt(OffsetDateTime.now());
        
        var savedBook = bookRepository.save(book);
        return savedBook.getId();
    }
}