package org.roubinet.librarie.infrastructure.adapter.out.persistence;

import org.roubinet.librarie.application.port.out.FormatRepository;
import org.roubinet.librarie.domain.entity.Format;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the FormatRepository port.
 * This adapter translates domain repository operations to Panache ORM calls.
 */
@ApplicationScoped
public class FormatRepositoryAdapter implements FormatRepository {
    
    @Override
    public List<Format> findAll() {
        return Format.listAll();
    }
    
    @Override
    public Optional<Format> findById(UUID id) {
        return Format.findByIdOptional(id);
    }
    
    @Override
    public List<Format> findByType(String type) {
        return Format.find("type", type).list();
    }
    
    @Override
    public List<String> findDistinctTypes() {
        return Format.find("SELECT DISTINCT f.type FROM Format f ORDER BY f.type").project(String.class).list();
    }
    
    @Override
    public Format save(Format format) {
        Format.persist(format);
        return format;
    }
    
    @Override
    public void deleteById(UUID id) {
        Format.deleteById(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return Format.findByIdOptional(id).isPresent();
    }
    
    @Override
    public long count() {
        return Format.count();
    }
}