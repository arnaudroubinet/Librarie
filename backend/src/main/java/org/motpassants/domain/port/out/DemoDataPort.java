package org.motpassants.domain.port.out;

/**
 * Outbound port for seeding demo data from external sources (e.g., CSV files in the repository).
 * Implementations should be idempotent and safe to run at startup.
 */
public interface DemoDataPort {

    /**
     * Seed demo data into the database if necessary.
     * Implementations should handle locating the data folder and skip if data already exists.
     */
    void seed();
}
