package org.roubinet.librarie.infrastructure.adapter.out.persistence;

import org.roubinet.librarie.application.port.out.LanguageRepository;
import org.roubinet.librarie.domain.entity.Language;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of the LanguageRepository port.
 * This adapter translates domain repository operations to Panache ORM calls.
 */
@ApplicationScoped
public class LanguageRepositoryAdapter implements LanguageRepository {
    
    @Override
    public List<Language> findAll() {
        return Language.listAll();
    }
    
    @Override
    public Optional<Language> findByCode(String code) {
        return Language.findByIdOptional(code);
    }
    
    @Override
    public Language save(Language language) {
        Language.persist(language);
        return language;
    }
    
    @Override
    public void deleteByCode(String code) {
        Language.deleteById(code);
    }
    
    @Override
    public boolean existsByCode(String code) {
        return Language.findByIdOptional(code).isPresent();
    }
    
    @Override
    public long count() {
        return Language.count();
    }
}