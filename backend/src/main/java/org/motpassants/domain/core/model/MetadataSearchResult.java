package org.motpassants.domain.core.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a metadata search result from an external provider.
 * Contains book information retrieved from metadata APIs.
 * Pure domain object without infrastructure dependencies.
 */
public class MetadataSearchResult {

    private String title;
    private String subtitle;
    private List<String> authors;
    private String isbn10;
    private String isbn13;
    private String description;
    private Integer pageCount;
    private String publisher;
    private LocalDate publishedDate;
    private String language;
    private List<String> categories;
    private String coverImageUrl;
    private Double averageRating;
    private Integer ratingsCount;
    private MetadataSource source;
    private String providerBookId;
    private Double confidenceScore; // 0.0 to 1.0, how confident we are in this result

    public MetadataSearchResult() {
        this.authors = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    // Builder pattern for convenient construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final MetadataSearchResult result = new MetadataSearchResult();

        public Builder title(String title) {
            result.title = title;
            return this;
        }

        public Builder subtitle(String subtitle) {
            result.subtitle = subtitle;
            return this;
        }

        public Builder authors(List<String> authors) {
            result.authors = authors != null ? new ArrayList<>(authors) : new ArrayList<>();
            return this;
        }

        public Builder author(String author) {
            if (author != null && !author.trim().isEmpty()) {
                result.authors.add(author);
            }
            return this;
        }

        public Builder isbn10(String isbn10) {
            result.isbn10 = isbn10;
            return this;
        }

        public Builder isbn13(String isbn13) {
            result.isbn13 = isbn13;
            return this;
        }

        public Builder description(String description) {
            result.description = description;
            return this;
        }

        public Builder pageCount(Integer pageCount) {
            result.pageCount = pageCount;
            return this;
        }

        public Builder publisher(String publisher) {
            result.publisher = publisher;
            return this;
        }

        public Builder publishedDate(LocalDate publishedDate) {
            result.publishedDate = publishedDate;
            return this;
        }

        public Builder language(String language) {
            result.language = language;
            return this;
        }

        public Builder categories(List<String> categories) {
            result.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
            return this;
        }

        public Builder category(String category) {
            if (category != null && !category.trim().isEmpty()) {
                result.categories.add(category);
            }
            return this;
        }

        public Builder coverImageUrl(String coverImageUrl) {
            result.coverImageUrl = coverImageUrl;
            return this;
        }

        public Builder averageRating(Double averageRating) {
            result.averageRating = averageRating;
            return this;
        }

        public Builder ratingsCount(Integer ratingsCount) {
            result.ratingsCount = ratingsCount;
            return this;
        }

        public Builder source(MetadataSource source) {
            result.source = source;
            return this;
        }

        public Builder providerBookId(String providerBookId) {
            result.providerBookId = providerBookId;
            return this;
        }

        public Builder confidenceScore(Double confidenceScore) {
            result.confidenceScore = confidenceScore;
            return this;
        }

        public MetadataSearchResult build() {
            // Set defaults if not provided
            if (result.source == null) {
                result.source = MetadataSource.UNKNOWN;
            }
            if (result.confidenceScore == null) {
                result.confidenceScore = 0.5;
            }
            return result;
        }
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public String getPublisher() {
        return publisher;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public String getLanguage() {
        return language;
    }

    public List<String> getCategories() {
        return categories;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public Integer getRatingsCount() {
        return ratingsCount;
    }

    public MetadataSource getSource() {
        return source;
    }

    public String getProviderBookId() {
        return providerBookId;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public void setRatingsCount(Integer ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public void setSource(MetadataSource source) {
        this.source = source;
    }

    public void setProviderBookId(String providerBookId) {
        this.providerBookId = providerBookId;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    @Override
    public String toString() {
        return "MetadataSearchResult{" +
                "title='" + title + '\'' +
                ", authors=" + authors +
                ", isbn13='" + isbn13 + '\'' +
                ", source=" + source +
                ", confidence=" + confidenceScore +
                '}';
    }
}
