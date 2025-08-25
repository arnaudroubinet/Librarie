package org.motpassants.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.metadata.AuthorMetadata;
import org.motpassants.domain.core.model.metadata.BookMetadata;
import org.motpassants.domain.core.model.metadata.MetadataPreview;
import org.motpassants.domain.core.model.metadata.ProviderStatus;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.LoggingPort;
import org.motpassants.domain.port.out.metadata.MetadataAggregatorPort;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MetadataService.
 * Tests DATA-002: Metadata Editing and External Providers business logic.
 */
@ExtendWith(MockitoExtension.class)
public class MetadataServiceTest {

    @Mock
    private MetadataAggregatorPort metadataAggregatorPort;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoggingPort loggingPort;

    private MetadataService metadataService;

    @BeforeEach
    void setUp() {
        metadataService = new MetadataService(metadataAggregatorPort, bookRepository, loggingPort);
    }

    @Test
    void testSearchMetadataByIsbn() {
        // Given
        String isbn = "9780545010221";
        List<BookMetadata> expectedResults = Arrays.asList(createTestBookMetadata("Test Title"));
        when(metadataAggregatorPort.findByIsbnFromAllProviders(isbn)).thenReturn(expectedResults);

        // When
        List<BookMetadata> results = metadataService.searchMetadataByIsbn(isbn);

        // Then
        assertEquals(expectedResults, results);
        verify(metadataAggregatorPort).findByIsbnFromAllProviders(isbn);
        verify(loggingPort).info("Searching metadata by ISBN: " + isbn);
    }

    @Test
    void testSearchMetadataByTitle() {
        // Given
        String title = "Harry Potter";
        String author = "Rowling";
        List<BookMetadata> expectedResults = Arrays.asList(createTestBookMetadata("Harry Potter"));
        when(metadataAggregatorPort.searchByTitleFromAllProviders(title, author)).thenReturn(expectedResults);

        // When
        List<BookMetadata> results = metadataService.searchMetadataByTitle(title, author);

        // Then
        assertEquals(expectedResults, results);
        verify(metadataAggregatorPort).searchByTitleFromAllProviders(title, author);
        verify(loggingPort).info("Searching metadata by title: " + title + ", author: " + author);
    }

