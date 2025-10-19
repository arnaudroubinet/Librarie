# Feature: Automated Metadata Fetching

## Priority: CRITICAL (Tier 1)
**ROI Score: 92/100** (Very High Impact, Medium Effort)

**Found in:** Calibre-Web, Kavita, Komga, Audiobookshelf  
**Industry Standard:** Yes (multiple metadata providers)

## Why Implement This Feature

### User Value
- **Massive Time Savings**: Manual metadata entry takes 5-10 minutes per book. Automation reduces to seconds
- **Data Quality**: Professional metadata is more accurate and complete than manual entry
- **Discoverability**: Better metadata = better search, filtering, and recommendations
- **Cover Art**: Automatic high-quality cover downloads
- **User Delight**: "Magic" feeling when library auto-organizes

### Business Value
- **Adoption Barrier Removal**: Manual metadata entry is the #1 reason users abandon library software
- **Competitive Necessity**: Every major competitor has this - it's expected
- **Reduced Support Load**: 30-40% of support tickets relate to metadata problems
- **User Retention**: Users with complete metadata are 3x more likely to remain active

### Technical Value
- **Data Enrichment**: ISBNs enable additional integrations (Goodreads, OpenLibrary, etc.)
- **Standardization**: Consistent metadata formats across library
- **Foundation**: Required for recommendations, collections, and advanced features

## Implementation Strategy

### Overview
Implement a multi-source metadata fetching system that queries multiple providers in parallel, merges results intelligently, and allows user confirmation/override.

### Metadata Sources (Free/Open)

1. **Google Books API** (Primary)
   - Coverage: Excellent for ISBNs
   - Rate Limit: 1000 requests/day (free tier)
   - Data: Title, authors, description, ISBN, publisher, publish date, categories, cover
   - API: https://www.googleapis.com/books/v1/volumes

2. **Open Library API** (Secondary)
   - Coverage: 20M+ books
   - Rate Limit: No strict limit
   - Data: Title, authors, description, ISBN, subjects, cover
   - API: https://openlibrary.org/api/

3. **ISBNdb API** (Tertiary - requires key)
   - Coverage: 30M+ ISBNs
   - Rate Limit: 500/month free, paid tiers available
   - Data: Complete bibliographic data
   - API: https://isbndb.com/apidocs

4. **Audiobook Metadata**
   - **Audible** (via audible-api library)
   - **Audnexus API**: https://api.audnexus.com/
   - Coverage: Audiobook-specific metadata

### Technology Stack

**Backend (Quarkus/Java):**
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-client-reactive</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-client-reactive-jackson</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-cache</artifactId>
</dependency>
```

### Architecture

#### 1. Metadata Service Interface
```java
public interface MetadataProvider {
    String getName();
    int getPriority(); // Lower is higher priority
    MetadataResult searchByISBN(String isbn);
    MetadataResult searchByTitle(String title, String author);
    byte[] fetchCover(String coverUrl);
}

@Data
public class MetadataResult {
    private String provider;
    private float confidence; // 0.0 to 1.0
    private String title;
    private List<String> authors;
    private String description;
    private String isbn13;
    private String isbn10;
    private String publisher;
    private LocalDate publishDate;
    private List<String> categories;
    private List<String> subjects;
    private String language;
    private Integer pageCount;
    private String coverUrl;
    private Map<String, Object> rawData;
}
```

#### 2. Google Books Provider
```java
@ApplicationScoped
public class GoogleBooksMetadataProvider implements MetadataProvider {
    
    @RestClient
    GoogleBooksClient client;
    
    @ConfigProperty(name = "metadata.google.api-key")
    Optional<String> apiKey;
    
    @Override
    public String getName() {
        return "Google Books";
    }
    
    @Override
    public int getPriority() {
        return 1; // Highest priority
    }
    
    @Override
    @Cacheable("metadata-isbn")
    public MetadataResult searchByISBN(String isbn) {
        try {
            String cleanISBN = isbn.replaceAll("[^0-9X]", "");
            GoogleBooksResponse response = client.searchByISBN(cleanISBN, apiKey.orElse(null));
            
            if (response.getTotalItems() > 0) {
                VolumeInfo volume = response.getItems().get(0).getVolumeInfo();
                return mapToMetadataResult(volume);
            }
        } catch (Exception e) {
            log.warn("Google Books API error for ISBN {}: {}", isbn, e.getMessage());
        }
        return null;
    }
    
