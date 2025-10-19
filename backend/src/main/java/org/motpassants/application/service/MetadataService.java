package org.motpassants.application.service;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.MetadataSearchResult;
import org.motpassants.domain.port.in.MetadataUseCase;
import org.motpassants.domain.port.out.BookRepository;
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

    @Inject
    BookRepository bookRepository;

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

    @Override
    @Transactional
    public Book applyMetadataToBook(UUID bookId, MetadataSearchResult metadataResult, boolean downloadCover) {
        if (bookId == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
        if (metadataResult == null) {
            throw new IllegalArgumentException("Metadata result cannot be null");
        }

        log.info("Applying metadata from {} to book {}", metadataResult.getSource(), bookId);

        // Find the book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        // Apply metadata to book
        if (metadataResult.getTitle() != null) {
            book.setTitle(metadataResult.getTitle());
            // Also set title sort if not already set
            if (book.getTitleSort() == null || book.getTitleSort().isEmpty()) {
                book.setTitleSort(metadataResult.getTitle());
            }
        }

        if (metadataResult.getDescription() != null) {
            book.setDescription(metadataResult.getDescription());
        }

        if (metadataResult.getIsbn13() != null) {
            book.setIsbn(metadataResult.getIsbn13());
        } else if (metadataResult.getIsbn10() != null) {
            book.setIsbn(metadataResult.getIsbn10());
        }

        if (metadataResult.getPageCount() != null) {
            book.setPageCount(metadataResult.getPageCount());
        }

        if (metadataResult.getPublishedDate() != null) {
            book.setPublicationDate(metadataResult.getPublishedDate());
            book.setPublicationYear(metadataResult.getPublishedDate().getYear());
        }

        if (metadataResult.getLanguage() != null) {
            book.setLanguage(metadataResult.getLanguage());
        }

        // Store additional metadata in the JSONB field
        Map<String, Object> metadata = book.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        
        metadata.put("metadata_source", metadataResult.getSource().name());
        metadata.put("provider_book_id", metadataResult.getProviderBookId());
        metadata.put("confidence_score", metadataResult.getConfidenceScore());
        
        if (metadataResult.getSubtitle() != null) {
            metadata.put("subtitle", metadataResult.getSubtitle());
        }
        if (metadataResult.getCategories() != null && !metadataResult.getCategories().isEmpty()) {
            metadata.put("categories", metadataResult.getCategories());
        }
        if (metadataResult.getAverageRating() != null) {
            metadata.put("average_rating", metadataResult.getAverageRating());
        }
        if (metadataResult.getRatingsCount() != null) {
            metadata.put("ratings_count", metadataResult.getRatingsCount());
        }
        
        book.setMetadata(metadata);
        book.markAsUpdated();

        // TODO: Download cover image if requested
        // This would involve:
        // 1. Downloading the image from metadataResult.getCoverImageUrl()
        // 2. Storing it in the asset storage
        // 3. Setting book.setHasCover(true)

        return bookRepository.save(book);
    }

    @Override
    public CompletableFuture<MetadataSearchResult> fetchMetadataForBook(UUID bookId) {
        if (bookId == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Book ID cannot be null"));
        }

        log.info("Fetching metadata for book: {}", bookId);

        // Find the book
        return CompletableFuture.supplyAsync(() -> {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

            // Try ISBN first if available
            if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                log.debug("Searching by ISBN: {}", book.getIsbn());
                try {
                    List<MetadataSearchResult> results = searchMetadataByIsbn(book.getIsbn()).join();
                    if (!results.isEmpty()) {
                        return results.get(0); // Return best match
                    }
                } catch (Exception e) {
                    log.warn("ISBN search failed, falling back to title/author", e);
                }
            }

            // Fall back to title and author search
            log.debug("Searching by title: {}", book.getTitle());
            List<MetadataSearchResult> results = searchMetadataByTitleAndAuthor(book.getTitle(), null).join();
            
            if (results.isEmpty()) {
                throw new IllegalStateException("No metadata found for book: " + bookId);
            }
            
            return results.get(0); // Return best match
        });
    }

    @Override
    public CompletableFuture<Void> batchFetchMetadata(List<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("Starting batch metadata fetch for {} books", bookIds.size());

        // Process books in parallel with a limit to avoid overwhelming external APIs
        List<CompletableFuture<Void>> futures = bookIds.stream()
                .map(bookId -> fetchMetadataForBook(bookId)
                        .thenAccept(metadata -> {
                            try {
                                applyMetadataToBook(bookId, metadata, false);
                                log.info("Applied metadata for book: {}", bookId);
                            } catch (Exception e) {
                                log.error("Failed to apply metadata for book: {}", bookId, e);
                            }
                        })
                        .exceptionally(throwable -> {
                            log.error("Failed to fetch metadata for book: {}", bookId, throwable);
                            return null;
                        }))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
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
