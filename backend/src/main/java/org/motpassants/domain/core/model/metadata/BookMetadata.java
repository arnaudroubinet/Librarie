package org.motpassants.domain.core.model.metadata;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Rich metadata model for books with all possible fields from external providers.
 * Used for metadata exchange between providers and the core domain.
 */
public record BookMetadata(
    // Basic information
    String isbn,
    String isbn10,
    String isbn13,
    String title,
    String subtitle,
    String originalTitle,
    String titleSort,
    String description,
    String language,
    
    // Publication details
    String publisher,
    LocalDate publicationDate,
    Integer publicationYear,
    Integer pageCount,
    
    // Identifiers
    String googleBooksId,
    String openLibraryId,
    String goodreadsId,
    String asin,
    String doi,
    String lccn,
    String oclc,
    
    // Authors
    List<AuthorMetadata> authors,
    
    // Classification
    Set<String> subjects,
    Set<String> genres,
    Set<String> tags,
    String deweyDecimal,
    String lcc,
    
    // Series
    String seriesName,
    Integer seriesIndex,
    
    // Physical attributes
    String format,
    String binding,
    String dimensions,
    String weight,
    
    // Cover and images
    String smallThumbnail,
    String thumbnail,
    String mediumImage,
    String largeImage,
    String extraLargeImage,
    
    // Ratings and reviews
    Double averageRating,
    Integer ratingsCount,
    
    // Provider source
    String providerId,
    String providerName,
    Double confidence
) {
    
    /**
     * Builder for constructing BookMetadata incrementally.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String isbn;
        private String isbn10;
        private String isbn13;
        private String title;
        private String subtitle;
        private String originalTitle;
        private String titleSort;
        private String description;
        private String language;
        private String publisher;
        private LocalDate publicationDate;
        private Integer publicationYear;
        private Integer pageCount;
        private String googleBooksId;
        private String openLibraryId;
        private String goodreadsId;
        private String asin;
        private String doi;
        private String lccn;
        private String oclc;
        private List<AuthorMetadata> authors;
        private Set<String> subjects;
        private Set<String> genres;
        private Set<String> tags;
        private String deweyDecimal;
        private String lcc;
        private String seriesName;
        private Integer seriesIndex;
        private String format;
        private String binding;
        private String dimensions;
        private String weight;
        private String smallThumbnail;
        private String thumbnail;
        private String mediumImage;
        private String largeImage;
        private String extraLargeImage;
        private Double averageRating;
        private Integer ratingsCount;
        private String providerId;
        private String providerName;
        private Double confidence;
        
        public Builder isbn(String isbn) { this.isbn = isbn; return this; }
        public Builder isbn10(String isbn10) { this.isbn10 = isbn10; return this; }
        public Builder isbn13(String isbn13) { this.isbn13 = isbn13; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder subtitle(String subtitle) { this.subtitle = subtitle; return this; }
        public Builder originalTitle(String originalTitle) { this.originalTitle = originalTitle; return this; }
        public Builder titleSort(String titleSort) { this.titleSort = titleSort; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder language(String language) { this.language = language; return this; }
        public Builder publisher(String publisher) { this.publisher = publisher; return this; }
        public Builder publicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; return this; }
        public Builder publicationYear(Integer publicationYear) { this.publicationYear = publicationYear; return this; }
        public Builder pageCount(Integer pageCount) { this.pageCount = pageCount; return this; }
        public Builder googleBooksId(String googleBooksId) { this.googleBooksId = googleBooksId; return this; }
        public Builder openLibraryId(String openLibraryId) { this.openLibraryId = openLibraryId; return this; }
        public Builder goodreadsId(String goodreadsId) { this.goodreadsId = goodreadsId; return this; }
        public Builder asin(String asin) { this.asin = asin; return this; }
        public Builder doi(String doi) { this.doi = doi; return this; }
        public Builder lccn(String lccn) { this.lccn = lccn; return this; }
        public Builder oclc(String oclc) { this.oclc = oclc; return this; }
        public Builder authors(List<AuthorMetadata> authors) { this.authors = authors; return this; }
        public Builder subjects(Set<String> subjects) { this.subjects = subjects; return this; }
        public Builder genres(Set<String> genres) { this.genres = genres; return this; }
        public Builder tags(Set<String> tags) { this.tags = tags; return this; }
        public Builder deweyDecimal(String deweyDecimal) { this.deweyDecimal = deweyDecimal; return this; }
        public Builder lcc(String lcc) { this.lcc = lcc; return this; }
        public Builder seriesName(String seriesName) { this.seriesName = seriesName; return this; }
        public Builder seriesIndex(Integer seriesIndex) { this.seriesIndex = seriesIndex; return this; }
        public Builder format(String format) { this.format = format; return this; }
        public Builder binding(String binding) { this.binding = binding; return this; }
        public Builder dimensions(String dimensions) { this.dimensions = dimensions; return this; }
        public Builder weight(String weight) { this.weight = weight; return this; }
        public Builder smallThumbnail(String smallThumbnail) { this.smallThumbnail = smallThumbnail; return this; }
        public Builder thumbnail(String thumbnail) { this.thumbnail = thumbnail; return this; }
        public Builder mediumImage(String mediumImage) { this.mediumImage = mediumImage; return this; }
        public Builder largeImage(String largeImage) { this.largeImage = largeImage; return this; }
        public Builder extraLargeImage(String extraLargeImage) { this.extraLargeImage = extraLargeImage; return this; }
        public Builder averageRating(Double averageRating) { this.averageRating = averageRating; return this; }
        public Builder ratingsCount(Integer ratingsCount) { this.ratingsCount = ratingsCount; return this; }
        public Builder providerId(String providerId) { this.providerId = providerId; return this; }
        public Builder providerName(String providerName) { this.providerName = providerName; return this; }
        public Builder confidence(Double confidence) { this.confidence = confidence; return this; }
        
        public BookMetadata build() {
            return new BookMetadata(
                isbn, isbn10, isbn13, title, subtitle, originalTitle, titleSort, description, language,
                publisher, publicationDate, publicationYear, pageCount,
                googleBooksId, openLibraryId, goodreadsId, asin, doi, lccn, oclc,
                authors, subjects, genres, tags, deweyDecimal, lcc,
                seriesName, seriesIndex, format, binding, dimensions, weight,
                smallThumbnail, thumbnail, mediumImage, largeImage, extraLargeImage,
                averageRating, ratingsCount, providerId, providerName, confidence
            );
        }
    }
}