package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.SeriesUseCase;
import org.roubinet.librarie.application.port.out.SeriesRepository;
import org.roubinet.librarie.domain.entity.Series;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for Series operations.
 */
@ApplicationScoped
public class SeriesService implements SeriesUseCase {
    
    private final SeriesRepository seriesRepository;
    
    @Inject
    public SeriesService(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }
    
    @Override
    public CursorPageResult<Series> getAllSeries(String cursor, int limit) {
        return seriesRepository.getAllSeries(cursor, limit);
    }
    
    @Override
    public Optional<Series> getSeriesById(UUID id) {
        return seriesRepository.findById(id);
    }
}