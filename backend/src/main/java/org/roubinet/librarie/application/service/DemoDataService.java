package org.roubinet.librarie.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.roubinet.librarie.domain.entity.*;
import org.roubinet.librarie.infrastructure.config.LibrarieConfigProperties;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Service for populating demo data when demo mode is enabled.
 * Creates hundreds of realistic books, series, authors, and publishers.
 */
@ApplicationScoped
public class DemoDataService {
    
    private static final Logger LOG = Logger.getLogger(DemoDataService.class);
    
    private final LibrarieConfigProperties config;
    private final EntityManager entityManager;
    
    @Inject
    public DemoDataService(LibrarieConfigProperties config, EntityManager entityManager) {
        this.config = config;
        this.entityManager = entityManager;
    }
    
    @Transactional
    public void populateDemoData() {
        if (!config.demo().enabled()) {
            LOG.info("Demo mode disabled, skipping demo data population");
            return;
        }
        
        if (Book.count() > 0) {
            LOG.info("Database already contains books, skipping demo data population");
            return;
        }
        
        LOG.info("Demo mode enabled, populating demo data...");
        
        // Create reference data first
        Map<String, Language> languages = createLanguages();
        Map<String, Publisher> publishers = createPublishers();
        Map<String, Author> authors = createAuthors();
        
        // Create simple standalone books only
        createSimpleBooks(languages, publishers, authors);
        
        LOG.info("Demo data population completed successfully");
    }
    
    private Map<String, Language> createLanguages() {
        Map<String, Language> languages = new HashMap<>();
        
        String[][] languageData = {
            {"en-US", "English (United States)", "false"},
            {"en-GB", "English (United Kingdom)", "false"},
            {"fr-FR", "French (France)", "false"},
            {"fr-CA", "French (Canada)", "false"},
            {"es-ES", "Spanish (Spain)", "false"},
            {"de-DE", "German (Germany)", "false"},
            {"it-IT", "Italian (Italy)", "false"},
            {"pt-BR", "Portuguese (Brazil)", "false"},
            {"ru-RU", "Russian (Russia)", "false"},
            {"ja-JP", "Japanese (Japan)", "false"},
            {"zh-CN", "Chinese (Simplified)", "false"},
            {"ar-SA", "Arabic (Saudi Arabia)", "true"}
        };
        
        Arrays.stream(languageData)
            .forEach(data -> {
                // Check if language already exists by trying to find and merge
                try {
                    Language existing = entityManager.find(Language.class, data[0]);
                    if (existing == null) {
                        Language language = new Language(data[0], data[1], Boolean.parseBoolean(data[2]));
                        entityManager.persist(language);
                        languages.put(data[0], language);
                    } else {
                        languages.put(data[0], existing);
                    }
                } catch (Exception e) {
                    // If persist fails due to constraint, try to find existing
                    Language existing = entityManager.find(Language.class, data[0]);
                    if (existing != null) {
                        languages.put(data[0], existing);
                    }
                }
            });
        
        return languages;
    }
    
    private Map<String, Publisher> createPublishers() {
        Map<String, Publisher> publishers = new HashMap<>();
        
        String[] publisherNames = {
            "Penguin Random House", "HarperCollins", "Macmillan Publishers", "Simon & Schuster",
            "Hachette Book Group", "Scholastic", "Pearson Education", "Wiley", "Oxford University Press",
            "Cambridge University Press", "Bantam Books", "Del Rey", "Tor Books", "Ace Books",
            "DAW Books", "Baen Books", "Orbit", "Gallery Books", "Crown Publishing", "Knopf",
            "Vintage Books", "Anchor Books", "Ballantine Books", "Doubleday", "Dutton",
            "G.P. Putnam's Sons", "Viking Press", "Little, Brown and Company", "Scribner",
            "Atheneum Books", "Farrar, Straus and Giroux", "Henry Holt and Company"
        };
        
        for (String name : publisherNames) {
            // Check if publisher already exists by name
            Publisher existing = Publisher.find("name", name).firstResult();
            if (existing == null) {
                Publisher publisher = new Publisher();
                publisher.setName(name);
                publisher.setWebsiteUrl("https://" + name.toLowerCase().replace(" ", "").replace("&", "and") + ".com");
                publisher.setMetadata(Map.of("founded", getRandomYear(1800, 1990), "country", "US"));
                publisher.persist();
                publishers.put(name, publisher);
            } else {
                publishers.put(name, existing);
            }
        }
        
        return publishers;
    }
    