    @Test
    void testGetBestMetadata() {
        // Given
        String isbn = "9780545010221";
        BookMetadata expectedMetadata = createTestBookMetadata("Best Result");
        when(metadataAggregatorPort.getBestMetadataByIsbn(isbn)).thenReturn(Optional.of(expectedMetadata));

        // When
        Optional<BookMetadata> result = metadataService.getBestMetadata(isbn);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedMetadata, result.get());
        verify(metadataAggregatorPort).getBestMetadataByIsbn(isbn);
        verify(loggingPort).info("Getting best metadata for ISBN: " + isbn);
    }

    @Test
    void testGetBestMetadataNotFound() {
        // Given
        String isbn = "9780000000000";
        when(metadataAggregatorPort.getBestMetadataByIsbn(isbn)).thenReturn(Optional.empty());

        // When
        Optional<BookMetadata> result = metadataService.getBestMetadata(isbn);

        // Then
        assertFalse(result.isPresent());
        verify(metadataAggregatorPort).getBestMetadataByIsbn(isbn);
    }

    @Test
    void testApplyMetadataToBook() {
        // Given
        UUID bookId = UUID.randomUUID();
        BookMetadata metadata = createTestBookMetadata("New Title");
        Book existingBook = createTestBook(bookId, "Old Title");
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        // When
        UUID result = metadataService.applyMetadataToBook(bookId, metadata, false);

        // Then
        assertEquals(bookId, result);
        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(any(Book.class));
        verify(loggingPort).info("Applying metadata to book: " + bookId);
    }

    @Test
    void testApplyMetadataToBookNotFound() {
        // Given
        UUID bookId = UUID.randomUUID();
        BookMetadata metadata = createTestBookMetadata("Test Title");
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metadataService.applyMetadataToBook(bookId, metadata, false);
        });
        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testPreviewMetadataChanges() {
        // Given
        UUID bookId = UUID.randomUUID();
        BookMetadata metadata = createTestBookMetadataWithChanges("New Title", "New Description");
        Book existingBook = createTestBook(bookId, "Old Title");
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        // When
        MetadataPreview preview = metadataService.previewMetadataChanges(bookId, metadata, false);

        // Then
        assertNotNull(preview);
        assertEquals(bookId, preview.bookId());
        assertEquals("Old Title", preview.currentTitle());
        assertEquals("New Title", preview.newTitle());
        assertTrue(preview.totalChanges() > 0);
        verify(bookRepository).findById(bookId);
        verify(loggingPort).info("Previewing metadata changes for book: " + bookId);
    }

    @Test
    void testPreviewMetadataChangesBookNotFound() {
        // Given
        UUID bookId = UUID.randomUUID();
        BookMetadata metadata = createTestBookMetadata("Test Title");
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            metadataService.previewMetadataChanges(bookId, metadata, false);
        });
        verify(bookRepository).findById(bookId);
    }

    @Test
    void testMergeMetadata() {
        // Given
        BookMetadata metadata1 = createTestBookMetadataWithConfidence("Title 1", 0.8);
        BookMetadata metadata2 = createTestBookMetadataWithPublisher("Title 2", "Test Publisher", 0.9);
        
        List<BookMetadata> metadataList = Arrays.asList(metadata1, metadata2);
        BookMetadata expectedMerged = createTestBookMetadataWithConfidence("Title 2", 0.85); // Higher confidence wins
        
        when(metadataAggregatorPort.mergeMetadata(metadataList)).thenReturn(expectedMerged);

        // When
        BookMetadata result = metadataService.mergeMetadata(metadataList);

        // Then
        assertEquals(expectedMerged, result);
        verify(metadataAggregatorPort).mergeMetadata(metadataList);
        verify(loggingPort).info("Merging 2 metadata results");
    }

    @Test
    void testGetProviderStatuses() {
        // Given
        List<ProviderStatus> expectedStatuses = Arrays.asList(
            new ProviderStatus("google-books", "Google Books", true, true, null),
            new ProviderStatus("open-library", "Open Library", true, false, "Connection timeout")
        );
        when(metadataAggregatorPort.getProviderStatuses()).thenReturn(expectedStatuses);

        // When
        List<ProviderStatus> results = metadataService.getProviderStatuses();

        // Then
        assertEquals(expectedStatuses, results);
        verify(metadataAggregatorPort).getProviderStatuses();
        verify(loggingPort).info("Getting provider statuses");
    }

    @Test
    void testTestProviderConnections() {
        // Given
        List<ProviderStatus> expectedStatuses = Arrays.asList(
            new ProviderStatus("google-books", "Google Books", true, true, null),
            new ProviderStatus("open-library", "Open Library", true, true, null)
        );
        when(metadataAggregatorPort.testAllProviders()).thenReturn(expectedStatuses);

        // When
        List<ProviderStatus> results = metadataService.testProviderConnections();

        // Then
        assertEquals(expectedStatuses, results);
        verify(metadataAggregatorPort).testAllProviders();
        verify(loggingPort).info("Testing all metadata provider connections");
    }

    private BookMetadata createTestBookMetadata(String title) {
        AuthorMetadata author = new AuthorMetadata("Test Author", null, null, "author", null, null);
        
        return new BookMetadata(
            "9780000000000", // isbn
            "0000000000", // isbn10
            "9780000000000", // isbn13
            title, // title
            null, // subtitle
            null, // originalTitle
            null, // titleSort
            "Test description", // description
            "en", // language
            "Test Publisher", // publisher
            null, // publicationDate
            2023, // publicationYear
            300, // pageCount
            "test-provider-id", // googleBooksId
            null, // openLibraryId
            null, // goodreadsId
            null, // asin
            null, // doi
            null, // lccn
            null, // oclc
            Arrays.asList(author), // authors
            Set.of("Fiction"), // subjects
            Set.of("Novel"), // genres
            Set.of("test"), // tags
            null, // deweyDecimal
            null, // lcc
            null, // seriesName
            null, // seriesIndex
            null, // format
            null, // binding
            null, // dimensions
            null, // weight
            null, // smallThumbnail
            null, // thumbnail
            null, // mediumImage
            null, // largeImage
            null, // extraLargeImage
            4.5, // averageRating
            100, // ratingsCount
            "test-provider", // providerId
            "Test Provider", // providerName
            0.9 // confidence
        );
    }

    private BookMetadata createTestBookMetadataWithChanges(String title, String description) {
        AuthorMetadata author = new AuthorMetadata("Test Author", null, null, "author", null, null);
        
        return new BookMetadata(
            "9780000000000", // isbn
            "0000000000", // isbn10
            "9780000000000", // isbn13
            title, // title
            null, // subtitle
            null, // originalTitle
            null, // titleSort
            description, // description
            "en", // language
            "Test Publisher", // publisher
            null, // publicationDate
            2023, // publicationYear
            300, // pageCount
            "test-provider-id", // googleBooksId
            null, // openLibraryId
            null, // goodreadsId
            null, // asin
            null, // doi
            null, // lccn
            null, // oclc
            Arrays.asList(author), // authors
            Set.of("Fiction"), // subjects
            Set.of("Novel"), // genres
            Set.of("test"), // tags
            null, // deweyDecimal
            null, // lcc
            null, // seriesName
            null, // seriesIndex
            null, // format
            null, // binding
            null, // dimensions
            null, // weight
            null, // smallThumbnail
            null, // thumbnail
            null, // mediumImage
            null, // largeImage
            null, // extraLargeImage
            4.5, // averageRating
            100, // ratingsCount
            "test-provider", // providerId
            "Test Provider", // providerName
            0.9 // confidence
        );
    }

    private BookMetadata createTestBookMetadataWithConfidence(String title, double confidence) {
        AuthorMetadata author = new AuthorMetadata("Test Author", null, null, "author", null, null);
        
        return new BookMetadata(
            "9780000000000", // isbn
            "0000000000", // isbn10
            "9780000000000", // isbn13
            title, // title
            null, // subtitle
            null, // originalTitle
            null, // titleSort
            "Test description", // description
            "en", // language
            "Test Publisher", // publisher
            null, // publicationDate
            2023, // publicationYear
            300, // pageCount
            "test-provider-id", // googleBooksId
            null, // openLibraryId
            null, // goodreadsId
            null, // asin
            null, // doi
            null, // lccn
            null, // oclc
            Arrays.asList(author), // authors
            Set.of("Fiction"), // subjects
            Set.of("Novel"), // genres
            Set.of("test"), // tags
            null, // deweyDecimal
            null, // lcc
            null, // seriesName
            null, // seriesIndex
            null, // format
            null, // binding
            null, // dimensions
            null, // weight
            null, // smallThumbnail
            null, // thumbnail
            null, // mediumImage
            null, // largeImage
            null, // extraLargeImage
            4.5, // averageRating
            100, // ratingsCount
            "test-provider", // providerId
            "Test Provider", // providerName
            confidence // confidence
        );
    }

    private BookMetadata createTestBookMetadataWithPublisher(String title, String publisher, double confidence) {
        AuthorMetadata author = new AuthorMetadata("Test Author", null, null, "author", null, null);
        
        return new BookMetadata(
            "9780000000000", // isbn
            "0000000000", // isbn10
            "9780000000000", // isbn13
            title, // title
            null, // subtitle
            null, // originalTitle
            null, // titleSort
            "Test description", // description
            "en", // language
            publisher, // publisher
            null, // publicationDate
            2023, // publicationYear
            300, // pageCount
            "test-provider-id", // googleBooksId
            null, // openLibraryId
            null, // goodreadsId
            null, // asin
            null, // doi
            null, // lccn
            null, // oclc
            Arrays.asList(author), // authors
            Set.of("Fiction"), // subjects
            Set.of("Novel"), // genres
            Set.of("test"), // tags
            null, // deweyDecimal
            null, // lcc
            null, // seriesName
            null, // seriesIndex
            null, // format
            null, // binding
            null, // dimensions
            null, // weight
            null, // smallThumbnail
            null, // thumbnail
            null, // mediumImage
            null, // largeImage
            null, // extraLargeImage
            4.5, // averageRating
            100, // ratingsCount
            "test-provider", // providerId
            "Test Provider", // providerName
            confidence // confidence
        );
    }

    private Book createTestBook(UUID id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        // Note: setFilePath may not exist - this is just for testing
        return book;
    }
}