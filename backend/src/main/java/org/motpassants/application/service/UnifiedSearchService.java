package org.motpassants.application.service;

import org.motpassants.domain.core.model.UnifiedSearchResult;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.Author;
import org.motpassants.domain.core.model.Series;
import org.motpassants.domain.port.in.UnifiedSearchUseCase;
import org.motpassants.domain.port.in.BookUseCase;
import org.motpassants.domain.port.in.AuthorUseCase;
import org.motpassants.domain.port.in.SeriesUseCase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unified search service implementing cross-entity search logic.
 * Orchestrates search across multiple entity types.
 */
@ApplicationScoped
public class UnifiedSearchService implements UnifiedSearchUseCase {
    
    private final BookUseCase bookUseCase;
    private final AuthorUseCase authorUseCase;
    private final SeriesUseCase seriesUseCase;
    
    private static final List<String> ALL_ENTITY_TYPES = Arrays.asList("books", "authors", "series");
    
    @Inject
    public UnifiedSearchService(BookUseCase bookUseCase, 
                               AuthorUseCase authorUseCase,
                               SeriesUseCase seriesUseCase) {
        this.bookUseCase = bookUseCase;
        this.authorUseCase = authorUseCase;
        this.seriesUseCase = seriesUseCase;
    }
    
    @Override
    public UnifiedSearchResult unifiedSearch(String query, int limit, List<String> entityTypes) {
        // Validate and sanitize inputs
        if (query == null || query.trim().isEmpty()) {
            return new UnifiedSearchResult(List.of(), List.of(), List.of());
        }
        
        if (limit <= 0) {
            limit = 10; // Default limit
        }
        if (limit > 50) {
            limit = 50; // Max limit to avoid performance issues
        }
        
        // Determine which entity types to search
        List<String> typesToSearch = (entityTypes == null || entityTypes.isEmpty()) 
            ? ALL_ENTITY_TYPES 
            : entityTypes;
        
        // Perform searches based on requested types
        List<Book> books = new ArrayList<>();
        List<Author> authors = new ArrayList<>();
        List<Series> series = new ArrayList<>();
        
        String sanitizedQuery = query.trim();
        
        if (typesToSearch.contains("books")) {
            try {
                // Note: BookUseCase.searchBooks might return a PageResponseDto, we need to adapt this
                books = bookUseCase.searchBooks(sanitizedQuery);
                if (books.size() > limit) {
                    books = books.subList(0, limit);
                }
            } catch (Exception e) {
                // Log error and continue with empty results for books
                books = new ArrayList<>();
            }
        }
        
        if (typesToSearch.contains("authors")) {
            try {
                // AuthorUseCase.searchAuthors returns PageResult<Author>
                var pageResult = authorUseCase.searchAuthors(sanitizedQuery, null, limit);
                authors = pageResult.getItems();
                if (authors.size() > limit) {
                    authors = authors.subList(0, limit);
                }
            } catch (Exception e) {
                // Log error and continue with empty results for authors
                authors = new ArrayList<>();
            }
        }
        
        if (typesToSearch.contains("series")) {
            try {
                series = seriesUseCase.searchSeries(sanitizedQuery);
                if (series.size() > limit) {
                    series = series.subList(0, limit);
                }
            } catch (Exception e) {
                // Log error and continue with empty results for series
                series = new ArrayList<>();
            }
        }
        
        return new UnifiedSearchResult(books, authors, series);
    }
}