    private Map<String, Author> createAuthors() {
        Map<String, Author> authors = new HashMap<>();
        
        String[][] authorData = {
            // Classic Literature
            {"Jane Austen", "Austen, Jane", "1775", "1817"},
            {"Charles Dickens", "Dickens, Charles", "1812", "1870"},
            {"William Shakespeare", "Shakespeare, William", "1564", "1616"},
            {"Leo Tolstoy", "Tolstoy, Leo", "1828", "1910"},
            {"Mark Twain", "Twain, Mark", "1835", "1910"},
            {"Emily Brontë", "Brontë, Emily", "1818", "1848"},
            {"Charlotte Brontë", "Brontë, Charlotte", "1816", "1855"},
            {"George Orwell", "Orwell, George", "1903", "1950"},
            {"Virginia Woolf", "Woolf, Virginia", "1882", "1941"},
            {"James Joyce", "Joyce, James", "1882", "1941"},
            
            // Modern Fiction
            {"J.K. Rowling", "Rowling, J.K.", "1965", null},
            {"Stephen King", "King, Stephen", "1947", null},
            {"Margaret Atwood", "Atwood, Margaret", "1939", null},
            {"Haruki Murakami", "Murakami, Haruki", "1949", null},
            {"Toni Morrison", "Morrison, Toni", "1931", "2019"},
            {"Gabriel García Márquez", "Márquez, Gabriel García", "1927", "2014"},
            {"Isabel Allende", "Allende, Isabel", "1942", null},
            {"Paulo Coelho", "Coelho, Paulo", "1947", null},
            {"Dan Brown", "Brown, Dan", "1964", null},
            {"John Grisham", "Grisham, John", "1955", null},
            
            // Science Fiction & Fantasy
            {"Isaac Asimov", "Asimov, Isaac", "1920", "1992"},
            {"Arthur C. Clarke", "Clarke, Arthur C.", "1917", "2008"},
            {"Robert Heinlein", "Heinlein, Robert", "1907", "1988"},
            {"Philip K. Dick", "Dick, Philip K.", "1928", "1982"},
            {"Ursula K. Le Guin", "Le Guin, Ursula K.", "1929", "2018"},
            {"Frank Herbert", "Herbert, Frank", "1920", "1986"},
            {"J.R.R. Tolkien", "Tolkien, J.R.R.", "1892", "1973"},
            {"George R.R. Martin", "Martin, George R.R.", "1948", null},
            {"Brandon Sanderson", "Sanderson, Brandon", "1975", null},
            {"Neil Gaiman", "Gaiman, Neil", "1960", null},
            
            // Mystery & Thriller
            {"Agatha Christie", "Christie, Agatha", "1890", "1976"},
            {"Arthur Conan Doyle", "Doyle, Arthur Conan", "1859", "1930"},
            {"Raymond Chandler", "Chandler, Raymond", "1888", "1959"},
            {"Dashiell Hammett", "Hammett, Dashiell", "1894", "1961"},
            {"Michael Crichton", "Crichton, Michael", "1942", "2008"},
            {"Tom Clancy", "Clancy, Tom", "1947", "2013"},
            {"Lee Child", "Child, Lee", "1954", null},
            {"James Patterson", "Patterson, James", "1947", null},
            {"Gillian Flynn", "Flynn, Gillian", "1971", null},
            {"Louise Penny", "Penny, Louise", "1958", null}
        };
        
        for (String[] data : authorData) {
            // Check if author already exists by name
            Author existing = Author.find("name", data[0]).firstResult();
            if (existing == null) {
                Author author = new Author();
                author.setName(data[0]);
                author.setSortName(data[1]);
                author.setBirthDate(data[2] != null ? LocalDate.of(Integer.parseInt(data[2]), 1, 1) : null);
                author.setDeathDate(data[3] != null ? LocalDate.of(Integer.parseInt(data[3]), 1, 1) : null);
                author.setBio(Map.of("en", "Renowned author known for literary contributions to world literature."));
                author.persist();
                authors.put(data[0], author);
            } else {
                authors.put(data[0], existing);
            }
        }
        
        return authors;
    }
    
