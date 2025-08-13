package org.roubinet.librarie.infrastructure.adapter.out.persistence;

import org.roubinet.librarie.application.port.out.AuthorRepository;
import org.roubinet.librarie.domain.entity.Author;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the AuthorRepository port.
 * This adapter translates domain repository operations to Panache ORM calls.
 */
@ApplicationScoped
public class AuthorRepositoryAdapter implements AuthorRepository {
    
    @Override
    public List<Author> findAll() {
        return Author.listAll();
    }
    
    @Override
    public Optional<Author> findById(UUID id) {
        return Author.findByIdOptional(id);
    }
    
    @Override
    public List<Author> findByNameContainingIgnoreCase(String name) {
        return Author.find("LOWER(name) LIKE LOWER(?1)", "%" + name + "%").list();
    }
    
    @Override
    public Author save(Author author) {
        Author.persist(author);
        return author;
    }
    
    @Override
    public void deleteById(UUID id) {
        Author.deleteById(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return Author.findByIdOptional(id).isPresent();
    }
    
    @Override
    public long count() {
        return Author.count();
    }
}