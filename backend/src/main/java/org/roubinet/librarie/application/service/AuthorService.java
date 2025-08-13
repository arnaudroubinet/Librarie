package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.AuthorUseCase;
import org.roubinet.librarie.application.port.out.AuthorRepository;
import org.roubinet.librarie.domain.entity.Author;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AuthorUseCase.
 * Orchestrates author-related business operations.
 */
@ApplicationScoped
public class AuthorService implements AuthorUseCase {
    
    private final AuthorRepository authorRepository;
    
    @Inject
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }
    
    @Override
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }
    
    @Override
    public Optional<Author> getAuthorById(UUID id) {
        return authorRepository.findById(id);
    }
    
    @Override
    public List<Author> searchAuthors(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return authorRepository.findByNameContainingIgnoreCase(query.trim());
    }
}