    private Map<String, Series> createSeries() {
        Map<String, Series> series = new HashMap<>();
        
        String[][] seriesData = {
            {"Harry Potter", "Potter, Harry", "The magical adventures of Harry Potter at Hogwarts School of Witchcraft and Wizardry."},
            {"The Chronicles of Narnia", "Chronicles of Narnia, The", "Fantasy series about children who discover a magical land through a wardrobe."},
            {"A Song of Ice and Fire", "Song of Ice and Fire, A", "Epic fantasy series set in the fictional Seven Kingdoms of Westeros."},
            {"The Lord of the Rings", "Lord of the Rings, The", "Epic high fantasy adventure in Middle-earth."},
            {"Foundation", "Foundation", "Science fiction series about psychohistory and the fall of the Galactic Empire."},
            {"Dune Chronicles", "Dune Chronicles", "Science fiction saga set in the distant future on the desert planet Arrakis."},
            {"The Wheel of Time", "Wheel of Time, The", "High fantasy series following the Dragon Reborn and the Last Battle."},
            {"Hercule Poirot", "Poirot, Hercule", "Mystery series featuring the Belgian detective Hercule Poirot."},
            {"Sherlock Holmes", "Holmes, Sherlock", "Classic detective stories featuring the brilliant consulting detective."},
            {"Jack Reacher", "Reacher, Jack", "Thriller series following former military police officer Jack Reacher."},
            {"The Hunger Games", "Hunger Games, The", "Dystopian trilogy set in post-apocalyptic North America."},
            {"Twilight", "Twilight", "Vampire romance series set in the Pacific Northwest."},
            {"The Millennium Trilogy", "Millennium Trilogy, The", "Swedish crime thriller series featuring Lisbeth Salander."},
            {"The Dark Tower", "Dark Tower, The", "Dark fantasy series blending elements of science fiction, horror, and westerns."},
            {"Discworld", "Discworld", "Comic fantasy series set on a flat world carried by four elephants on a giant turtle."},
            {"The Culture", "Culture, The", "Science fiction series about a post-scarcity anarchist utopia."},
            {"Outlander", "Outlander", "Historical fiction series featuring time travel and romance."},
            {"The Expanse", "Expanse, The", "Space opera series set in a future where humanity has colonized the solar system."},
            {"The Witcher", "Witcher, The", "Fantasy series about Geralt of Rivia, a monster hunter in a medieval-inspired world."},
            {"Inspector Gamache", "Gamache, Inspector", "Mystery series set in the fictional village of Three Pines, Quebec."}
        };
        
        for (String[] data : seriesData) {
            Series seriesEntity = new Series(data[0], data[1]);
            seriesEntity.setDescription(data[2]);
            seriesEntity.persist();
            series.put(data[0], seriesEntity);
        }
        
        return series;
    }
    
