package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.Book;
import org.motpassants.domain.core.model.metadata.BookMetadata;
import org.motpassants.domain.core.model.metadata.FieldChange;
import org.motpassants.domain.core.model.metadata.MetadataPreview;
import org.motpassants.domain.core.model.metadata.ProviderStatus;
import org.motpassants.domain.port.in.MetadataUseCase;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.LoggingPort;
import org.motpassants.domain.port.out.metadata.MetadataAggregatorPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of MetadataUseCase for metadata operations and external provider integration.
 * Implements DATA-002 requirements for Metadata Editing and External Providers.
 */
@ApplicationScoped
public class MetadataService implements MetadataUseCase {
    
    private final MetadataAggregatorPort metadataAggregatorPort;
    private final BookRepository bookRepository;
    private final LoggingPort loggingPort;
    
    @Inject
    public MetadataService(MetadataAggregatorPort metadataAggregatorPort,
                          BookRepository bookRepository,
                          LoggingPort loggingPort) {
        this.metadataAggregatorPort = metadataAggregatorPort;
        this.bookRepository = bookRepository;
        this.loggingPort = loggingPort;
    }
    
    @Override
    public List<BookMetadata> searchMetadataByIsbn(String isbn) {
        loggingPort.info("Searching metadata by ISBN: " + isbn);
        return metadataAggregatorPort.findByIsbnFromAllProviders(isbn);
    }
    
    @Override
    public List<BookMetadata> searchMetadataByTitle(String title, String author) {
        loggingPort.info("Searching metadata by title: " + title + ", author: " + author);
        return metadataAggregatorPort.searchByTitleFromAllProviders(title, author);
    }
    
    @Override
    public Optional<BookMetadata> getBestMetadata(String isbn) {
        loggingPort.info("Getting best metadata for ISBN: " + isbn);
        return metadataAggregatorPort.getBestMetadataByIsbn(isbn);
    }
    
    @Override
    public UUID applyMetadataToBook(UUID bookId, BookMetadata metadata, boolean overwriteExisting) {
        loggingPort.info("Applying metadata to book: " + bookId);
        
        // Get existing book
        Optional<Book> existingBookOpt = bookRepository.findById(bookId);
        if (existingBookOpt.isEmpty()) {
            throw new IllegalArgumentException("Book not found: " + bookId);
        }
        
        Book existingBook = existingBookOpt.get();
        Book updatedBook = applyMetadataToBookEntity(existingBook, metadata, overwriteExisting);
        
        // Save the updated book
        bookRepository.save(updatedBook);
        
        loggingPort.info("Successfully applied metadata to book: " + bookId);
        return bookId;
    }
    
    @Override
    public MetadataPreview previewMetadataChanges(UUID bookId, BookMetadata metadata, boolean overwriteExisting) {
        loggingPort.info("Previewing metadata changes for book: " + bookId);
        
        // Get existing book
        Optional<Book> existingBookOpt = bookRepository.findById(bookId);
        if (existingBookOpt.isEmpty()) {
            throw new IllegalArgumentException("Book not found: " + bookId);
        }
        
        Book existingBook = existingBookOpt.get();
        List<FieldChange> changes = new ArrayList<>();
        
        // Compare and build change list
        addFieldChange(changes, "title", existingBook.getTitle(), metadata.title(), overwriteExisting);
        addFieldChange(changes, "description", existingBook.getDescription(), metadata.description(), overwriteExisting);
        addFieldChange(changes, "isbn", existingBook.getIsbn(), metadata.isbn(), overwriteExisting);
        addFieldChange(changes, "publisher", 
            existingBook.getPublisher() != null ? existingBook.getPublisher().getName() : null, 
            metadata.publisher(), overwriteExisting);
        
        // Authors comparison - simplified for preview
        String currentAuthors = formatAuthorsForPreview(existingBook);
        String newAuthors = formatAuthorsForPreview(metadata);
        addFieldChange(changes, "authors", currentAuthors, newAuthors, overwriteExisting);
        
        // Tags comparison - simplified for preview  
        String currentTags = formatTagsForPreview(existingBook);
        String newTags = formatTagsForPreview(metadata);
        addFieldChange(changes, "tags", currentTags, newTags, overwriteExisting);
        
        // Cover URL
        String newCoverUrl = getBestCoverUrl(metadata);
        addFieldChange(changes, "cover", null, newCoverUrl, overwriteExisting); // Current cover URL would need implementation
        
        return new MetadataPreview(
            bookId,
            existingBook.getTitle(),
            metadata.title(),
            existingBook.getDescription(),
            metadata.description(),
            existingBook.getIsbn(),
            metadata.isbn(),
            existingBook.getPublisher() != null ? existingBook.getPublisher().getName() : null,
            metadata.publisher(),
            parseCurrentAuthors(existingBook),
            parseNewAuthors(metadata),
            parseCurrentTags(existingBook),
            parseNewTags(metadata),
            null, // Current cover URL - would need implementation
            newCoverUrl,
            changes.size(),
            changes
        );
    }
    
