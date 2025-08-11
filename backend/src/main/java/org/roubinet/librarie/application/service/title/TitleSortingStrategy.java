package org.roubinet.librarie.application.service.title;

/**
 * Interface for language-specific title sorting.
 * Implementations handle language-specific article rules.
 */
public interface TitleSortingStrategy {
    
    /**
     * Convert a title to its sortable form by handling language-specific articles.
     * 
     * @param title the original title
     * @return the sortable title with articles moved to the end
     */
    String generateSortableTitle(String title);
    
    /**
     * Get the language code this strategy handles.
     * 
     * @return ISO 639-1 language code (e.g., "en", "fr", "de")
     */
    String getLanguageCode();
    
    /**
     * Check if this strategy can handle the given language.
     * 
     * @param languageCode ISO 639-1 language code
     * @return true if this strategy handles the language
     */
    default boolean supports(String languageCode) {
        return getLanguageCode().equalsIgnoreCase(languageCode);
    }
}