    private void createBooksAndOriginalWorks(Map<String, Language> languages, 
                                            Map<String, Publisher> publishers,
                                            Map<String, Author> authors, 
                                            Map<String, Series> series) {
        Random random = new Random();
        List<String> publisherList = new ArrayList<>(publishers.keySet());
        
        // Harry Potter Series
        createHarryPotterSeries(languages, publishers, authors, series);
        
        // Lord of the Rings
        createLordOfTheRingsSeries(languages, publishers, authors, series);
        
        // Foundation Series
        createFoundationSeries(languages, publishers, authors, series);
        
        // Sherlock Holmes
        createSherlockHolmesSeries(languages, publishers, authors, series);
        
        // Hercule Poirot (sample books)
        createHerculePoirotSeries(languages, publishers, authors, series);
        
        // Jack Reacher (sample books)
        createJackReacherSeries(languages, publishers, authors, series);
        
        // Standalone classics and modern books
        createStandaloneBooks(languages, publishers, authors, random, publisherList);
    }
    
    private void createHarryPotterSeries(Map<String, Language> languages, 
                                        Map<String, Publisher> publishers,
                                        Map<String, Author> authors, 
                                        Map<String, Series> series) {
        String[] titles = {
            "Harry Potter and the Philosopher's Stone",
            "Harry Potter and the Chamber of Secrets", 
            "Harry Potter and the Prisoner of Azkaban",
            "Harry Potter and the Goblet of Fire",
            "Harry Potter and the Order of the Phoenix",
            "Harry Potter and the Half-Blood Prince",
            "Harry Potter and the Deathly Hallows"
        };
        
        int[] years = {1997, 1998, 1999, 2000, 2003, 2005, 2007};
        
        for (int i = 0; i < titles.length; i++) {
            createBookInSeries(titles[i], "Rowling, " + titles[i], 
                             authors.get("J.K. Rowling"), 
                             publishers.get("Bantam Books"),
                             languages.get("en"), 
                             series.get("Harry Potter"),
                             BigDecimal.valueOf(i + 1),
                             years[i]);
        }
    }
    
    private void createLordOfTheRingsSeries(Map<String, Language> languages, 
                                           Map<String, Publisher> publishers,
                                           Map<String, Author> authors, 
                                           Map<String, Series> series) {
        String[] titles = {
            "The Fellowship of the Ring",
            "The Two Towers",
            "The Return of the King"
        };
        
        for (int i = 0; i < titles.length; i++) {
            createBookInSeries(titles[i], "Tolkien, " + titles[i],
                             authors.get("J.R.R. Tolkien"),
                             publishers.get("Bantam Books"),
                             languages.get("en"),
                             series.get("The Lord of the Rings"),
                             BigDecimal.valueOf(i + 1),
                             1954 + i);
        }
    }
    
    private void createFoundationSeries(Map<String, Language> languages, 
                                       Map<String, Publisher> publishers,
                                       Map<String, Author> authors, 
                                       Map<String, Series> series) {
        String[] titles = {
            "Foundation",
            "Foundation and Empire", 
            "Second Foundation",
            "Foundation's Edge",
            "Foundation and Earth",
            "Prelude to Foundation",
            "Forward the Foundation"
        };
        
        int[] years = {1951, 1952, 1953, 1982, 1986, 1988, 1993};
        
        for (int i = 0; i < titles.length; i++) {
            createBookInSeries(titles[i], "Asimov, " + titles[i],
                             authors.get("Isaac Asimov"),
                             publishers.get("Bantam Books"),
                             languages.get("en"),
                             series.get("Foundation"),
                             BigDecimal.valueOf(i + 1),
                             years[i]);
        }
    }
    
