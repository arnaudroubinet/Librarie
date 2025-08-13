package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.AuthorUseCase;
import org.roubinet.librarie.application.port.out.AuthorRepository;
import org.roubinet.librarie.domain.entity.Author;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;
import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

/**
 * Application service implementing author management use cases.
 * This is the core business logic layer in the hexagonal architecture.
 */
@ApplicationScoped
public class AuthorService implements AuthorUseCase {
    
    private final AuthorRepository authorRepository;
    private final LibrarieConfigProperties config;
    
    @Inject
    public AuthorService(AuthorRepository authorRepository,
                        LibrarieConfigProperties config) {
        this.authorRepository = authorRepository;
        this.config = config;
    }
    
    @Override
    public CursorPageResult<Author> getAllAuthors(String cursor, int limit) {
        // Validate pagination parameters using configuration
        if (limit <= 0) {
            limit = config.pagination().defaultPageSize();
        }
        if (limit > config.pagination().maxPageSize()) {
            limit = config.pagination().maxPageSize();
        }
        
        // Since AuthorRepository doesn't have cursor-based pagination yet,
        // let's implement a simple cursor-based pagination using the existing findAll method
        List<Author> allAuthors = authorRepository.findAll();
        return createCursorPageResult(allAuthors, cursor, limit);
    }
    
    @Override
    public Optional<Author> getAuthorById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return authorRepository.findById(id);
    }
    
    @Override
    public CursorPageResult<Author> searchAuthors(String name, String cursor, int limit) {
        if (name == null || name.trim().isEmpty()) {
            return getAllAuthors(cursor, limit);
        }
        
        // Validate pagination parameters
        if (limit <= 0) {
            limit = config.pagination().defaultPageSize();
        }
        if (limit > config.pagination().maxPageSize()) {
            limit = config.pagination().maxPageSize();
        }
        
        List<Author> foundAuthors = authorRepository.findByNameContainingIgnoreCase(name.trim());
        return createCursorPageResult(foundAuthors, cursor, limit);
    }
    
    @Override
    @Transactional
    public Author createAuthor(Author author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        
        // Validate required fields
        if (author.getName() == null || author.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Author name is required");
        }
        
        // Set sort name if not provided
        if (author.getSortName() == null || author.getSortName().trim().isEmpty()) {
            author.setSortName(generateSortName(author.getName()));
        }
        
        return authorRepository.save(author);
    }
    
    @Override
    @Transactional
    public Author updateAuthor(Author author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
        
        if (author.getId() == null) {
            throw new IllegalArgumentException("Author ID is required for update");
        }
        
        // Validate required fields
        if (author.getName() == null || author.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Author name is required");
        }
        
        // Check if author exists
        Optional<Author> existingAuthor = authorRepository.findById(author.getId());
        if (existingAuthor.isEmpty()) {
            throw new IllegalArgumentException("Author not found");
        }
        
        // Set sort name if not provided
        if (author.getSortName() == null || author.getSortName().trim().isEmpty()) {
            author.setSortName(generateSortName(author.getName()));
        }
        
        return authorRepository.save(author);
    }
    
    @Override
    @Transactional
    public void deleteAuthor(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Author ID cannot be null");
        }
        
        if (!authorRepository.existsById(id)) {
            throw new IllegalArgumentException("Author not found");
        }
        
        authorRepository.deleteById(id);
    }
    
    /**
     * Create a simple cursor-based page result from a list.
     * This is a simplified implementation - in production, you'd want proper cursor-based queries.
     */
    private CursorPageResult<Author> createCursorPageResult(List<Author> authors, String cursor, int limit) {
        // For simplicity, using offset-based pagination disguised as cursor pagination
        int offset = parseCursor(cursor);
        int totalSize = authors.size();
        
        int startIndex = Math.min(offset, totalSize);
        int endIndex = Math.min(startIndex + limit, totalSize);
        
        List<Author> pageItems = authors.subList(startIndex, endIndex);
        
        String nextCursor = (endIndex < totalSize) ? String.valueOf(endIndex) : null;
        String previousCursor = (startIndex > 0) ? String.valueOf(Math.max(0, startIndex - limit)) : null;
        
        boolean hasNext = endIndex < totalSize;
        boolean hasPrevious = startIndex > 0;
        
        return new CursorPageResult<>(
            pageItems,
            nextCursor,
            previousCursor,
            hasNext,
            hasPrevious,
            limit,
            (long) totalSize
        );
    }
    
    /**
     * Parse cursor to offset. For simplicity, cursor is just the offset as string.
     */
    private int parseCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(cursor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Generate sort name from display name.
     * Moves articles (The, A, An) to the end and normalizes case.
     */
    private String generateSortName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        
        String trimmed = name.trim();
        String[] articles = {"The ", "A ", "An "};
        
        for (String article : articles) {
            if (trimmed.startsWith(article)) {
                return trimmed.substring(article.length()) + ", " + article.trim();
            }
        }
        
        return trimmed;
    }
}