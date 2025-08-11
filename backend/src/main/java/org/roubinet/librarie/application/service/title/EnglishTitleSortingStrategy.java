package org.roubinet.librarie.application.service.title;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * English title sorting strategy.
 * Handles English definite and indefinite articles: "the", "a", "an".
 */
@ApplicationScoped
public class EnglishTitleSortingStrategy implements TitleSortingStrategy {
    
    private static final String[] ENGLISH_ARTICLES = {"the ", "a ", "an "};
    private static final String[] ENGLISH_SUFFIXES = {", The", ", A", ", An"};
    
    @Override
    public String generateSortableTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        String cleaned = title.trim();
        String lowerTitle = cleaned.toLowerCase();
        
        // Check for English articles
        for (int i = 0; i < ENGLISH_ARTICLES.length; i++) {
            if (lowerTitle.startsWith(ENGLISH_ARTICLES[i])) {
                // Remove article from beginning and add to end
                String withoutArticle = cleaned.substring(ENGLISH_ARTICLES[i].length()).trim();
                if (!withoutArticle.isEmpty()) {
                    return withoutArticle + ENGLISH_SUFFIXES[i];
                }
            }
        }
        
        return cleaned;
    }
    
    @Override
    public String getLanguageCode() {
        return "en";
    }
}