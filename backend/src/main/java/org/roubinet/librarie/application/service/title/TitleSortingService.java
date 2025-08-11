package org.roubinet.librarie.application.service.title;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.Instance;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling title sorting with language-specific strategies.
 * Manages different title sorting strategies for various languages.
 */
@ApplicationScoped
public class TitleSortingService {
    
    private final List<TitleSortingStrategy> strategies;
    private final TitleSortingStrategy defaultStrategy;
    
    @Inject
    public TitleSortingService(Instance<TitleSortingStrategy> strategiesInstance) {
        this.strategies = strategiesInstance.stream().toList();
        this.defaultStrategy = strategies.stream()
            .filter(s -> s.getLanguageCode().equals("en"))
            .findFirst()
            .orElse(new EnglishTitleSortingStrategy());
    }
    
    /**
     * Generate a sortable title using the appropriate language strategy.
     * 
     * @param title the original title
     * @param languageCode ISO 639-1 language code (e.g., "en", "fr", "de")
     * @return the sortable title
     */
    public String generateSortableTitle(String title, String languageCode) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        TitleSortingStrategy strategy = findStrategyForLanguage(languageCode)
            .orElse(defaultStrategy);
            
        return strategy.generateSortableTitle(title);
    }
    
    /**
     * Generate a sortable title using the default English strategy.
     * 
     * @param title the original title
     * @return the sortable title
     */
    public String generateSortableTitle(String title) {
        return generateSortableTitle(title, "en");
    }
    
    /**
     * Find the strategy for a specific language.
     * 
     * @param languageCode ISO 639-1 language code
     * @return the strategy if found
     */
    private Optional<TitleSortingStrategy> findStrategyForLanguage(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return strategies.stream()
            .filter(strategy -> strategy.supports(languageCode))
            .findFirst();
    }
    
    /**
     * Get all supported language codes.
     * 
     * @return list of supported language codes
     */
    public List<String> getSupportedLanguages() {
        return strategies.stream()
            .map(TitleSortingStrategy::getLanguageCode)
            .toList();
    }
}