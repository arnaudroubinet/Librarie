package org.motpassants.infrastructure.adapter.out.persistence;

import org.motpassants.domain.core.model.Author;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.port.out.AuthorRepositoryPort;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the AuthorRepositoryPort for testing.
 * This adapter provides a working implementation during the migration phase.
 * Will be replaced with JPA implementation once database entities are ready.
 */
@ApplicationScoped
public class AuthorRepositoryAdapter implements AuthorRepositoryPort {

    private final Map<UUID, Author> authors = new ConcurrentHashMap<>();

    @Override
    public Author save(Author author) {
        authors.put(author.getId(), author);
        return author;
    }

    @Override
    public Author update(Author author) {
        authors.put(author.getId(), author);
        return author;
    }

    @Override
    public Optional<Author> findById(UUID id) {
        return Optional.ofNullable(authors.get(id));
    }

    @Override
    public Optional<Author> findByName(String name) {
        return authors.values().stream()
            .filter(author -> author.getName().equals(name))
            .findFirst();
    }

    @Override
    public void deleteById(UUID id) {
        authors.remove(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return authors.containsKey(id);
    }

    @Override
    public boolean existsByName(String name) {
        return authors.values().stream()
            .anyMatch(author -> author.getName().equals(name));
    }

    @Override
    public PageResult<Author> findAll(String cursor, int limit) {
        List<Author> allAuthors = new ArrayList<>(authors.values());
        allAuthors.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        
        // Simple implementation - return all results (ignoring cursor for now)
        return new PageResult<>(allAuthors, null, null, false, false, allAuthors.size());
    }

    @Override
    public PageResult<Author> searchByName(String query, String cursor, int limit) {
        List<Author> matchingAuthors = authors.values().stream()
            .filter(author -> 
                author.getName().toLowerCase().contains(query.toLowerCase()) ||
                author.getSortName().toLowerCase().contains(query.toLowerCase())
            )
            .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
            .collect(Collectors.toList());
            
        // Simple implementation - return all matching results (ignoring cursor for now)
        return new PageResult<>(matchingAuthors, null, null, false, false, matchingAuthors.size());
    }

    @Override
    public long count() {
        return authors.size();
    }
}