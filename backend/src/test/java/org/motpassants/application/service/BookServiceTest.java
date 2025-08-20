package org.motpassants.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.motpassants.domain.core.model.*;
import org.motpassants.domain.port.out.BookRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookService.
 * Tests business logic and validation methods.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);
    }

    @Test
    @DisplayName("Should validate and parse sort criteria correctly")
    void shouldValidateAndParseSortCriteriaCorrectly() {
        // Test valid sort criteria
        BookSortCriteria criteria = bookService.parseAndValidateSortCriteria("TITLE_SORT", "ASC");
        assertNotNull(criteria);
        assertEquals(SortField.TITLE_SORT, criteria.getField());
        assertEquals(SortDirection.ASC, criteria.getDirection());
    }

    @Test
    @DisplayName("Should return default sort criteria when field is null or empty")
    void shouldReturnDefaultSortCriteriaWhenFieldIsNullOrEmpty() {
        BookSortCriteria criteria1 = bookService.parseAndValidateSortCriteria(null, "ASC");
        assertEquals(BookSortCriteria.DEFAULT, criteria1);

        BookSortCriteria criteria2 = bookService.parseAndValidateSortCriteria("", "ASC");
        assertEquals(BookSortCriteria.DEFAULT, criteria2);

        BookSortCriteria criteria3 = bookService.parseAndValidateSortCriteria("   ", "ASC");
        assertEquals(BookSortCriteria.DEFAULT, criteria3);
    }

    @Test
    @DisplayName("Should throw exception for invalid sort criteria")
    void shouldThrowExceptionForInvalidSortCriteria() {
        assertThrows(IllegalArgumentException.class, () -> {
            bookService.parseAndValidateSortCriteria("INVALID_FIELD", "ASC");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            bookService.parseAndValidateSortCriteria("TITLE_SORT", "INVALID_DIRECTION");
        });
    }

    @Test
    @DisplayName("Should validate search query correctly")
    void shouldValidateSearchQueryCorrectly() {
        // Valid queries should not throw
        assertDoesNotThrow(() -> bookService.validateSearchQuery("test query"));
        assertDoesNotThrow(() -> bookService.validateSearchQuery("a"));
        assertDoesNotThrow(() -> bookService.validateSearchQuery("  valid  "));
    }

    @Test
    @DisplayName("Should throw exception for invalid search queries")
    void shouldThrowExceptionForInvalidSearchQueries() {
        assertThrows(IllegalArgumentException.class, () -> {
            bookService.validateSearchQuery(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            bookService.validateSearchQuery("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            bookService.validateSearchQuery("   ");
        });
    }

    @Test
    @DisplayName("Should validate and parse UUID correctly")
    void shouldValidateAndParseUuidCorrectly() {
        String validUuid = "123e4567-e89b-12d3-a456-426614174000";
        UUID parsedUuid = bookService.validateAndParseId(validUuid);
        assertEquals(UUID.fromString(validUuid), parsedUuid);
    }

    @Test
    @DisplayName("Should throw exception for invalid UUID")
    void shouldThrowExceptionForInvalidUuid() {
        assertThrows(IllegalArgumentException.class, () -> {
            bookService.validateAndParseId("invalid-uuid");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            bookService.validateAndParseId("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            bookService.validateAndParseId(null);
        });
    }

    @Test
    @DisplayName("Should apply pagination limits correctly")
    void shouldApplyPaginationLimitsCorrectly() {
        PageResult<Book> mockResult = new PageResult<>(List.of(), null, null, false, false, 0);
        when(bookRepository.findAll(anyString(), anyInt(), any(BookSortCriteria.class)))
                .thenReturn(mockResult);

        // Test default limit
        bookService.getAllBooks("cursor", 0, BookSortCriteria.DEFAULT);
        verify(bookRepository, times(1)).findAll("cursor", 20, BookSortCriteria.DEFAULT);
        
        reset(bookRepository);
        when(bookRepository.findAll(anyString(), anyInt(), any(BookSortCriteria.class)))
                .thenReturn(mockResult);
        
        // Test negative limit
        bookService.getAllBooks("cursor", -5, BookSortCriteria.DEFAULT);
        verify(bookRepository, times(1)).findAll("cursor", 20, BookSortCriteria.DEFAULT);
        
        reset(bookRepository);
        when(bookRepository.findAll(anyString(), anyInt(), any(BookSortCriteria.class)))
                .thenReturn(mockResult);
        
        // Test max limit
        bookService.getAllBooks("cursor", 150, BookSortCriteria.DEFAULT);
        verify(bookRepository, times(1)).findAll("cursor", 100, BookSortCriteria.DEFAULT);
        
        reset(bookRepository);
        when(bookRepository.findAll(anyString(), anyInt(), any(BookSortCriteria.class)))
                .thenReturn(mockResult);
        
        // Test valid limit
        bookService.getAllBooks("cursor", 50, BookSortCriteria.DEFAULT);
        verify(bookRepository, times(1)).findAll("cursor", 50, BookSortCriteria.DEFAULT);
    }

    @Test
    @DisplayName("Should apply default sort criteria when null")
    void shouldApplyDefaultSortCriteriaWhenNull() {
        PageResult<Book> mockResult = new PageResult<>(List.of(), null, null, false, false, 0);
        when(bookRepository.findAll(anyString(), anyInt(), any(BookSortCriteria.class)))
                .thenReturn(mockResult);

        bookService.getAllBooks("cursor", 20, null);
        verify(bookRepository).findAll("cursor", 20, BookSortCriteria.DEFAULT);
    }

    @Test
    @DisplayName("Should get book by ID")
    void shouldGetBookById() {
        UUID bookId = UUID.randomUUID();
        Book expectedBook = createTestBook(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(expectedBook));

        Optional<Book> result = bookService.getBookById(bookId);
        assertTrue(result.isPresent());
        assertEquals(expectedBook, result.get());
        verify(bookRepository).findById(bookId);
    }

    @Test
    @DisplayName("Should return empty when book not found")
    void shouldReturnEmptyWhenBookNotFound() {
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.getBookById(bookId);
        assertFalse(result.isPresent());
        verify(bookRepository).findById(bookId);
    }

    @Test
    @DisplayName("Should get total books count")
    void shouldGetTotalBooksCount() {
        when(bookRepository.count()).thenReturn(42L);

        long count = bookService.getTotalBooksCount();
        assertEquals(42L, count);
        verify(bookRepository).count();
    }

    private Book createTestBook(UUID id) {
        Book book = new Book();
        book.setId(id);
        book.setTitle("Test Book");
        book.setCreatedAt(OffsetDateTime.now());
        book.setUpdatedAt(OffsetDateTime.now());
        return book;
    }
}