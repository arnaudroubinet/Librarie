package org.motpassants.infrastructure.adapter.out.metadata.openlibrary;

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
 * Open Library API implementation of MetadataProviderPort.
 * Retrieves book metadata from Open Library API.
 */
@ApplicationScoped
public class OpenLibraryMetadataProvider implements MetadataProviderPort {

    private static final Logger log = LoggerFactory.getLogger(OpenLibraryMetadataProvider.class);
    private static final int MAX_RESULTS = 10;
    private static final int SECONDARY_PRIORITY = 20; // Open Library as secondary source

    @Inject
    @RestClient
    OpenLibraryApiClient apiClient;

    @ConfigProperty(name = "metadata.open-library.enabled", defaultValue = "true")
    boolean enabled;

    @Override
    public CompletableFuture<List<MetadataSearchResult>> searchByIsbn(String isbn) {
        if (!enabled) {
            log.debug("Open Library provider is disabled");
            return CompletableFuture.completedFuture(List.of());
        }

        if (isbn == null || isbn.trim().isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        String cleanIsbn = isbn.replaceAll("[^0-9X]", "");
        log.info("Searching Open Library by ISBN: {}", cleanIsbn);

        return apiClient.searchByIsbn(cleanIsbn, MAX_RESULTS)
                .toCompletableFuture()
                .thenApply(this::convertResponse)
                .exceptionally(throwable -> {
                    log.error("Error searching Open Library by ISBN: {}", isbn, throwable);
                    return List.of();
                });
    }

    @Override
    public CompletableFuture<List<MetadataSearchResult>> searchByTitleAndAuthor(String title, String author) {
        if (!enabled) {
            log.debug("Open Library provider is disabled");
            return CompletableFuture.completedFuture(List.of());
        }

        if (title == null || title.trim().isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(title);

        if (author != null && !author.trim().isEmpty()) {
            queryBuilder.append(" ").append(author);
        }

        String query = queryBuilder.toString();
        log.info("Searching Open Library by title/author: {}", query);

        return apiClient.searchBooks(query, MAX_RESULTS)
                .toCompletableFuture()
                .thenApply(this::convertResponse)
                .exceptionally(throwable -> {
                    log.error("Error searching Open Library by title/author: {}", query, throwable);
                    return List.of();
                });
    }

    @Override
    public String getProviderName() {
        return "Open Library";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getPriority() {
        return SECONDARY_PRIORITY;
    }

    /**
     * Convert Open Library API response to domain model.
     */
    private List<MetadataSearchResult> convertResponse(OpenLibrarySearchResponse response) {
        if (response == null || response.getDocs() == null) {
            return List.of();
        }

        List<MetadataSearchResult> results = new ArrayList<>();
        for (OpenLibraryDoc doc : response.getDocs()) {
            try {
                MetadataSearchResult result = convertDoc(doc);
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                log.warn("Error converting Open Library doc: {}", doc.getKey(), e);
            }
        }

        return results;
    }

    /**
     * Convert a single Open Library document to domain model.
     */
    private MetadataSearchResult convertDoc(OpenLibraryDoc doc) {
        if (doc == null) {
            return null;
        }

        MetadataSearchResult.Builder builder = MetadataSearchResult.builder()
                .source(MetadataSource.OPEN_LIBRARY)
                .providerBookId(doc.getKey())
                .title(doc.getTitle())
                .subtitle(doc.getSubtitle())
                .pageCount(doc.getNumberOfPagesMedian());

        // Add authors
        if (doc.getAuthorName() != null && !doc.getAuthorName().isEmpty()) {
            builder.authors(doc.getAuthorName());
        }

        // Add subjects as categories
        if (doc.getSubject() != null && !doc.getSubject().isEmpty()) {
            // Limit categories to first 5 to avoid overwhelming results
            List<String> limitedSubjects = doc.getSubject().stream()
                    .limit(5)
                    .toList();
            builder.categories(limitedSubjects);
        }

        // Extract ISBNs - Open Library returns them in a mixed list
        if (doc.getIsbn() != null && !doc.getIsbn().isEmpty()) {
            extractIsbns(doc.getIsbn(), builder);
        }

        // Use first publisher if available
        if (doc.getPublisher() != null && !doc.getPublisher().isEmpty()) {
            builder.publisher(doc.getPublisher().get(0));
        }

        // Use first sentence as description if available
        if (doc.getFirstSentence() != null && !doc.getFirstSentence().isEmpty()) {
            builder.description(doc.getFirstSentence().get(0));
        }

        // Parse published date - try publish year first
        if (doc.getPublishYear() != null && !doc.getPublishYear().isEmpty()) {
            Integer year = doc.getPublishYear().get(0);
            if (year != null && year > 0) {
                builder.publishedDate(LocalDate.of(year, 1, 1));
            }
        } else if (doc.getPublishDate() != null && !doc.getPublishDate().isEmpty()) {
            builder.publishedDate(parsePublishDate(doc.getPublishDate().get(0)));
        }

        // Use first language if available
        if (doc.getLanguage() != null && !doc.getLanguage().isEmpty()) {
            builder.language(doc.getLanguage().get(0));
        }

        // Get cover image URL
        String coverUrl = doc.getCoverImageUrl();
        if (coverUrl != null) {
            builder.coverImageUrl(coverUrl);
        }

        // Calculate confidence score
        builder.confidenceScore(calculateConfidenceScore(doc));

        return builder.build();
    }

    /**
     * Extract ISBN-10 and ISBN-13 from mixed list of ISBNs.
     */
    private void extractIsbns(List<String> isbns, MetadataSearchResult.Builder builder) {
        for (String isbn : isbns) {
            if (isbn == null) continue;
            
            String cleanIsbn = isbn.replaceAll("[^0-9X]", "");
            
            if (cleanIsbn.length() == 10) {
                builder.isbn10(cleanIsbn);
            } else if (cleanIsbn.length() == 13) {
                builder.isbn13(cleanIsbn);
            }
            
            // Stop after finding one of each type
            if (builder.build().getIsbn10() != null && builder.build().getIsbn13() != null) {
                break;
            }
        }
    }

    /**
     * Parse published date which can be in various formats.
     * Open Library dates are often human-readable strings like "January 1997" or "1997".
     */
    private LocalDate parsePublishDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Try common formats
            String normalized = dateStr.trim();
            
            // Full date: "2020-01-15"
            if (normalized.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            // Year only: "2020"
            if (normalized.matches("\\d{4}")) {
                return LocalDate.of(Integer.parseInt(normalized), 1, 1);
            }
            
            // Month Year: "January 2020", "Jan 2020"
            if (normalized.matches("\\w+ \\d{4}")) {
                try {
                    return LocalDate.parse(normalized, DateTimeFormatter.ofPattern("MMMM yyyy"));
                } catch (DateTimeParseException e) {
                    return LocalDate.parse(normalized, DateTimeFormatter.ofPattern("MMM yyyy"));
                }
            }
            
            log.warn("Unexpected date format from Open Library: {}", dateStr);
            return null;
        } catch (Exception e) {
            log.warn("Failed to parse Open Library date: {}", dateStr, e);
            return null;
        }
    }

    /**
     * Calculate confidence score based on data completeness.
     */
    private double calculateConfidenceScore(OpenLibraryDoc doc) {
        double score = 0.5; // Base score

        if (doc.getTitle() != null && !doc.getTitle().isEmpty()) score += 0.1;
        if (doc.getAuthorName() != null && !doc.getAuthorName().isEmpty()) score += 0.1;
        if (doc.getIsbn() != null && !doc.getIsbn().isEmpty()) score += 0.1;
        if (doc.getPublisher() != null && !doc.getPublisher().isEmpty()) score += 0.05;
        if (doc.getNumberOfPagesMedian() != null && doc.getNumberOfPagesMedian() > 0) score += 0.05;
        if (doc.getCoverId() != null) score += 0.05;
        if (doc.getSubject() != null && !doc.getSubject().isEmpty()) score += 0.05;

        return Math.min(score, 1.0);
    }
}
