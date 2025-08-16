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
 * Integration tests for UnifiedSearch functionality in hexagonal architecture.
 * Tests the complete flow from REST API to database with DDD approach.
 * Based on backend-copy UnifiedSearchController endpoints.
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnifiedSearchIntegrationTest {

    @Test
    @Order(1)
    public void testUnifiedSearchBasic() {
        given()
            .queryParam("q", "test")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue())
            .body("totalResults", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(2)
    public void testUnifiedSearchWithLimit() {
        given()
            .queryParam("q", "test")
            .queryParam("limit", 5)
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", hasSize(lessThanOrEqualTo(5)))
            .body("authors", hasSize(lessThanOrEqualTo(5)))
            .body("series", hasSize(lessThanOrEqualTo(5)));
    }

    @Test
    @Order(3)
    public void testUnifiedSearchWithEntityTypeFilter() {
        given()
            .queryParam("q", "test")
            .queryParam("types", "books")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", hasSize(0))
            .body("series", hasSize(0));
    }

    @Test
    @Order(4)
    public void testUnifiedSearchWithMultipleEntityTypes() {
        given()
            .queryParam("q", "test")
            .queryParam("types", "books,authors")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", hasSize(0));
    }

    @Test
    @Order(5)
    public void testUnifiedSearchPostEndpoint() {
        String searchRequestJson = """
            {
                "query": "test",
                "types": ["books", "authors", "series"],
                "limit": 10
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(searchRequestJson)
            .when().post("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue())
            .body("totalResults", greaterThanOrEqualTo(0));
    }

    @Test
    @Order(6)
    public void testUnifiedSearchWithSorting() {
        given()
            .queryParam("q", "test")
            .queryParam("sortBy", "relevance")
            .queryParam("sortOrder", "desc")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue());
    }

    @Test
    @Order(7)
    public void testUnifiedSearchWithPagination() {
        given()
            .queryParam("q", "test")
            .queryParam("offset", 0)
            .queryParam("limit", 5)
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", hasSize(lessThanOrEqualTo(5)))
            .body("authors", hasSize(lessThanOrEqualTo(5)))
            .body("series", hasSize(lessThanOrEqualTo(5)))
            .body("pagination", notNullValue())
            .body("pagination.offset", equalTo(0))
            .body("pagination.limit", equalTo(5));
    }

    @Test
    @Order(8)
    public void testUnifiedSearchEmptyQuery() {
        given()
            .queryParam("q", "")
            .when().get("/v1/search")
            .then()
            .statusCode(400)
            .body("message", containsString("query"));
    }

    @Test
    @Order(9)
    public void testUnifiedSearchMissingQuery() {
        given()
            .when().get("/v1/search")
            .then()
            .statusCode(400)
            .body("message", containsString("query"));
    }

    @Test
    @Order(10)
    public void testUnifiedSearchInvalidEntityType() {
        given()
            .queryParam("q", "test")
            .queryParam("types", "invalid_type")
            .when().get("/v1/search")
            .then()
            .statusCode(400)
            .body("message", containsString("Invalid entity type"));
    }

    @Test
    @Order(11)
    public void testUnifiedSearchWithExactMatch() {
        given()
            .queryParam("q", "\"exact phrase\"")
            .queryParam("exactMatch", true)
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue());
    }

    @Test
    @Order(12)
    public void testUnifiedSearchWithWildcard() {
        given()
            .queryParam("q", "test*")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue());
    }

    @Test
    @Order(13)
    public void testUnifiedSearchResponseStructure() {
        given()
            .queryParam("q", "test")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("$", hasKey("books"))
            .body("$", hasKey("authors"))
            .body("$", hasKey("series"))
            .body("$", hasKey("totalResults"))
            .body("$", hasKey("searchTime"))
            .body("$", hasKey("query"));
    }

    @Test
    @Order(14)
    public void testUnifiedSearchLongQuery() {
        String longQuery = "a".repeat(1000); // Very long query
        given()
            .queryParam("q", longQuery)
            .when().get("/v1/search")
            .then()
            .statusCode(anyOf(equalTo(200), equalTo(400))); // Either works or rejected for being too long
    }

    @Test
    @Order(15)
    public void testUnifiedSearchSpecialCharacters() {
        given()
            .queryParam("q", "test!@#$%^&*()")
            .when().get("/v1/search")
            .then()
            .statusCode(200)
            .body("books", notNullValue())
            .body("authors", notNullValue())
            .body("series", notNullValue());
    }
}