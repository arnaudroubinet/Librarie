package org.motpassants.application.service;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.MetadataSearchResult;
import org.motpassants.domain.core.model.MetadataSource;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.MetadataProviderPort;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for MetadataService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetadataService Unit Tests")
class MetadataServiceTest {

    @Mock
    private Instance<MetadataProviderPort> providersInstance;

    @Mock
    private MetadataProviderPort googleBooksProvider;

    @Mock
    private MetadataProviderPort openLibraryProvider;

    @Mock
    private BookRepository bookRepository;

    private MetadataService service;

    @BeforeEach
    void setUp() {
        service = new MetadataService();
        service.providers = providersInstance;
        service.bookRepository = bookRepository;

        // Setup provider defaults - use lenient to avoid unnecessary stubbing warnings
        lenient().when(googleBooksProvider.getProviderName()).thenReturn("Google Books");
        lenient().when(googleBooksProvider.isEnabled()).thenReturn(true);
        lenient().when(googleBooksProvider.getPriority()).thenReturn(10);

        lenient().when(openLibraryProvider.getProviderName()).thenReturn("Open Library");
        lenient().when(openLibraryProvider.isEnabled()).thenReturn(true);
        lenient().when(openLibraryProvider.getPriority()).thenReturn(20);
    }

    @Test
    @DisplayName("Should search metadata by ISBN using all enabled providers")
    void shouldSearchMetadataByIsbnUsingAllEnabledProviders() throws Exception {
        // Arrange
        MetadataSearchResult googleResult = createMockResult(MetadataSource.GOOGLE_BOOKS, "1234567890", 0.9);
        MetadataSearchResult openLibResult = createMockResult(MetadataSource.OPEN_LIBRARY, "1234567890", 0.8);

        when(providersInstance.stream()).thenReturn(Stream.of(googleBooksProvider, openLibraryProvider));
        when(googleBooksProvider.searchByIsbn(anyString()))
                .thenReturn(CompletableFuture.completedFuture(List.of(googleResult)));
        when(openLibraryProvider.searchByIsbn(anyString()))
                .thenReturn(CompletableFuture.completedFuture(List.of(openLibResult)));

        // Act
        List<MetadataSearchResult> results = service.searchMetadataByIsbn("1234567890").get();

        // Assert
        assertEquals(1, results.size()); // Deduped by ISBN
        assertEquals(MetadataSource.GOOGLE_BOOKS, results.get(0).getSource()); // Higher confidence wins
        verify(googleBooksProvider).searchByIsbn("1234567890");
        verify(openLibraryProvider).searchByIsbn("1234567890");
    }

