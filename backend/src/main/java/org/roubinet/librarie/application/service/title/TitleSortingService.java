package org.roubinet.librarie.application.service.title;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

/**
 * Service for handling title sorting with multi-language support.
 * Applies all language rules to any book since we don't know which language it uses.
 */
@ApplicationScoped
public class TitleSortingService {
    
    // Articles for different languages
    private static final Map<String, String[]> LANGUAGE_ARTICLES = Map.of(
        "en", new String[]{"the", "a", "an"},
        "fr", new String[]{"le", "la", "les", "l'", "un", "une", "des", "du", "de", "d'"},
        "de", new String[]{"der", "die", "das", "ein", "eine", "einen", "einem", "einer"},
        "es", new String[]{"el", "la", "los", "las", "un", "una", "unos", "unas"},
        "hi", new String[]{"एक", "यह", "वह", "उस", "इस"}
    );
    
    /**
     * Generate a sortable title by applying all language rules.
     * Since we don't know the language of the book, we try all patterns.
     * 
     * @param title the original title
     * @return the sortable title
     */
    public String generateSortableTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        String sortableTitle = title.trim();
        
        // Try to find any article from any language
        String lowerTitle = sortableTitle.toLowerCase();
        
        for (Map.Entry<String, String[]> entry : LANGUAGE_ARTICLES.entrySet()) {
            String[] articles = entry.getValue();
            
            for (String article : articles) {
                String articlePrefix = article + " ";
                if (lowerTitle.startsWith(articlePrefix.toLowerCase())) {
                    // Remove article and append it at the end with comma
                    String titleWithoutArticle = sortableTitle.substring(articlePrefix.length());
                    return titleWithoutArticle + ", " + sortableTitle.substring(0, articlePrefix.length() - 1);
                }
            }
        }
        
        // Handle Chinese titles - normalize spacing and punctuation
        if (containsChineseCharacters(sortableTitle)) {
            return sortableTitle
                .replaceAll("\\s+", " ")  // normalize whitespace
                .replaceAll("^[\\p{Punct}\\s]+", "")  // remove leading punctuation
                .replaceAll("[\\p{Punct}\\s]+$", ""); // remove trailing punctuation
        }
        
        return sortableTitle;
    }
    
    /**
     * Generate a sortable title with optional language hint.
     * 
     * @param title the original title
     * @param languageCode ISO 639-1 language code (optional, can be null)
     * @return the sortable title
     */
    public String generateSortableTitle(String title, String languageCode) {
        // Since we apply all rules regardless of language, 
        // we can ignore the language code for now
        return generateSortableTitle(title);
    }
    
    /**
     * Check if the title contains Chinese characters.
     * 
     * @param title the title to check
     * @return true if contains Chinese characters
     */
    private boolean containsChineseCharacters(String title) {
        return title.codePoints().anyMatch(codepoint -> 
            Character.UnicodeScript.of(codepoint) == Character.UnicodeScript.HAN);
    }
    
    /**
     * Get all supported language codes.
     * 
     * @return list of supported language codes
     */
    public List<String> getSupportedLanguages() {
        return List.of("en", "fr", "de", "es", "hi", "zh");
    }
}