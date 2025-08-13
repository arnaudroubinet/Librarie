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
        
        LOG.info("Demo mode enabled, populating comprehensive demo data...");
        
        // Create reference data first
        Map<String, Language> languages = createLanguages();
        Map<String, Publisher> publishers = createPublishers();
        Map<String, Author> authors = createComprehensiveAuthors();
        Map<String, Series> series = createComprehensiveSeries();
        
        // Create comprehensive books with proper relationships
        LOG.infof("Creating books with authors: Tolkien=%s, Jordan=%s, Sanderson=%s, King=%s, Asimov=%s", 
                 authors.get("J.R.R. Tolkien") != null ? authors.get("J.R.R. Tolkien").getName() : "NULL",
                 authors.get("Robert Jordan") != null ? authors.get("Robert Jordan").getName() : "NULL", 
                 authors.get("Brandon Sanderson") != null ? authors.get("Brandon Sanderson").getName() : "NULL",
                 authors.get("Stephen King") != null ? authors.get("Stephen King").getName() : "NULL",
                 authors.get("Isaac Asimov") != null ? authors.get("Isaac Asimov").getName() : "NULL");
        createComprehensiveBooks(languages, publishers, authors, series);
        
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
    
    private Map<String, Author> createComprehensiveAuthors() {
        Map<String, Author> authors = new HashMap<>();
        
        // J.R.R. Tolkien
        Author tolkien = new Author();
        tolkien.setName("J.R.R. Tolkien");
        tolkien.setSortName("Tolkien, J.R.R.");
        tolkien.setBirthDate(LocalDate.of(1892, 1, 3));
        tolkien.setDeathDate(LocalDate.of(1973, 9, 2));
        tolkien.setBio(Map.of("en", "John Ronald Reuel Tolkien was an English writer, poet, philologist, and academic, best known as the author of the high fantasy works The Hobbit and The Lord of the Rings."));
        tolkien.setWebsiteUrl("https://www.tolkienestate.com/");
        tolkien.setMetadata(Map.of(
            "nationality", "British",
            "genres", List.of("Fantasy", "Philology"),
            "education", "Oxford University",
            "imageUrl", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/J._R._R._Tolkien%2C_ca._1925.jpg/256px-J._R._R._Tolkien%2C_ca._1925.jpg"
        ));
        tolkien.persist();
        authors.put("J.R.R. Tolkien", tolkien);
        
        // Robert Jordan
        Author jordan = new Author();
        jordan.setName("Robert Jordan");
        jordan.setSortName("Jordan, Robert");
        jordan.setBirthDate(LocalDate.of(1948, 10, 17));
        jordan.setDeathDate(LocalDate.of(2007, 9, 16));
        jordan.setBio(Map.of("en", "Robert Jordan was an American author of epic fantasy. He is best known for the Wheel of Time series, which comprises 14 books and a prequel novel."));
        jordan.setWebsiteUrl("https://www.dragonmount.com/");
        jordan.setMetadata(Map.of(
            "nationality", "American",
            "genres", List.of("Epic Fantasy"),
            "realName", "James Oliver Rigney Jr.",
            "imageUrl", "https://upload.wikimedia.org/wikipedia/en/thumb/8/81/Robert_Jordan.jpg/256px-Robert_Jordan.jpg"
        ));
        jordan.persist();
        authors.put("Robert Jordan", jordan);
        
        // Brandon Sanderson  
        Author sanderson = new Author();
        sanderson.setName("Brandon Sanderson");
        sanderson.setSortName("Sanderson, Brandon");
        sanderson.setBirthDate(LocalDate.of(1975, 12, 19));
        sanderson.setBio(Map.of("en", "Brandon Sanderson is an American author of epic fantasy and science fiction. He is best known for the Cosmere fictional universe, in which most of his fantasy novels, most notably the Mistborn series and The Stormlight Archive, are set."));
        sanderson.setWebsiteUrl("https://www.brandonsanderson.com/");
        sanderson.setMetadata(Map.of(
            "nationality", "American",
            "genres", List.of("Epic Fantasy", "Science Fiction"),
            "university", "Brigham Young University",
            "imageUrl", "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3c/Brandon_Sanderson_sign_autographs_2024.jpg/256px-Brandon_Sanderson_sign_autographs_2024.jpg"
        ));
        sanderson.persist();
        authors.put("Brandon Sanderson", sanderson);
        
        // Stephen King
        Author king = new Author();
        king.setName("Stephen King");
        king.setSortName("King, Stephen");
        king.setBirthDate(LocalDate.of(1947, 9, 21));
        king.setBio(Map.of("en", "Stephen Edwin King is an American author of horror, supernatural fiction, suspense, crime, science-fiction, and fantasy novels. He has published 64 novels, including seven under the pen name Richard Bachman, and more than 200 short stories."));
        king.setWebsiteUrl("https://stephenking.com/");
        king.setMetadata(Map.of(
            "nationality", "American",
            "genres", List.of("Horror", "Supernatural Fiction", "Suspense", "Science Fiction"),
            "penNames", List.of("Richard Bachman"),
            "imageUrl", "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e3/Stephen_King%2C_Comicon.jpg/256px-Stephen_King%2C_Comicon.jpg"
        ));
        king.persist();
        authors.put("Stephen King", king);
        
        // Isaac Asimov
        Author asimov = new Author();
        asimov.setName("Isaac Asimov");
        asimov.setSortName("Asimov, Isaac");
        asimov.setBirthDate(LocalDate.of(1920, 1, 2));
        asimov.setDeathDate(LocalDate.of(1992, 4, 6));
        asimov.setBio(Map.of("en", "Isaac Asimov was an American writer and professor of biochemistry at Boston University. He was known for his works of science fiction and popular science. Asimov was a prolific writer who wrote or edited more than 500 books."));
        asimov.setWebsiteUrl("https://www.asimovonline.com/");
        asimov.setMetadata(Map.of(
            "nationality", "American",
            "genres", List.of("Science Fiction", "Popular Science", "Mystery"),
            "profession", "Biochemist",
            "imageUrl", "https://upload.wikimedia.org/wikipedia/commons/thumb/3/34/Isaac.Asimov01.jpg/256px-Isaac.Asimov01.jpg"
        ));
        asimov.persist();
        authors.put("Isaac Asimov", asimov);
        
        LOG.infof("Created %d comprehensive authors", authors.size());
        return authors;
    }
    
    private Map<String, Series> createComprehensiveSeries() {
        Map<String, Series> series = new HashMap<>();
        
        // The Lord of the Rings
        Series lotr = new Series("The Lord of the Rings", "Lord of the Rings, The");
        lotr.setDescription("Epic high fantasy adventure in Middle-earth following the journey to destroy the One Ring and defeat the Dark Lord Sauron.");
        lotr.setImagePath("https://upload.wikimedia.org/wikipedia/en/thumb/8/8e/The_Lord_of_the_Rings_-_The_Fellowship_of_the_Ring_%282001%29.jpg/256px-The_Lord_of_the_Rings_-_The_Fellowship_of_the_Ring_%282001%29.jpg");
        lotr.setMetadata(Map.of(
            "genre", "Epic Fantasy",
            "setting", "Middle-earth",
            "totalBooks", 3,
            "seriesImageUrl", "https://upload.wikimedia.org/wikipedia/en/thumb/8/8e/The_Lord_of_the_Rings_-_The_Fellowship_of_the_Ring_%282001%29.jpg/256px-The_Lord_of_the_Rings_-_The_Fellowship_of_the_Ring_%282001%29.jpg"
        ));
        lotr.persist();
        series.put("The Lord of the Rings", lotr);
        
        // The Wheel of Time
        Series wot = new Series("The Wheel of Time", "Wheel of Time, The");
        wot.setDescription("High fantasy series following the Dragon Reborn and the Last Battle against the Dark One. Originally begun by Robert Jordan and completed by Brandon Sanderson after Jordan's death.");
        wot.setImagePath("https://upload.wikimedia.org/wikipedia/en/thumb/5/56/WoT01_TheEyeOfTheWorld.jpg/256px-WoT01_TheEyeOfTheWorld.jpg");
        wot.setMetadata(Map.of(
            "genre", "Epic Fantasy",
            "setting", "Randland",
            "totalBooks", 14,
            "originalAuthor", "Robert Jordan",
            "completedBy", "Brandon Sanderson",
            "seriesImageUrl", "https://upload.wikimedia.org/wikipedia/en/thumb/5/56/WoT01_TheEyeOfTheWorld.jpg/256px-WoT01_TheEyeOfTheWorld.jpg"
        ));
        wot.persist();
        series.put("The Wheel of Time", wot);
        
        // The Dark Tower (Stephen King)
        Series darkTower = new Series("The Dark Tower", "Dark Tower, The");
        darkTower.setDescription("Dark fantasy series blending elements of science fiction, horror, and westerns, following Roland Deschain, the last gunslinger, on his quest for the Dark Tower.");
        darkTower.setImagePath("https://upload.wikimedia.org/wikipedia/en/thumb/6/6a/TheDarkTower1_TheGunslinger.jpg/256px-TheDarkTower1_TheGunslinger.jpg");
        darkTower.setMetadata(Map.of(
            "genre", "Dark Fantasy",
            "subgenres", List.of("Science Fiction", "Horror", "Western"),
            "totalBooks", 8,
            "seriesImageUrl", "https://upload.wikimedia.org/wikipedia/en/thumb/6/6a/TheDarkTower1_TheGunslinger.jpg/256px-TheDarkTower1_TheGunslinger.jpg"
        ));
        darkTower.persist();
        series.put("The Dark Tower", darkTower);
        
        // Foundation Series (Isaac Asimov)
        Series foundation = new Series("Foundation", "Foundation");
        foundation.setDescription("Science fiction series about psychohistory and the fall and rebirth of the Galactic Empire, spanning thousands of years.");
        foundation.setImagePath("https://upload.wikimedia.org/wikipedia/en/thumb/7/73/Foundation_gnome_press.jpg/256px-Foundation_gnome_press.jpg");
        foundation.setMetadata(Map.of(
            "genre", "Science Fiction",
            "concepts", List.of("Psychohistory", "Galactic Empire"),
            "totalBooks", 7,
            "seriesImageUrl", "https://upload.wikimedia.org/wikipedia/en/thumb/7/73/Foundation_gnome_press.jpg/256px-Foundation_gnome_press.jpg"
        ));
        foundation.persist();
        series.put("Foundation", foundation);
        
        // Robot Series (Isaac Asimov)
        Series robot = new Series("Robot", "Robot");
        robot.setDescription("Science fiction series exploring the relationship between humans and robots, featuring the famous Three Laws of Robotics.");
        robot.setImagePath("https://upload.wikimedia.org/wikipedia/en/thumb/e/ea/I_robot.jpg/256px-I_robot.jpg");
        robot.setMetadata(Map.of(
            "genre", "Science Fiction",
            "concepts", List.of("Three Laws of Robotics", "Artificial Intelligence"),
            "totalBooks", 4,
            "seriesImageUrl", "https://upload.wikimedia.org/wikipedia/en/thumb/e/ea/I_robot.jpg/256px-I_robot.jpg"
        ));
        robot.persist();
        series.put("Robot", robot);
        
        LOG.infof("Created %d comprehensive series", series.size());
        return series;
    }
    
    private void createComprehensiveBooks(Map<String, Language> languages, 
                                          Map<String, Publisher> publishers,
                                          Map<String, Author> authors, 
                                          Map<String, Series> series) {
        
        Language english = languages.get("en-US");
        Publisher randomHouse = publishers.get("Penguin Random House");
        Publisher tor = publishers.get("Tor Books");
        Publisher bantam = publishers.get("Bantam Books");
        Publisher delRey = publishers.get("Del Rey");
        
        // J.R.R. Tolkien - The Lord of the Rings series
        createTolkienBooks(english, randomHouse, authors.get("J.R.R. Tolkien"), series.get("The Lord of the Rings"));
        
        // The Wheel of Time series - Robert Jordan & Brandon Sanderson
        createWheelOfTimeBooks(english, tor, authors.get("Robert Jordan"), authors.get("Brandon Sanderson"), series.get("The Wheel of Time"));
        
        // Stephen King books
        createStephenKingBooks(english, randomHouse, authors.get("Stephen King"), series.get("The Dark Tower"));
        
        // Isaac Asimov books
        createIsaacAsimovBooks(english, bantam, authors.get("Isaac Asimov"), series.get("Foundation"), series.get("Robot"));
        
        LOG.info("Created comprehensive books for all requested authors");
    }
    
    private void createTolkienBooks(Language language, Publisher publisher, Author author, Series series) {
        // The Lord of the Rings trilogy
        String[][] lotrBooks = {
            {"The Fellowship of the Ring", "1954", "https://upload.wikimedia.org/wikipedia/en/thumb/8/8e/The_Lord_of_the_Rings_-_The_Fellowship_of_the_Ring_%282001%29.jpg/256px-The_Lord_of_the_Rings_-_The_Fellowship_of_the_Ring_%282001%29.jpg", "The first volume of The Lord of the Rings follows Frodo Baggins as he begins his journey to destroy the One Ring."},
            {"The Two Towers", "1954", "https://upload.wikimedia.org/wikipedia/en/thumb/a/ad/The_Lord_of_the_Rings_-_The_Two_Towers_%282002%29.jpg/256px-The_Lord_of_the_Rings_-_The_Two_Towers_%282002%29.jpg", "The second volume follows the continuing quest of the Fellowship as it divides into separate paths."},
            {"The Return of the King", "1955", "https://upload.wikimedia.org/wikipedia/en/thumb/b/be/The_Lord_of_the_Rings_-_The_Return_of_the_King_%282003%29.jpg/256px-The_Lord_of_the_Rings_-_The_Return_of_the_King_%282003%29.jpg", "The final volume of The Lord of the Rings chronicles the final battle against Sauron and the return of the king."}
        };
        
        for (int i = 0; i < lotrBooks.length; i++) {
            String[] bookData = lotrBooks[i];
            createBookInSeries(
                bookData[0], // title
                "Tolkien, " + bookData[0], // titleSort
                author,
                publisher,
                language,
                series,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Other Tolkien standalone works
        String[][] standaloneBooks = {
            {"The Hobbit", "1937", "https://upload.wikimedia.org/wikipedia/en/thumb/4/4a/TheHobbit_FirstEdition.jpg/256px-TheHobbit_FirstEdition.jpg", "A fantasy novel about the hobbit Bilbo Baggins' journey to help reclaim the Lonely Mountain from the dragon Smaug."},
            {"The Silmarillion", "1977", "https://upload.wikimedia.org/wikipedia/en/thumb/8/86/Silmarillion.gif/256px-Silmarillion.gif", "A collection of mythopoeic stories that forms the history and cosmology of Tolkien's Middle-earth."},
            {"Unfinished Tales", "1980", "https://upload.wikimedia.org/wikipedia/en/thumb/5/5c/Unfinished_Tales_cover.jpg/256px-Unfinished_Tales_cover.jpg", "A collection of stories and essays by J.R.R. Tolkien, edited and published posthumously by his son Christopher Tolkien."}
        };
        
        for (String[] bookData : standaloneBooks) {
            createStandaloneBook(
                bookData[0], // title
                "Tolkien, " + bookData[0], // titleSort
                author,
                publisher,
                language,
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        LOG.infof("Created %d books for J.R.R. Tolkien", lotrBooks.length + standaloneBooks.length);
    }
    
    private void createWheelOfTimeBooks(Language language, Publisher publisher, Author jordan, Author sanderson, Series series) {
        // Robert Jordan's books (1-11)
        String[][] jordanBooks = {
            {"The Eye of the World", "1990", "https://upload.wikimedia.org/wikipedia/en/thumb/5/56/WoT01_TheEyeOfTheWorld.jpg/256px-WoT01_TheEyeOfTheWorld.jpg", "The first book in The Wheel of Time series, introducing Rand al'Thor and his friends as they flee their village."},
            {"The Great Hunt", "1990", "https://upload.wikimedia.org/wikipedia/en/thumb/6/66/WoT02_TheGreatHunt.jpg/256px-WoT02_TheGreatHunt.jpg", "The second book follows the hunt for the stolen Horn of Valere."},
            {"The Dragon Reborn", "1991", "https://upload.wikimedia.org/wikipedia/en/thumb/3/39/WoT03_TheDragonReborn.jpg/256px-WoT03_TheDragonReborn.jpg", "The third book focuses on Rand's journey to claim Callandor in the Stone of Tear."},
            {"The Shadow Rising", "1992", "https://upload.wikimedia.org/wikipedia/en/thumb/1/1c/WoT04_TheShadowRising.jpg/256px-WoT04_TheShadowRising.jpg", "The fourth book explores the history of the Aiel and the Two Rivers."},
            {"The Fires of Heaven", "1993", "https://upload.wikimedia.org/wikipedia/en/thumb/2/21/WoT05_TheFiresOfHeaven.jpg/256px-WoT05_TheFiresOfHeaven.jpg", "The fifth book deals with civil war in Cairhien and the Aiel Waste."},
            {"Lord of Chaos", "1994", "https://upload.wikimedia.org/wikipedia/en/thumb/7/77/WoT06_LordOfChaos.jpg/256px-WoT06_LordOfChaos.jpg", "The sixth book culminates in the Battle of Dumai's Wells."},
            {"A Crown of Swords", "1996", "https://upload.wikimedia.org/wikipedia/en/thumb/8/82/WoT07_ACrownOfSwords.jpg/256px-WoT07_ACrownOfSwords.jpg", "The seventh book follows the aftermath of Dumai's Wells."},
            {"The Path of Daggers", "1998", "https://upload.wikimedia.org/wikipedia/en/thumb/9/9d/WoT08_ThePathOfDaggers.jpg/256px-WoT08_ThePathOfDaggers.jpg", "The eighth book focuses on the use of the Bowl of the Winds."},
            {"Winter's Heart", "2000", "https://upload.wikimedia.org/wikipedia/en/thumb/b/bb/WoT09_WintersHeart.jpg/256px-WoT09_WintersHeart.jpg", "The ninth book deals with the cleansing of saidin."},
            {"Crossroads of Twilight", "2003", "https://upload.wikimedia.org/wikipedia/en/thumb/f/fc/WoT10_CrossroadsOfTwilight.jpg/256px-WoT10_CrossroadsOfTwilight.jpg", "The tenth book explores the aftermath of the cleansing."},
            {"Knife of Dreams", "2005", "https://upload.wikimedia.org/wikipedia/en/thumb/7/71/WoT11_KnifeOfDreams.jpg/256px-WoT11_KnifeOfDreams.jpg", "The eleventh book was Robert Jordan's final completed novel in the series."}
        };
        
        for (int i = 0; i < jordanBooks.length; i++) {
            String[] bookData = jordanBooks[i];
            createBookInSeries(
                bookData[0], // title
                "Jordan, " + bookData[0], // titleSort
                jordan,
                publisher,
                language,
                series,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Brandon Sanderson's completing books (12-14)
        String[][] sandersonBooks = {
            {"The Gathering Storm", "2009", "https://upload.wikimedia.org/wikipedia/en/thumb/3/31/WoT12_TheGatheringStorm.jpg/256px-WoT12_TheGatheringStorm.jpg", "The twelfth book, completed by Brandon Sanderson from Robert Jordan's notes."},
            {"Towers of Midnight", "2010", "https://upload.wikimedia.org/wikipedia/en/thumb/8/8d/WoT13_TowersOfMidnight.jpg/256px-WoT13_TowersOfMidnight.jpg", "The thirteenth book, continuing Sanderson's completion of the series."},
            {"A Memory of Light", "2013", "https://upload.wikimedia.org/wikipedia/en/thumb/0/0c/WoT14_AMemoryOfLight.jpg/256px-WoT14_AMemoryOfLight.jpg", "The fourteenth and final book in The Wheel of Time series."}
        };
        
        for (int i = 0; i < sandersonBooks.length; i++) {
            String[] bookData = sandersonBooks[i];
            // These books have both Jordan and Sanderson as authors, but we'll attribute to Sanderson as primary
            createBookInSeries(
                bookData[0], // title
                "Sanderson, " + bookData[0], // titleSort
                sanderson,
                publisher,
                language,
                series,
                BigDecimal.valueOf(jordanBooks.length + i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        LOG.infof("Created %d books for The Wheel of Time series", jordanBooks.length + sandersonBooks.length);
    }
    
    private void createStephenKingBooks(Language language, Publisher publisher, Author author, Series darkTowerSeries) {
        // The Dark Tower series
        String[][] darkTowerBooks = {
            {"The Gunslinger", "1982", "https://upload.wikimedia.org/wikipedia/en/thumb/6/6a/TheDarkTower1_TheGunslinger.jpg/256px-TheDarkTower1_TheGunslinger.jpg", "The first book in The Dark Tower series, introducing Roland Deschain, the last gunslinger."},
            {"The Drawing of the Three", "1987", "https://upload.wikimedia.org/wikipedia/en/thumb/9/91/Drawingthreetower.jpg/256px-Drawingthreetower.jpg", "The second book where Roland draws three companions from our world."},
            {"The Waste Lands", "1991", "https://upload.wikimedia.org/wikipedia/en/thumb/b/b3/Wastelandstower.jpg/256px-Wastelandstower.jpg", "The third book as the ka-tet travels through the waste lands."},
            {"Wizard and Glass", "1997", "https://upload.wikimedia.org/wikipedia/en/thumb/c/c7/Wizardglassdark.jpg/256px-Wizardglassdark.jpg", "The fourth book reveals Roland's past in Mejis."},
            {"Wolves of the Calla", "2003", "https://upload.wikimedia.org/wikipedia/en/thumb/a/ae/Wolves_of_the_calla.jpg/256px-Wolves_of_the_calla.jpg", "The fifth book where the ka-tet defends a farming community."},
            {"Song of Susannah", "2004", "https://upload.wikimedia.org/wikipedia/en/thumb/d/dc/Song_of_susannah.jpg/256px-Song_of_susannah.jpg", "The sixth book follows Susannah's journey to our world."},
            {"The Dark Tower", "2004", "https://upload.wikimedia.org/wikipedia/en/thumb/4/4a/The_Dark_Tower_VII.jpg/256px-The_Dark_Tower_VII.jpg", "The seventh book concludes Roland's quest for the Dark Tower."},
            {"The Wind Through the Keyhole", "2012", "https://upload.wikimedia.org/wikipedia/en/thumb/3/39/The_Wind_Through_the_Keyhole.jpg/256px-The_Wind_Through_the_Keyhole.jpg", "A mid-series book set between Wizard and Glass and Wolves of the Calla."}
        };
        
        for (int i = 0; i < darkTowerBooks.length; i++) {
            String[] bookData = darkTowerBooks[i];
            createBookInSeries(
                bookData[0], // title
                "King, " + bookData[0], // titleSort
                author,
                publisher,
                language,
                darkTowerSeries,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Stephen King standalone books
        String[][] standaloneBooks = {
            {"Carrie", "1974", "https://upload.wikimedia.org/wikipedia/en/thumb/3/31/Carrie_novel.jpg/256px-Carrie_novel.jpg", "King's first published novel about a teenage girl with telekinetic powers."},
            {"The Shining", "1977", "https://upload.wikimedia.org/wikipedia/en/thumb/4/4c/Shining_novel.jpg/256px-Shining_novel.jpg", "A horror novel about a family isolated in a haunted hotel."},
            {"The Stand", "1978", "https://upload.wikimedia.org/wikipedia/en/thumb/9/96/The_Stand_cover.jpg/256px-The_Stand_cover.jpg", "An epic post-apocalyptic dark fantasy novel."},
            {"It", "1986", "https://upload.wikimedia.org/wikipedia/en/thumb/8/8e/It_%281986%29_front_cover%2C_first_edition.jpg/256px-It_%281986%29_front_cover%2C_first_edition.jpg", "A horror novel about a creature that preys on children in the town of Derry."},
            {"Misery", "1987", "https://upload.wikimedia.org/wikipedia/en/thumb/e/e7/Misery_%28novel%29.jpg/256px-Misery_%28novel%29.jpg", "A psychological horror novel about a writer held captive by his 'number one fan'."},
            {"Pet Sematary", "1983", "https://upload.wikimedia.org/wikipedia/en/thumb/2/24/Pet_Sematary_%28book_cover%29.jpg/256px-Pet_Sematary_%28book_cover%29.jpg", "A horror novel about a burial ground with supernatural powers."},
            {"Salem's Lot", "1975", "https://upload.wikimedia.org/wikipedia/en/thumb/5/50/Salem%27s_Lot_cover.jpg/256px-Salem%27s_Lot_cover.jpg", "A horror novel about vampires in a small New England town."},
            {"The Dead Zone", "1979", "https://upload.wikimedia.org/wikipedia/en/thumb/1/16/The_Dead_Zone_%28novel%29.jpg/256px-The_Dead_Zone_%28novel%29.jpg", "A supernatural thriller about a man who awakens from a coma with psychic abilities."}
        };
        
        for (String[] bookData : standaloneBooks) {
            createStandaloneBook(
                bookData[0], // title
                "King, " + bookData[0], // titleSort
                author,
                publisher,
                language,
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        LOG.infof("Created %d books for Stephen King", darkTowerBooks.length + standaloneBooks.length);
    }
    
    private void createIsaacAsimovBooks(Language language, Publisher publisher, Author author, Series foundationSeries, Series robotSeries) {
        // Foundation series
        String[][] foundationBooks = {
            {"Foundation", "1951", "https://upload.wikimedia.org/wikipedia/en/thumb/7/73/Foundation_gnome_press.jpg/256px-Foundation_gnome_press.jpg", "The first book in the Foundation series about Hari Seldon's psychohistory."},
            {"Foundation and Empire", "1952", "https://upload.wikimedia.org/wikipedia/en/thumb/8/80/Foundation_and_Empire_Cover.jpg/256px-Foundation_and_Empire_Cover.jpg", "The second Foundation book dealing with the rise of the Mule."},
            {"Second Foundation", "1953", "https://upload.wikimedia.org/wikipedia/en/thumb/a/a8/Second_foundation.jpg/256px-Second_foundation.jpg", "The third book revealing the location of the Second Foundation."},
            {"Foundation's Edge", "1982", "https://upload.wikimedia.org/wikipedia/en/thumb/2/29/Foundation%27s_Edge.jpg/256px-Foundation%27s_Edge.jpg", "The fourth book set centuries after the original trilogy."},
            {"Foundation and Earth", "1986", "https://upload.wikimedia.org/wikipedia/en/thumb/9/9c/Foundation_and_Earth_Cover.jpg/256px-Foundation_and_Earth_Cover.jpg", "The fifth book following Golan Trevize's search for Earth."},
            {"Prelude to Foundation", "1988", "https://upload.wikimedia.org/wikipedia/en/thumb/b/bf/Prelude_to_Foundation.jpg/256px-Prelude_to_Foundation.jpg", "A prequel focusing on young Hari Seldon."},
            {"Forward the Foundation", "1993", "https://upload.wikimedia.org/wikipedia/en/thumb/6/61/Forward_the_Foundation.jpg/256px-Forward_the_Foundation.jpg", "The final prequel about Seldon's later years."}
        };
        
        for (int i = 0; i < foundationBooks.length; i++) {
            String[] bookData = foundationBooks[i];
            createBookInSeries(
                bookData[0], // title
                "Asimov, " + bookData[0], // titleSort
                author,
                publisher,
                language,
                foundationSeries,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Robot series
        String[][] robotBooks = {
            {"I, Robot", "1950", "https://upload.wikimedia.org/wikipedia/en/thumb/e/ea/I_robot.jpg/256px-I_robot.jpg", "A collection of nine short stories about robots and the Three Laws of Robotics."},
            {"The Caves of Steel", "1954", "https://upload.wikimedia.org/wikipedia/en/thumb/f/fc/Caves_of_steel.jpg/256px-Caves_of_steel.jpg", "A science fiction detective novel featuring Elijah Baley and R. Daneel Olivaw."},
            {"The Naked Sun", "1957", "https://upload.wikimedia.org/wikipedia/en/thumb/d/dc/The_Naked_Sun.jpg/256px-The_Naked_Sun.jpg", "The second robot novel continuing the partnership of Baley and Daneel."},
            {"The Robots of Dawn", "1983", "https://upload.wikimedia.org/wikipedia/en/thumb/5/50/Robots_of_dawn.jpg/256px-Robots_of_dawn.jpg", "The third robot novel set on the Spacer world of Aurora."}
        };
        
        for (int i = 0; i < robotBooks.length; i++) {
            String[] bookData = robotBooks[i];
            createBookInSeries(
                bookData[0], // title
                "Asimov, " + bookData[0], // titleSort
                author,
                publisher,
                language,
                robotSeries,
                BigDecimal.valueOf(i + 1),
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        // Isaac Asimov standalone books
        String[][] standaloneBooks = {
            {"The End of Eternity", "1955", "https://upload.wikimedia.org/wikipedia/en/thumb/7/7f/EndOfEternity.jpg/256px-EndOfEternity.jpg", "A science fiction novel about time travel and the organization Eternity."},
            {"The Gods Themselves", "1972", "https://upload.wikimedia.org/wikipedia/en/thumb/b/b4/TheGodsThemselves%281stEd%29.jpg/256px-TheGodsThemselves%281stEd%29.jpg", "A science fiction novel about parallel universes and energy exchange."},
            {"Nightfall", "1990", "https://upload.wikimedia.org/wikipedia/en/thumb/c/cb/Nightfall_novel.jpg/256px-Nightfall_novel.jpg", "An expansion of his famous short story about a planet with six suns."}
        };
        
        for (String[] bookData : standaloneBooks) {
            createStandaloneBook(
                bookData[0], // title
                "Asimov, " + bookData[0], // titleSort
                author,
                publisher,
                language,
                Integer.parseInt(bookData[1]),
                bookData[2], // coverUrl
                bookData[3]  // description
            );
        }
        
        LOG.infof("Created %d books for Isaac Asimov", foundationBooks.length + robotBooks.length + standaloneBooks.length);
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
    
    private void createBookInSeries(String title, String titleSort, Author author, Publisher publisher,
                                   Language language, Series seriesEntity, BigDecimal seriesIndex, int year,
                                   String coverUrl, String description) {
        LOG.infof("Creating book '%s' with author '%s'", title, author.getName());
        
        // Create original work
        OriginalWork originalWork = new OriginalWork();
        originalWork.setTitle(title);
        originalWork.setTitleSort(titleSort);
        originalWork.setFirstPublication(LocalDate.of(year, 1, 1));
        originalWork.setDescription(description);
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
        book.setHasCover(coverUrl != null && !coverUrl.trim().isEmpty());
        book.setPublicationDate(LocalDate.of(year, (int)(Math.random() * 12) + 1, (int)(Math.random() * 28) + 1));
        book.setLanguage(language);
        book.setPublisher(publisher);
        
        // Add cover URL and enhanced metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("genre", "Fiction");
        metadata.put("pages", (int)(200 + Math.random() * 600));
        if (coverUrl != null && !coverUrl.trim().isEmpty()) {
            metadata.put("coverUrl", coverUrl);
        }
        if (description != null && !description.trim().isEmpty()) {
            metadata.put("description", description);
        }
        book.setMetadata(metadata);
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
                                     Language language, int year, String coverUrl, String description) {
        // Create original work
        OriginalWork originalWork = new OriginalWork();
        originalWork.setTitle(title);
        originalWork.setTitleSort(titleSort);
        originalWork.setFirstPublication(LocalDate.of(year, 1, 1));
        originalWork.setDescription(description);
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
        book.setHasCover(coverUrl != null && !coverUrl.trim().isEmpty());
        book.setPublicationDate(LocalDate.of(year, (int)(Math.random() * 12) + 1, (int)(Math.random() * 28) + 1));
        book.setLanguage(language);
        book.setPublisher(publisher);
        
        // Add cover URL and enhanced metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("genre", "Fiction");
        metadata.put("pages", (int)(200 + Math.random() * 600));
        if (coverUrl != null && !coverUrl.trim().isEmpty()) {
            metadata.put("coverUrl", coverUrl);
        }
        if (description != null && !description.trim().isEmpty()) {
            metadata.put("description", description);
        }
        book.setMetadata(metadata);
        book.persist();
        
        // Link book to original work
        BookOriginalWork bookOriginalWork = new BookOriginalWork(book, originalWork, 
                                                                  BookOriginalWorkRelationType.PRIMARY, 0);
        entityManager.persist(bookOriginalWork);
    }
}