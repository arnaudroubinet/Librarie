package org.motpassants.infrastructure.adapter.out.metadata.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Document (book) from Open Library Search API.
 * Open Library returns a flattened structure with arrays for most fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenLibraryDoc {
    
    private String key; // e.g., "/works/OL45883W"
    private String title;
    private String subtitle;
    @JsonProperty("author_name")
    private List<String> authorName;
    @JsonProperty("author_key")
    private List<String> authorKey;
    private List<String> isbn;
    @JsonProperty("publisher")
    private List<String> publisher;
    @JsonProperty("publish_date")
    private List<String> publishDate;
    @JsonProperty("publish_year")
    private List<Integer> publishYear;
    @JsonProperty("number_of_pages_median")
    private Integer numberOfPagesMedian;
    private List<String> language;
    private List<String> subject;
    @JsonProperty("first_sentence")
    private List<String> firstSentence;
    @JsonProperty("cover_i")
    private Long coverId;
    @JsonProperty("cover_edition_key")
    private String coverEditionKey;
    @JsonProperty("edition_count")
    private Integer editionCount;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

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

    public List<String> getAuthorName() {
        return authorName;
    }

    public void setAuthorName(List<String> authorName) {
        this.authorName = authorName;
    }

    public List<String> getAuthorKey() {
        return authorKey;
    }

    public void setAuthorKey(List<String> authorKey) {
        this.authorKey = authorKey;
    }

    public List<String> getIsbn() {
        return isbn;
    }

    public void setIsbn(List<String> isbn) {
        this.isbn = isbn;
    }

    public List<String> getPublisher() {
        return publisher;
    }

    public void setPublisher(List<String> publisher) {
        this.publisher = publisher;
    }

    public List<String> getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(List<String> publishDate) {
        this.publishDate = publishDate;
    }

    public List<Integer> getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(List<Integer> publishYear) {
        this.publishYear = publishYear;
    }

    public Integer getNumberOfPagesMedian() {
        return numberOfPagesMedian;
    }

    public void setNumberOfPagesMedian(Integer numberOfPagesMedian) {
        this.numberOfPagesMedian = numberOfPagesMedian;
    }

    public List<String> getLanguage() {
        return language;
    }

    public void setLanguage(List<String> language) {
        this.language = language;
    }

    public List<String> getSubject() {
        return subject;
    }

    public void setSubject(List<String> subject) {
        this.subject = subject;
    }

    public List<String> getFirstSentence() {
        return firstSentence;
    }

    public void setFirstSentence(List<String> firstSentence) {
        this.firstSentence = firstSentence;
    }

    public Long getCoverId() {
        return coverId;
    }

    public void setCoverId(Long coverId) {
        this.coverId = coverId;
    }

    public String getCoverEditionKey() {
        return coverEditionKey;
    }

    public void setCoverEditionKey(String coverEditionKey) {
        this.coverEditionKey = coverEditionKey;
    }

    public Integer getEditionCount() {
        return editionCount;
    }

    public void setEditionCount(Integer editionCount) {
        this.editionCount = editionCount;
    }

    /**
     * Get the cover image URL from Open Library.
     * Format: https://covers.openlibrary.org/b/id/{cover_id}-{size}.jpg
     * Sizes: S (small), M (medium), L (large)
     */
    public String getCoverImageUrl() {
        if (coverId != null) {
            return "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
        }
        return null;
    }
}
