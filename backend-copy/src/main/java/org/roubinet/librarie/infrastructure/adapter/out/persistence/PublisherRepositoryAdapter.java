package org.roubinet.librarie.infrastructure.adapter.out.persistence;

import org.roubinet.librarie.application.port.out.PublisherRepository;
import org.roubinet.librarie.domain.entity.Publisher;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the PublisherRepository port.
 * This adapter translates domain repository operations to Panache ORM calls.
 */
@ApplicationScoped
public class PublisherRepositoryAdapter implements PublisherRepository {
    
    @Override
    public List<Publisher> findAll() {
        return Publisher.listAll();
    }
    
    @Override
    public Optional<Publisher> findById(UUID id) {
        return Publisher.findByIdOptional(id);
    }
    
    @Override
    public List<Publisher> findByNameContainingIgnoreCase(String name) {
        return Publisher.find("LOWER(name) LIKE LOWER(?1)", "%" + name + "%").list();
    }
    
    @Override
    public Publisher save(Publisher publisher) {
        Publisher.persist(publisher);
        return publisher;
    }
    
    @Override
    public void deleteById(UUID id) {
        Publisher.deleteById(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return Publisher.findByIdOptional(id).isPresent();
    }
    
    @Override
    public long count() {
        return Publisher.count();
    }
}