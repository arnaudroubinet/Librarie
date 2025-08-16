package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.motpassants.domain.core.model.*;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.AuthorRepositoryPort;
import org.motpassants.domain.port.out.SeriesRepositoryPort;
import org.motpassants.infrastructure.config.LibrarieConfigProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for populating demo data when demo mode is enabled.
 * Creates realistic books, series, authors, and publishers.
 */
@ApplicationScoped
public class DemoDataService {
    
    private static final Logger LOG = Logger.getLogger(DemoDataService.class);
    
    private final LibrarieConfigProperties config;
    private final BookRepository bookRepository;
    private final AuthorRepositoryPort authorRepository;
    private final SeriesRepositoryPort seriesRepository;
    
    @Inject
    public DemoDataService(LibrarieConfigProperties config, 
                          BookRepository bookRepository,
                          AuthorRepositoryPort authorRepository,
                          SeriesRepositoryPort seriesRepository) {
        this.config = config;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.seriesRepository = seriesRepository;
    }
    
    public void populateDemoData() {
        if (!config.getDemoConfig().isEnabled()) {
            LOG.info("Demo mode disabled, skipping demo data population");
            return;
        }
        
        // Temporarily disabled - would need to implement proper Book creation methods
        LOG.info("Demo data population temporarily disabled pending Book model enhancements");
        
        /*
        // Check if data already exists
        if (bookRepository.count() > 0) {
            LOG.info("Demo data already exists, skipping population");
            return;
        }
        
        LOG.info("Starting demo data population...");
        
        try {
            // Create demo authors
            List<Author> authors = createDemoAuthors();
            LOG.info("Created " + authors.size() + " demo authors");
            
            // Create demo series
            List<Series> series = createDemoSeries();
            LOG.info("Created " + series.size() + " demo series");
            
            // Create demo books
            List<Book> books = createDemoBooks(authors, series);
            LOG.info("Created " + books.size() + " demo books");
            
            LOG.info("Demo data population completed successfully");
            
        } catch (Exception e) {
            LOG.error("Failed to populate demo data", e);
            throw new RuntimeException("Demo data population failed", e);
        }
        */
    }
    
    /*
    private List<Author> createDemoAuthors() {
        List<Author> authors = new ArrayList<>();
        String[] firstNames = {"John", "Jane", "Michael", "Sarah", "David", "Emily", "Robert", "Lisa", "James", "Maria"};
        String[] lastNames = {"Smith", "Johnson", "Brown", "Davis", "Wilson", "Miller", "Moore", "Taylor", "Anderson", "Thomas"};
        
        for (int i = 0; i < config.demo().authorCount(); i++) {
            String firstName = firstNames[i % firstNames.length];
            String lastName = lastNames[i % lastNames.length];
            String fullName = firstName + " " + lastName + (i > 9 ? " " + (i / 10) : "");
            
            Map<String, String> bio = Map.of("en", "Demo author biography for " + fullName);
            LocalDate birthDate = LocalDate.of(1950 + (i % 50), 1 + (i % 12), 1 + (i % 28));
            Author author = Author.create(fullName, fullName, bio, birthDate, null, null, Map.of());
            
            authors.add(authorRepository.save(author));
        }
        
        return authors;
    }
    
    private List<Series> createDemoSeries() {
        List<Series> seriesList = new ArrayList<>();
        String[] seriesNames = {
            "Epic Fantasy Chronicles", "Mystery Detective Stories", "Science Fiction Saga", 
            "Romance Collection", "Historical Fiction Series", "Adventure Tales",
            "Thriller Mysteries", "Young Adult Adventures", "Classic Literature",
            "Modern Fiction Series"
        };
        
        for (int i = 0; i < config.demo().seriesCount(); i++) {
            String name = seriesNames[i % seriesNames.length] + (i >= seriesNames.length ? " " + (i / seriesNames.length + 1) : "");
            
            Series series = Series.create(name);
            series.updateDescription("Demo series description for " + name);
            series.updateStartDate(LocalDate.of(2000 + (i % 20), 1, 1));
            
            seriesList.add(seriesRepository.save(series));
        }
        
        return seriesList;
    }
    
    private List<Book> createDemoBooks(List<Author> authors, List<Series> series) {
        List<Book> books = new ArrayList<>();
        String[] genres = {"Fantasy", "Mystery", "Science Fiction", "Romance", "Historical Fiction", "Adventure", "Thriller"};
        String[] titles = {
            "The Great Adventure", "Mystery of the Lost City", "Future Horizons", "Love in Time",
            "Ancient Secrets", "Journey to Tomorrow", "The Last Stand", "Hidden Treasures",
            "Echoes of the Past", "Dreams of Tomorrow", "The Silent Observer", "Waves of Change"
        };
        
        Random random = new Random(42); // Fixed seed for reproducibility
        
        for (int i = 0; i < config.demo().bookCount(); i++) {
            String title = titles[i % titles.length] + (i >= titles.length ? " " + (i / titles.length + 1) : "");
            String genre = genres[i % genres.length];
            
            Book book = new Book(title, "/demo/books/" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
            // Note: Book model doesn't have the update methods used here
            // Would need to use setters or add the methods to Book model if needed
            
            // Assign random author
            if (!authors.isEmpty()) {
                Author randomAuthor = authors.get(i % authors.size());
                // Note: Author assignment would need to be implemented in the Book model
            }
            
            // Assign to series occasionally
            if (!series.isEmpty() && i % 3 == 0) {
                Series randomSeries = series.get(i % series.size());
                // Note: Series assignment would need to be implemented in the Book model
            }
            
            books.add(bookRepository.save(book));
        }
        
        return books;
    }
    */
}