package org.motpassants.infrastructure.adapter.out.metadata;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.metadata.BookMetadata;
import org.motpassants.domain.core.model.metadata.ProviderStatus;
import org.motpassants.domain.port.out.LoggingPort;
import org.motpassants.domain.port.out.metadata.MetadataAggregatorPort;
import org.motpassants.domain.port.out.metadata.MetadataProviderPort;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of MetadataAggregatorPort that manages multiple metadata providers.
 */
@ApplicationScoped
public class MetadataAggregatorService implements MetadataAggregatorPort {
    
    private final List<MetadataProviderPort> providers;
    private final LoggingPort loggingPort;
    
    @Inject
    public MetadataAggregatorService(LoggingPort loggingPort) {
        this.loggingPort = loggingPort;
        this.providers = new ArrayList<>();
    }
    
    @Override
    public List<BookMetadata> findByIsbnFromAllProviders(String isbn) {
        List<BookMetadata> results = new ArrayList<>();
        
        for (MetadataProviderPort provider : getEnabledProviders()) {
            try {
                Optional<BookMetadata> metadata = provider.findByIsbn(isbn);
                metadata.ifPresent(results::add);
            } catch (Exception e) {
                loggingPort.error("Error searching provider " + provider.getProviderId() + " for ISBN: " + isbn, e);
            }
        }
        
        // Sort by confidence score descending
        results.sort((a, b) -> Double.compare(
            b.confidence() != null ? b.confidence() : 0.0,
            a.confidence() != null ? a.confidence() : 0.0
        ));
        
        return results;
    }
    
    @Override
    public Optional<BookMetadata> getBestMetadataByIsbn(String isbn) {
        List<BookMetadata> results = findByIsbnFromAllProviders(isbn);
        
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        // Return the highest confidence result, or merge multiple if they're close
        BookMetadata best = results.get(0);
        
        // If we have multiple results with similar confidence, merge them
        List<BookMetadata> topResults = results.stream()
            .filter(m -> m.confidence() != null && best.confidence() != null && 
                        Math.abs(m.confidence() - best.confidence()) < 0.1)
            .limit(3)  // Merge at most 3 top results
            .collect(Collectors.toList());
        
        if (topResults.size() > 1) {
            return Optional.of(mergeMetadata(topResults));
        } else {
            return Optional.of(best);
        }
    }
    
    @Override
    public List<BookMetadata> searchByTitleFromAllProviders(String title, String author) {
        List<BookMetadata> results = new ArrayList<>();
        
        for (MetadataProviderPort provider : getEnabledProviders()) {
            try {
                List<BookMetadata> providerResults = provider.searchByTitle(title, author);
                results.addAll(providerResults);
            } catch (Exception e) {
                loggingPort.error("Error searching provider " + provider.getProviderId() + " for title: " + title, e);
            }
        }
        
        // Sort by relevance (confidence + title similarity)
        results.sort((a, b) -> {
            double scoreA = calculateRelevanceScore(a, title, author);
            double scoreB = calculateRelevanceScore(b, title, author);
            return Double.compare(scoreB, scoreA);
        });
        
        return results;
    }
    
