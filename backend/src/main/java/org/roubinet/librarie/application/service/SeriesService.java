package org.roubinet.librarie.application.service;

import org.roubinet.librarie.application.port.in.SeriesUseCase;
import org.roubinet.librarie.application.port.out.SeriesRepository;
import org.roubinet.librarie.domain.entity.Series;
import org.roubinet.librarie.domain.model.SeriesData;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;
import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Series operations.
 */
@ApplicationScoped
public class SeriesService implements SeriesUseCase {
    
    private final SeriesRepository seriesRepository;
    private final LibrarieConfigProperties config;
    
    @Inject
    public SeriesService(SeriesRepository seriesRepository, LibrarieConfigProperties config) {
        this.seriesRepository = seriesRepository;
        this.config = config;
    }
    
    @Override
    public CursorPageResult<SeriesData> getAllSeries(String cursor, int limit) {
        CursorPageResult<Series> entityResult = seriesRepository.getAllSeries(cursor, limit);
        
        List<SeriesData> seriesDataList = entityResult.getItems().stream()
            .map(this::toSeriesData)
            .collect(Collectors.toList());
            
        return CursorPageResult.<SeriesData>builder()
            .items(seriesDataList)
            .nextCursor(entityResult.getNextCursor())
            .previousCursor(entityResult.getPreviousCursor())
            .hasNext(entityResult.isHasNext())
            .hasPrevious(entityResult.isHasPrevious())
            .limit(entityResult.getLimit())
            .totalCount(entityResult.getTotalCount())
            .build();
    }
    
    @Override
    public Optional<SeriesData> getSeriesById(UUID id) {
        return seriesRepository.findById(id)
            .map(this::toSeriesData);
    }
    
    @Override
    public List<SeriesData> searchSeries(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        return seriesRepository.findByNameContainingIgnoreCase(query.trim()).stream()
            .map(this::toSeriesData)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert Series entity to SeriesData domain model with computed properties.
     */
    private SeriesData toSeriesData(Series series) {
        String defaultCoverPath = config.fileProcessing().defaultCoverPath();
        String effectiveImagePath = series.getEffectiveImagePath(defaultCoverPath);
        
        return new SeriesData(
            series.getId(),
            series.getName(),
            series.getSortName(),
            series.getDescription(),
            series.getImagePath(),
            series.getMetadata(),
            series.getCreatedAt(),
            series.getUpdatedAt(),
            series.getBookCount(),
            effectiveImagePath
        );
    }
}