package org.motpassants.infrastructure.adapter.out.metadata.openlibrary;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OpenLibraryMetadataProvider.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OpenLibraryMetadataProvider Unit Tests")
class OpenLibraryMetadataProviderTest {

    @Mock
    private OpenLibraryApiClient apiClient;

    private OpenLibraryMetadataProvider provider;

    @BeforeEach
    void setUp() {
        provider = new OpenLibraryMetadataProvider();
        provider.apiClient = apiClient;
        provider.enabled = true;
    }

    @Test
    @DisplayName("Should return provider name")
    void shouldReturnProviderName() {
        assertEquals("Open Library", provider.getProviderName());
    }

    @Test
    @DisplayName("Should return secondary priority")
    void shouldReturnSecondaryPriority() {
        assertEquals(20, provider.getPriority());
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
        verify(apiClient, never()).searchByIsbn(anyString(), anyInt());
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
        verify(apiClient, never()).searchByIsbn(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should search by ISBN and convert results")
    void shouldSearchByIsbnAndConvertResults() throws Exception {
        // Arrange
        OpenLibrarySearchResponse mockResponse = createMockResponse();
        when(apiClient.searchByIsbn(anyString(), anyInt()))
                .thenReturn(CompletableFuture.completedStage(mockResponse));

        // Act
        List<MetadataSearchResult> results = provider.searchByIsbn("978-0201633610").get();

        // Assert
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        
        MetadataSearchResult result = results.get(0);
        assertEquals("Design Patterns", result.getTitle());
        assertEquals("Elements of Reusable Object-Oriented Software", result.getSubtitle());
        assertEquals(List.of("Erich Gamma", "Richard Helm", "Ralph Johnson", "John Vlissides"), 
                     result.getAuthors());
        assertEquals("0201633612", result.getIsbn10());
        assertEquals("9780201633610", result.getIsbn13());
        assertEquals("Addison-Wesley Professional", result.getPublisher());
        assertEquals(LocalDate.of(1994, 1, 1), result.getPublishedDate());
        assertEquals(395, result.getPageCount());
        assertEquals(MetadataSource.OPEN_LIBRARY, result.getSource());
        
        verify(apiClient).searchByIsbn(eq("9780201633610"), eq(10));
    }

    @Test
    @DisplayName("Should search by title and author")
    void shouldSearchByTitleAndAuthor() throws Exception {
        // Arrange
        OpenLibrarySearchResponse mockResponse = createMockResponse();
        when(apiClient.searchBooks(anyString(), anyInt()))
                .thenReturn(CompletableFuture.completedStage(mockResponse));

        // Act
        List<MetadataSearchResult> results = provider.searchByTitleAndAuthor(
                "Design Patterns", "Gamma").get();

        // Assert
        assertFalse(results.isEmpty());
        verify(apiClient).searchBooks(eq("Design Patterns Gamma"), eq(10));
    }

    @Test
    @DisplayName("Should search by title only when author is null")
    void shouldSearchByTitleOnlyWhenAuthorIsNull() throws Exception {
        // Arrange
        OpenLibrarySearchResponse mockResponse = createMockResponse();
        when(apiClient.searchBooks(anyString(), anyInt()))
                .thenReturn(CompletableFuture.completedStage(mockResponse));

        // Act
        List<MetadataSearchResult> results = provider.searchByTitleAndAuthor(
                "Design Patterns", null).get();

        // Assert
        assertFalse(results.isEmpty());
        verify(apiClient).searchBooks(eq("Design Patterns"), eq(10));
    }

    @Test
    @DisplayName("Should return empty list when title is null or empty")
    void shouldReturnEmptyListWhenTitleIsNullOrEmpty() throws Exception {
        List<MetadataSearchResult> results1 = provider.searchByTitleAndAuthor(null, "Author").get();
        List<MetadataSearchResult> results2 = provider.searchByTitleAndAuthor("", "Author").get();

        assertTrue(results1.isEmpty());
        assertTrue(results2.isEmpty());
        verify(apiClient, never()).searchBooks(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should handle API errors gracefully")
    void shouldHandleApiErrorsGracefully() throws Exception {
        // Arrange
        CompletionStage<OpenLibrarySearchResponse> failedFuture = CompletableFuture.failedStage(
                new RuntimeException("API Error"));
        when(apiClient.searchByIsbn(anyString(), anyInt()))
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
        when(apiClient.searchByIsbn(anyString(), anyInt()))
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
        OpenLibrarySearchResponse emptyResponse = new OpenLibrarySearchResponse();
        emptyResponse.setDocs(null);
        when(apiClient.searchByIsbn(anyString(), anyInt()))
                .thenReturn(CompletableFuture.completedStage(emptyResponse));

        // Act
        List<MetadataSearchResult> results = provider.searchByIsbn("1234567890").get();

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should extract ISBNs from mixed list")
    void shouldExtractIsbnsFromMixedList() throws Exception {
        // Arrange
        OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
        OpenLibraryDoc doc = new OpenLibraryDoc();
        doc.setKey("/works/OL123W");
        doc.setTitle("Test Book");
        doc.setIsbn(List.of("0-12-345678-9", "978-0-12-345678-9", "invalid"));
        
        response.setDocs(List.of(doc));
        
        when(apiClient.searchByIsbn(anyString(), anyInt()))
                .thenReturn(CompletableFuture.completedStage(response));

        // Act
        List<MetadataSearchResult> results = provider.searchByIsbn("1234567890").get();

        // Assert
        assertEquals(1, results.size());
        MetadataSearchResult result = results.get(0);
        assertEquals("0123456789", result.getIsbn10());
        assertEquals("9780123456789", result.getIsbn13());
    }

    @Test
    @DisplayName("Should generate cover image URL")
    void shouldGenerateCoverImageUrl() throws Exception {
        // Arrange
        OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
        OpenLibraryDoc doc = new OpenLibraryDoc();
        doc.setKey("/works/OL123W");
        doc.setTitle("Test Book");
        doc.setCoverId(12345L);
        
        response.setDocs(List.of(doc));
        
        when(apiClient.searchByIsbn(anyString(), anyInt()))
                .thenReturn(CompletableFuture.completedStage(response));

        // Act
        List<MetadataSearchResult> results = provider.searchByIsbn("1234567890").get();

        // Assert
        assertEquals(1, results.size());
        MetadataSearchResult result = results.get(0);
        assertEquals("https://covers.openlibrary.org/b/id/12345-L.jpg", result.getCoverImageUrl());
    }

    /**
     * Helper method to create a mock Open Library API response.
     */
    private OpenLibrarySearchResponse createMockResponse() {
        OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
        response.setNumFound(1);
        
        OpenLibraryDoc doc = new OpenLibraryDoc();
        doc.setKey("/works/OL123W");
        doc.setTitle("Design Patterns");
        doc.setSubtitle("Elements of Reusable Object-Oriented Software");
        doc.setAuthorName(List.of("Erich Gamma", "Richard Helm", "Ralph Johnson", "John Vlissides"));
        doc.setIsbn(List.of("0201633612", "9780201633610"));
        doc.setPublisher(List.of("Addison-Wesley Professional"));
        doc.setPublishYear(List.of(1994));
        doc.setNumberOfPagesMedian(395);
        doc.setLanguage(List.of("eng"));
        doc.setSubject(List.of("Software Design", "Object-Oriented Programming", "Design Patterns"));
        doc.setFirstSentence(List.of("This book isn't an introduction to object-oriented technology or design."));
        doc.setCoverId(12345L);
        
        response.setDocs(List.of(doc));
        
        return response;
    }
}
