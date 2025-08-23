package org.motpassants.domain.core.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain model representing a batch edit request with metadata changes.
 * Supports partial updates where null fields are ignored.
 */
public record BatchEditRequest(
    Optional<String> title,
    Optional<String> subtitle,
    Optional<String> description,
    Optional<List<String>> authors,
    Optional<UUID> seriesId,
    Optional<Integer> seriesPosition,
    Optional<List<String>> tags,
    Optional<String> language,
    Optional<String> publisher,
    Optional<String> isbn,
    Optional<Boolean> addTags,  // If true, tags are added to existing; if false, tags replace existing
    Optional<Boolean> addAuthors  // If true, authors are added to existing; if false, authors replace existing
) {
    
    /**
     * Creates a builder for constructing batch edit requests.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder for BatchEditRequest.
     */
    public static class Builder {
        private Optional<String> title = Optional.empty();
        private Optional<String> subtitle = Optional.empty();
        private Optional<String> description = Optional.empty();
        private Optional<List<String>> authors = Optional.empty();
        private Optional<UUID> seriesId = Optional.empty();
        private Optional<Integer> seriesPosition = Optional.empty();
        private Optional<List<String>> tags = Optional.empty();
        private Optional<String> language = Optional.empty();
        private Optional<String> publisher = Optional.empty();
        private Optional<String> isbn = Optional.empty();
        private Optional<Boolean> addTags = Optional.of(false);
        private Optional<Boolean> addAuthors = Optional.of(false);
        
        public Builder title(String title) {
            this.title = Optional.ofNullable(title);
            return this;
        }
        
        public Builder subtitle(String subtitle) {
            this.subtitle = Optional.ofNullable(subtitle);
            return this;
        }
        
        public Builder description(String description) {
            this.description = Optional.ofNullable(description);
            return this;
        }
        
        public Builder authors(List<String> authors) {
            this.authors = Optional.ofNullable(authors);
            return this;
        }
        
        public Builder seriesId(UUID seriesId) {
            this.seriesId = Optional.ofNullable(seriesId);
            return this;
        }
        
        public Builder seriesPosition(Integer seriesPosition) {
            this.seriesPosition = Optional.ofNullable(seriesPosition);
            return this;
        }
        
        public Builder tags(List<String> tags) {
            this.tags = Optional.ofNullable(tags);
            return this;
        }
        
        public Builder language(String language) {
            this.language = Optional.ofNullable(language);
            return this;
        }
        
        public Builder publisher(String publisher) {
            this.publisher = Optional.ofNullable(publisher);
            return this;
        }
        
        public Builder isbn(String isbn) {
            this.isbn = Optional.ofNullable(isbn);
            return this;
        }
        
        public Builder addTags(Boolean addTags) {
            this.addTags = Optional.ofNullable(addTags);
            return this;
        }
        
        public Builder addAuthors(Boolean addAuthors) {
            this.addAuthors = Optional.ofNullable(addAuthors);
            return this;
        }
        
        public BatchEditRequest build() {
            return new BatchEditRequest(
                title, subtitle, description, authors, seriesId, seriesPosition,
                tags, language, publisher, isbn, addTags, addAuthors
            );
        }
    }
    
    /**
     * Checks if this edit request has any changes to apply.
     */
    public boolean hasChanges() {
        return title.isPresent() || subtitle.isPresent() || description.isPresent() ||
               authors.isPresent() || seriesId.isPresent() || seriesPosition.isPresent() ||
               tags.isPresent() || language.isPresent() || publisher.isPresent() || isbn.isPresent();
    }
}