    @Test
    @DisplayName("Should return empty list when no providers enabled")
    void shouldReturnEmptyListWhenNoProvidersEnabled() throws Exception {
        // Arrange
        when(providersInstance.stream()).thenReturn(Stream.empty());

        // Act
        List<MetadataSearchResult> results = service.searchMetadataByIsbn("1234567890").get();

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when ISBN is null or empty")
    void shouldReturnEmptyListWhenIsbnIsNullOrEmpty() throws Exception {
        List<MetadataSearchResult> results1 = service.searchMetadataByIsbn(null).get();
        List<MetadataSearchResult> results2 = service.searchMetadataByIsbn("").get();
        List<MetadataSearchResult> results3 = service.searchMetadataByIsbn("   ").get();

        assertTrue(results1.isEmpty());
        assertTrue(results2.isEmpty());
        assertTrue(results3.isEmpty());
    }

    @Test
    @DisplayName("Should search by title and author using all enabled providers")
    void shouldSearchByTitleAndAuthorUsingAllEnabledProviders() throws Exception {
        // Arrange
        MetadataSearchResult googleResult = createMockResult(MetadataSource.GOOGLE_BOOKS, "1234567890", 0.85);
        MetadataSearchResult openLibResult = createMockResult(MetadataSource.OPEN_LIBRARY, "1234567890", 0.75);

        when(providersInstance.stream()).thenReturn(Stream.of(googleBooksProvider, openLibraryProvider));
        when(googleBooksProvider.searchByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(List.of(googleResult)));
        when(openLibraryProvider.searchByTitleAndAuthor(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(List.of(openLibResult)));

        // Act
        List<MetadataSearchResult> results = service.searchMetadataByTitleAndAuthor("Test Book", "Test Author").get();

        // Assert
        assertEquals(1, results.size()); // Deduped by ISBN
        assertEquals(MetadataSource.GOOGLE_BOOKS, results.get(0).getSource()); // Higher confidence wins
        verify(googleBooksProvider).searchByTitleAndAuthor("Test Book", "Test Author");
        verify(openLibraryProvider).searchByTitleAndAuthor("Test Book", "Test Author");
    }

    @Test
    @DisplayName("Should return empty list when title is null or empty")
    void shouldReturnEmptyListWhenTitleIsNullOrEmpty() throws Exception {
        List<MetadataSearchResult> results1 = service.searchMetadataByTitleAndAuthor(null, "Author").get();
        List<MetadataSearchResult> results2 = service.searchMetadataByTitleAndAuthor("", "Author").get();

        assertTrue(results1.isEmpty());
        assertTrue(results2.isEmpty());
    }

    @Test
    @DisplayName("Should sort results by confidence score descending")
    void shouldSortResultsByConfidenceScoreDescending() throws Exception {
        // Arrange
        MetadataSearchResult result1 = createMockResult(MetadataSource.GOOGLE_BOOKS, "111", 0.6);
        MetadataSearchResult result2 = createMockResult(MetadataSource.OPEN_LIBRARY, "222", 0.9);
        MetadataSearchResult result3 = createMockResult(MetadataSource.GOOGLE_BOOKS, "333", 0.7);

        when(providersInstance.stream()).thenReturn(Stream.of(googleBooksProvider));
        when(googleBooksProvider.searchByIsbn(anyString()))
                .thenReturn(CompletableFuture.completedFuture(List.of(result1, result2, result3)));

        // Act
        List<MetadataSearchResult> results = service.searchMetadataByIsbn("test").get();

        // Assert
        assertEquals(3, results.size());
        assertEquals(0.9, results.get(0).getConfidenceScore());
        assertEquals(0.7, results.get(1).getConfidenceScore());
        assertEquals(0.6, results.get(2).getConfidenceScore());
    }

    @Test
    @DisplayName("Should deduplicate results by ISBN and keep highest confidence")
    void shouldDeduplicateResultsByIsbnAndKeepHighestConfidence() throws Exception {
        // Arrange
        MetadataSearchResult googleResult = createMockResult(MetadataSource.GOOGLE_BOOKS, "1234567890", 0.9);
        MetadataSearchResult openLibResult = createMockResult(MetadataSource.OPEN_LIBRARY, "1234567890", 0.7);

        when(providersInstance.stream()).thenReturn(Stream.of(googleBooksProvider, openLibraryProvider));
        when(googleBooksProvider.searchByIsbn(anyString()))
                .thenReturn(CompletableFuture.completedFuture(List.of(googleResult)));
        when(openLibraryProvider.searchByIsbn(anyString()))
                .thenReturn(CompletableFuture.completedFuture(List.of(openLibResult)));

        // Act
        List<MetadataSearchResult> results = service.searchMetadataByIsbn("1234567890").get();

        // Assert
        assertEquals(1, results.size());
        assertEquals(MetadataSource.GOOGLE_BOOKS, results.get(0).getSource());
        assertEquals(0.9, results.get(0).getConfidenceScore());
    }

    @Test
    @DisplayName("Should apply metadata to book successfully")
    void shouldApplyMetadataToBookSuccessfully() {
        // Arrange
        UUID bookId = UUID.randomUUID();
        Book book = new Book("Original Title", "/path/to/book");
        book.setId(bookId);

        MetadataSearchResult metadata = MetadataSearchResult.builder()
                .title("Updated Title")
                .subtitle("Updated Subtitle")
                .description("Updated Description")
                .isbn13("9781234567890")
                .pageCount(500)
                .publishedDate(LocalDate.of(2020, 1, 15))
                .language("en")
                .source(MetadataSource.GOOGLE_BOOKS)
                .providerBookId("abc123")
                .confidenceScore(0.95)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Book updatedBook = service.applyMetadataToBook(bookId, metadata, false);

        // Assert
        assertNotNull(updatedBook);
        assertEquals("Updated Title", updatedBook.getTitle());
        assertEquals("Updated Description", updatedBook.getDescription());
        assertEquals("9781234567890", updatedBook.getIsbn());
        assertEquals(500, updatedBook.getPageCount());
        assertEquals(LocalDate.of(2020, 1, 15), updatedBook.getPublicationDate());
        assertEquals(2020, updatedBook.getPublicationYear());
        assertEquals("en", updatedBook.getLanguage());

        // Verify metadata stored in JSONB
        Map<String, Object> storedMetadata = updatedBook.getMetadata();
        assertNotNull(storedMetadata);
        assertEquals("GOOGLE_BOOKS", storedMetadata.get("metadata_source"));
        assertEquals("abc123", storedMetadata.get("provider_book_id"));
        assertEquals(0.95, storedMetadata.get("confidence_score"));
        assertEquals("Updated Subtitle", storedMetadata.get("subtitle"));

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(updatedBook);
    }

    @Test
    @DisplayName("Should throw exception when applying metadata to non-existent book")
    void shouldThrowExceptionWhenApplyingMetadataToNonExistentBook() {
        // Arrange
        UUID bookId = UUID.randomUUID();
        MetadataSearchResult metadata = createMockResult(MetadataSource.GOOGLE_BOOKS, "123", 0.9);

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            service.applyMetadataToBook(bookId, metadata, false);
        });
    }

