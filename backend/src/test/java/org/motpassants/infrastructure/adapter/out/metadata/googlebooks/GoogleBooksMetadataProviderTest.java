package org.motpassants.infrastructure.adapter.out.metadata.googlebooks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.motpassants.domain.core.model.MetadataSearchResult;
import org.motpassants.domain.core.model.MetadataSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GoogleBooksMetadataProvider.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleBooksMetadataProvider Unit Tests")
class GoogleBooksMetadataProviderTest {

    @Mock
    private GoogleBooksApiClient apiClient;

    private GoogleBooksMetadataProvider provider;

    @BeforeEach
    void setUp() {
        provider = new GoogleBooksMetadataProvider();
        provider.apiClient = apiClient;
        provider.enabled = true;
        provider.apiKey = Optional.empty();
    }

    @Test
    @DisplayName("Should return provider name")
    void shouldReturnProviderName() {
        assertEquals("Google Books", provider.getProviderName());
    }

    @Test
    @DisplayName("Should return high priority")
    void shouldReturnHighPriority() {
        assertEquals(10, provider.getPriority());
    }

    @Test
    @DisplayName("Should be enabled by default")
    void shouldBeEnabledByDefault() {
        assertTrue(provider.isEnabled());
    }

    @Test
    @DisplayName("Should return empty list when disabled")
    void shouldReturnEmptyListWhenDisabled() throws Exception {
        provider.enabled = false;

        List<MetadataSearchResult> results = provider.searchByIsbn("1234567890").get();

        assertTrue(results.isEmpty());
        verify(apiClient, never()).searchBooks(anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("Should return empty list when ISBN is null or empty")
    void shouldReturnEmptyListWhenIsbnIsNullOrEmpty() throws Exception {
        List<MetadataSearchResult> results1 = provider.searchByIsbn(null).get();
        List<MetadataSearchResult> results2 = provider.searchByIsbn("").get();
        List<MetadataSearchResult> results3 = provider.searchByIsbn("   ").get();

        assertTrue(results1.isEmpty());
        assertTrue(results2.isEmpty());
        assertTrue(results3.isEmpty());
        verify(apiClient, never()).searchBooks(anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("Should search by ISBN and convert results")
    void shouldSearchByIsbnAndConvertResults() throws Exception {
        // Arrange
        GoogleBooksResponse mockResponse = createMockResponse();
        when(apiClient.searchBooks(anyString(), anyInt(), isNull()))
                .thenReturn(CompletableFuture.completedStage(mockResponse));

        // Act
        List<MetadataSearchResult> results = provider.searchByIsbn("978-0201896831").get();

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        
        MetadataSearchResult result = results.get(0);
        assertEquals("The Art of Computer Programming", result.getTitle());
        assertEquals("Volume 1", result.getSubtitle());
        assertEquals(List.of("Donald Knuth"), result.getAuthors());
        assertEquals("0201896834", result.getIsbn10());
        assertEquals("9780201896831", result.getIsbn13());
        assertEquals("Addison-Wesley", result.getPublisher());
        assertEquals(LocalDate.of(1997, 1, 1), result.getPublishedDate());
        assertEquals(672, result.getPageCount());
        assertEquals(MetadataSource.GOOGLE_BOOKS, result.getSource());
        
        verify(apiClient).searchBooks(eq("isbn:9780201896831"), eq(10), isNull());
    }

    @Test
    @DisplayName("Should search by title and author")
    void shouldSearchByTitleAndAuthor() throws Exception {
        // Arrange
        GoogleBooksResponse mockResponse = createMockResponse();
        when(apiClient.searchBooks(anyString(), anyInt(), isNull()))
                .thenReturn(CompletableFuture.completedStage(mockResponse));

        // Act
        List<MetadataSearchResult> results = provider.searchByTitleAndAuthor(
                "Computer Programming", "Donald Knuth").get();

        // Assert
        assertFalse(results.isEmpty());
        verify(apiClient).searchBooks(
                eq("intitle:Computer Programming+inauthor:Donald Knuth"), eq(10), isNull());
    }

    @Test
    @DisplayName("Should search by title only when author is null")
    void shouldSearchByTitleOnlyWhenAuthorIsNull() throws Exception {
        // Arrange
        GoogleBooksResponse mockResponse = createMockResponse();
        when(apiClient.searchBooks(anyString(), anyInt(), isNull()))
                .thenReturn(CompletableFuture.completedStage(mockResponse));

        // Act
        List<MetadataSearchResult> results = provider.searchByTitleAndAuthor(
                "Computer Programming", null).get();

        // Assert
        assertFalse(results.isEmpty());
        verify(apiClient).searchBooks(eq("intitle:Computer Programming"), eq(10), isNull());
    }

    @Test
    @DisplayName("Should return empty list when title is null or empty")
    void shouldReturnEmptyListWhenTitleIsNullOrEmpty() throws Exception {
        List<MetadataSearchResult> results1 = provider.searchByTitleAndAuthor(null, "Author").get();
        List<MetadataSearchResult> results2 = provider.searchByTitleAndAuthor("", "Author").get();

        assertTrue(results1.isEmpty());
        assertTrue(results2.isEmpty());
        verify(apiClient, never()).searchBooks(anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("Should handle API errors gracefully")
    void shouldHandleApiErrorsGracefully() throws Exception {
        // Arrange
        CompletionStage<GoogleBooksResponse> failedFuture = CompletableFuture.failedStage(
                new RuntimeException("API Error"));
        when(apiClient.searchBooks(anyString(), anyInt(), isNull()))
                .thenReturn(failedFuture);

        // Act
        List<MetadataSearchResult> results = provider.searchByIsbn("1234567890").get();

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle null API response")
    void shouldHandleNullApiResponse() throws Exception {
        // Arrange
        when(apiClient.searchBooks(anyString(), anyInt(), isNull()))
                .thenReturn(CompletableFuture.completedStage(null));

        // Act
        List<MetadataSearchResult> results = provider.searchByIsbn("1234567890").get();

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty API response")
    void shouldHandleEmptyApiResponse() throws Exception {
        // Arrange
        GoogleBooksResponse emptyResponse = new GoogleBooksResponse();
        emptyResponse.setItems(null);
        when(apiClient.searchBooks(anyString(), anyInt(), isNull()))
                .thenReturn(CompletableFuture.completedStage(emptyResponse));

        // Act
        List<MetadataSearchResult> results = provider.searchByIsbn("1234567890").get();

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should use API key when provided")
    void shouldUseApiKeyWhenProvided() throws Exception {
        // Arrange
        provider.apiKey = Optional.of("test-api-key");
        GoogleBooksResponse mockResponse = createMockResponse();
        when(apiClient.searchBooks(anyString(), anyInt(), anyString()))
                .thenReturn(CompletableFuture.completedStage(mockResponse));

        // Act
        provider.searchByIsbn("1234567890").get();

        // Assert
        verify(apiClient).searchBooks(anyString(), eq(10), eq("test-api-key"));
    }

    /**
     * Helper method to create a mock Google Books API response.
     */
    private GoogleBooksResponse createMockResponse() {
        GoogleBooksResponse response = new GoogleBooksResponse();
        response.setTotalItems(1);
        
        GoogleBooksVolume volume = new GoogleBooksVolume();
        volume.setId("abc123");
        
        GoogleBooksVolumeInfo info = new GoogleBooksVolumeInfo();
        info.setTitle("The Art of Computer Programming");
        info.setSubtitle("Volume 1");
        info.setAuthors(List.of("Donald Knuth"));
        info.setPublisher("Addison-Wesley");
        info.setPublishedDate("1997");
        info.setPageCount(672);
        info.setLanguage("en");
        info.setDescription("A comprehensive book on algorithms");
        
        GoogleBooksIndustryIdentifier isbn10 = new GoogleBooksIndustryIdentifier();
        isbn10.setType("ISBN_10");
        isbn10.setIdentifier("0201896834");
        
        GoogleBooksIndustryIdentifier isbn13 = new GoogleBooksIndustryIdentifier();
        isbn13.setType("ISBN_13");
        isbn13.setIdentifier("9780201896831");
        
        info.setIndustryIdentifiers(List.of(isbn10, isbn13));
        
        GoogleBooksImageLinks imageLinks = new GoogleBooksImageLinks();
        imageLinks.setThumbnail("https://example.com/cover.jpg");
        info.setImageLinks(imageLinks);
        
        volume.setVolumeInfo(info);
        response.setItems(List.of(volume));
        
        return response;
    }
}
