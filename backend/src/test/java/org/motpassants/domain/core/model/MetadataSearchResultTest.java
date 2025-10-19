package org.motpassants.domain.core.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MetadataSearchResult domain model.
 */
@DisplayName("MetadataSearchResult Unit Tests")
class MetadataSearchResultTest {

    @Test
    @DisplayName("Should create metadata result with builder pattern")
    void shouldCreateMetadataResultWithBuilder() {
        LocalDate publishedDate = LocalDate.of(2020, 1, 15);
        
        MetadataSearchResult result = MetadataSearchResult.builder()
                .title("The Art of Computer Programming")
                .subtitle("Volume 1")
                .author("Donald Knuth")
                .isbn13("9780201896831")
                .isbn10("0201896834")
                .description("A comprehensive book on algorithms")
                .pageCount(672)
                .publisher("Addison-Wesley")
                .publishedDate(publishedDate)
                .language("en")
                .category("Computer Science")
                .category("Programming")
                .coverImageUrl("https://example.com/cover.jpg")
                .averageRating(4.8)
                .ratingsCount(1250)
                .source(MetadataSource.GOOGLE_BOOKS)
                .providerBookId("abc123")
                .confidenceScore(0.95)
                .build();

        assertNotNull(result);
        assertEquals("The Art of Computer Programming", result.getTitle());
        assertEquals("Volume 1", result.getSubtitle());
        assertEquals(List.of("Donald Knuth"), result.getAuthors());
        assertEquals("9780201896831", result.getIsbn13());
        assertEquals("0201896834", result.getIsbn10());
        assertEquals("A comprehensive book on algorithms", result.getDescription());
        assertEquals(672, result.getPageCount());
        assertEquals("Addison-Wesley", result.getPublisher());
        assertEquals(publishedDate, result.getPublishedDate());
        assertEquals("en", result.getLanguage());
        assertEquals(List.of("Computer Science", "Programming"), result.getCategories());
        assertEquals("https://example.com/cover.jpg", result.getCoverImageUrl());
        assertEquals(4.8, result.getAverageRating());
        assertEquals(1250, result.getRatingsCount());
        assertEquals(MetadataSource.GOOGLE_BOOKS, result.getSource());
        assertEquals("abc123", result.getProviderBookId());
        assertEquals(0.95, result.getConfidenceScore());
    }

    @Test
    @DisplayName("Should handle multiple authors")
    void shouldHandleMultipleAuthors() {
        MetadataSearchResult result = MetadataSearchResult.builder()
                .title("Test Book")
                .author("Author One")
                .author("Author Two")
                .author("Author Three")
                .build();

        assertEquals(3, result.getAuthors().size());
        assertTrue(result.getAuthors().contains("Author One"));
        assertTrue(result.getAuthors().contains("Author Two"));
        assertTrue(result.getAuthors().contains("Author Three"));
    }

    @Test
    @DisplayName("Should set default values when not provided")
    void shouldSetDefaultValues() {
        MetadataSearchResult result = MetadataSearchResult.builder()
                .title("Minimal Book")
                .build();

        assertEquals(MetadataSource.UNKNOWN, result.getSource());
        assertEquals(0.5, result.getConfidenceScore());
        assertNotNull(result.getAuthors());
        assertTrue(result.getAuthors().isEmpty());
        assertNotNull(result.getCategories());
        assertTrue(result.getCategories().isEmpty());
    }

    @Test
    @DisplayName("Should filter out null or empty authors")
    void shouldFilterOutNullOrEmptyAuthors() {
        MetadataSearchResult result = MetadataSearchResult.builder()
                .title("Test Book")
                .author("Valid Author")
                .author(null)
                .author("")
                .author("   ")
                .build();

        assertEquals(1, result.getAuthors().size());
        assertEquals("Valid Author", result.getAuthors().get(0));
    }

    @Test
    @DisplayName("Should filter out null or empty categories")
    void shouldFilterOutNullOrEmptyCategories() {
        MetadataSearchResult result = MetadataSearchResult.builder()
                .title("Test Book")
                .category("Fiction")
                .category(null)
                .category("")
                .category("   ")
                .category("Drama")
                .build();

        assertEquals(2, result.getCategories().size());
        assertTrue(result.getCategories().contains("Fiction"));
        assertTrue(result.getCategories().contains("Drama"));
    }

    @Test
    @DisplayName("Should handle bulk author list")
    void shouldHandleBulkAuthorList() {
        List<String> authors = List.of("Author 1", "Author 2", "Author 3");
        
        MetadataSearchResult result = MetadataSearchResult.builder()
                .title("Test Book")
                .authors(authors)
                .build();

        assertEquals(3, result.getAuthors().size());
        assertEquals(authors, result.getAuthors());
    }

    @Test
    @DisplayName("Should handle bulk category list")
    void shouldHandleBulkCategoryList() {
        List<String> categories = List.of("Fiction", "Mystery", "Thriller");
        
        MetadataSearchResult result = MetadataSearchResult.builder()
                .title("Test Book")
                .categories(categories)
                .build();

        assertEquals(3, result.getCategories().size());
        assertEquals(categories, result.getCategories());
    }

    @Test
    @DisplayName("ToString should contain key information")
    void toStringShouldContainKeyInformation() {
        MetadataSearchResult result = MetadataSearchResult.builder()
                .title("Test Book")
                .author("Test Author")
                .isbn13("1234567890123")
                .source(MetadataSource.GOOGLE_BOOKS)
                .confidenceScore(0.8)
                .build();

        String toString = result.toString();
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains("Test Book"));
        assertTrue(toString.contains("1234567890123"));
        // Source is printed but uses enum's display name
        assertTrue(toString.contains("source="));
    }
}
