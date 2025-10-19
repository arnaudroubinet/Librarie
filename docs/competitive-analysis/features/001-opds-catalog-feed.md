# Feature: OPDS Catalog Feed Support

## Priority: CRITICAL (Tier 1)
**ROI Score: 95/100** (High Impact, Medium Effort)

**Found in:** Calibre-Web, Kavita, Komga, Audiobookshelf  
**Industry Standard:** Yes (OPDS 1.2 and OPDS 2.0 specifications)

## Why Implement This Feature

### User Value
- **Universal E-Reader Compatibility**: OPDS (Open Publication Distribution System) is the industry standard for distributing digital publications
- **Device Freedom**: Users can access their library from ANY OPDS-compatible reader (Moon+ Reader, FBReader, KyBook, Marvin, etc.)
- **Zero Lock-in**: No need to force users into a specific mobile app
- **Immediate Mobile Access**: Users get mobile access without building native apps first

### Business Value
- **Competitive Necessity**: All major competitors support OPDS - this is table stakes
- **Reduced Development Load**: Don't need to build mobile apps immediately
- **Standards Compliance**: Following open standards increases trust and adoption
- **Network Effects**: OPDS compatibility opens Librarie to the entire e-reader ecosystem

### Technical Value
- **Well-Defined Spec**: OPDS 1.2 and 2.0 have clear specifications
- **Mature Libraries**: Many implementation libraries available
- **Testing Tools**: OPDS validators and test readers available

## Implementation Strategy

### Overview
Implement OPDS 1.2 feed (Atom/XML based) with OPDS 2.0 (JSON-based) as stretch goal. Provide discovery, navigation, search, and acquisition feeds.

### Technology Stack

**Backend (Quarkus/Java):**
- **Library**: Use `com.rometools:rome:2.1.0` for Atom feed generation
- **Alternative**: Build custom XML/JSON serialization using Jackson
- **Authentication**: Extend existing OIDC to support Basic Auth for OPDS clients

**Key Dependencies:**
```xml
<dependency>
    <groupId>com.rometools</groupId>
    <artifactId>rome</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>com.rometools</groupId>
    <artifactId>rome-opds</artifactId>
    <version>2.1.0</version>
</dependency>
```

### Architecture

#### 1. OPDS Controller/Resource
```java
@Path("/opds")
@Produces({MediaType.APPLICATION_ATOM_XML, "application/atom+xml;profile=opds-catalog"})
public class OpdsResource {
    
    @Inject
    OpdsService opdsService;
    
    @GET
    @Path("/")
    public Response getRootCatalog() {
        // Root catalog with navigation links
    }
    
    @GET
    @Path("/all")
    public Response getAllBooks(@QueryParam("page") int page) {
        // Paginated list of all books
    }
    
    @GET
    @Path("/recent")
    public Response getRecentBooks() {
        // Recently added books
    }
    
    @GET
    @Path("/search")
    public Response search(@QueryParam("q") String query) {
        // Search results
    }
    
    @GET
    @Path("/authors")
    public Response getAuthors() {
        // List of authors
    }
    
    @GET
    @Path("/authors/{id}")
    public Response getBooksByAuthor(@PathParam("id") Long id) {
        // Books by specific author
    }
}
```