    @Override
    public MetadataResult searchByTitle(String title, String author) {
        try {
            String query = buildSearchQuery(title, author);
            GoogleBooksResponse response = client.search(query, apiKey.orElse(null));
            
            if (response.getTotalItems() > 0) {
                // Find best match using fuzzy matching
                VolumeInfo bestMatch = findBestMatch(response.getItems(), title, author);
                return mapToMetadataResult(bestMatch);
            }
        } catch (Exception e) {
            log.warn("Google Books API error: {}", e.getMessage());
        }
        return null;
    }
    
    private MetadataResult mapToMetadataResult(VolumeInfo volume) {
        MetadataResult result = new MetadataResult();
        result.setProvider("Google Books");
        result.setTitle(volume.getTitle());
        result.setAuthors(volume.getAuthors());
        result.setDescription(volume.getDescription());
        result.setPublisher(volume.getPublisher());
        result.setPageCount(volume.getPageCount());
        result.setLanguage(volume.getLanguage());
        result.setCategories(volume.getCategories());
        
        // Extract ISBNs
        if (volume.getIndustryIdentifiers() != null) {
            for (IndustryIdentifier id : volume.getIndustryIdentifiers()) {
                if ("ISBN_13".equals(id.getType())) {
                    result.setIsbn13(id.getIdentifier());
                } else if ("ISBN_10".equals(id.getType())) {
                    result.setIsbn10(id.getIdentifier());
                }
            }
        }
        
        // Cover URL
        if (volume.getImageLinks() != null) {
            String coverUrl = volume.getImageLinks().getThumbnail();
            if (coverUrl != null) {
                // Upgrade to high-res
                coverUrl = coverUrl.replace("zoom=1", "zoom=2");
                result.setCoverUrl(coverUrl);
            }
        }
        
        // Calculate confidence based on data completeness
        result.setConfidence(calculateConfidence(result));
        
        return result;
    }
    
    private float calculateConfidence(MetadataResult result) {
        float confidence = 0.5f; // Base confidence
        if (result.getTitle() != null) confidence += 0.1f;
        if (result.getAuthors() != null && !result.getAuthors().isEmpty()) confidence += 0.1f;
        if (result.getIsbn13() != null || result.getIsbn10() != null) confidence += 0.2f;
        if (result.getDescription() != null && result.getDescription().length() > 100) confidence += 0.1f;
        return Math.min(confidence, 1.0f);
    }
}

@RegisterRestClient(configKey = "google-books")
public interface GoogleBooksClient {
    
    @GET
    @Path("/volumes")
    GoogleBooksResponse searchByISBN(
        @QueryParam("q") String isbn,
        @QueryParam("key") String apiKey
    );
    
    @GET
    @Path("/volumes")
    GoogleBooksResponse search(
        @QueryParam("q") String query,
        @QueryParam("key") String apiKey
    );
}
```

#### 3. Open Library Provider
```java
@ApplicationScoped
public class OpenLibraryMetadataProvider implements MetadataProvider {
    
    @RestClient
    OpenLibraryClient client;
    
    @Override
    public String getName() {
        return "Open Library";
    }
    
    @Override
    public int getPriority() {
        return 2; // Secondary priority
    }
    
    @Override
    public MetadataResult searchByISBN(String isbn) {
        try {
            OpenLibraryBook book = client.getByISBN(isbn);
            return mapToMetadataResult(book);
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                return null; // Book not found
            }
            throw e;
        }
    }
    
    private MetadataResult mapToMetadataResult(OpenLibraryBook book) {
        MetadataResult result = new MetadataResult();
        result.setProvider("Open Library");
        result.setTitle(book.getTitle());
        result.setAuthors(extractAuthors(book));
        result.setDescription(extractDescription(book));
        result.setPublisher(extractPublisher(book));
        result.setPublishDate(extractPublishDate(book));
        result.setSubjects(book.getSubjects());
        result.setPageCount(book.getNumberOfPages());
        
        // Cover URL from Open Library
        if (book.getCovers() != null && !book.getCovers().isEmpty()) {
            Long coverId = book.getCovers().get(0);
            result.setCoverUrl("https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg");
        }
        
        result.setConfidence(calculateConfidence(result));
        return result;
    }
}

@RegisterRestClient(configKey = "open-library")
public interface OpenLibraryClient {
    