    @Override
    public BookMetadata mergeMetadata(List<BookMetadata> metadataList) {
        if (metadataList.isEmpty()) {
            throw new IllegalArgumentException("Cannot merge empty metadata list");
        }
        
        if (metadataList.size() == 1) {
            return metadataList.get(0);
        }
        
        // Start with the highest confidence result as base
        BookMetadata base = metadataList.get(0);
        BookMetadata.Builder merged = BookMetadata.builder();
        
        // Basic strategy: prefer non-null values, with higher confidence values taking precedence
        merged.providerId("merged")
              .providerName("Merged from multiple sources")
              .confidence(metadataList.stream().mapToDouble(m -> m.confidence() != null ? m.confidence() : 0.0).average().orElse(0.5));
        
        // Merge fields with preference for non-null values
        merged.title(preferNonNull(metadataList, BookMetadata::title))
              .subtitle(preferNonNull(metadataList, BookMetadata::subtitle))
              .originalTitle(preferNonNull(metadataList, BookMetadata::originalTitle))
              .titleSort(preferNonNull(metadataList, BookMetadata::titleSort))
              .description(preferLongest(metadataList, BookMetadata::description))
              .language(preferNonNull(metadataList, BookMetadata::language))
              .publisher(preferNonNull(metadataList, BookMetadata::publisher))
              .publicationDate(preferNonNull(metadataList, BookMetadata::publicationDate))
              .publicationYear(preferNonNull(metadataList, BookMetadata::publicationYear))
              .pageCount(preferNonNull(metadataList, BookMetadata::pageCount));
        
        // ISBNs - prefer ISBN-13 over ISBN-10
        merged.isbn13(preferNonNull(metadataList, BookMetadata::isbn13))
              .isbn10(preferNonNull(metadataList, BookMetadata::isbn10))
              .isbn(preferNonNull(metadataList, BookMetadata::isbn13, BookMetadata::isbn));
        
        // External IDs - collect all non-null values
        merged.googleBooksId(preferNonNull(metadataList, BookMetadata::googleBooksId))
              .openLibraryId(preferNonNull(metadataList, BookMetadata::openLibraryId))
              .goodreadsId(preferNonNull(metadataList, BookMetadata::goodreadsId))
              .asin(preferNonNull(metadataList, BookMetadata::asin))
              .doi(preferNonNull(metadataList, BookMetadata::doi))
              .lccn(preferNonNull(metadataList, BookMetadata::lccn))
              .oclc(preferNonNull(metadataList, BookMetadata::oclc));
        
        // Authors - merge all unique authors
        Set<String> allAuthorNames = new HashSet<>();
        List<org.motpassants.domain.core.model.metadata.AuthorMetadata> mergedAuthors = new ArrayList<>();
        
        for (BookMetadata metadata : metadataList) {
            if (metadata.authors() != null) {
                for (var author : metadata.authors()) {
                    if (author.name() != null && allAuthorNames.add(author.name())) {
                        mergedAuthors.add(author);
                    }
                }
            }
        }
        merged.authors(mergedAuthors);
        
        // Subjects/tags - merge all unique values
        Set<String> allSubjects = new HashSet<>();
        Set<String> allGenres = new HashSet<>();
        Set<String> allTags = new HashSet<>();
        
        for (BookMetadata metadata : metadataList) {
            if (metadata.subjects() != null) allSubjects.addAll(metadata.subjects());
            if (metadata.genres() != null) allGenres.addAll(metadata.genres());
            if (metadata.tags() != null) allTags.addAll(metadata.tags());
        }
        
        merged.subjects(allSubjects.isEmpty() ? null : allSubjects)
              .genres(allGenres.isEmpty() ? null : allGenres)
              .tags(allTags.isEmpty() ? null : allTags);
        
        // Series
        merged.seriesName(preferNonNull(metadataList, BookMetadata::seriesName))
              .seriesIndex(preferNonNull(metadataList, BookMetadata::seriesIndex));
        
        // Physical attributes
        merged.format(preferNonNull(metadataList, BookMetadata::format))
              .binding(preferNonNull(metadataList, BookMetadata::binding))
              .dimensions(preferNonNull(metadataList, BookMetadata::dimensions))
              .weight(preferNonNull(metadataList, BookMetadata::weight));
        
        // Images - prefer larger/higher quality images
        merged.smallThumbnail(preferNonNull(metadataList, BookMetadata::smallThumbnail))
              .thumbnail(preferNonNull(metadataList, BookMetadata::thumbnail))
              .mediumImage(preferNonNull(metadataList, BookMetadata::mediumImage))
              .largeImage(preferNonNull(metadataList, BookMetadata::largeImage))
              .extraLargeImage(preferNonNull(metadataList, BookMetadata::extraLargeImage));
        
        // Ratings - average them
        double avgRating = metadataList.stream()
            .filter(m -> m.averageRating() != null)
            .mapToDouble(BookMetadata::averageRating)
            .average().orElse(0.0);
        
        int totalRatings = metadataList.stream()
            .filter(m -> m.ratingsCount() != null)
            .mapToInt(BookMetadata::ratingsCount)
            .sum();
        
        if (avgRating > 0) merged.averageRating(avgRating);
        if (totalRatings > 0) merged.ratingsCount(totalRatings);
        
        // Classification
        merged.deweyDecimal(preferNonNull(metadataList, BookMetadata::deweyDecimal))
              .lcc(preferNonNull(metadataList, BookMetadata::lcc));
        
        return merged.build();
    }
    
