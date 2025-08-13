package org.roubinet.librarie.application.port.out;

import org.roubinet.librarie.domain.entity.Language;

import java.util.List;
import java.util.Optional;

/**
 * Secondary port (driven port) for language persistence operations.
 * This interface will be implemented by JPA repository adapters.
 */
public interface LanguageRepository {
    
    /**
     * Find all languages.
     * 
     * @return list of all languages
     */
    List<Language> findAll();
    
    /**
     * Find a language by its code.
     * 
     * @param code the language code
     * @return optional containing the language if found
     */
    Optional<Language> findByCode(String code);
    
    /**
     * Save a language (create or update).
     * 
     * @param language the language to save
     * @return the saved language
     */
    Language save(Language language);
    
    /**
     * Delete a language by code.
     * 
     * @param code the language code
     */
    void deleteByCode(String code);
    
    /**
     * Check if a language exists by code.
     * 
     * @param code the language code
     * @return true if the language exists
     */
    boolean existsByCode(String code);
    
    /**
     * Count total number of languages.
     * 
     * @return total count
     */
    long count();
}