    private void createSherlockHolmesSeries(Map<String, Language> languages, 
                                           Map<String, Publisher> publishers,
                                           Map<String, Author> authors, 
                                           Map<String, Series> series) {
        String[] titles = {
            "A Study in Scarlet",
            "The Sign of the Four",
            "The Adventures of Sherlock Holmes",
            "The Memoirs of Sherlock Holmes",
            "The Hound of the Baskervilles",
            "The Return of Sherlock Holmes",
            "The Valley of Fear",
            "His Last Bow",
            "The Case-Book of Sherlock Holmes"
        };
        
        int[] years = {1887, 1890, 1892, 1893, 1902, 1905, 1915, 1917, 1927};
        
        for (int i = 0; i < titles.length; i++) {
            createBookInSeries(titles[i], "Doyle, " + titles[i],
                             authors.get("Arthur Conan Doyle"),
                             publishers.get("Penguin Random House"),
                             languages.get("en"),
                             series.get("Sherlock Holmes"),
                             BigDecimal.valueOf(i + 1),
                             years[i]);
        }
    }
    
    private void createHerculePoirotSeries(Map<String, Language> languages, 
                                          Map<String, Publisher> publishers,
                                          Map<String, Author> authors, 
                                          Map<String, Series> series) {
        String[] titles = {
            "The Mysterious Affair at Styles",
            "The Murder on the Links",
            "The Murder of Roger Ackroyd",
            "The Big Four",
            "The Mystery of the Blue Train",
            "Peril at End House",
            "Lord Edgware Dies",
            "Murder on the Orient Express",
            "Three Act Tragedy",
            "Death on the Nile",
            "The A.B.C. Murders",
            "Murder in Mesopotamia",
            "Cards on the Table",
            "Dumb Witness",
            "Death on the Nile",
            "Curtain"
        };
        
        int startYear = 1920;
        
        for (int i = 0; i < Math.min(titles.length, 16); i++) {
            createBookInSeries(titles[i], "Christie, " + titles[i],
                             authors.get("Agatha Christie"),
                             publishers.get("HarperCollins"),
                             languages.get("en"),
                             series.get("Hercule Poirot"),
                             BigDecimal.valueOf(i + 1),
                             startYear + i * 2);
        }
    }
    
    private void createJackReacherSeries(Map<String, Language> languages, 
                                        Map<String, Publisher> publishers,
                                        Map<String, Author> authors, 
                                        Map<String, Series> series) {
        String[] titles = {
            "Killing Floor",
            "Die Trying", 
            "Tripwire",
            "The Visitor",
            "Echo Burning",
            "Without Fail",
            "Persuader",
            "The Enemy",
            "One Shot",
            "The Hard Way",
            "Bad Luck and Trouble",
            "Nothing to Lose",
            "Gone Tomorrow",
            "61 Hours"
        };
        
        int startYear = 1997;
        
        for (int i = 0; i < Math.min(titles.length, 14); i++) {
            createBookInSeries(titles[i], "Child, " + titles[i],
                             authors.get("Lee Child"),
                             publishers.get("Bantam Books"),
                             languages.get("en"),
                             series.get("Jack Reacher"),
                             BigDecimal.valueOf(i + 1),
                             startYear + i);
        }
    }
    
    private void createStandaloneBooks(Map<String, Language> languages, 
                                      Map<String, Publisher> publishers,
                                      Map<String, Author> authors, 
                                      Random random,
                                      List<String> publisherList) {
        
        // Classic standalone novels
        String[][] classicBooks = {
            {"Pride and Prejudice", "Jane Austen", "1813"},
            {"Jane Eyre", "Charlotte Brontë", "1847"},
            {"Wuthering Heights", "Emily Brontë", "1847"},
            {"Great Expectations", "Charles Dickens", "1861"},
            {"A Tale of Two Cities", "Charles Dickens", "1859"},
            {"1984", "George Orwell", "1949"},
            {"Animal Farm", "George Orwell", "1945"},
            {"To Kill a Mockingbird", "Harper Lee", "1960"},
            {"The Catcher in the Rye", "J.D. Salinger", "1951"},
            {"One Hundred Years of Solitude", "Gabriel García Márquez", "1967"},
            {"The Alchemist", "Paulo Coelho", "1988"},
            {"Beloved", "Toni Morrison", "1987"},
            {"Norwegian Wood", "Haruki Murakami", "1987"},
            {"The Handmaid's Tale", "Margaret Atwood", "1985"},
            {"Dune", "Frank Herbert", "1965"},
            {"Fahrenheit 451", "Ray Bradbury", "1953"},
            {"Brave New World", "Aldous Huxley", "1932"},
            {"The Time Machine", "H.G. Wells", "1895"},
            {"Frankenstein", "Mary Shelley", "1818"},
            {"Dracula", "Bram Stoker", "1897"}
        };
        
        for (String[] bookData : classicBooks) {
            Author author = authors.get(bookData[1]);
            if (author != null) {
                String publisherName = publisherList.get(random.nextInt(publisherList.size()));
                createStandaloneBook(bookData[0], 
                                   author.getSortName() + ", " + bookData[0],
                                   author,
                                   publishers.get(publisherName),
                                   languages.get("en"),
                                   Integer.parseInt(bookData[2]));
            }
        }
        
        // Additional modern books to reach hundreds
        createAdditionalModernBooks(languages, publishers, authors, random, publisherList);
    }
    
