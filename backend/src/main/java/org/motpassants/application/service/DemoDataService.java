package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.motpassants.domain.core.model.*;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.AuthorRepositoryPort;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.SeriesRepositoryPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for populating demo data when demo mode is enabled.
 * Creates comprehensive demo data including famous authors, series, and books.
 */
@ApplicationScoped
public class DemoDataService {
    
    private final ConfigurationPort configurationPort;
    private final BookRepository bookRepository;
    private final AuthorRepositoryPort authorRepository;
    private final SeriesRepositoryPort seriesRepository;
    
    @Inject
    public DemoDataService(ConfigurationPort configurationPort, 
                          BookRepository bookRepository,
                          AuthorRepositoryPort authorRepository,
                          SeriesRepositoryPort seriesRepository) {
        this.configurationPort = configurationPort;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.seriesRepository = seriesRepository;
    }
    
    @Transactional
    public void populateDemoData() {
        if (!configurationPort.isDemoEnabled()) {
            return;
        }
        
        if (bookRepository.count() > 0) {
            return;
        }
        
        try {
            // Create comprehensive demo data
            Map<String, Author> authors = createComprehensiveAuthors();
            Map<String, Series> series = createComprehensiveSeries();
            createComprehensiveBooks(authors, series);
            
        } catch (Exception e) {
            throw new RuntimeException("Demo data population failed", e);
        }
    }
    
    private Map<String, Author> createComprehensiveAuthors() {
        Map<String, Author> authors = new HashMap<>();
        
        // J.R.R. Tolkien
        Author tolkien = Author.create(
            "J.R.R. Tolkien",
            "Tolkien, J.R.R.",
            Map.of("en", "John Ronald Reuel Tolkien was an English writer, poet, philologist, and academic, best known as the author of the high fantasy works The Hobbit and The Lord of the Rings."),
            LocalDate.of(1892, 1, 3),
            LocalDate.of(1973, 9, 2),
            "https://www.tolkienestate.com/",
            Map.of(
                "nationality", "British",
                "genres", List.of("Fantasy", "Philology"),
                "education", "Oxford University"
            )
        );
        authors.put("J.R.R. Tolkien", authorRepository.save(tolkien));
        
        // Robert Jordan
        Author jordan = Author.create(
            "Robert Jordan",
            "Jordan, Robert",
            Map.of("en", "Robert Jordan was an American author of epic fantasy. He is best known for the Wheel of Time series, which comprises 14 books and a prequel novel."),
            LocalDate.of(1948, 10, 17),
            LocalDate.of(2007, 9, 16),
            "https://www.dragonmount.com/",
            Map.of(
                "nationality", "American",
                "genres", List.of("Epic Fantasy"),
                "realName", "James Oliver Rigney Jr."
            )
        );
        authors.put("Robert Jordan", authorRepository.save(jordan));
        
        // Brandon Sanderson  
        Author sanderson = Author.create(
            "Brandon Sanderson",
            "Sanderson, Brandon",
            Map.of("en", "Brandon Sanderson is an American author of epic fantasy and science fiction. He is best known for the Cosmere fictional universe, in which most of his fantasy novels, most notably the Mistborn series and The Stormlight Archive, are set."),
            LocalDate.of(1975, 12, 19),
            null,
            "https://www.brandonsanderson.com/",
            Map.of(
                "nationality", "American",
                "genres", List.of("Epic Fantasy", "Science Fiction"),
                "university", "Brigham Young University"
            )
        );
        authors.put("Brandon Sanderson", authorRepository.save(sanderson));
        
        // Stephen King
        Author king = Author.create(
            "Stephen King",
            "King, Stephen",
            Map.of("en", "Stephen Edwin King is an American author of horror, supernatural fiction, suspense, crime, science-fiction, and fantasy novels. He has published 64 novels, including seven under the pen name Richard Bachman, and more than 200 short stories."),
            LocalDate.of(1947, 9, 21),
            null,
            "https://stephenking.com/",
            Map.of(
                "nationality", "American",
                "genres", List.of("Horror", "Supernatural Fiction", "Suspense", "Science Fiction"),
                "penNames", List.of("Richard Bachman")
            )
        );
        authors.put("Stephen King", authorRepository.save(king));
        
        // Isaac Asimov
        Author asimov = Author.create(
            "Isaac Asimov",
            "Asimov, Isaac",
            Map.of("en", "Isaac Asimov was an American writer and professor of biochemistry at Boston University. He was known for his works of science fiction and popular science. Asimov was a prolific writer who wrote or edited more than 500 books."),
            LocalDate.of(1920, 1, 2),
            LocalDate.of(1992, 4, 6),
            "https://www.asimovonline.com/",
            Map.of(
                "nationality", "American",
                "genres", List.of("Science Fiction", "Popular Science", "Mystery"),
                "profession", "Biochemist"
            )
        );
        authors.put("Isaac Asimov", authorRepository.save(asimov));
        
        return authors;
    }
    
