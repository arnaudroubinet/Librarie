package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.motpassants.domain.core.model.*;
import org.motpassants.domain.port.out.BookRepository;
import org.motpassants.domain.port.out.AuthorRepositoryPort;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.SeriesRepositoryPort;
import org.motpassants.domain.port.out.SecureFileProcessingPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
    private final SecureFileProcessingPort secureFileProcessingPort;
    
    @Inject
    public DemoDataService(ConfigurationPort configurationPort, 
                          BookRepository bookRepository,
                          AuthorRepositoryPort authorRepository,
                          SeriesRepositoryPort seriesRepository,
                          SecureFileProcessingPort secureFileProcessingPort) {
        this.configurationPort = configurationPort;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.seriesRepository = seriesRepository;
        this.secureFileProcessingPort = secureFileProcessingPort;
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
    String tolkienImage = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/J._R._R._Tolkien%2C_ca._1925.jpg/256px-J._R._R._Tolkien%2C_ca._1925.jpg";
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
    // For demo, mark picture present
    tolkien.setHasPicture(true);
    Author savedTolkien = authorRepository.save(tolkien);
    authors.put("J.R.R. Tolkien", savedTolkien);
    downloadImageToAssets(tolkienImage, "authors", "pictures", savedTolkien.getId());
        
        // Robert Jordan
    String jordanImage = "https://images.findagrave.com/photos/2007/260/21632571_119013305313.jpg?size=photos250";
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
    jordan.setHasPicture(true);
    Author savedJordan = authorRepository.save(jordan);
    authors.put("Robert Jordan", savedJordan);
    downloadImageToAssets(jordanImage, "authors", "pictures", savedJordan.getId());
        
        // Brandon Sanderson  
    String sandersonImage = "https://cdn1.booknode.com/author_picture/5462/full/brandon-sanderson-5461821.jpg";
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
    sanderson.setHasPicture(true);
    Author savedSanderson = authorRepository.save(sanderson);
    authors.put("Brandon Sanderson", savedSanderson);
    downloadImageToAssets(sandersonImage, "authors", "pictures", savedSanderson.getId());
        
        // Stephen King
    String kingImage = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e3/Stephen_King%2C_Comicon.jpg/256px-Stephen_King%2C_Comicon.jpg";
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
    king.setHasPicture(true);
    Author savedKing = authorRepository.save(king);
    authors.put("Stephen King", savedKing);
    downloadImageToAssets(kingImage, "authors", "pictures", savedKing.getId());
        
        // Isaac Asimov
    String asimovImage = "https://upload.wikimedia.org/wikipedia/commons/thumb/3/34/Isaac.Asimov01.jpg/256px-Isaac.Asimov01.jpg";
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
    asimov.setHasPicture(true);
    Author savedAsimov = authorRepository.save(asimov);
    authors.put("Isaac Asimov", savedAsimov);
    downloadImageToAssets(asimovImage, "authors", "pictures", savedAsimov.getId());
        
        return authors;
    }
    
    private Map<String, Series> createComprehensiveSeries() {
        Map<String, Series> series = new HashMap<>();
        
        // The Lord of the Rings
    String lotrImage = "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcRqBUw96HgCQoy6ivCbnviDPbGVFY7VZ_qw-WphuHfyvJmq-2Au";
    Series lotr = Series.create("The Lord of the Rings", "Lord of the Rings, The");
    lotr.setDescription("Epic high fantasy adventure in Middle-earth following the journey to destroy the One Ring and defeat the Dark Lord Sauron.");
        lotr.setMetadata(Map.of(
            "genre", "Epic Fantasy",
            "setting", "Middle-earth",
            "totalBooks", 3
        ));
    lotr.setHasPicture(true);
    Series savedLotr = seriesRepository.save(lotr);
    series.put("The Lord of the Rings", savedLotr);
    downloadImageToAssets(lotrImage, "series", "covers", savedLotr.getId());
        
        // The Wheel of Time
    String wotImage = "https://m.media-amazon.com/images/I/71SGsrQY+yL._SL1500_.jpg";
    Series wot = Series.create("The Wheel of Time", "Wheel of Time, The");
        wot.setDescription("High fantasy series following the Dragon Reborn and the Last Battle against the Dark One. Originally begun by Robert Jordan and completed by Brandon Sanderson after Jordan's death.");
        wot.setMetadata(Map.of(
            "genre", "Epic Fantasy",
            "setting", "Randland",
            "totalBooks", 14,
            "originalAuthor", "Robert Jordan",
            "completedBy", "Brandon Sanderson"
        ));
    wot.setHasPicture(true);
    Series savedWot = seriesRepository.save(wot);
    series.put("The Wheel of Time", savedWot);
    downloadImageToAssets(wotImage, "series", "covers", savedWot.getId());
        
        // The Dark Tower (Stephen King)
    String darkTowerImage = "https://m.media-amazon.com/images/I/91EsBf4G0HL._SL1500_.jpg";
    Series darkTower = Series.create("The Dark Tower", "Dark Tower, The");
        darkTower.setDescription("Dark fantasy series blending elements of science fiction, horror, and westerns, following Roland Deschain, the last gunslinger, on his quest for the Dark Tower.");
        darkTower.setMetadata(Map.of(
            "genre", "Dark Fantasy",
            "subgenres", List.of("Science Fiction", "Horror", "Western"),
            "totalBooks", 8
        ));
    darkTower.setHasPicture(true);
    Series savedDarkTower = seriesRepository.save(darkTower);
    series.put("The Dark Tower", savedDarkTower);
    downloadImageToAssets(darkTowerImage, "series", "covers", savedDarkTower.getId());
        
        // Foundation Series (Isaac Asimov)
    String foundationImage = "https://m.media-amazon.com/images/I/81fbZnrYyoS._SL1500_.jpg";
    Series foundation = Series.create("Foundation", "Foundation");
        foundation.setDescription("Science fiction series about psychohistory and the fall and rebirth of the Galactic Empire, spanning thousands of years.");
        foundation.setMetadata(Map.of(
            "genre", "Science Fiction",
            "concepts", List.of("Psychohistory", "Galactic Empire"),
            "totalBooks", 7
        ));
    foundation.setHasPicture(true);
    Series savedFoundation = seriesRepository.save(foundation);
    series.put("Foundation", savedFoundation);
    downloadImageToAssets(foundationImage, "series", "covers", savedFoundation.getId());
        
        // Robot Series (Isaac Asimov)
    String robotImage = "https://m.media-amazon.com/images/I/71Vm4mFih+L._SL1500_.jpg";
    Series robot = Series.create("Robot", "Robot");
        robot.setDescription("Science fiction series exploring the relationship between humans and robots, featuring the famous Three Laws of Robotics.");
        robot.setMetadata(Map.of(
            "genre", "Science Fiction",
            "concepts", List.of("Three Laws of Robotics", "Artificial Intelligence"),
            "totalBooks", 4
        ));
    robot.setHasPicture(true);
    Series savedRobot = seriesRepository.save(robot);
    series.put("Robot", savedRobot);
    downloadImageToAssets(robotImage, "series", "covers", savedRobot.getId());
        
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
            {"The Fellowship of the Ring", "1954", "https://m.media-amazon.com/images/I/81krXXdKn2L._SL1500_.jpg", "The first volume of The Lord of the Rings follows Frodo Baggins as he begins his journey to destroy the One Ring."},
            {"The Two Towers", "1954", "https://m.media-amazon.com/images/I/81OWByGqGdL._SL1500_.jpg", "The second volume follows the continuing quest of the Fellowship as it divides into separate paths."},
            {"The Return of the King", "1955", "https://m.media-amazon.com/images/I/81krXXdKn2L._SL1500_.jpg", "The final volume of The Lord of the Rings chronicles the final battle against Sauron and the return of the king."}
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
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Other Tolkien standalone works
        String[][] standaloneBooks = {
            {"The Hobbit", "1937", "https://m.media-amazon.com/images/I/812AkwqjfgL._SL1500_.jpg", "A fantasy novel about the hobbit Bilbo Baggins' journey to help reclaim the Lonely Mountain from the dragon Smaug."},
            {"The Silmarillion", "1977", "https://m.media-amazon.com/images/I/81Ygs92I6GL._SL1500_.jpg", "A collection of mythopoeic stories that forms the history and cosmology of Tolkien's Middle-earth."},
            {"Unfinished Tales", "1980", "https://m.media-amazon.com/images/I/812AkwqjfgL._SL1500_.jpg", "A collection of stories and essays by J.R.R. Tolkien, edited and published posthumously by his son Christopher Tolkien."}
        };
        
        for (String[] bookData : standaloneBooks) {
            createStandaloneBook(
                bookData[0], // title
                "Tolkien, " + bookData[0], // titleSort
                author,
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
    }
    
    private void createWheelOfTimeBooks(Author jordan, Author sanderson, Series series) {
        // Robert Jordan's books (1-11)
        String[][] jordanBooks = {
            {"The Eye of the World", "1990", "https://m.media-amazon.com/images/I/71SGsrQY+yL._SL1500_.jpg", "The first book in The Wheel of Time series, introducing Rand al'Thor and his friends as they flee their village."},
            {"The Great Hunt", "1990", "https://m.media-amazon.com/images/I/81txFRYutnL._SL1500_.jpg", "The second book follows the hunt for the stolen Horn of Valere."},
            {"The Dragon Reborn", "1991", "https://m.media-amazon.com/images/I/81gjpHrpWjL._SL1500_.jpg", "The third book focuses on Rand's journey to claim Callandor in the Stone of Tear."},
            {"The Shadow Rising", "1992", "https://m.media-amazon.com/images/I/71Eztr2yp+L._SL1500_.jpg", "The fourth book explores the history of the Aiel and the Two Rivers."},
            {"The Fires of Heaven", "1993", "https://m.media-amazon.com/images/I/81L6SV7D-GL._SL1500_.jpg", "The fifth book deals with civil war in Cairhien and the Aiel Waste."}
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
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Brandon Sanderson's completing books (12-14)
        String[][] sandersonBooks = {
            {"The Gathering Storm", "2009", "https://m.media-amazon.com/images/I/81nIiL65y2L._SL1500_.jpg", "The twelfth book, completed by Brandon Sanderson from Robert Jordan's notes."},
            {"Towers of Midnight", "2010", "https://m.media-amazon.com/images/I/81s8xAwqrXL._SL1500_.jpg", "The thirteenth book, continuing Sanderson's completion of the series."},
            {"A Memory of Light", "2013", "https://m.media-amazon.com/images/I/819qaQPNKoL._SL1500_.jpg", "The fourteenth and final book in The Wheel of Time series."}
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
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
    }
    
    private void createStephenKingBooks(Author author, Series darkTowerSeries) {
        // The Dark Tower series
        String[][] darkTowerBooks = {
            {"The Gunslinger", "1982", "https://m.media-amazon.com/images/I/91EsBf4G0HL._SL1500_.jpg", "The first book in The Dark Tower series, introducing Roland Deschain, the last gunslinger."},
            {"The Drawing of the Three", "1987", "https://m.media-amazon.com/images/I/91W49YUjiML._SL1500_.jpg", "The second book where Roland draws three companions from our world."},
            {"The Waste Lands", "1991", "https://m.media-amazon.com/images/I/91EsBf4G0HL._SL1500_.jpg", "The third book as the ka-tet travels through the waste lands."},
            {"Wizard and Glass", "1997", "https://m.media-amazon.com/images/I/91xib2N15WL._SL1500_.jpg", "The fourth book reveals Roland's past in Mejis."}
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
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Stephen King standalone books
        String[][] standaloneBooks = {
            {"Carrie", "1974", "https://m.media-amazon.com/images/I/71JVmpGUqhL._SL1500_.jpg", "King's first published novel about a teenage girl with telekinetic powers."},
            {"The Shining", "1977", "https://m.media-amazon.com/images/I/71LWM5fjbRL._SL1500_.jpg", "A horror novel about a family isolated in a haunted hotel."},
            {"The Stand", "1978", "https://m.media-amazon.com/images/I/815cT8CLaKL._SL1500_.jpg", "An epic post-apocalyptic dark fantasy novel."},
            {"It", "1986", "https://m.media-amazon.com/images/I/71JVmpGUqhL._SL1500_.jpg", "A horror novel about a creature that preys on children in the town of Derry."}
        };
        
        for (String[] bookData : standaloneBooks) {
            createStandaloneBook(
                bookData[0], // title
                "King, " + bookData[0], // titleSort
                author,
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
    }
    
    private void createIsaacAsimovBooks(Author author, Series foundationSeries, Series robotSeries) {
        // Foundation series
        String[][] foundationBooks = {
            {"Foundation", "1951", "https://m.media-amazon.com/images/I/81fbZnrYyoS._SL1500_.jpg", "The first book in the Foundation series about Hari Seldon's psychohistory."},
            {"Foundation and Empire", "1952", "https://m.media-amazon.com/images/I/91GMN74HkyS._SL1500_.jpg", "The second Foundation book dealing with the rise of the Mule."},
            {"Second Foundation", "1953", "https://m.media-amazon.com/images/I/61yeuxTHLSL._SL1120_.jpg", "The third book revealing the location of the Second Foundation."},
            {"Foundation's Edge", "1982", "https://m.media-amazon.com/images/I/81fbZnrYyoS._SL1500_.jpg", "The fourth book set centuries after the original trilogy."}
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
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Robot series
        String[][] robotBooks = {
            {"I, Robot", "1950", "https://m.media-amazon.com/images/I/51v9O7rn83L.jpg", "A collection of nine short stories about robots and the Three Laws of Robotics."},
            {"The Caves of Steel", "1954", "https://m.media-amazon.com/images/I/61vi13m2tvL._SL1120_.jpg", "A science fiction detective novel featuring Elijah Baley and R. Daneel Olivaw."},
            {"The Naked Sun", "1957", "https://m.media-amazon.com/images/I/71mvqJWhTKL._SL1500_.jpg", "The second robot novel continuing the partnership of Baley and Daneel."},
            {"The Robots of Dawn", "1983", "https://m.media-amazon.com/images/I/71Vm4mFih+L._SL1500_.jpg", "The third robot novel set on the Spacer world of Aurora."}
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
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Isaac Asimov standalone books
        String[][] standaloneBooks = {
            {"The End of Eternity", "1955", "https://m.media-amazon.com/images/I/71GVYzeOtYL._SL1500_.jpg", "A science fiction novel about time travel and the organization Eternity."},
            {"The Gods Themselves", "1972", "https://m.media-amazon.com/images/I/917nJWgAiBL._SL1500_.jpg", "A science fiction novel about parallel universes and energy exchange."},
            {"Nightfall", "1990", "https://m.media-amazon.com/images/I/81jNOXtgQWL._SL1500_.jpg", "An expansion of his famous short story about a planet with six suns."}
        };
        
        for (String[] bookData : standaloneBooks) {
            createStandaloneBook(
                bookData[0], // title
                "Asimov, " + bookData[0], // titleSort
                author,
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
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
                                   String coverUrl, String description) {
        
        // Create book using new Book constructor
        String path = "/demo/books/" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".epub";
        Book book = new Book(title, path);
        book.setTitleSort(titleSort);
        book.setFileSize((long) (500000 + Math.random() * 2000000)); // 500KB to 2.5MB
    book.setFileHash(generateRandomHash());
    // Populate identifiers and language with hardcoded demo values (BCP 47 tag)
    book.setIsbn("9780000000002");
    book.setLanguage("en-US");
    // Do not persist cover info in DB; we'll hydrate local assets instead
        book.setPublicationDate(LocalDate.of(year, (int)(Math.random() * 12) + 1, (int)(Math.random() * 28) + 1));
        
        // Add enhanced metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("genre", "Fiction");
        metadata.put("pages", (int)(200 + Math.random() * 600));
    if (description != null && !description.trim().isEmpty()) {
            metadata.put("description", description);
        }
        book.setMetadata(metadata);
        
    // In demo, mark cover present and ensure a placeholder book file exists
    book.setHasCover(true);
    ensureDemoBookFileExists(path);
    // Save book and hydrate cover asset locally (if URL provided)
    bookRepository.save(book);
        if (coverUrl != null && !coverUrl.isBlank() && book.getId() != null) {
            downloadImageToAssets(coverUrl, "books", "covers", book.getId());
        }
        
        // Note: In the new architecture, author and series relationships 
        // would need to be handled through separate relationship entities
        // This is a simplified version for demo purposes
    }
    
    private void createStandaloneBook(String title, String titleSort, Author author, 
                                     int year, String coverUrl, String description) {
        String path = "/demo/books/" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".epub";
        Book book = new Book(title, path);
        book.setTitleSort(titleSort);
        book.setFileSize((long) (500000 + Math.random() * 2000000)); // 500KB to 2.5MB
    book.setFileHash(generateRandomHash());
    // Populate identifiers and language with hardcoded demo values (BCP 47 tag)
    book.setIsbn("9780000000002");
    book.setLanguage("en-US");
    // Do not persist cover info in DB; we'll hydrate local assets instead
        book.setPublicationDate(LocalDate.of(year, (int)(Math.random() * 12) + 1, (int)(Math.random() * 28) + 1));
        
        // Add enhanced metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("genre", "Fiction");
        metadata.put("pages", (int)(200 + Math.random() * 600));
        if (description != null && !description.trim().isEmpty()) {
            metadata.put("description", description);
        }
        book.setMetadata(metadata);
        
    // In demo, mark cover present and ensure a placeholder book file exists
    book.setHasCover(true);
    ensureDemoBookFileExists(path);
    // Save book and hydrate cover asset locally (if URL provided)
    bookRepository.save(book);
        if (coverUrl != null && !coverUrl.isBlank() && book.getId() != null) {
            downloadImageToAssets(coverUrl, "books", "covers", book.getId());
        }
        
        // Note: Author relationship would need to be handled through 
        // separate relationship entities in the full implementation
    }

    private void downloadImageToAssets(String url, String folder, String subFolder, UUID id) {
        try {
            if (url == null || url.isBlank() || id == null) return;
            // Prepare paths safely
            String baseDir = configurationPort.getStorageConfig().getBaseDir();
            Path base = secureFileProcessingPort.sanitizePath(baseDir, ".");
            Path targetDir = base.resolve(folder).resolve(subFolder);
            try { Files.createDirectories(targetDir); } catch (Exception ignored) {}
            Path target = targetDir.resolve(id.toString());

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "Librarie-Demo/1.0")
                    .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                    .GET()
                    .build();
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300 && resp.body() != null && resp.body().length > 0) {
                Files.write(target, resp.body(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (Exception ignored) {
            // Best-effort: demo hydration shouldn't fail startup
        }
    }

    private void ensureDemoBookFileExists(String relativePath) {
        try {
            if (relativePath == null || relativePath.isBlank()) return;
            String baseDir = configurationPort.getStorageConfig().getBaseDir();
            // Normalize input path and ensure it's within baseDir
            String safeRel = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            Path safeTarget = secureFileProcessingPort.sanitizePath(baseDir, safeRel);
            // Create parent directories
            try { Files.createDirectories(safeTarget.getParent()); } catch (Exception ignored) {}
            // If file doesn't exist, write a tiny placeholder EPUB-like file
            if (!Files.exists(safeTarget)) {
                byte[] minimalEpub = new byte[] { 'P','K',3,4,20,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 }; // zip header
                Files.write(safeTarget, minimalEpub, StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception ignored) {
        }
    }
}