    private void createAdditionalModernBooks(Map<String, Language> languages, 
                                            Map<String, Publisher> publishers,
                                            Map<String, Author> authors, 
                                            Random random,
                                            List<String> publisherList) {
        
        String[] titleTemplates = {
            "The {adjective} {noun}", "A {adjective} {noun}", "The {noun} of {noun}",
            "Beyond the {noun}", "The Last {noun}", "The Secret {noun}",
            "Tales from {location}", "The {adjective} {profession}",
            "Mysteries of {location}", "The {noun} Chronicles"
        };
        
        String[] adjectives = {
            "Ancient", "Hidden", "Forgotten", "Lost", "Sacred", "Dark", "Golden",
            "Silent", "Mystic", "Eternal", "Broken", "Shattered", "Crimson",
            "Silver", "Royal", "Noble", "Wild", "Distant", "Infinite", "Perfect"
        };
        
        String[] nouns = {
            "Kingdom", "Empire", "Castle", "Tower", "Forest", "Mountain", "Ocean",
            "Desert", "City", "Village", "Palace", "Temple", "Library", "Garden",
            "Bridge", "River", "Valley", "Island", "Harbor", "Cathedral"
        };
        
        String[] locations = {
            "Atlantis", "Avalon", "Eldorado", "Shangri-La", "Camelot", "Olympus",
            "the North", "the East", "the West", "the Mountains", "the Sea"
        };
        
        String[] professions = {
            "Scholar", "Warrior", "Merchant", "Priest", "Knight", "Wizard",
            "Hunter", "Sailor", "Explorer", "Artist", "Poet", "Musician"
        };
        
        List<Author> authorList = new ArrayList<>(authors.values());
        
        // Generate additional books to reach around 200+ books total
        for (int i = 0; i < 100; i++) {
            String template = titleTemplates[random.nextInt(titleTemplates.length)];
            String title = generateTitleFromTemplate(template, adjectives, nouns, locations, professions, random);
            
            Author author = authorList.get(random.nextInt(authorList.size()));
            String publisherName = publisherList.get(random.nextInt(publisherList.size()));
            int year = getRandomYear(1950, 2023);
            
            createStandaloneBook(title,
                               author.getSortName() + ", " + title,
                               author,
                               publishers.get(publisherName),
                               languages.get("en"),
                               year);
        }
    }
    
    private String generateTitleFromTemplate(String template, String[] adjectives, String[] nouns, 
                                           String[] locations, String[] professions, Random random) {
        return template
            .replace("{adjective}", adjectives[random.nextInt(adjectives.length)])
            .replace("{noun}", nouns[random.nextInt(nouns.length)])
            .replace("{location}", locations[random.nextInt(locations.length)])
            .replace("{profession}", professions[random.nextInt(professions.length)]);
    }
    
