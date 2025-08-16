package org.motpassants.infrastructure.adapter.out.persistence;

import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.BookSearchCriteria;
import org.motpassants.domain.core.model.PageResult;
import org.motpassants.domain.port.out.BookRepository;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the BookRepository port for testing.
 * This adapter provides a working implementation during the migration phase.
 * Will be replaced with JPA implementation once database entities are ready.
 */
@ApplicationScoped
public class BookRepositoryAdapter implements BookRepository {

    private final Map<UUID, Book> books = new ConcurrentHashMap<>();
    
    @Override
    public PageResult<Book> findAll(String cursor, int limit) {
        List<Book> allBooks = new ArrayList<>(books.values());
        allBooks.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        
        return new PageResult<>(allBooks, null, null, false, false, allBooks.size());
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public Optional<Book> findByPath(String path) {
        return books.values().stream()
            .filter(book -> Objects.equals(book.getPath(), path))
            .findFirst();
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return books.values().stream()
            .filter(book -> Objects.equals(book.getIsbn(), isbn))
            .findFirst();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(UUID.randomUUID());
        }
        books.put(book.getId(), book);
        return book;
    }

    @Override
    public void deleteById(UUID id) {
        books.remove(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return books.containsKey(id);
    }

    @Override
    public long count() {
        return books.size();
    }

    @Override
    public PageResult<Book> search(BookSearchCriteria criteria) {
        List<Book> results = findByCriteria(criteria);
        return new PageResult<>(results, null, null, false, false, results.size());
    }

    @Override
    public List<Book> findByTitleOrAuthorContaining(String query) {
        return books.values().stream()
            .filter(book -> 
                (book.getTitle() != null && book.getTitle().toLowerCase().contains(query.toLowerCase())) ||
                (book.getDescription() != null && book.getDescription().toLowerCase().contains(query.toLowerCase())))
            .collect(Collectors.toList());
    }

    @Override
    public List<Book> findByCriteria(BookSearchCriteria criteria) {
        return books.values().stream()
            .filter(book -> matchesCriteria(book, criteria))
            .collect(Collectors.toList());
    }
    
    private boolean matchesCriteria(Book book, BookSearchCriteria criteria) {
        if (criteria.getTitle() != null && !criteria.getTitle().trim().isEmpty()) {
            return book.getTitle() != null && 
                   book.getTitle().toLowerCase().contains(criteria.getTitle().toLowerCase());
        }
        return true;
    }
}