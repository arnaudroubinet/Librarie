package org.roubinet.librarie.infrastructure.adapter.in.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.roubinet.librarie.application.port.in.AuthorUseCase;
import org.roubinet.librarie.application.port.in.BookUseCase;
import org.roubinet.librarie.application.port.in.SeriesUseCase;
import org.roubinet.librarie.domain.entity.Author;
import org.roubinet.librarie.domain.entity.Book;
import org.roubinet.librarie.domain.entity.Language;
import org.roubinet.librarie.domain.entity.Publisher;
import org.roubinet.librarie.domain.model.SeriesData;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.pagination.CursorPageResult;
import org.roubinet.librarie.infrastructure.adapter.in.rest.dto.UnifiedSearchResultDto;
import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;
import org.roubinet.librarie.infrastructure.security.InputSanitizationService;

import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UnifiedSearchController.
 */
public class UnifiedSearchControllerTest {

    @Mock
    private BookUseCase bookUseCase;
    
    @Mock
    private SeriesUseCase seriesUseCase;
    
    @Mock
    private AuthorUseCase authorUseCase;
    
    @Mock
    private InputSanitizationService sanitizationService;
    
    @Mock
    private LibrarieConfigProperties config;

    private UnifiedSearchController unifiedSearchController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        unifiedSearchController = new UnifiedSearchController(
            bookUseCase, seriesUseCase, authorUseCase, sanitizationService, config);
    }

    @Test
    void testUnifiedSearch_Success() {
        // Given
        String query = "robot";
        String sanitizedQuery = "robot";
        int limit = 10;
        
        // Mock sanitization
        when(sanitizationService.sanitizeSearchQuery(query)).thenReturn(sanitizedQuery);
        
        // Mock book search
        Book mockBook = createMockBook();
        CursorPageResult<Book> bookResult = CursorPageResult.<Book>builder()
            .items(List.of(mockBook))
            .limit(limit)
            .totalCount(1L)
            .build();
        when(bookUseCase.searchBooks(eq(sanitizedQuery), isNull(), eq(limit))).thenReturn(bookResult);
        
        // Mock series search
        SeriesData mockSeries = createMockSeries();
        when(seriesUseCase.searchSeries(sanitizedQuery)).thenReturn(List.of(mockSeries));
        
        // Mock author search
        Author mockAuthor = createMockAuthor();
        when(authorUseCase.searchAuthors(sanitizedQuery)).thenReturn(List.of(mockAuthor));

        // When
        Response response = unifiedSearchController.unifiedSearch(query, limit);

        // Then
        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        
        UnifiedSearchResultDto result = (UnifiedSearchResultDto) response.getEntity();
        assertEquals(1, result.getBooks().size());
        assertEquals(1, result.getSeries().size());
        assertEquals(1, result.getAuthors().size());
        
        assertEquals("Test Book", result.getBooks().get(0).getTitle());
        assertEquals("Test Series", result.getSeries().get(0).getName());
        assertEquals("Test Author", result.getAuthors().get(0).getName());
    }

    @Test
    void testUnifiedSearch_EmptyQuery() {
        // When
        Response response = unifiedSearchController.unifiedSearch("", 10);

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("Search query is required", response.getEntity());
    }

    @Test
    void testUnifiedSearch_NullQuery() {
        // When
        Response response = unifiedSearchController.unifiedSearch(null, 10);

        // Then
        assertEquals(400, response.getStatus());
        assertEquals("Search query is required", response.getEntity());
    }

    @Test
    void testUnifiedSearch_LimitValidation() {
        // Given
        String query = "test";
        String sanitizedQuery = "test";
        
        when(sanitizationService.sanitizeSearchQuery(query)).thenReturn(sanitizedQuery);
        when(bookUseCase.searchBooks(eq(sanitizedQuery), isNull(), eq(10)))
            .thenReturn(CursorPageResult.<Book>builder().items(List.of()).build());
        when(seriesUseCase.searchSeries(sanitizedQuery)).thenReturn(List.of());
        when(authorUseCase.searchAuthors(sanitizedQuery)).thenReturn(List.of());

        // When - test negative limit (should default to 10)
        Response response1 = unifiedSearchController.unifiedSearch(query, -5);
        
        // Then
        assertEquals(200, response1.getStatus());
        
        // Reset mocks for next test
        when(bookUseCase.searchBooks(eq(sanitizedQuery), isNull(), eq(50)))
            .thenReturn(CursorPageResult.<Book>builder().items(List.of()).build());
        
        // When - test limit over max (should cap at 50)
        Response response2 = unifiedSearchController.unifiedSearch(query, 100);
        
        // Then
        assertEquals(200, response2.getStatus());
    }

    @Test
    void testUnifiedSearch_LimitResultsCorrectly() {
        // Given
        String query = "test";
        String sanitizedQuery = "test";
        int limit = 2;
        
        when(sanitizationService.sanitizeSearchQuery(query)).thenReturn(sanitizedQuery);
        when(bookUseCase.searchBooks(eq(sanitizedQuery), isNull(), eq(limit)))
            .thenReturn(CursorPageResult.<Book>builder().items(List.of()).build());
        
        // Create more items than the limit
        List<SeriesData> manySeries = List.of(
            createMockSeriesWithName("Series 1"),
            createMockSeriesWithName("Series 2"),
            createMockSeriesWithName("Series 3")
        );
        when(seriesUseCase.searchSeries(sanitizedQuery)).thenReturn(manySeries);
        
        List<Author> manyAuthors = List.of(
            createMockAuthorWithName("Author 1"),
            createMockAuthorWithName("Author 2"),
            createMockAuthorWithName("Author 3")
        );
        when(authorUseCase.searchAuthors(sanitizedQuery)).thenReturn(manyAuthors);

        // When
        Response response = unifiedSearchController.unifiedSearch(query, limit);

        // Then
        assertEquals(200, response.getStatus());
        UnifiedSearchResultDto result = (UnifiedSearchResultDto) response.getEntity();
        
        // Should limit series and authors to the specified limit
        assertTrue(result.getSeries().size() <= limit);
        assertTrue(result.getAuthors().size() <= limit);
    }

    @Test
    void testUnifiedSearch_Exception() {
        // Given
        String query = "test";
        when(sanitizationService.sanitizeSearchQuery(query)).thenThrow(new RuntimeException("Test exception"));

        // When
        Response response = unifiedSearchController.unifiedSearch(query, 10);

        // Then
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity().toString().contains("Internal server error"));
    }

    private Book createMockBook() {
        Book book = new Book();
        book.setId(UUID.randomUUID());
        book.setTitle("Test Book");
        book.setTitleSort("Test Book");
        book.setIsbn("123456789");
        book.setPath("/test/path");
        book.setFileSize(1024L);
        book.setFileHash("abcd1234");
        book.setHasCover(true);
        book.setCreatedAt(OffsetDateTime.now());
        book.setUpdatedAt(OffsetDateTime.now());
        book.setPublicationDate(LocalDate.now());
        book.setMetadata(Map.of("key", "value"));
        
        Language language = new Language();
        language.setName("English");
        book.setLanguage(language);
        
        Publisher publisher = new Publisher();
        publisher.setName("Test Publisher");
        book.setPublisher(publisher);
        
        return book;
    }

    private SeriesData createMockSeries() {
        return createMockSeriesWithName("Test Series");
    }
    
    private SeriesData createMockSeriesWithName(String name) {
        return new SeriesData(
            UUID.randomUUID(),
            name,
            name,
            "Test Description",
            "/test/image.jpg",
            Map.of("key", "value"),
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            5,
            "/test/image.jpg"
        );
    }

    private Author createMockAuthor() {
        return createMockAuthorWithName("Test Author");
    }
    
    private Author createMockAuthorWithName(String name) {
        Author author = new Author();
        author.setId(UUID.randomUUID());
        author.setName(name);
        author.setSortName(name);
        author.setBio(Map.of("en", "Test Biography"));
        author.setBirthDate(LocalDate.of(1920, 1, 1));
        author.setDeathDate(LocalDate.of(1992, 4, 6));
        author.setWebsiteUrl("https://example.com");
        author.setMetadata(Map.of("key", "value"));
        author.setCreatedAt(OffsetDateTime.now());
        author.setUpdatedAt(OffsetDateTime.now());
        return author;
    }
}