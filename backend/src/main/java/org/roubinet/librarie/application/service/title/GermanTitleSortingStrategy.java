package org.roubinet.librarie.application.service.title;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * German title sorting strategy.
 * Handles German definite and indefinite articles: "der", "die", "das", "ein", "eine".
 */
@ApplicationScoped
public class GermanTitleSortingStrategy implements TitleSortingStrategy {
    
    private static final String[][] GERMAN_ARTICLES = {
        {"der ", ", Der"},
        {"die ", ", Die"},
        {"das ", ", Das"},
        {"ein ", ", Ein"},
        {"eine ", ", Eine"}
    };
    
    @Override
    public String generateSortableTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        String cleaned = title.trim();
        String lowerTitle = cleaned.toLowerCase();
        
        // Check for German articles
        for (String[] articlePair : GERMAN_ARTICLES) {
            String article = articlePair[0];
            String suffix = articlePair[1];
            
            if (lowerTitle.startsWith(article)) {
                // Remove article from beginning and add to end
                String withoutArticle = cleaned.substring(article.length()).trim();
                if (!withoutArticle.isEmpty()) {
                    return withoutArticle + suffix;
                }
            }
        }
        
        return cleaned;
    }
    
    @Override
    public String getLanguageCode() {
        return "de";
    }
}