#### 2. OPDS Service Layer
```java
@ApplicationScoped
public class OpdsService {
    
    @Inject
    BookRepository bookRepository;
    
    public Feed generateRootFeed(String baseUrl) {
        Feed feed = new Feed();
        feed.setTitle("Librarie OPDS Catalog");
        feed.setId("urn:uuid:" + UUID.randomUUID());
        feed.setUpdated(new Date());
        
        // Add navigation links
        feed.addLink(createLink(baseUrl + "/opds/all", "subsection", "application/atom+xml;profile=opds-catalog", "All Books"));
        feed.addLink(createLink(baseUrl + "/opds/recent", "subsection", "application/atom+xml;profile=opds-catalog", "Recent Additions"));
        feed.addLink(createLink(baseUrl + "/opds/authors", "subsection", "application/atom+xml;profile=opds-catalog", "By Author"));
        
        // Add search link
        Link searchLink = new Link();
        searchLink.setHref(baseUrl + "/opds/search?q={searchTerms}");
        searchLink.setType("application/atom+xml;profile=opds-catalog");
        searchLink.setRel("search");
        feed.addLink(searchLink);
        
        return feed;
    }
    
    public Feed generateAcquisitionFeed(List<Book> books, String baseUrl, String feedTitle) {
        Feed feed = new Feed();
        feed.setTitle(feedTitle);
        feed.setId("urn:uuid:" + UUID.randomUUID());
        feed.setUpdated(new Date());
        
        for (Book book : books) {
            Entry entry = createBookEntry(book, baseUrl);
            feed.addEntry(entry);
        }
        
        return feed;
    }
    
    private Entry createBookEntry(Book book, String baseUrl) {
        Entry entry = new Entry();
        entry.setTitle(book.getTitle());
        entry.setId("urn:uuid:" + book.getId());
        entry.setUpdated(book.getModifiedDate());
        entry.setSummary(book.getDescription());
        
        // Author
        if (book.getAuthor() != null) {
            Person author = new Person();
            author.setName(book.getAuthor());
            entry.addAuthor(author);
        }
        
        // Acquisition link (download)
        Link acquisitionLink = new Link();
        acquisitionLink.setHref(baseUrl + "/api/books/" + book.getId() + "/download");
        acquisitionLink.setType(book.getMimeType()); // e.g., "application/epub+zip"
        acquisitionLink.setRel("http://opds-spec.org/acquisition");
        entry.addLink(acquisitionLink);
        
        // Cover image link
        if (book.hasCover()) {
            Link coverLink = new Link();
            coverLink.setHref(baseUrl + "/api/books/" + book.getId() + "/cover");
            coverLink.setType("image/jpeg");
            coverLink.setRel("http://opds-spec.org/image");
            entry.addLink(coverLink);
            
            // Thumbnail
            Link thumbnailLink = new Link();
            thumbnailLink.setHref(baseUrl + "/api/books/" + book.getId() + "/thumbnail");
            thumbnailLink.setType("image/jpeg");
            thumbnailLink.setRel("http://opds-spec.org/image/thumbnail");
            entry.addLink(thumbnailLink);
        }
        
        return entry;
    }
}
```

#### 3. Authentication Enhancement
```java
@Provider
public class OpdsAuthenticationFilter implements ContainerRequestFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        
        if (path.startsWith("/opds")) {
            // Check for Basic Auth header
            String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
            
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                // Decode and validate Basic Auth
                String base64Credentials = authHeader.substring("Basic ".length());
                String credentials = new String(Base64.getDecoder().decode(base64Credentials));
                String[] values = credentials.split(":", 2);
                
                // Validate credentials
                if (validateCredentials(values[0], values[1])) {
                    return; // Authorized
                }
            }
            
            // If no valid auth, return 401
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Librarie OPDS\"")
                .build());
        }
    }
}
```

### Database Schema Changes

**No major schema changes required** - OPDS feeds are generated from existing Book, Author, and metadata tables.

Optional enhancement:
```sql
-- Add OPDS-specific tracking
ALTER TABLE books ADD COLUMN opds_downloads INTEGER DEFAULT 0;
CREATE INDEX idx_books_updated_desc ON books(updated_at DESC);
CREATE INDEX idx_books_created_desc ON books(created_at DESC);
```

### Configuration
```properties
# application.properties
opds.enabled=true
opds.title=Librarie OPDS Catalog
opds.page-size=50
opds.max-results=1000
opds.auth.required=true
opds.auth.basic.enabled=true
```

## Implementation Phases

### Phase 1: Core OPDS 1.2 (Week 1-2)
- [ ] Root catalog endpoint
- [ ] All books feed (paginated)
- [ ] Recent additions feed
- [ ] Basic Auth support
- [ ] Book download links
- [ ] Cover image links

### Phase 2: Navigation & Search (Week 3)
- [ ] Authors catalog
- [ ] Books by author
- [ ] Search endpoint
- [ ] Genre/category navigation
- [ ] Pagination links (next/prev)

### Phase 3: Enhanced Features (Week 4)
- [ ] Series support
- [ ] Rating/review metadata
- [ ] Publisher information
- [ ] Language filtering
- [ ] Advanced search (by author, title, ISBN)

### Phase 4: OPDS 2.0 (Optional)
- [ ] JSON-based feeds
- [ ] Streaming support
- [ ] Enhanced metadata