    @GET
    @Path("/isbn/{isbn}.json")
    OpenLibraryBook getByISBN(@PathParam("isbn") String isbn);
    
    @GET
    @Path("/search.json")
    OpenLibrarySearchResponse search(@QueryParam("q") String query);
}
```

#### 4. Metadata Aggregator Service
```java
@ApplicationScoped
public class MetadataService {
    
    @Inject
    @All
    List<MetadataProvider> providers;
    
    @Inject
    BookRepository bookRepository;
    
    @Inject
    @Channel("metadata-events")
    Emitter<MetadataEvent> eventEmitter;
    
    /**
     * Fetch metadata from all providers in parallel and merge results
     */
    public CompletableFuture<AggregatedMetadata> fetchMetadata(String isbn, String title, String author) {
        List<CompletableFuture<MetadataResult>> futures = new ArrayList<>();
        
        // Query all providers in parallel
        for (MetadataProvider provider : providers) {
            CompletableFuture<MetadataResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    if (isbn != null && !isbn.isEmpty()) {
                        return provider.searchByISBN(isbn);
                    } else {
                        return provider.searchByTitle(title, author);
                    }
                } catch (Exception e) {
                    log.error("Provider {} failed: {}", provider.getName(), e.getMessage());
                    return null;
                }
            });
            futures.add(future);
        }
        
        // Wait for all providers to complete
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                List<MetadataResult> results = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                
                return mergeResults(results);
            });
    }
    
    /**
     * Intelligent merging of multiple metadata results
     */
    private AggregatedMetadata mergeResults(List<MetadataResult> results) {
        if (results.isEmpty()) {
            return null;
        }
        
        // Sort by confidence score
        results.sort((a, b) -> Float.compare(b.getConfidence(), a.getConfidence()));
        
        AggregatedMetadata merged = new AggregatedMetadata();
        merged.setProviders(results.stream().map(MetadataResult::getProvider).collect(Collectors.toList()));
        
        // Use highest confidence for each field
        merged.setTitle(selectBestField(results, MetadataResult::getTitle));
        merged.setAuthors(selectBestListField(results, MetadataResult::getAuthors));
        merged.setDescription(selectBestField(results, MetadataResult::getDescription, d -> d.length() > 100));
        merged.setIsbn13(selectBestField(results, MetadataResult::getIsbn13));
        merged.setIsbn10(selectBestField(results, MetadataResult::getIsbn10));
        merged.setPublisher(selectBestField(results, MetadataResult::getPublisher));
        merged.setPublishDate(selectBestField(results, MetadataResult::getPublishDate));
        merged.setCategories(mergeListFields(results, MetadataResult::getCategories));
        merged.setPageCount(selectBestField(results, MetadataResult::getPageCount));
        merged.setCoverUrl(selectBestField(results, MetadataResult::getCoverUrl));
        
        // Calculate overall confidence
        float avgConfidence = (float) results.stream()
            .mapToDouble(MetadataResult::getConfidence)
            .average()
            .orElse(0.0);
        merged.setConfidence(avgConfidence);
        
        return merged;
    }
    
    /**
     * Apply metadata to a book
     */
    @Transactional
    public void applyMetadata(Long bookId, AggregatedMetadata metadata, boolean autoApprove) {
        Book book = bookRepository.findById(bookId);
        
        if (autoApprove) {
            // Directly apply metadata
            book.setTitle(metadata.getTitle());
            book.setAuthors(String.join(", ", metadata.getAuthors()));
            book.setDescription(metadata.getDescription());
            book.setIsbn(metadata.getIsbn13());
            book.setPublisher(metadata.getPublisher());
            book.setPublishDate(metadata.getPublishDate());
            book.setCategories(metadata.getCategories());
            book.setPageCount(metadata.getPageCount());
            
            // Download and store cover
            if (metadata.getCoverUrl() != null) {
                downloadAndStoreCover(book, metadata.getCoverUrl());
            }
            
            book.setMetadataSource(String.join(", ", metadata.getProviders()));
            book.setMetadataFetchedAt(LocalDateTime.now());
            
            bookRepository.persist(book);
            
            // Emit event
            eventEmitter.send(new MetadataEvent(bookId, "applied", metadata));
        } else {
            // Create pending metadata suggestion
            createMetadataSuggestion(bookId, metadata);
        }
    }
    
    /**
     * Batch metadata fetch for entire library
     */
    public void fetchLibraryMetadata(boolean autoApprove) {
        List<Book> booksWithoutMetadata = bookRepository.findBooksNeedingMetadata();
        
        log.info("Starting metadata fetch for {} books", booksWithoutMetadata.size());
        
        for (Book book : booksWithoutMetadata) {
            fetchMetadata(book.getIsbn(), book.getTitle(), book.getAuthor())
                .thenAccept(metadata -> {
                    if (metadata != null) {
                        applyMetadata(book.getId(), metadata, autoApprove);
                    }
                })
                .exceptionally(e -> {
                    log.error("Failed to fetch metadata for book {}: {}", book.getId(), e.getMessage());
                    return null;
                });
            
            // Rate limiting
            try {
                Thread.sleep(100); // 10 requests/second
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
```

### Database Schema Changes

```sql
-- Add metadata tracking fields
ALTER TABLE books ADD COLUMN metadata_source VARCHAR(255);
ALTER TABLE books ADD COLUMN metadata_fetched_at TIMESTAMP;
ALTER TABLE books ADD COLUMN metadata_confidence FLOAT;
ALTER TABLE books ADD COLUMN metadata_needs_review BOOLEAN DEFAULT FALSE;

-- Metadata suggestions table (for manual approval workflow)
CREATE TABLE metadata_suggestions (
    id BIGSERIAL PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    provider VARCHAR(100),
    confidence FLOAT,
    suggested_title VARCHAR(500),
    suggested_authors TEXT,
    suggested_description TEXT,
    suggested_isbn VARCHAR(20),
    suggested_publisher VARCHAR(255),
    suggested_publish_date DATE,
    suggested_categories TEXT,
    suggested_cover_url TEXT,
    status VARCHAR(50) DEFAULT 'pending', -- pending, approved, rejected
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT REFERENCES users(id)
);

CREATE INDEX idx_metadata_suggestions_book ON metadata_suggestions(book_id);
CREATE INDEX idx_metadata_suggestions_status ON metadata_suggestions(status);

-- Cache table for metadata API responses
CREATE TABLE metadata_cache (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(100),
    query_type VARCHAR(50), -- isbn, title
    query_value VARCHAR(500),
    response_data JSONB,
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE INDEX idx_metadata_cache_lookup ON metadata_cache(provider, query_type, query_value);
CREATE INDEX idx_metadata_cache_expires ON metadata_cache(expires_at);
```

### Configuration

```properties
# Metadata providers
metadata.enabled=true
metadata.auto-approve=false
metadata.batch-fetch-on-import=true
metadata.rate-limit-per-second=10

# Google Books
metadata.google.enabled=true
metadata.google.api-key=${GOOGLE_BOOKS_API_KEY}
quarkus.rest-client.google-books.url=https://www.googleapis.com/books/v1

# Open Library
metadata.open-library.enabled=true
quarkus.rest-client.open-library.url=https://openlibrary.org

# ISBNdb (optional)
metadata.isbndb.enabled=false
metadata.isbndb.api-key=${ISBNDB_API_KEY}

# Caching
quarkus.cache.caffeine.metadata-isbn.maximum-size=10000
quarkus.cache.caffeine.metadata-isbn.expire-after-write=168h
```

## Implementation Phases

### Phase 1: Core Infrastructure (Week 1-2)
- [ ] Provider interface and base classes
- [ ] Google Books provider implementation
- [ ] Open Library provider implementation
- [ ] Result merging logic
- [ ] Basic API endpoints

### Phase 2: User Interface (Week 2-3)
- [ ] Manual metadata search UI
- [ ] Metadata preview/comparison
- [ ] One-click apply
- [ ] Batch metadata fetch UI
- [ ] Progress tracking

### Phase 3: Automated Workflows (Week 3-4)
- [ ] Auto-fetch on book import
- [ ] Background job for existing books
- [ ] Manual review queue
- [ ] Approval/rejection workflow

### Phase 4: Advanced Features (Week 4-5)
- [ ] ISBNdb integration
- [ ] Audiobook metadata (Audnexus)
- [ ] Custom provider plugins
- [ ] Metadata quality scoring

## Acceptance Tests

### Unit Tests

```java
@QuarkusTest
public class MetadataServiceTest {
    
    @Inject
    MetadataService metadataService;
    
    @Test
    public void testFetchMetadataByISBN() {
        String isbn = "9780544003415"; // The Hobbit
        
        CompletableFuture<AggregatedMetadata> future = 
            metadataService.fetchMetadata(isbn, null, null);
        
        AggregatedMetadata metadata = future.join();
        
        assertNotNull(metadata);
        assertTrue(metadata.getTitle().contains("Hobbit"));
        assertTrue(metadata.getAuthors().contains("Tolkien"));
        assertNotNull(metadata.getDescription());
        assertNotNull(metadata.getCoverUrl());
        assertTrue(metadata.getConfidence() > 0.7f);
    }
    
    @Test
    public void testMergeMultipleResults() {
        List<MetadataResult> results = Arrays.asList(
            createResult("Provider A", 0.9f, "The Hobbit", List.of("J.R.R. Tolkien")),
            createResult("Provider B", 0.8f, "Hobbit", List.of("Tolkien")),
            createResult("Provider C", 0.7f, "The Hobbit", List.of("J. R. R. Tolkien"))
        );
        
        AggregatedMetadata merged = metadataService.mergeResults(results);
        
        assertEquals("The Hobbit", merged.getTitle());
        assertEquals(1, merged.getAuthors().size());
        assertTrue(merged.getProviders().size() == 3);
    }
    
    @Test
    public void testApplyMetadataToBook() {
        Book book = createTestBook();
        AggregatedMetadata metadata = createTestMetadata();
        
        metadataService.applyMetadata(book.getId(), metadata, true);
        
        Book updated = bookRepository.findById(book.getId());
        assertEquals(metadata.getTitle(), updated.getTitle());
        assertEquals(String.join(", ", metadata.getAuthors()), updated.getAuthors());
        assertNotNull(updated.getMetadataFetchedAt());
    }
}
```

### Integration Tests

```java
@QuarkusTest
public class MetadataAPITest {
    
    @Test
    public void testSearchMetadataEndpoint() {
        given()
            .auth().oauth2(getValidToken())
            .queryParam("isbn", "9780544003415")
        .when()
            .get("/api/metadata/search")
        .then()
            .statusCode(200)
            .body("title", containsString("Hobbit"))
            .body("authors", hasItem(containsString("Tolkien")))
            .body("confidence", greaterThan(0.7f));
    }
    
    @Test
    public void testApplyMetadataToBook() {
        Long bookId = createTestBook().getId();
        
        given()
            .auth().oauth2(getValidToken())
            .contentType(ContentType.JSON)
            .body("""
                {
                    "isbn": "9780544003415"
                }
                """)
        .when()
            .post("/api/books/" + bookId + "/metadata/fetch")
        .then()
            .statusCode(200);
        
        // Verify book was updated
        given()
            .auth().oauth2(getValidToken())
        .when()
            .get("/api/books/" + bookId)
        .then()
            .statusCode(200)
            .body("title", containsString("Hobbit"));
    }
    
    @Test
    public void testBatchMetadataFetch() {
        createBooksWithISBNs(10);
        
        given()
            .auth().oauth2(getValidToken())
            .queryParam("autoApprove", true)
        .when()
            .post("/api/metadata/batch-fetch")
        .then()
            .statusCode(202); // Accepted - async processing
        
        // Wait for processing
        await().atMost(30, TimeUnit.SECONDS)
            .until(() -> countBooksWithMetadata() >= 8); // 80% success rate
    }
}
```

### End-to-End Tests

```gherkin
Feature: Automated Metadata Fetching

Scenario: User imports book with ISBN and metadata is auto-fetched
    Given the user uploads "book-with-isbn.epub"
    And the book contains ISBN "9780544003415"
    And auto-fetch is enabled
    When the book is imported
    Then metadata should be fetched automatically
    And the book title should be "The Hobbit"
    And the book author should be "J.R.R. Tolkien"
    And a cover image should be present
    
Scenario: User manually searches for metadata
    Given a book exists with title "hobbit" and no metadata
    When the user opens the book details page
    And clicks "Search Metadata"
    Then metadata search results should appear
    And results should show title "The Hobbit"
    And results should show author "J.R.R. Tolkien"
    When the user clicks "Apply"
    Then the book metadata should be updated
    And a success message should appear
    
Scenario: User triggers batch metadata fetch
    Given 50 books exist without metadata
    And 40 of them have valid ISBNs
    When the user goes to Settings > Library
    And clicks "Fetch Missing Metadata"
    And confirms the action
    Then a progress bar should appear
    And metadata should be fetched for ~35 books
    And results should show success/failure counts
    
Scenario: User reviews metadata suggestions
    Given metadata suggestion exists for a book
    And suggestion has confidence 0.8
    When the user views the book
    Then a "Review Metadata" banner should appear
    When the user clicks "Review"
    Then side-by-side comparison should show
    And current metadata should be on the left
    And suggested metadata should be on the right
    When the user clicks "Accept"
    Then suggested metadata should be applied
    And the suggestion should be marked as approved
```

### Performance Tests

```java
@QuarkusTest
public class MetadataPerformanceTest {
    
    @Test
    public void testSingleMetadataFetch() {
        long start = System.currentTimeMillis();
        
        CompletableFuture<AggregatedMetadata> future = 
            metadataService.fetchMetadata("9780544003415", null, null);
        future.join();
        
        long duration = System.currentTimeMillis() - start;
        assertTrue(duration < 3000); // Should complete within 3 seconds
    }
    
    @Test
    public void testBatchMetadataFetch() {
        List<Book> books = createBooksWithISBNs(100);
        
        long start = System.currentTimeMillis();
        metadataService.fetchLibraryMetadata(true);
        long duration = System.currentTimeMillis() - start;
        
        // Should process ~10 books/second
        assertTrue(duration < 12000); // 100 books in under 12 seconds
    }
    
    @Test
    public void testCacheEffectiveness() {
        String isbn = "9780544003415";
        
        // First fetch (cache miss)
        long start1 = System.currentTimeMillis();
        metadataService.fetchMetadata(isbn, null, null).join();
        long duration1 = System.currentTimeMillis() - start1;
        
        // Second fetch (cache hit)
        long start2 = System.currentTimeMillis();
        metadataService.fetchMetadata(isbn, null, null).join();
        long duration2 = System.currentTimeMillis() - start2;
        
        // Cache should be 10x faster
        assertTrue(duration2 < duration1 / 10);
    }
}
```

## Success Metrics

### Technical Metrics
- Metadata fetch success rate > 85% for books with ISBNs
- Metadata fetch success rate > 60% for title/author search
- Average fetch time < 2 seconds per book
- Cache hit rate > 70% after first week

### User Metrics
- 90%+ of users enable auto-fetch
- 70%+ of books have complete metadata within 48 hours
- < 2% of users manually edit auto-fetched metadata
- Metadata-related support tickets reduced by 80%

### Business Metrics
- Time-to-first-value reduced by 90% (10 min → 1 min per book)
- User activation rate increases by 40%
- User retention (30-day) increases by 25%

## Dependencies & Risks

### Dependencies
- Google Books API (free tier: 1000 req/day)
- Open Library API (no strict limits)
- Optional: ISBNdb API (paid for high volume)
- REST client libraries (Quarkus built-in)

### Technical Risks
- **Risk**: API rate limits exceeded with large libraries  
  **Mitigation**: Implement aggressive caching, respect rate limits, use multiple providers

- **Risk**: Metadata quality/accuracy issues  
  **Mitigation**: Multi-provider merge, confidence scoring, manual review option

- **Risk**: Cover image copyright/licensing  
  **Mitigation**: Use only API-provided covers, cache locally, provide proper attribution

### Business Risks
- **Risk**: API costs scale with usage  
  **Mitigation**: Start with free tiers, implement caching, evaluate paid tiers at scale

## Rollout Strategy

### Phase 1: Opt-In Beta (Week 1-2)
- Enable for internal testing
- Test with diverse library (fiction, non-fiction, international)
- Collect quality metrics

### Phase 2: Auto-Fetch on Import (Week 3-4)
- Enable auto-fetch for new imports only
- Monitor success rates and user feedback
- Iterate on confidence scoring

### Phase 3: Batch Fetch Tool (Week 5-6)
- Release batch fetch UI for existing libraries
- Provide progress tracking and reports
- Monitor server load

### Phase 4: Full Auto (Week 7+)
- Enable by default for all users
- Implement background job for periodic checks
- Add manual review queue

## Estimated Effort

**Total: 5-6 weeks (1 developer)**

- Core providers: 2 weeks
- Merging logic: 1 week
- UI components: 1 week
- Testing & refinement: 1 week
- Documentation: 0.5 weeks

## Related Features

- **Depends On**: Book import pipeline, cover image storage
- **Enables**: Better search, recommendations, collections
- **Complements**: OPDS feeds, mobile apps, reading lists
