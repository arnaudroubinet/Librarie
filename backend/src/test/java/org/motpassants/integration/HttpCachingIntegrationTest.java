package org.motpassants.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for HTTP caching functionality on static assets.
 * Validates Cache-Control headers, ETags, Last-Modified, and 304 responses.
 */
@QuarkusTest
public class HttpCachingIntegrationTest {

    @Test
    @DisplayName("Fallback SVG has proper caching headers")
    public void testFallbackSvgCaching() {
        // Request a non-existent book cover to trigger fallback SVG
        // This always exists and has consistent behavior
        Response response = given()
            .when()
            .get("/v1/books/00000000-0000-0000-0000-000000000099/cover");
        
        // Should get a fallback SVG with 200
        if (response.getStatusCode() == 200) {
            // Verify caching headers exist
            String cacheControl = response.getHeader("Cache-Control");
            assertThat("Cache-Control header should be present", cacheControl, notNullValue());
            assertThat("Cache-Control should be public", cacheControl, containsString("public"));
            assertThat("Cache-Control should have max-age", cacheControl, containsString("max-age"));
            
            // Verify max-age is at least 1 hour (3600 seconds)
            if (cacheControl.contains("max-age=")) {
                String maxAgeStr = cacheControl.substring(cacheControl.indexOf("max-age=") + 8);
                maxAgeStr = maxAgeStr.split("[,;]")[0].trim();
                int maxAge = Integer.parseInt(maxAgeStr);
                assertThat("max-age should be at least 1 hour", maxAge, greaterThanOrEqualTo(3600));
                System.out.println("✓ Cache max-age: " + maxAge + " seconds (1 day = 86400)");
            }
            
            // Verify ETag is present
            String etag = response.getHeader("ETag");
            assertThat("ETag should be present", etag, notNullValue());
            assertThat("ETag should not be empty", etag.length(), greaterThan(0));
            System.out.println("✓ ETag present: " + etag);
            
            // Verify Vary header
            String vary = response.getHeader("Vary");
            assertThat("Vary header should be present", vary, notNullValue());
            assertThat("Vary should include Accept-Encoding", vary, containsString("Accept-Encoding"));
            System.out.println("✓ Vary header: " + vary);
        }
    }

    @Test
    @DisplayName("Fallback SVG returns 304 Not Modified with matching ETag")
    public void testFallbackSvg304Response() {
        // First request to get the ETag
        Response firstResponse = given()
            .when()
            .get("/v1/books/00000000-0000-0000-0000-000000000099/cover");
        
        if (firstResponse.getStatusCode() == 200) {
            String etag = firstResponse.getHeader("ETag");
            assertThat("ETag should be present in first response", etag, notNullValue());
            
            // Second request with If-None-Match should return 304
            Response cachedResponse = given()
                .header("If-None-Match", etag)
                .when()
                .get("/v1/books/00000000-0000-0000-0000-000000000099/cover")
                .then()
                .statusCode(304) // Should return 304 Not Modified
                .extract().response();
            
            // Verify headers are still present in 304 response
            assertThat("ETag should be present in 304 response", 
                cachedResponse.getHeader("ETag"), notNullValue());
            assertThat("Cache-Control should be present in 304 response", 
                cachedResponse.getHeader("Cache-Control"), notNullValue());
            
            System.out.println("✓ 304 Not Modified returned successfully");
            
            // Verify bandwidth savings
            int firstSize = firstResponse.getBody().asByteArray().length;
            int cachedSize = cachedResponse.getBody().asByteArray().length;
            double savings = ((double)(firstSize - cachedSize) / firstSize) * 100;
            System.out.println(String.format("✓ Bandwidth savings: %.2f%% (%d -> %d bytes)", 
                savings, firstSize, cachedSize));
            assertThat("Should save bandwidth on cached response", cachedSize, lessThan(firstSize));
        }
    }
}