    private Map<String, Series> createComprehensiveSeries() {
        Map<String, Series> series = new HashMap<>();
        
        // The Lord of the Rings
        Series lotr = Series.create("The Lord of the Rings", "Lord of the Rings, The");
        lotr.setDescription("Epic high fantasy adventure in Middle-earth following the journey to destroy the One Ring and defeat the Dark Lord Sauron.");
        lotr.setMetadata(Map.of(
            "genre", "Epic Fantasy",
            "setting", "Middle-earth",
            "totalBooks", 3
        ));
        series.put("The Lord of the Rings", seriesRepository.save(lotr));
        
        // The Wheel of Time
        Series wot = Series.create("The Wheel of Time", "Wheel of Time, The");
        wot.setDescription("High fantasy series following the Dragon Reborn and the Last Battle against the Dark One. Originally begun by Robert Jordan and completed by Brandon Sanderson after Jordan's death.");
        wot.setMetadata(Map.of(
            "genre", "Epic Fantasy",
            "setting", "Randland",
            "totalBooks", 14,
            "originalAuthor", "Robert Jordan",
            "completedBy", "Brandon Sanderson"
        ));
        series.put("The Wheel of Time", seriesRepository.save(wot));
        
        // The Dark Tower (Stephen King)
        Series darkTower = Series.create("The Dark Tower", "Dark Tower, The");
        darkTower.setDescription("Dark fantasy series blending elements of science fiction, horror, and westerns, following Roland Deschain, the last gunslinger, on his quest for the Dark Tower.");
        darkTower.setMetadata(Map.of(
            "genre", "Dark Fantasy",
            "subgenres", List.of("Science Fiction", "Horror", "Western"),
            "totalBooks", 8
        ));
        series.put("The Dark Tower", seriesRepository.save(darkTower));
        
        // Foundation Series (Isaac Asimov)
        Series foundation = Series.create("Foundation", "Foundation");
        foundation.setDescription("Science fiction series about psychohistory and the fall and rebirth of the Galactic Empire, spanning thousands of years.");
        foundation.setMetadata(Map.of(
            "genre", "Science Fiction",
            "concepts", List.of("Psychohistory", "Galactic Empire"),
            "totalBooks", 7
        ));
        series.put("Foundation", seriesRepository.save(foundation));
        
        // Robot Series (Isaac Asimov)
        Series robot = Series.create("Robot", "Robot");
        robot.setDescription("Science fiction series exploring the relationship between humans and robots, featuring the famous Three Laws of Robotics.");
        robot.setMetadata(Map.of(
            "genre", "Science Fiction",
            "concepts", List.of("Three Laws of Robotics", "Artificial Intelligence"),
            "totalBooks", 4
        ));
        series.put("Robot", seriesRepository.save(robot));
        
        return series;
    }
    
    private void createComprehensiveBooks(Map<String, Author> authors, Map<String, Series> series) {
        // J.R.R. Tolkien - The Lord of the Rings series
        createTolkienBooks(authors.get("J.R.R. Tolkien"), series.get("The Lord of the Rings"));
        
        // The Wheel of Time series - Robert Jordan & Brandon Sanderson
        createWheelOfTimeBooks(authors.get("Robert Jordan"), authors.get("Brandon Sanderson"), series.get("The Wheel of Time"));
        
        // Stephen King books
        createStephenKingBooks(authors.get("Stephen King"), series.get("The Dark Tower"));
        
        // Isaac Asimov books
        createIsaacAsimovBooks(authors.get("Isaac Asimov"), series.get("Foundation"), series.get("Robot"));
        
    }
    
    private void createTolkienBooks(Author author, Series series) {
        // The Lord of the Rings trilogy
        String[][] lotrBooks = {
            {"The Fellowship of the Ring", "1954", "The first volume of The Lord of the Rings follows Frodo Baggins as he begins his journey to destroy the One Ring."},
            {"The Two Towers", "1954", "The second volume follows the continuing quest of the Fellowship as it divides into separate paths."},
            {"The Return of the King", "1955", "The final volume of The Lord of the Rings chronicles the final battle against Sauron and the return of the king."}
        };
        
        for (int i = 0; i < lotrBooks.length; i++) {
            String[] bookData = lotrBooks[i];
            createBookInSeries(
                bookData[0], // title
                "Tolkien, " + bookData[0], // titleSort
                author,
                series,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2]  // description
            );
        }
        