    @Override
    public BookMetadata mergeMetadata(List<BookMetadata> metadataList) {
        return metadataAggregatorPort.mergeMetadata(metadataList);
    }
    
    @Override
    public List<ProviderStatus> getProviderStatuses() {
        return metadataAggregatorPort.testAllProviders();
    }
    
    @Override
    public List<ProviderStatus> testProviderConnections() {
        loggingPort.info("Testing all metadata provider connections");
        return metadataAggregatorPort.testAllProviders();
    }
    
    private Book applyMetadataToBookEntity(Book book, BookMetadata metadata, boolean overwriteExisting) {
        // Apply basic fields
        if (shouldUpdateField(book.getTitle(), metadata.title(), overwriteExisting)) {
            book.setTitle(metadata.title());
            if (metadata.titleSort() != null) {
                book.setTitleSort(metadata.titleSort());
            }
        }
        
        if (shouldUpdateField(book.getDescription(), metadata.description(), overwriteExisting)) {
            book.setDescription(metadata.description());
        }
        
        if (shouldUpdateField(book.getIsbn(), metadata.isbn(), overwriteExisting)) {
            book.setIsbn(metadata.isbn());
        }
        
        if (metadata.pageCount() != null && shouldUpdateField(book.getPageCount(), metadata.pageCount(), overwriteExisting)) {
            book.setPageCount(metadata.pageCount());
        }
        
        if (metadata.publicationYear() != null && shouldUpdateField(book.getPublicationYear(), metadata.publicationYear(), overwriteExisting)) {
            book.setPublicationYear(metadata.publicationYear());
        }
        
        if (metadata.publicationDate() != null && shouldUpdateField(book.getPublicationDate(), metadata.publicationDate(), overwriteExisting)) {
            book.setPublicationDate(metadata.publicationDate());
        }
        
        if (shouldUpdateField(book.getLanguage(), metadata.language(), overwriteExisting)) {
            book.setLanguage(metadata.language());
        }
        
        // TODO: Apply publisher, authors, tags, series, etc.
        // This would require more complex logic to handle relationships
        
        book.markAsUpdated();
        return book;
    }
    
    private <T> boolean shouldUpdateField(T currentValue, T newValue, boolean overwriteExisting) {
        if (newValue == null) {
            return false;
        }
        
        if (currentValue == null) {
            return true;
        }
        
        return overwriteExisting;
    }
    
    private void addFieldChange(List<FieldChange> changes, String fieldName, String currentValue, String newValue, boolean overwriteExisting) {
        if (shouldUpdateField(currentValue, newValue, overwriteExisting)) {
            String changeType = currentValue == null ? "add" : "update";
            changes.add(new FieldChange(fieldName, currentValue, newValue, changeType));
        }
    }
    
    private String formatAuthorsForPreview(Book book) {
        // TODO: Implement when authors relationship is properly established
        return null;
    }
    
    private String formatAuthorsForPreview(BookMetadata metadata) {
        if (metadata.authors() == null || metadata.authors().isEmpty()) {
            return null;
        }
        return metadata.authors().stream()
            .map(author -> author.name())
            .collect(Collectors.joining(", "));
    }
    
    private String formatTagsForPreview(Book book) {
        if (book.getTags() == null || book.getTags().isEmpty()) {
            return null;
        }
        return book.getTags().stream()
            .map(tag -> tag.getName())
            .collect(Collectors.joining(", "));
    }
    
    private String formatTagsForPreview(BookMetadata metadata) {
        if (metadata.tags() == null || metadata.tags().isEmpty()) {
            return null;
        }
        return String.join(", ", metadata.tags());
    }
    
    private List<String> parseCurrentAuthors(Book book) {
        // TODO: Implement when authors relationship is properly established
        return List.of();
    }
    
    private List<String> parseNewAuthors(BookMetadata metadata) {
        if (metadata.authors() == null) {
            return List.of();
        }
        return metadata.authors().stream()
            .map(author -> author.name())
            .collect(Collectors.toList());
    }
    
    private List<String> parseCurrentTags(Book book) {
        if (book.getTags() == null) {
            return List.of();
        }
        return book.getTags().stream()
            .map(tag -> tag.getName())
            .collect(Collectors.toList());
    }
    
    private List<String> parseNewTags(BookMetadata metadata) {
        if (metadata.tags() == null) {
            return List.of();
        }
        return new ArrayList<>(metadata.tags());
    }
    
    private String getBestCoverUrl(BookMetadata metadata) {
        // Prefer larger images
        if (metadata.extraLargeImage() != null) return metadata.extraLargeImage();
        if (metadata.largeImage() != null) return metadata.largeImage();
        if (metadata.mediumImage() != null) return metadata.mediumImage();
        if (metadata.thumbnail() != null) return metadata.thumbnail();
        if (metadata.smallThumbnail() != null) return metadata.smallThumbnail();
        return null;
    }
}