package org.roubinet.librarie;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class DevProfileConfigurationTest {

    @Inject
    @ConfigProperty(name = "quarkus.smallrye-openapi.enable")
    boolean openApiEnabled;

    @Inject
    @ConfigProperty(name = "quarkus.swagger-ui.enable")
    boolean swaggerUiEnabled;

    @Test
    void testDevProfileOpenApiEnabled() {
        // In dev profile (default), OpenAPI and Swagger should be enabled
        assertTrue(openApiEnabled, "OpenAPI should be enabled in dev profile");
        assertTrue(swaggerUiEnabled, "Swagger UI should be enabled in dev profile");
    }

    public static class ProdProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("quarkus.profile", "prod");
        }
    }

    @QuarkusTest
    @TestProfile(ProdProfile.class)
    static class ProdProfileConfigurationTest {

        @Inject
        @ConfigProperty(name = "quarkus.smallrye-openapi.enable")
        boolean openApiEnabled;

        @Inject
        @ConfigProperty(name = "quarkus.swagger-ui.enable")
        boolean swaggerUiEnabled;

        @Test
        void testProdProfileOpenApiDisabled() {
            // In prod profile, OpenAPI and Swagger should be disabled
            assertFalse(openApiEnabled, "OpenAPI should be disabled in prod profile");
            assertFalse(swaggerUiEnabled, "Swagger UI should be disabled in prod profile");
        }
    }
}