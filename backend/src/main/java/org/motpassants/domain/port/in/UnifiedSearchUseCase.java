package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.UnifiedSearchResult;

import java.util.List;

/**
 * Use case interface for unified search operations.
 * Defines the business capabilities for cross-entity search.
 */
public interface UnifiedSearchUseCase {
    
    /**
     * Perform unified search across all entity types.
     * 
     * @param query Search query
     * @param limit Maximum number of results per entity type
     * @param entityTypes List of entity types to search (null for all)
     * @return Unified search results
     */
    UnifiedSearchResult unifiedSearch(String query, int limit, List<String> entityTypes);
}