    @Override
    public List<MetadataProviderPort> getAllProviders() {
        return new ArrayList<>(providers);
    }
    
    @Override
    public List<MetadataProviderPort> getEnabledProviders() {
        return providers.stream()
            .filter(MetadataProviderPort::isEnabled)
            .sorted(Comparator.comparingInt(MetadataProviderPort::getPriority))
            .collect(Collectors.toList());
    }
    
    @Override
    public void registerProvider(MetadataProviderPort provider) {
        providers.add(provider);
        loggingPort.info("Registered metadata provider: " + provider.getProviderName());
    }
    
    @Override
    public List<ProviderStatus> testAllProviders() {
        return providers.stream()
            .map(provider -> {
                boolean connected = false;
                String error = null;
                
                try {
                    connected = provider.testConnection();
                } catch (Exception e) {
                    error = e.getMessage();
                }
                
                return new ProviderStatus(
                    provider.getProviderId(),
                    provider.getProviderName(),
                    provider.isEnabled(),
                    connected,
                    error
                );
            })
            .collect(Collectors.toList());
    }
    
    private <T> T preferNonNull(List<BookMetadata> metadataList, java.util.function.Function<BookMetadata, T> getter) {
        return metadataList.stream()
            .map(getter)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
    
    @SafeVarargs
    private final <T> T preferNonNull(List<BookMetadata> metadataList, java.util.function.Function<BookMetadata, T>... getters) {
        for (java.util.function.Function<BookMetadata, T> getter : getters) {
            T value = preferNonNull(metadataList, getter);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
    
    private String preferLongest(List<BookMetadata> metadataList, java.util.function.Function<BookMetadata, String> getter) {
        return metadataList.stream()
            .map(getter)
            .filter(Objects::nonNull)
            .filter(s -> !s.trim().isEmpty())
            .max(Comparator.comparingInt(String::length))
            .orElse(null);
    }
    
    private double calculateRelevanceScore(BookMetadata metadata, String searchTitle, String searchAuthor) {
        double score = 0.0;
        
        // Base confidence
        if (metadata.confidence() != null) {
            score += metadata.confidence() * 0.5;
        }
        
        // Title similarity
        if (metadata.title() != null && searchTitle != null) {
            double titleSimilarity = calculateStringSimilarity(metadata.title().toLowerCase(), searchTitle.toLowerCase());
            score += titleSimilarity * 0.3;
        }
        
        // Author similarity
        if (searchAuthor != null && metadata.authors() != null) {
            double authorSimilarity = metadata.authors().stream()
                .filter(author -> author.name() != null)
                .mapToDouble(author -> calculateStringSimilarity(author.name().toLowerCase(), searchAuthor.toLowerCase()))
                .max()
                .orElse(0.0);
            score += authorSimilarity * 0.2;
        }
        
        return score;
    }
    
    private double calculateStringSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        if (s1.contains(s2) || s2.contains(s1)) return 0.8;
        
        // Simple Levenshtein distance-based similarity
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        return maxLength == 0 ? 1.0 : 1.0 - (double) distance / maxLength;
    }
    
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
}