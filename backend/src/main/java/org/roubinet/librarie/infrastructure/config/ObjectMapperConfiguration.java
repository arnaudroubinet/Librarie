package org.roubinet.librarie.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/**
 * Configuration for Jackson ObjectMapper.
 * Provides a singleton ObjectMapper instance for the application.
 */
@ApplicationScoped
public class ObjectMapperConfiguration {
    
    /**
     * Produces a singleton ObjectMapper instance configured for the application.
     * Thread-safe and optimized for performance.
     * 
     * @return configured ObjectMapper singleton
     */
    @Produces
    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule());
    }
}