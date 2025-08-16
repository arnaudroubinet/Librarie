package org.roubinet.librarie.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * BookSeries entity representing the relationship between books and series.
 * A book can only be in one series (enforced by unique constraint on book_id).
 */
@Entity
@Table(name = "book_series", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"book_id"}, name = "uk_book_series_book_id")
})
@IdClass(BookSeries.BookSeriesId.class)
public class BookSeries {
    
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id")
    private Book book;
    
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "series_id") 
    private Series series;
    
    @Column(name = "series_index", nullable = false, precision = 10, scale = 2, columnDefinition = "decimal(10,2) default 1.0")
    private BigDecimal seriesIndex = BigDecimal.ONE;

    // Default constructor for JPA
    public BookSeries() {}
    
    // Constructor with all fields
    public BookSeries(Book book, Series series, BigDecimal seriesIndex) {
        this.book = book;
        this.series = series;
        this.seriesIndex = seriesIndex != null ? seriesIndex : BigDecimal.ONE;
    }

    // Getters and setters
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public BigDecimal getSeriesIndex() {
        return seriesIndex;
    }

    public void setSeriesIndex(BigDecimal seriesIndex) {
        this.seriesIndex = seriesIndex;
    }

    // Composite key class
    public static record BookSeriesId(Book book, Series series) implements Serializable {}
}