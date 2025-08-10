package org.roubinet.librarie.domain.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * BookSeries record representing the immutable relationship between books and series.
 * A book can only be in one series (enforced by unique constraint on book_id).
 */
@Entity
@Table(name = "book_series", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"book_id"}, name = "uk_book_series_book_id")
})
@IdClass(BookSeries.BookSeriesId.class)
public record BookSeries(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    Book book,
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id") 
    Series series,
    
    @Column(name = "series_index", nullable = false, precision = 10, scale = 2, columnDefinition = "decimal(10,2) default 1.0")
    BigDecimal seriesIndex
) {
    
    public BookSeries {
        if (seriesIndex == null) {
            seriesIndex = BigDecimal.ONE;
        }
    }

    // Composite key class
    public static record BookSeriesId(Book book, Series series) implements Serializable {}
}