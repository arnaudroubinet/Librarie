package org.roubinet.librarie.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.roubinet.librarie.application.port.out.BookRepository;
import org.roubinet.librarie.domain.entity.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookService to verify hexagonal architecture implementation.
 * Tests the application layer in isolation from infrastructure concerns.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    
    @Mock
    private BookRepository bookRepository;
    
    private BookService bookService;
    
    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);
    }
    
    @Test
    void getAllBooks_WithValidPagination_ShouldReturnBooks() {
        // Given
        List<Book> expectedBooks = List.of(createTestBook("Test Book"));
        when(bookRepository.findAll(0, 20)).thenReturn(expectedBooks);
        
        // When
        List<Book> result = bookService.getAllBooks(0, 20);
        
        // Then
        assertEquals(expectedBooks, result);
        verify(bookRepository).findAll(0, 20);
    }
    
    @Test
    void getAllBooks_WithInvalidPagination_ShouldUseDefaults() {
        // Given
        when(bookRepository.findAll(0, 20)).thenReturn(List.of());
        
        // When
        bookService.getAllBooks(-1, 0);
        
        // Then
        verify(bookRepository).findAll(0, 20); // Should use defaults
    }
    
    @Test
    void getBookById_WithValidId_ShouldReturnBook() {
        // Given
        UUID bookId = UUID.randomUUID();
        Book expectedBook = createTestBook("Test Book");
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(expectedBook));
        
        // When
        Optional<Book> result = bookService.getBookById(bookId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedBook, result.get());
        verify(bookRepository).findById(bookId);
    }
    
    @Test
    void getBookById_WithNullId_ShouldReturnEmpty() {
        // When
        Optional<Book> result = bookService.getBookById(null);
        
        // Then
        assertFalse(result.isPresent());
        verify(bookRepository, never()).findById(any());
    }
    
    @Test
    void createBook_WithValidBook_ShouldCreateSuccessfully() {
        // Given
        Book bookToCreate = createTestBook("A New Book");
        Book savedBook = createTestBook("A New Book");
        savedBook.setId(UUID.randomUUID());
        
        when(bookRepository.findByPath(bookToCreate.getPath())).thenReturn(Optional.empty());
        when(bookRepository.save(bookToCreate)).thenReturn(savedBook);
        
        // When
        Book result = bookService.createBook(bookToCreate);
        
        // Then
        assertEquals(savedBook, result);
        assertEquals("New Book, A", result.getTitleSort()); // Article should be moved
        verify(bookRepository).findByPath(bookToCreate.getPath());
        verify(bookRepository).save(bookToCreate);
    }
    
    @Test
    void createBook_WithNullBook_ShouldThrowException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(null));
        verify(bookRepository, never()).save(any());
    }
    
    @Test
    void createBook_WithExistingPath_ShouldThrowException() {
        // Given
        Book bookToCreate = createTestBook("Duplicate Book");
        Book existingBook = createTestBook("Existing Book");
        
        when(bookRepository.findByPath(bookToCreate.getPath())).thenReturn(Optional.of(existingBook));
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(bookToCreate));
        verify(bookRepository).findByPath(bookToCreate.getPath());
        verify(bookRepository, never()).save(any());
    }
    
    @Test
    void searchBooks_WithValidQuery_ShouldReturnMatchingBooks() {
        // Given
        String query = "test query";
        List<Book> expectedBooks = List.of(createTestBook("Test Book"));
        when(bookRepository.searchBooks(query, 0, 20)).thenReturn(expectedBooks);
        
        // When
        List<Book> result = bookService.searchBooks(query, 0, 20);
        
        // Then
        assertEquals(expectedBooks, result);
        verify(bookRepository).searchBooks(query, 0, 20);
    }
    
    @Test
    void searchBooks_WithEmptyQuery_ShouldReturnAllBooks() {
        // Given
        List<Book> expectedBooks = List.of(createTestBook("Test Book"));
        when(bookRepository.findAll(0, 20)).thenReturn(expectedBooks);
        
        // When
        List<Book> result = bookService.searchBooks("", 0, 20);
        
        // Then
        assertEquals(expectedBooks, result);
        verify(bookRepository).findAll(0, 20);
        verify(bookRepository, never()).searchBooks(anyString(), anyInt(), anyInt());
    }
    
    @Test
    void deleteBook_WithValidId_ShouldDeleteSuccessfully() {
        // Given
        UUID bookId = UUID.randomUUID();
        when(bookRepository.existsById(bookId)).thenReturn(true);
        
        // When
        bookService.deleteBook(bookId);
        
        // Then
        verify(bookRepository).existsById(bookId);
        verify(bookRepository).deleteById(bookId);
    }
    
    @Test
    void deleteBook_WithNonExistentId_ShouldThrowException() {
        // Given
        UUID bookId = UUID.randomUUID();
        when(bookRepository.existsById(bookId)).thenReturn(false);
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> bookService.deleteBook(bookId));
        verify(bookRepository).existsById(bookId);
        verify(bookRepository, never()).deleteById(any());
    }
    
    @Test
    void getTotalBooksCount_ShouldReturnCount() {
        // Given
        long expectedCount = 42L;
        when(bookRepository.count()).thenReturn(expectedCount);
        
        // When
        long result = bookService.getTotalBooksCount();
        
        // Then
        assertEquals(expectedCount, result);
        verify(bookRepository).count();
    }
    
    private Book createTestBook(String title) {
        Book book = new Book();
        book.setTitle(title);
        book.setPath("/test/path/" + title.replaceAll(" ", "_") + ".epub");
        return book;
    }
}