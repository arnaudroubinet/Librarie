package org.roubinet.librarie.infrastructure.adapter.out.persistence;

import org.roubinet.librarie.application.port.out.BookRepository;
import org.roubinet.librarie.domain.entity.Book;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the BookRepository port.
 * This adapter translates domain repository operations to Panache ORM calls.
 */
@ApplicationScoped
public class BookRepositoryAdapter implements BookRepository {
    
    @Override
    public List<Book> findAll(int page, int size) {
        PanacheQuery<Book> query = Book.findAll(Sort.by("titleSort").ascending());
        return query.page(Page.of(page, size)).list();
    }
    
    @Override
    public Optional<Book> findById(UUID id) {
        return Book.findByIdOptional(id);
    }
    
    @Override
    public List<Book> findByTitleContainingIgnoreCase(String title, int page, int size) {
        PanacheQuery<Book> query = Book.find("LOWER(title) LIKE LOWER(?1)", "%" + title + "%");
        return query.page(Page.of(page, size)).list();
    }
    
    @Override
    public Optional<Book> findByPath(String path) {
        return Book.find("path", path).firstResultOptional();
    }
    
    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return Book.find("isbn", isbn).firstResultOptional();
    }
    
    @Override
    public Book save(Book book) {
        book.persist();
        return book;
    }
    
    @Override
    public void deleteById(UUID id) {
        Book.deleteById(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return Book.findById(id) != null;
    }
    
    @Override
    public long count() {
        return Book.count();
    }
    
    @Override
    public List<Book> findByAuthorName(String authorName, int page, int size) {
        // Join with original works and authors
        String query = """
            SELECT DISTINCT b FROM Book b 
            JOIN b.originalWorks bw 
            JOIN bw.originalWork w 
            JOIN w.authors wa 
            JOIN wa.author a 
            WHERE LOWER(a.name) LIKE LOWER(?1)
            ORDER BY b.titleSort
            """;
        
        return Book.getEntityManager()
            .createQuery(query, Book.class)
            .setParameter(1, "%" + authorName + "%")
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }
    
    @Override
    public List<Book> findBySeriesName(String seriesName, int page, int size) {
        // Join with book series
        String query = """
            SELECT DISTINCT b FROM Book b 
            JOIN b.series bs 
            JOIN bs.series s 
            WHERE LOWER(s.name) LIKE LOWER(?1)
            ORDER BY b.titleSort
            """;
        
        return Book.getEntityManager()
            .createQuery(query, Book.class)
            .setParameter(1, "%" + seriesName + "%")
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }
    
    @Override
    public List<Book> searchBooks(String searchQuery, int page, int size) {
        // Multi-field search across title, authors, and series
        String query = """
            SELECT DISTINCT b FROM Book b 
            LEFT JOIN b.originalWorks bw 
            LEFT JOIN bw.originalWork w 
            LEFT JOIN w.authors wa 
            LEFT JOIN wa.author a 
            LEFT JOIN b.series bs 
            LEFT JOIN bs.series s 
            WHERE LOWER(b.title) LIKE LOWER(?1) 
            OR LOWER(a.name) LIKE LOWER(?1) 
            OR LOWER(s.name) LIKE LOWER(?1)
            OR LOWER(b.isbn) LIKE LOWER(?1)
            ORDER BY b.titleSort
            """;
        
        String searchPattern = "%" + searchQuery + "%";
        
        return Book.getEntityManager()
            .createQuery(query, Book.class)
            .setParameter(1, searchPattern)
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }
}