package org.motpassants.application.service;

import org.motpassants.domain.core.model.Page;
import org.motpassants.domain.core.model.Series;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.port.in.SeriesUseCase;
import org.motpassants.domain.port.out.SeriesRepositoryPort;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Series service implementing business logic.
 * Orchestrates between domain models and repository ports.
 */
@ApplicationScoped
@Transactional
public class SeriesService implements SeriesUseCase {
    
    private final SeriesRepositoryPort seriesRepository;
    
    @Inject
    public SeriesService(SeriesRepositoryPort seriesRepository) {
        this.seriesRepository = seriesRepository;
    }
    
    @Override
    public Page<Series> getAllSeries(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
        
        int offset = page * size;
        List<Series> series = seriesRepository.findAll(offset, size);
        long totalCount = seriesRepository.count();
        
        Page.PaginationMetadata metadata = new Page.PaginationMetadata(
            page, // Keep 0-based for domain layer
            size, 
            totalCount
        );
        
        return new Page<>(series, metadata);
    }
    
    @Override
    public Optional<Series> getSeriesById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return seriesRepository.findById(id);
    }
    
    @Override
    public Series createSeries(String name, String sortName, String description,
                              String imagePath, Integer totalBooks, Boolean isCompleted,
                              Map<String, Object> metadata) {
        
        // Check if series already exists
        if (seriesRepository.existsByName(name)) {
            throw new IllegalArgumentException("Series with name '" + name + "' already exists");
        }
        
        // Create series using domain factory method
        Series series = Series.create(name, sortName);
        series.setDescription(description);
        series.setImagePath(imagePath);
        if (totalBooks != null) {
            series.setTotalBooks(Math.max(0, totalBooks));
        }
        if (isCompleted != null) {
            series.setIsCompleted(isCompleted);
        }
        series.setMetadata(metadata);
        
        return seriesRepository.save(series);
    }
    
    @Override
    public Optional<Series> updateSeries(UUID id, String name, String sortName, String description,
                                        String imagePath, Integer totalBooks, Boolean isCompleted,
                                        Map<String, Object> metadata) {
        
        Optional<Series> existingOpt = seriesRepository.findById(id);
        if (existingOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Series series = existingOpt.get();
        
        // Check for name conflicts (excluding current series)
        if (name != null && !name.equals(series.getName()) && 
            seriesRepository.existsByNameAndIdNot(name, id)) {
            throw new IllegalArgumentException("Series with name '" + name + "' already exists");
        }
        
        // Update using domain method
        series.updateDetails(name, sortName, description, imagePath, totalBooks, isCompleted, metadata);
        
        return Optional.of(seriesRepository.save(series));
    }
    
    @Override
    public boolean deleteSeries(UUID id) {
        if (id == null) {
            return false;
        }
        
        // Check if series exists
        if (!seriesRepository.findById(id).isPresent()) {
            return false;
        }
        
        // TODO: Check if series has books and handle accordingly
        // For now, allow deletion
        
        return seriesRepository.deleteById(id);
    }
    
    @Override
    public List<Series> searchSeries(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return seriesRepository.searchByName(query.trim());
    }
    
    @Override
    public List<Book> getSeriesBooks(UUID seriesId) {
        if (seriesId == null) {
            return List.of();
        }
        
        // TODO: Implement relationship between Series and Books
        // For now, return empty list
        return List.of();
    }
}