package org.motpassants.infrastructure.adapter.out.metadata.googlebooks;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.motpassants.domain.core.model.MetadataSearchResult;
import org.motpassants.domain.core.model.MetadataSource;
import org.motpassants.domain.port.out.MetadataProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Google Books API implementation of MetadataProviderPort.
 * Retrieves book metadata from Google Books API.
 */
@ApplicationScoped
public class GoogleBooksMetadataProvider implements MetadataProviderPort {

    private static final Logger log = LoggerFactory.getLogger(GoogleBooksMetadataProvider.class);
    private static final int MAX_RESULTS = 10;
    private static final int HIGH_PRIORITY = 10; // Google Books is a primary source

    @Inject
    @RestClient
    GoogleBooksApiClient apiClient;

    @ConfigProperty(name = "metadata.google.enabled", defaultValue = "true")
    boolean enabled;

    @ConfigProperty(name = "metadata.google.api-key")
    java.util.Optional<String> apiKey;

    @Override
    public CompletableFuture<List<MetadataSearchResult>> searchByIsbn(String isbn) {
        if (!enabled) {
            log.debug("Google Books provider is disabled");
            return CompletableFuture.completedFuture(List.of());
        }

        if (isbn == null || isbn.trim().isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        String cleanIsbn = isbn.replaceAll("[^0-9X]", "");
        String query = "isbn:" + cleanIsbn;
        
        log.info("Searching Google Books by ISBN: {}", cleanIsbn);
        
        return apiClient.searchBooks(query, MAX_RESULTS, apiKey.orElse(null))
                .toCompletableFuture()
                .thenApply(this::convertResponse)
                .exceptionally(throwable -> {
                    log.error("Error searching Google Books by ISBN: {}", isbn, throwable);
                    return List.of();
                });
    }

    @Override
    public CompletableFuture<List<MetadataSearchResult>> searchByTitleAndAuthor(String title, String author) {
        if (!enabled) {
            log.debug("Google Books provider is disabled");
            return CompletableFuture.completedFuture(List.of());
        }

        if (title == null || title.trim().isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("intitle:").append(title);
        
        if (author != null && !author.trim().isEmpty()) {
            queryBuilder.append("+inauthor:").append(author);
        }

        String query = queryBuilder.toString();
        log.info("Searching Google Books by title/author: {}", query);

        return apiClient.searchBooks(query, MAX_RESULTS, apiKey.orElse(null))
                .toCompletableFuture()
                .thenApply(this::convertResponse)
                .exceptionally(throwable -> {
                    log.error("Error searching Google Books by title/author: {}", query, throwable);
                    return List.of();
                });
    }

    @Override
    public String getProviderName() {
        return "Google Books";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getPriority() {
        return HIGH_PRIORITY;
    }

    /**
     * Convert Google Books API response to domain model.
     */
    private List<MetadataSearchResult> convertResponse(GoogleBooksResponse response) {
        if (response == null || response.getItems() == null) {
            return List.of();
        }

        List<MetadataSearchResult> results = new ArrayList<>();
        for (GoogleBooksVolume volume : response.getItems()) {
            try {
                MetadataSearchResult result = convertVolume(volume);
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                log.warn("Error converting Google Books volume: {}", volume.getId(), e);
            }
        }

        return results;
    }

    /**
     * Convert a single Google Books volume to domain model.
     */
    private MetadataSearchResult convertVolume(GoogleBooksVolume volume) {
        if (volume == null || volume.getVolumeInfo() == null) {
            return null;
        }

        GoogleBooksVolumeInfo info = volume.getVolumeInfo();
        
        MetadataSearchResult.Builder builder = MetadataSearchResult.builder()
                .source(MetadataSource.GOOGLE_BOOKS)
                .providerBookId(volume.getId())
                .title(info.getTitle())
                .subtitle(info.getSubtitle())
                .description(info.getDescription())
                .pageCount(info.getPageCount())
                .publisher(info.getPublisher())
                .language(info.getLanguage())
                .averageRating(info.getAverageRating())
                .ratingsCount(info.getRatingsCount());

        // Add authors
        if (info.getAuthors() != null) {
            builder.authors(info.getAuthors());
        }

        // Add categories
        if (info.getCategories() != null) {
            builder.categories(info.getCategories());
        }

        // Extract ISBNs
        if (info.getIndustryIdentifiers() != null) {
            for (GoogleBooksIndustryIdentifier id : info.getIndustryIdentifiers()) {
                if ("ISBN_10".equals(id.getType())) {
                    builder.isbn10(id.getIdentifier());
                } else if ("ISBN_13".equals(id.getType())) {
                    builder.isbn13(id.getIdentifier());
                }
            }
        }

        // Parse published date
        if (info.getPublishedDate() != null) {
            builder.publishedDate(parsePublishedDate(info.getPublishedDate()));
        }

        // Get best cover image URL
        if (info.getImageLinks() != null) {
            builder.coverImageUrl(info.getImageLinks().getBestImageUrl());
        }

        // Calculate confidence score based on data completeness
        builder.confidenceScore(calculateConfidenceScore(info));

        return builder.build();
    }

    /**
     * Parse published date which can be in various formats:
     * - "2020-01-15" (full date)
     * - "2020-01" (year-month)
     * - "2020" (year only)
     */
    private LocalDate parsePublishedDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Try full date first
            if (dateStr.length() == 10) {
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            // Try year-month
            if (dateStr.length() == 7) {
                return LocalDate.parse(dateStr + "-01", DateTimeFormatter.ISO_LOCAL_DATE);
            }
            // Try year only
            if (dateStr.length() == 4) {
                return LocalDate.parse(dateStr + "-01-01", DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            log.warn("Unexpected date format from Google Books: {}", dateStr);
            return null;
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse Google Books date: {}", dateStr, e);
            return null;
        }
    }

    /**
     * Calculate confidence score based on data completeness.
     * More complete data = higher confidence.
     */
    private double calculateConfidenceScore(GoogleBooksVolumeInfo info) {
        double score = 0.5; // Base score

        if (info.getTitle() != null && !info.getTitle().isEmpty()) score += 0.1;
        if (info.getAuthors() != null && !info.getAuthors().isEmpty()) score += 0.1;
        if (info.getIndustryIdentifiers() != null && !info.getIndustryIdentifiers().isEmpty()) score += 0.1;
        if (info.getDescription() != null && !info.getDescription().isEmpty()) score += 0.05;
        if (info.getPublisher() != null && !info.getPublisher().isEmpty()) score += 0.05;
        if (info.getPageCount() != null && info.getPageCount() > 0) score += 0.05;
        if (info.getImageLinks() != null) score += 0.05;

        return Math.min(score, 1.0);
    }
}