    private void createBookInSeries(String title, String titleSort, Author author, Publisher publisher,
                                   Language language, Series seriesEntity, BigDecimal seriesIndex, int year) {
        // Create original work
        OriginalWork originalWork = new OriginalWork();
        originalWork.setTitle(title);
        originalWork.setTitleSort(titleSort);
        originalWork.setFirstPublication(LocalDate.of(year, 1, 1));
        originalWork.setDescription("A captivating work of literature.");
        originalWork.persist();
        
        // Link author to original work
        OriginalWorkAuthor originalWorkAuthor = new OriginalWorkAuthor(originalWork, author, "author");
        entityManager.persist(originalWorkAuthor);
        
        // Create book
        Book book = new Book();
        book.setTitle(title);
        book.setTitleSort(titleSort);
        book.setPath("/demo/books/" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".epub");
        book.setFileSize((long) (500000 + Math.random() * 2000000)); // 500KB to 2.5MB
        book.setFileHash(generateRandomHash());
        book.setHasCover(Math.random() > 0.3); // 70% have covers
        book.setPublicationDate(LocalDate.of(year, (int)(Math.random() * 12) + 1, (int)(Math.random() * 28) + 1));
        book.setLanguage(language);
        book.setPublisher(publisher);
        book.setMetadata(Map.of("genre", "Fiction", "pages", (int)(200 + Math.random() * 600)));
        book.persist();
        
        // Link book to original work
        BookOriginalWork bookOriginalWork = new BookOriginalWork(book, originalWork, 
                                                                  BookOriginalWorkRelationType.PRIMARY, 0);
        entityManager.persist(bookOriginalWork);
        
        // Link book to series  
        BookSeries bookSeries = new BookSeries(book, seriesEntity, seriesIndex);
        entityManager.persist(bookSeries);
        
        // Update series book count
        seriesEntity.incrementBookCount();
        entityManager.persist(seriesEntity);
    }
    
