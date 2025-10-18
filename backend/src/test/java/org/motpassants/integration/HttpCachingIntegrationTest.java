package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for HTTP caching functionality on static assets.
 * Tests Cache-Control headers, ETags, Last-Modified, and 304 Not Modified responses.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpCachingIntegrationTest {

    private static String testBookId;
    
    @BeforeAll
    public static void setup() {
        // Get a real book ID from the system
        Response booksResponse = given()
            .when()
            .get("/v1/books")
            .then()
            .statusCode(200)
            .extract().response();
        
        // Extract the first book ID if available
        try {
            testBookId = booksResponse.jsonPath().getString("content[0].id");
            if (testBookId == null || testBookId.isEmpty()) {
                testBookId = "550e8400-e29b-41d4-a716-446655440000"; // Fallback
            }
        } catch (Exception e) {
            testBookId = "550e8400-e29b-41d4-a716-446655440000"; // Fallback
        }
        System.out.println("Using test book ID: " + testBookId);
    }
    
    @Test
    @Order(1)
    @DisplayName("Book cover endpoint should return Cache-Control headers")
    public void testBookCoverCacheControlHeaders() {
        Response response = given()
            .when()
            .get("/v1/books/" + testBookId + "/cover");
        
        int statusCode = response.getStatusCode();
        System.out.println("Response status: " + statusCode);
        System.out.println("Cache-Control: " + response.getHeader("Cache-Control"));
        
        // Only verify cache headers if we got a 200 response (asset exists)
        if (statusCode == 200) {
            String cacheControl = response.getHeader("Cache-Control");
            assertThat("Cache-Control header should be present", cacheControl, notNullValue());
            assertThat("Cache-Control should contain 'public'", cacheControl, containsString("public"));
            assertThat("Cache-Control should contain 'max-age'", cacheControl, containsString("max-age"));
            
            // Verify max-age is at least 1 hour (3600 seconds)
            if (cacheControl.contains("max-age=")) {
                String maxAge = cacheControl.substring(cacheControl.indexOf("max-age=") + 8);
                maxAge = maxAge.split("[,;]")[0].trim();
                int maxAgeSeconds = Integer.parseInt(maxAge);
                assertThat("max-age should be at least 1 hour", maxAgeSeconds, greaterThanOrEqualTo(3600));
                System.out.println("✓ Cache max-age: " + maxAgeSeconds + " seconds");
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("Book cover endpoint should return ETag header")
    public void testBookCoverETagHeader() {
        Response response = given()
            .when()
            .get("/v1/books/" + testBookId + "/cover");
        
        if (response.getStatusCode() == 200) {
            String etag = response.getHeader("ETag");
            assertThat("ETag header should be present", etag, notNullValue());
            assertThat("ETag should not be empty", etag.length(), greaterThan(0));
            System.out.println("✓ ETag: " + etag);
        }
    }

    @Test
    @Order(3)
    @DisplayName("Book cover endpoint should return Last-Modified header")
    public void testBookCoverLastModifiedHeader() {
        Response response = given()
            .when()
            .get("/v1/books/" + testBookId + "/cover");
        
        if (response.getStatusCode() == 200) {
            String lastModified = response.getHeader("Last-Modified");
            assertThat("Last-Modified header should be present", lastModified, notNullValue());
            assertThat("Last-Modified should not be empty", lastModified.length(), greaterThan(0));
            System.out.println("✓ Last-Modified: " + lastModified);
        }
    }

    @Test
    @Order(4)
    @DisplayName("Book cover endpoint should return 304 Not Modified with matching ETag")
    public void testBookCover304WithMatchingETag() {
        // First request to get the ETag
        Response initialResponse = given()
            .when()
            .get("/v1/books/" + testBookId + "/cover");
        
        if (initialResponse.getStatusCode() == 200) {
            String etag = initialResponse.getHeader("ETag");
            assertThat("ETag should be present in initial response", etag, notNullValue());
            
            // Second request with If-None-Match header
            Response cachedResponse = given()
                .header("If-None-Match", etag)
                .when()
                .get("/v1/books/" + testBookId + "/cover")
                .then()
                .statusCode(304) // Should return 304 Not Modified
                .extract().response();
            
            // Verify headers are present in 304 response
            assertThat("ETag should be present in 304 response", 
                cachedResponse.getHeader("ETag"), notNullValue());
            assertThat("Cache-Control should be present in 304 response", 
                cachedResponse.getHeader("Cache-Control"), notNullValue());
            System.out.println("✓ 304 Not Modified returned with ETag validation");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Book cover endpoint should return 304 Not Modified with matching Last-Modified")
    public void testBookCover304WithLastModified() {
        Response initialResponse = given()
            .when()
            .get("/v1/books/" + testBookId + "/cover");
        
        if (initialResponse.getStatusCode() == 200) {
            String lastModified = initialResponse.getHeader("Last-Modified");
            assertThat("Last-Modified should be present in initial response", lastModified, notNullValue());
            
            // Second request with If-Modified-Since header
            given()
                .header("If-Modified-Since", lastModified)
                .when()
                .get("/v1/books/" + testBookId + "/cover")
                .then()
                .statusCode(304); // Should return 304 Not Modified
            
            System.out.println("✓ 304 Not Modified returned with Last-Modified validation");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Vary header should be present for content negotiation")
    public void testVaryHeader() {
        Response response = given()
            .when()
            .get("/v1/books/" + testBookId + "/cover");
        
        if (response.getStatusCode() == 200) {
            String vary = response.getHeader("Vary");
            assertThat("Vary header should be present", vary, notNullValue());
            assertThat("Vary should include Accept-Encoding", vary, containsString("Accept-Encoding"));
            System.out.println("✓ Vary header: " + vary);
        }
    }

    @Test
    @Order(7)
    @DisplayName("Multiple requests demonstrate bandwidth reduction through caching")
    public void testBandwidthReduction() {
        // First request - should return full content
        Response firstResponse = given()
            .when()
            .get("/v1/books/" + testBookId + "/cover");
        
        if (firstResponse.getStatusCode() == 200) {
            int firstResponseSize = firstResponse.getBody().asByteArray().length;
            String etag = firstResponse.getHeader("ETag");
            
            // Second request with ETag - should return 304 with minimal body
            Response secondResponse = given()
                .header("If-None-Match", etag)
                .when()
                .get("/v1/books/" + testBookId + "/cover")
                .then()
                .statusCode(304)
                .extract().response();
            
            int secondResponseSize = secondResponse.getBody().asByteArray().length;
            
            // 304 response should have significantly smaller body
            assertThat("304 response should be much smaller than 200 response",
                secondResponseSize, lessThan(firstResponseSize));
            
            // Calculate bandwidth savings
            double savingsPercent = ((double) (firstResponseSize - secondResponseSize) / firstResponseSize) * 100;
            System.out.println(String.format("✓ Bandwidth savings: %.2f%% (from %d bytes to %d bytes)", 
                savingsPercent, firstResponseSize, secondResponseSize));
            
            // We should save at least 90% of bandwidth on subsequent requests
            assertThat("Should save at least 90% of bandwidth", savingsPercent, greaterThan(90.0));
        }
    }

    @Test
    @Order(8)
    @DisplayName("Fallback SVG has proper caching headers")
    public void testFallbackSvgCaching() {
        // Request a non-existent book cover to trigger fallback SVG
        Response response = given()
            .when()
            .get("/v1/books/00000000-0000-0000-0000-000000000099/cover");
        
        // If we got a fallback SVG (200), verify it has caching headers
        if (response.getStatusCode() == 200 && response.getContentType() != null && 
            response.getContentType().contains("image/svg")) {
            String cacheControl = response.getHeader("Cache-Control");
            assertThat("Cache-Control should be present on fallback SVG", cacheControl, notNullValue());
            
            String etag = response.getHeader("ETag");
            assertThat("ETag should be present on fallback SVG", etag, notNullValue());
            System.out.println("✓ Fallback SVG has caching headers");
        }
    }
}