## Acceptance Tests

### Unit Tests

```java
@QuarkusTest
public class OpdsServiceTest {
    
    @Inject
    OpdsService opdsService;
    
    @Test
    public void testGenerateRootFeed() {
        String baseUrl = "http://localhost:8080";
        Feed feed = opdsService.generateRootFeed(baseUrl);
        
        assertNotNull(feed);
        assertEquals("Librarie OPDS Catalog", feed.getTitle());
        assertTrue(feed.getLinks().size() >= 4); // all, recent, authors, search
        
        // Verify search link
        Link searchLink = feed.getLinks().stream()
            .filter(l -> "search".equals(l.getRel()))
            .findFirst()
            .orElse(null);
        assertNotNull(searchLink);
        assertTrue(searchLink.getHref().contains("{searchTerms}"));
    }
    
    @Test
    public void testGenerateAcquisitionFeed() {
        List<Book> books = createTestBooks(10);
        Feed feed = opdsService.generateAcquisitionFeed(books, "http://localhost:8080", "Test Feed");
        
        assertNotNull(feed);
        assertEquals(10, feed.getEntries().size());
        
        Entry entry = feed.getEntries().get(0);
        assertNotNull(entry.getTitle());
        
        // Verify acquisition link
        Link acqLink = entry.getLinks().stream()
            .filter(l -> "http://opds-spec.org/acquisition".equals(l.getRel()))
            .findFirst()
            .orElse(null);
        assertNotNull(acqLink);
        assertTrue(acqLink.getHref().contains("/download"));
    }
    
    @Test
    public void testBookEntryHasCoverLinks() {
        Book book = createBookWithCover();
        Entry entry = opdsService.createBookEntry(book, "http://localhost:8080");
        
        // Verify image link
        Link imageLink = entry.getLinks().stream()
            .filter(l -> "http://opds-spec.org/image".equals(l.getRel()))
            .findFirst()
            .orElse(null);
        assertNotNull(imageLink);
        
        // Verify thumbnail link
        Link thumbLink = entry.getLinks().stream()
            .filter(l -> "http://opds-spec.org/image/thumbnail".equals(l.getRel()))
            .findFirst()
            .orElse(null);
        assertNotNull(thumbLink);
    }
}
```

### Integration Tests

```java
@QuarkusTest
public class OpdsResourceTest {
    
    @Test
    public void testRootCatalog() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/opds/")
        .then()
            .statusCode(200)
            .contentType("application/atom+xml;profile=opds-catalog")
            .body("feed.title", equalTo("Librarie OPDS Catalog"))
            .body("feed.link.size()", greaterThan(3));
    }
    
    @Test
    public void testAllBooksWithPagination() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("page", 0)
        .when()
            .get("/opds/all")
        .then()
            .statusCode(200)
            .body("feed.entry.size()", lessThanOrEqualTo(50));
    }
    
    @Test
    public void testSearchBooks() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("q", "harry potter")
        .when()
            .get("/opds/search")
        .then()
            .statusCode(200)
            .body("feed.entry.size()", greaterThan(0));
    }
    
    @Test
    public void testAuthenticationRequired() {
        when()
            .get("/opds/")
        .then()
            .statusCode(401)
            .header("WWW-Authenticate", containsString("Basic realm"));
    }
    
    @Test
    public void testInvalidCredentials() {
        given()
            .auth().basic("invalid", "wrong")
        .when()
            .get("/opds/")
        .then()
            .statusCode(401);
    }
}
```

### End-to-End Tests

```gherkin
Feature: OPDS Catalog Access

Scenario: User browses catalog from e-reader app
    Given the user has Moon+ Reader installed
    And OPDS authentication is configured with valid credentials
    When the user adds "http://librarie.example.com/opds/" as a catalog
    Then the catalog should appear in the app
    And the user should see navigation options for "All Books", "Recent", "Authors"
    
Scenario: User downloads a book via OPDS
    Given the user is browsing the OPDS catalog in FBReader
    And the catalog contains at least one EPUB book
    When the user selects a book
    And clicks download
    Then the book should download successfully
    And the user should be able to open and read the book
    
Scenario: User searches for books
    Given the user is in an OPDS-compatible reader
    And the search feature is enabled
    When the user searches for "tolkien"
    Then the results should contain books by Tolkien
    And each result should have cover images
    And each result should have download links
    
Scenario: Pagination works correctly
    Given the library has 500 books
    And page size is set to 50
    When the user browses "All Books"
    Then the first page should show 50 books
    And there should be a "next" link
    When the user follows the "next" link
    Then the second page should show the next 50 books
```

