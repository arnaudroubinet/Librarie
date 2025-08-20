package org.motpassants.application.service;

import org.motpassants.domain.core.model.Author;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.port.in.AuthorUseCase;
import org.motpassants.domain.port.out.AuthorRepositoryPort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import org.motpassants.domain.core.model.AuthorSortCriteria;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing Author use cases.
 * Orchestrates domain objects and repository operations.
 * Transactional boundary for Author operations.
 */
@ApplicationScoped
@Transactional
public class AuthorService implements AuthorUseCase {

    private final AuthorRepositoryPort authorRepository;

    @Inject
    public AuthorService(AuthorRepositoryPort authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public Author createAuthor(String name, String sortName, Map<String, String> bio,
                              LocalDate birthDate, LocalDate deathDate, String websiteUrl,
                              Map<String, Object> metadata) {
        
        // Business rule: Author name must be unique
        if (authorRepository.existsByName(name)) {
            throw new IllegalArgumentException("Author with name '" + name + "' already exists");
        }
        
        // Create domain object
        Author author = Author.create(name, sortName, bio, birthDate, deathDate, websiteUrl, metadata);
        
        // Persist
        return authorRepository.save(author);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<Author> getAuthorById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Author ID cannot be null");
        }
        return authorRepository.findById(id);
    }

    @Override
    public Author updateAuthor(UUID id, String name, String sortName, Map<String, String> bio,
                              LocalDate birthDate, LocalDate deathDate, String websiteUrl,
                              Map<String, Object> metadata) {
        
        if (id == null) {
            throw new IllegalArgumentException("Author ID cannot be null");
        }
        
        // Get existing author
        Author existingAuthor = authorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Author not found with ID: " + id));
        
        // Business rule: Author name must be unique (except for same author)
        Optional<Author> authorWithSameName = authorRepository.findByName(name);
        if (authorWithSameName.isPresent() && !authorWithSameName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Author with name '" + name + "' already exists");
        }
        
        // Update domain object
        existingAuthor.updateName(name);
        existingAuthor.updateSortName(sortName);
        existingAuthor.updateBio(bio);
        existingAuthor.updateBirthDate(birthDate);
        existingAuthor.updateDeathDate(deathDate);
        existingAuthor.updateWebsiteUrl(websiteUrl);
        existingAuthor.updateMetadata(metadata);
        
        // Persist changes
        return authorRepository.update(existingAuthor);
    }

    @Override
    public void deleteAuthor(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Author ID cannot be null");
        }
        
        if (!authorRepository.existsById(id)) {
            throw new IllegalArgumentException("Author not found with ID: " + id);
        }
        
        // TODO: Check for business constraints (e.g., author has books)
        // For now, allow deletion
        authorRepository.deleteById(id);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public PageResult<Author> getAllAuthors(String cursor, int limit) {
        return getAllAuthors(cursor, limit, org.motpassants.domain.core.model.AuthorSortCriteria.DEFAULT);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public PageResult<Author> getAllAuthors(String cursor, int limit, org.motpassants.domain.core.model.AuthorSortCriteria sortCriteria) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        if (limit > 100) {
            throw new IllegalArgumentException("Limit cannot exceed 100");
        }
        return authorRepository.findAll(cursor, limit, sortCriteria);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public PageResult<Author> searchAuthors(String query, String cursor, int limit) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be null or empty");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        if (limit > 100) {
            throw new IllegalArgumentException("Limit cannot exceed 100");
        }
        
        return authorRepository.searchByName(query.trim(), cursor, limit);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean existsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return authorRepository.existsByName(name);
    }
}