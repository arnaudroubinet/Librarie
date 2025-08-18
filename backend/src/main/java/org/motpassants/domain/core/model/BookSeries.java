package org.motpassants.domain.core.model;

import java.util.UUID;

/**
 * BookSeries domain model representing book-series relationships.
 * Placeholder for now - will be detailed later.
 */
public class BookSeries {
    private UUID id;
    private Book book;
    private Series series;
    private Double seriesIndex;
    
    public BookSeries() {}
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
    public Series getSeries() { return series; }
    public void setSeries(Series series) { this.series = series; }
    public Double getSeriesIndex() { return seriesIndex; }
    public void setSeriesIndex(Double seriesIndex) { this.seriesIndex = seriesIndex; }
}