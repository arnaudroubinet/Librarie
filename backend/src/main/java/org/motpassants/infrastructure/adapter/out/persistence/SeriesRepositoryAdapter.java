package org.motpassants.infrastructure.adapter.out.persistence;

import org.motpassants.domain.core.model.Series;
import org.motpassants.domain.port.out.SeriesRepositoryPort;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of SeriesRepositoryPort.
 * Used for development and testing.
 */
@ApplicationScoped
public class SeriesRepositoryAdapter implements SeriesRepositoryPort {
    
    private final Map<UUID, Series> seriesStorage = new ConcurrentHashMap<>();
    
    @Override
    public List<Series> findAll(int offset, int limit) {
        return seriesStorage.values().stream()
                .sorted(Comparator.comparing(Series::getName))
                .skip(offset)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public long count() {
        return seriesStorage.size();
    }
    
    @Override
    public Optional<Series> findById(UUID id) {
        return Optional.ofNullable(seriesStorage.get(id));
    }
    
    @Override
    public Series save(Series series) {
        if (series.getId() == null) {
            series.setId(UUID.randomUUID());
            series.setCreatedAt(OffsetDateTime.now());
        }
        series.setUpdatedAt(OffsetDateTime.now());
        
        seriesStorage.put(series.getId(), series);
        return series;
    }
    
    @Override
    public boolean deleteById(UUID id) {
        return seriesStorage.remove(id) != null;
    }
    
    @Override
    public List<Series> searchByName(String query) {
        String lowerQuery = query.toLowerCase();
        return seriesStorage.values().stream()
                .filter(series -> series.getName().toLowerCase().contains(lowerQuery) ||
                                 (series.getSortName() != null && series.getSortName().toLowerCase().contains(lowerQuery)))
                .sorted(Comparator.comparing(Series::getName))
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByName(String name) {
        return seriesStorage.values().stream()
                .anyMatch(series -> series.getName().equalsIgnoreCase(name));
    }
    
    @Override
    public boolean existsByNameAndIdNot(String name, UUID excludeId) {
        return seriesStorage.values().stream()
                .anyMatch(series -> !series.getId().equals(excludeId) && 
                                   series.getName().equalsIgnoreCase(name));
    }
}