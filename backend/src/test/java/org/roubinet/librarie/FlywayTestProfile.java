package org.roubinet.librarie;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

/**
 * Test profile that enables Flyway for testing database migrations.
 */
public class FlywayTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "quarkus.flyway.migrate-at-start", "true",
            "quarkus.flyway.baseline-on-migrate", "true",
            "quarkus.flyway.baseline-version", "1",
            "quarkus.flyway.baseline-description", "Initial baseline",
            "quarkus.hibernate-orm.database.generation", "validate"
        );
    }
}