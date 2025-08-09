package org.roubinet.librarie;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class HealthEndpointTest {

    @Test
    public void testHealthEndpoint() {
        given()
          .when().get("/q/health")
          .then()
             .statusCode(200)
             .body("status", is("UP"));
    }
}