### Performance Tests

```java
@QuarkusTest
public class OpdsPerformanceTest {
    
    @Test
    public void testLargeCatalogPerformance() {
        // Generate 10,000 books
        prepareTestLibrary(10000);
        
        long start = System.currentTimeMillis();
        
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/opds/all?page=0")
        .then()
            .statusCode(200)
            .time(lessThan(500L), TimeUnit.MILLISECONDS);
    }
    
    @Test
    public void testConcurrentRequests() {
        // Simulate 50 concurrent OPDS clients
        ExecutorService executor = Executors.newFixedThreadPool(50);
        
        for (int i = 0; i < 50; i++) {
            executor.submit(() -> {
                given()
                    .auth().basic("testuser", "testpass")
                .when()
                    .get("/opds/recent")
                .then()
                    .statusCode(200);
            });
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
    }
}
```

### Compatibility Tests

**Manual Testing with OPDS Clients:**
- [ ] Moon+ Reader (Android)
- [ ] FBReader (Android/iOS)
- [ ] KyBook 3 (iOS)
- [ ] Marvin (iOS)
- [ ] Calibre (Desktop)
- [ ] Aldiko (Android)
- [ ] PocketBook Reader (iOS/Android)

**OPDS Validator:**
- [ ] Use https://github.com/IDPF/opds-validator
- [ ] Validate root catalog
- [ ] Validate acquisition feeds
- [ ] Validate search results
- [ ] Check Atom/XML well-formedness

## Success Metrics

### Technical Metrics
- OPDS feed generation < 200ms for 50 books
- All OPDS validators pass
- Support for 95% of popular OPDS clients

### User Metrics
- 60%+ of users add OPDS to their reader within 30 days
- 80%+ successful connection rate
- < 5% support tickets related to OPDS

### Business Metrics
- Reduces need for immediate mobile app by 6-12 months
- Increases adoption among e-reader enthusiasts by 40%

## Dependencies & Risks

### Dependencies
- `rome:2.1.0` (or similar Atom/XML library)
- Existing authentication system extension
- Cover image generation/serving infrastructure

### Technical Risks
- **Risk**: OPDS client compatibility issues  
  **Mitigation**: Test with 7+ popular clients, follow spec strictly

- **Risk**: Performance degradation with large libraries  
  **Mitigation**: Implement aggressive caching, pagination, database indexing

- **Risk**: Authentication complexity (OIDC vs Basic Auth)  
  **Mitigation**: Support both, provide clear documentation

### Business Risks
- **Risk**: Users prefer native apps  
  **Mitigation**: OPDS doesn't preclude apps, provides immediate value

## Rollout Strategy

### Phase 1: Alpha (Internal)
- Deploy to test server
- Internal team testing with 3-4 OPDS clients
- Fix critical bugs

### Phase 2: Beta (Early Adopters)
- Release to 50-100 power users
- Collect compatibility reports
- Monitor performance metrics

### Phase 3: General Availability
- Enable by default for all users
- Documentation and tutorials
- Blog post announcement

### Feature Flags
```properties
opds.enabled=true
opds.auth.required=true
opds.opds2.enabled=false  # OPDS 2.0 as opt-in beta
```

## Documentation Requirements

### User Documentation
- Setup guide for popular OPDS readers
- Troubleshooting common connection issues
- FAQ for OPDS vs native apps

### Developer Documentation
- OPDS API reference
- Extension points for custom feeds
- Performance tuning guide

## Estimated Effort

**Total: 4-6 weeks (1 developer)**

- Core implementation: 2 weeks
- Testing & compatibility: 1 week
- Documentation: 0.5 weeks
- Bug fixes & polish: 1 week
- OPDS 2.0 (optional): +2 weeks

## Related Features

- **Enhancement Dependencies**: Cover image generation, metadata quality
- **Enables**: Mobile access without apps, e-reader integration
- **Complements**: Web reader, download management
