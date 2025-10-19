package org.motpassants.infrastructure.adapter.in.rest.dto.metadata;

import org.motpassants.domain.core.model.MetadataSearchResult;
import org.motpassants.domain.core.model.MetadataSource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for metadata search results returned to API clients.
 */
public class MetadataSearchResponseDto {
    
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
    private String source;
    private String providerBookId;
    private Double confidenceScore;

    public MetadataSearchResponseDto() {
    }

    /**
     * Convert domain model to DTO.
     */
    public static MetadataSearchResponseDto fromDomain(MetadataSearchResult result) {
        MetadataSearchResponseDto dto = new MetadataSearchResponseDto();
        dto.title = result.getTitle();
        dto.subtitle = result.getSubtitle();
        dto.authors = result.getAuthors();
        dto.isbn10 = result.getIsbn10();
        dto.isbn13 = result.getIsbn13();
        dto.description = result.getDescription();
        dto.pageCount = result.getPageCount();
        dto.publisher = result.getPublisher();
        dto.publishedDate = result.getPublishedDate();
        dto.language = result.getLanguage();
        dto.categories = result.getCategories();
        dto.coverImageUrl = result.getCoverImageUrl();
        dto.averageRating = result.getAverageRating();
        dto.ratingsCount = result.getRatingsCount();
        dto.source = result.getSource().name();
        dto.providerBookId = result.getProviderBookId();
        dto.confidenceScore = result.getConfidenceScore();
        return dto;
    }

    /**
     * Convert list of domain models to DTOs.
     */
    public static List<MetadataSearchResponseDto> fromDomainList(List<MetadataSearchResult> results) {
        return results.stream()
                .map(MetadataSearchResponseDto::fromDomain)
                .collect(Collectors.toList());
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getRatingsCount() {
        return ratingsCount;
    }

    public void setRatingsCount(Integer ratingsCount) {
        this.ratingsCount = ratingsCount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getProviderBookId() {
        return providerBookId;
    }

    public void setProviderBookId(String providerBookId) {
        this.providerBookId = providerBookId;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
}
