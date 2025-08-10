package org.roubinet.librarie.domain.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @Column(name = "series_index", nullable = false, precision = 10, scale = 2, columnDefinition = "decimal(10,2) default 1.0")
    private BigDecimal seriesIndex = BigDecimal.ONE;

    // Default constructor
    public BookSeries() {}

    public BookSeries(Book book, Series series, BigDecimal seriesIndex) {
        this.book = book;
        this.series = series;
        this.seriesIndex = seriesIndex;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookSeries)) return false;
        BookSeries that = (BookSeries) o;
        return Objects.equals(book, that.book) && Objects.equals(series, that.series);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, series);
    }

    // Composite key class
    public static class BookSeriesId implements Serializable {
        private Book book;
        private Series series;

        public BookSeriesId() {}

        public BookSeriesId(Book book, Series series) {
            this.book = book;
            this.series = series;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BookSeriesId)) return false;
            BookSeriesId that = (BookSeriesId) o;
            return Objects.equals(book, that.book) && Objects.equals(series, that.series);
        }

        @Override
        public int hashCode() {
            return Objects.hash(book, series);
        }
    }
}