    @Test
    @DisplayName("Should throw exception when bookId is null")
    void shouldThrowExceptionWhenBookIdIsNull() {
        MetadataSearchResult metadata = createMockResult(MetadataSource.GOOGLE_BOOKS, "123", 0.9);

        assertThrows(IllegalArgumentException.class, () -> {
            service.applyMetadataToBook(null, metadata, false);
        });
    }

    @Test
    @DisplayName("Should throw exception when metadata is null")
    void shouldThrowExceptionWhenMetadataIsNull() {
        UUID bookId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> {
            service.applyMetadataToBook(bookId, null, false);
        });
    }

    @Test
    @DisplayName("Should skip disabled providers")
    void shouldSkipDisabledProviders() throws Exception {
        // Arrange
        when(openLibraryProvider.isEnabled()).thenReturn(false);
        when(providersInstance.stream()).thenReturn(Stream.of(googleBooksProvider, openLibraryProvider));
        
        MetadataSearchResult googleResult = createMockResult(MetadataSource.GOOGLE_BOOKS, "123", 0.9);
        when(googleBooksProvider.searchByIsbn(anyString()))
                .thenReturn(CompletableFuture.completedFuture(List.of(googleResult)));

        // Act
        List<MetadataSearchResult> results = service.searchMetadataByIsbn("123").get();

        // Assert
        assertEquals(1, results.size());
        verify(googleBooksProvider).searchByIsbn("123");
        verify(openLibraryProvider, never()).searchByIsbn(anyString());
    }

    /**
     * Helper method to create mock metadata search results.
     */
    private MetadataSearchResult createMockResult(MetadataSource source, String isbn, double confidence) {
        return MetadataSearchResult.builder()
                .title("Test Book")
                .author("Test Author")
                .isbn13(isbn)
                .source(source)
                .confidenceScore(confidence)
                .build();
    }
}