    private void createStandaloneBook(String title, String titleSort, Author author, Publisher publisher,
                                     Language language, int year) {
        // Create original work
        OriginalWork originalWork = new OriginalWork();
        originalWork.setTitle(title);
        originalWork.setTitleSort(titleSort);
        originalWork.setFirstPublication(LocalDate.of(year, 1, 1));
        originalWork.setDescription("A remarkable standalone work of literature.");
        originalWork.persist();
        
        // Link author to original work
        OriginalWorkAuthor originalWorkAuthor = new OriginalWorkAuthor(originalWork, author, "author");
        entityManager.persist(originalWorkAuthor);
        
        // Create book
        Book book = new Book();
        book.setTitle(title);
        book.setTitleSort(titleSort);
        book.setPath("/demo/books/" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".epub");
        book.setFileSize((long) (500000 + Math.random() * 2000000)); // 500KB to 2.5MB
        book.setFileHash(generateRandomHash());
        book.setHasCover(Math.random() > 0.3); // 70% have covers
        book.setPublicationDate(LocalDate.of(year, (int)(Math.random() * 12) + 1, (int)(Math.random() * 28) + 1));
        book.setLanguage(language);
        book.setPublisher(publisher);
        book.setMetadata(Map.of("genre", "Fiction", "pages", (int)(200 + Math.random() * 600)));
        book.persist();
        
        // Link book to original work
        BookOriginalWork bookOriginalWork = new BookOriginalWork(book, originalWork, 
                                                                  BookOriginalWorkRelationType.PRIMARY, 0);
        entityManager.persist(bookOriginalWork);
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
    
    private int getRandomYear(int startYear, int endYear) {
        Random random = new Random();
        return startYear + random.nextInt(endYear - startYear + 1);
    }
    
    private void createSimpleBooks(Map<String, Language> languages, Map<String, Publisher> publishers, Map<String, Author> authors) {
        LOG.info("Creating simple demo books...");
        
        // Sample book data
        String[][] bookData = {
            {"The Great Gatsby", "English (United States)", "Charles Scribner's Sons", "F. Scott Fitzgerald"},
            {"To Kill a Mockingbird", "English (United States)", "J.B. Lippincott & Co.", "Harper Lee"},
            {"1984", "English (United Kingdom)", "Secker & Warburg", "George Orwell"},
            {"Pride and Prejudice", "English (United Kingdom)", "T. Egerton", "Jane Austen"},
            {"Le Petit Prince", "French (France)", "Reynal & Hitchcock", "Antoine de Saint-Exupéry"},
            {"L'Étranger", "French (France)", "Gallimard", "Albert Camus"},
            {"Don Quijote", "Spanish (Spain)", "Francisco de Robles", "Miguel de Cervantes"},
            {"Cien años de soledad", "Spanish (Spain)", "Editorial Sudamericana", "Gabriel García Márquez"},
            {"Der Prozess", "German (Germany)", "Die Schmiede", "Franz Kafka"},
            {"Faust", "German (Germany)", "J.G. Cotta", "Johann Wolfgang von Goethe"},
            {"Il nome della rosa", "Italian (Italy)", "Bompiani", "Umberto Eco"},
            {"La Divina Commedia", "Italian (Italy)", "Giovanni Boccaccio", "Dante Alighieri"},
            {"Dom Casmurro", "Portuguese (Brazil)", "H. Garnier", "Machado de Assis"},
            {"O Cortiço", "Portuguese (Brazil)", "B.L. Garnier", "Aluísio Azevedo"},
            {"Война и мир", "Russian (Russia)", "The Russian Messenger", "Leo Tolstoy"},
            {"Преступление и наказание", "Russian (Russia)", "The Russian Messenger", "Fyodor Dostoevsky"},
            {"ノルウェイの森", "Japanese (Japan)", "Kodansha", "Haruki Murakami"},
            {"吾輩は猫である", "Japanese (Japan)", "Hototogisu", "Natsume Soseki"},
            {"红楼梦", "Chinese (Simplified)", "程甲本", "Cao Xueqin"},
            {"西游记", "Chinese (Simplified)", "世德堂", "Wu Cheng'en"}
        };
        
        for (String[] data : bookData) {
            String title = data[0];
            String languageName = data[1];
            String publisherName = data[2];
            String authorName = data[3];
            
            Language language = languages.values().stream()
                .filter(l -> l.getName().equals(languageName))
                .findFirst()
                .orElse(languages.get("en-US"));
                
            Publisher publisher = publishers.values().stream()
                .filter(p -> p.getName().equals(publisherName))
                .findFirst()
                .orElse(publishers.get("Penguin Random House"));
                
            Author author = authors.values().stream()
                .filter(a -> a.getName().equals(authorName))
                .findFirst()
                .orElse(authors.get("William Shakespeare"));
            
            Book book = new Book();
            book.setTitle(title);
            book.setTitleSort(title);
            book.setFileHash(generateRandomHash());
            book.setFileSize((long)(500000 + Math.random() * 2000000)); // 500KB to 2.5MB
            book.setPath("/demo/books/" + title.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".pdf");
            book.setHasCover(Math.random() > 0.3); // 70% chance of having a cover
            book.setPublicationDate(LocalDate.of(getRandomYear(1800, 2020), 1, 1));
            book.setLanguage(language);
            book.setPublisher(publisher);
            book.setMetadata(Map.of(
                "genre", getRandomGenre(),
                "pages", (int)(100 + Math.random() * 800),
                "format", "PDF"
            ));
            
            book.persist();
            LOG.infof("Created book: %s", title);
        }
    }
    
    private String getRandomGenre() {
        String[] genres = {"Fiction", "Non-Fiction", "Mystery", "Romance", "Sci-Fi", "Fantasy", "Biography", "History", "Poetry", "Drama"};
        return genres[(int)(Math.random() * genres.length)];
    }
}