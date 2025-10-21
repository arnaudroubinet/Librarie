package org.motpassants.application.service;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.motpassants.domain.core.model.MetadataSearchResult;
import org.motpassants.domain.port.in.MetadataUseCase;
import org.motpassants.domain.port.out.MetadataProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Application service for metadata operations.
 * Orchestrates multiple metadata providers, merges results, and applies them to books.
 * Implements caching and fault tolerance patterns.
 */
@ApplicationScoped
public class MetadataService implements MetadataUseCase {

    private static final Logger log = LoggerFactory.getLogger(MetadataService.class);

    @Inject
    Instance<MetadataProviderPort> providers;

    @Override
    @CacheResult(cacheName = "metadata-isbn-cache")
    @Retry(maxRetries = 2, delay = 1000, jitter = 500)
    @Timeout(value = 30, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 60000)
    public CompletableFuture<List<MetadataSearchResult>> searchMetadataByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        log.info("Searching metadata by ISBN: {}", isbn);

        // Get enabled providers sorted by priority
        List<MetadataProviderPort> enabledProviders = getEnabledProvidersSortedByPriority();
        
        if (enabledProviders.isEmpty()) {
            log.warn("No metadata providers enabled");
            return CompletableFuture.completedFuture(List.of());
        }

        // Execute searches in parallel
        List<CompletableFuture<List<MetadataSearchResult>>> futures = enabledProviders.stream()
                .map(provider -> {
                    log.debug("Searching {} by ISBN", provider.getProviderName());
                    return provider.searchByIsbn(isbn).toCompletableFuture();
                })
                .toList();

        // Wait for all providers to complete and merge results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<MetadataSearchResult> allResults = futures.stream()
                            .map(CompletableFuture::join)
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    
                    log.info("Found {} metadata results for ISBN {} from {} providers", 
                             allResults.size(), isbn, enabledProviders.size());
                    
                    return mergeAndSortResults(allResults);
                });
    }

    @Override
    @CacheResult(cacheName = "metadata-title-cache")
    @Retry(maxRetries = 2, delay = 1000, jitter = 500)
    @Timeout(value = 30, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 60000)
    public CompletableFuture<List<MetadataSearchResult>> searchMetadataByTitleAndAuthor(String title, String author) {
        if (title == null || title.trim().isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        log.info("Searching metadata by title: {}, author: {}", title, author);

        // Get enabled providers sorted by priority
        List<MetadataProviderPort> enabledProviders = getEnabledProvidersSortedByPriority();
        
        if (enabledProviders.isEmpty()) {
            log.warn("No metadata providers enabled");
            return CompletableFuture.completedFuture(List.of());
        }

        // Execute searches in parallel
        List<CompletableFuture<List<MetadataSearchResult>>> futures = enabledProviders.stream()
                .map(provider -> {
                    log.debug("Searching {} by title/author", provider.getProviderName());
                    return provider.searchByTitleAndAuthor(title, author).toCompletableFuture();
                })
                .toList();

        // Wait for all providers to complete and merge results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<MetadataSearchResult> allResults = futures.stream()
                            .map(CompletableFuture::join)
                            .flatMap(List::stream)
                            .collect(Collectors.toList());
                    
                    log.info("Found {} metadata results for title/author from {} providers", 
                             allResults.size(), enabledProviders.size());
                    
                    return mergeAndSortResults(allResults);
                });
    }







    /**
     * Get enabled providers sorted by priority (lower number = higher priority).
     */
    private List<MetadataProviderPort> getEnabledProvidersSortedByPriority() {
        return providers.stream()
                .filter(MetadataProviderPort::isEnabled)
                .sorted(Comparator.comparingInt(MetadataProviderPort::getPriority))
                .collect(Collectors.toList());
    }

    /**
     * Merge results from multiple providers and sort by confidence score.
     * Deduplicates based on ISBN or title similarity.
     */
    private List<MetadataSearchResult> mergeAndSortResults(List<MetadataSearchResult> results) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        // For now, simple implementation: deduplicate by ISBN13 and sort by confidence
        Map<String, MetadataSearchResult> deduped = new HashMap<>();
        
        for (MetadataSearchResult result : results) {
            String key = result.getIsbn13() != null ? result.getIsbn13() : result.getTitle();
            if (key != null) {
                // Keep the result with higher confidence score
                MetadataSearchResult existing = deduped.get(key);
                if (existing == null || result.getConfidenceScore() > existing.getConfidenceScore()) {
                    deduped.put(key, result);
                }
            }
        }

        // Sort by confidence score (highest first)
        return deduped.values().stream()
                .sorted(Comparator.comparingDouble(MetadataSearchResult::getConfidenceScore).reversed())
                .collect(Collectors.toList());
    }
}
