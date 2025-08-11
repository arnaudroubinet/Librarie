package org.roubinet.librarie.application.service.title;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * French title sorting strategy.
 * Handles French definite and indefinite articles: "le", "la", "les", "un", "une", "des", "du", "de la", "des".
 */
@ApplicationScoped
public class FrenchTitleSortingStrategy implements TitleSortingStrategy {
    
    private static final String[][] FRENCH_ARTICLES = {
        {"le ", ", Le"},
        {"la ", ", La"}, 
        {"les ", ", Les"},
        {"un ", ", Un"},
        {"une ", ", Une"},
        {"des ", ", Des"},
        {"du ", ", Du"},
        {"de la ", ", De la"},
        {"l'", ", L'"}
    };
    
    @Override
    public String generateSortableTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "";
        }
        
        String cleaned = title.trim();
        String lowerTitle = cleaned.toLowerCase();
        
        // Check for French articles
        for (String[] articlePair : FRENCH_ARTICLES) {
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
        return "fr";
    }
}