        // Other Tolkien standalone works
        String[][] standaloneBooks = {
            {"The Hobbit", "1937", "A fantasy novel about the hobbit Bilbo Baggins' journey to help reclaim the Lonely Mountain from the dragon Smaug."},
            {"The Silmarillion", "1977", "A collection of mythopoeic stories that forms the history and cosmology of Tolkien's Middle-earth."},
            {"Unfinished Tales", "1980", "A collection of stories and essays by J.R.R. Tolkien, edited and published posthumously by his son Christopher Tolkien."}
        };
        
        for (String[] bookData : standaloneBooks) {
            createStandaloneBook(
                bookData[0], // title
                "Tolkien, " + bookData[0], // titleSort
                author,
                Integer.parseInt(bookData[1]),
                bookData[2]  // description
            );
        }
        
    }
    
    private void createWheelOfTimeBooks(Author jordan, Author sanderson, Series series) {
        // Robert Jordan's books (1-11)
        String[][] jordanBooks = {
            {"The Eye of the World", "1990", "The first book in The Wheel of Time series, introducing Rand al'Thor and his friends as they flee their village."},
            {"The Great Hunt", "1990", "The second book follows the hunt for the stolen Horn of Valere."},
            {"The Dragon Reborn", "1991", "The third book focuses on Rand's journey to claim Callandor in the Stone of Tear."},
            {"The Shadow Rising", "1992", "The fourth book explores the history of the Aiel and the Two Rivers."},
            {"The Fires of Heaven", "1993", "The fifth book deals with civil war in Cairhien and the Aiel Waste."}
        };
        
        for (int i = 0; i < jordanBooks.length; i++) {
            String[] bookData = jordanBooks[i];
            createBookInSeries(
                bookData[0], // title
                "Jordan, " + bookData[0], // titleSort
                jordan,
                series,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2]  // description
            );
        }
        
        // Brandon Sanderson's completing books (12-14)
        String[][] sandersonBooks = {
            {"The Gathering Storm", "2009", "The twelfth book, completed by Brandon Sanderson from Robert Jordan's notes."},
            {"Towers of Midnight", "2010", "The thirteenth book, continuing Sanderson's completion of the series."},
            {"A Memory of Light", "2013", "The fourteenth and final book in The Wheel of Time series."}
        };
        
        for (int i = 0; i < sandersonBooks.length; i++) {
            String[] bookData = sandersonBooks[i];
            createBookInSeries(
                bookData[0], // title
                "Sanderson, " + bookData[0], // titleSort
                sanderson,
                series,
                BigDecimal.valueOf(jordanBooks.length + i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2]  // description
            );
        }
        
    }
    
    private void createStephenKingBooks(Author author, Series darkTowerSeries) {
        // The Dark Tower series
        String[][] darkTowerBooks = {
            {"The Gunslinger", "1982", "The first book in The Dark Tower series, introducing Roland Deschain, the last gunslinger."},
            {"The Drawing of the Three", "1987", "The second book where Roland draws three companions from our world."},
            {"The Waste Lands", "1991", "The third book as the ka-tet travels through the waste lands."},
            {"Wizard and Glass", "1997", "The fourth book reveals Roland's past in Mejis."}
        };
        
        for (int i = 0; i < darkTowerBooks.length; i++) {
            String[] bookData = darkTowerBooks[i];
            createBookInSeries(
                bookData[0], // title
                "King, " + bookData[0], // titleSort
                author,
                darkTowerSeries,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2]  // description
            );
        }
        
        // Stephen King standalone books
        String[][] standaloneBooks = {
            {"Carrie", "1974", "King's first published novel about a teenage girl with telekinetic powers."},
            {"The Shining", "1977", "A horror novel about a family isolated in a haunted hotel."},
            {"The Stand", "1978", "An epic post-apocalyptic dark fantasy novel."},
            {"It", "1986", "A horror novel about a creature that preys on children in the town of Derry."}
        };
        
        for (String[] bookData : standaloneBooks) {
            createStandaloneBook(
                bookData[0], // title
                "King, " + bookData[0], // titleSort
                author,
                Integer.parseInt(bookData[1]),
                bookData[2]  // description
            );
        }
        
    }
    
    private void createIsaacAsimovBooks(Author author, Series foundationSeries, Series robotSeries) {
        // Foundation series
        String[][] foundationBooks = {
            {"Foundation", "1951", "The first book in the Foundation series about Hari Seldon's psychohistory."},
            {"Foundation and Empire", "1952", "The second Foundation book dealing with the rise of the Mule."},
            {"Second Foundation", "1953", "The third book revealing the location of the Second Foundation."},
            {"Foundation's Edge", "1982", "The fourth book set centuries after the original trilogy."}
        };
        
        for (int i = 0; i < foundationBooks.length; i++) {
            String[] bookData = foundationBooks[i];
            createBookInSeries(
                bookData[0], // title
                "Asimov, " + bookData[0], // titleSort
                author,
                foundationSeries,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2]  // description
            );
        }
        
        // Robot series
        String[][] robotBooks = {
            {"I, Robot", "1950", "A collection of nine short stories about robots and the Three Laws of Robotics."},
            {"The Caves of Steel", "1954", "A science fiction detective novel featuring Elijah Baley and R. Daneel Olivaw."},
            {"The Naked Sun", "1957", "The second robot novel continuing the partnership of Baley and Daneel."},
            {"The Robots of Dawn", "1983", "The third robot novel set on the Spacer world of Aurora."}
        };
        
        for (int i = 0; i < robotBooks.length; i++) {
            String[] bookData = robotBooks[i];
            createBookInSeries(
                bookData[0], // title
                "Asimov, " + bookData[0], // titleSort
                author,
                robotSeries,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2]  // description
            );
        }
        
        // Isaac Asimov standalone books
        String[][] standaloneBooks = {
            {"The End of Eternity", "1955", "A science fiction novel about time travel and the organization Eternity."},
            {"The Gods Themselves", "1972", "A science fiction novel about parallel universes and energy exchange."},
            {"Nightfall", "1990", "An expansion of his famous short story about a planet with six suns."}
        };
        
        for (String[] bookData : standaloneBooks) {
            createStandaloneBook(
                bookData[0], // title
                "Asimov, " + bookData[0], // titleSort
                author,
                Integer.parseInt(bookData[1]),
                bookData[2]  // description
            );
        }
        
    }
    
    private String generateRandomHash() {
        Random random = new Random();
        StringBuilder hash = new StringBuilder();
        String chars = "0123456789abcdef";
        for (int i = 0; i < 64; i++) {
            hash.append(chars.charAt(random.nextInt(chars.length())));
        }
        return hash.toString();
    }
    
    private void createBookInSeries(String title, String titleSort, Author author, 
                                   Series seriesEntity, BigDecimal seriesIndex, int year,
                                   String description) {
        
        // Create book using new Book constructor
        String path = "/demo/books/" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".epub";
        Book book = new Book(title, path);
        book.setTitleSort(titleSort);
        book.setFileSize((long) (500000 + Math.random() * 2000000)); // 500KB to 2.5MB
        book.setFileHash(generateRandomHash());
        book.setHasCover(true);
        book.setPublicationDate(LocalDate.of(year, (int)(Math.random() * 12) + 1, (int)(Math.random() * 28) + 1));
        
        // Add enhanced metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("genre", "Fiction");
        metadata.put("pages", (int)(200 + Math.random() * 600));
        if (description != null && !description.trim().isEmpty()) {
            metadata.put("description", description);
        }
        book.setMetadata(metadata);
        
        // Save book and create relationships
        Book savedBook = bookRepository.save(book);
        
        // Note: In the new architecture, author and series relationships 
        // would need to be handled through separate relationship entities
        // This is a simplified version for demo purposes
    }
    
    private void createStandaloneBook(String title, String titleSort, Author author, 
                                     int year, String description) {
        String path = "/demo/books/" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".epub";
        Book book = new Book(title, path);
        book.setTitleSort(titleSort);
        book.setFileSize((long) (500000 + Math.random() * 2000000)); // 500KB to 2.5MB
        book.setFileHash(generateRandomHash());
        book.setHasCover(true);
        book.setPublicationDate(LocalDate.of(year, (int)(Math.random() * 12) + 1, (int)(Math.random() * 28) + 1));
        
        // Add enhanced metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("genre", "Fiction");
        metadata.put("pages", (int)(200 + Math.random() * 600));
        if (description != null && !description.trim().isEmpty()) {
            metadata.put("description", description);
        }
        book.setMetadata(metadata);
        
        // Save book
        Book savedBook = bookRepository.save(book);
        
        // Note: Author relationship would need to be handled through 
        // separate relationship entities in the full implementation
    }
}