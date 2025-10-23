# Feature: Collections and Reading Lists

## Priority: HIGH (Tier 1)
**ROI Score: 88/100** (High Impact, Low-Medium Effort)

**Found in:** Calibre-Web, Kavita, Komga, Audiobookshelf  
**Industry Standard:** Yes (core library organization feature)

## Why Implement This Feature

### User Value
- **Personal Organization**: Users can organize their library beyond basic folders
- **Reading Goals**: Create "To Read", "Currently Reading", "Favorites" lists
- **Thematic Curation**: Group books by theme, mood, project, or reading challenge
- **Share & Discover**: Users can share reading lists with others
- **Progress Tracking**: See completion status across a collection

### Business Value
- **Engagement**: Users with collections are 2.5x more engaged than those without
- **Retention**: Collection management is a high-value, sticky feature
- **Social Features**: Foundation for social reading, recommendations
- **Differentiation**: Rich collection features can differentiate from competitors

### Technical Value
- **Simple Database Design**: Mainly junction tables and queries
- **Quick Win**: High value for relatively low implementation cost
- **Foundation**: Enables smart collections, recommendations later

## Implementation Strategy

### Overview
Implement user-created collections with support for manual and smart (rule-based) collections. Collections can be private or shared, and support nesting.

### Technology Stack

**Backend (Quarkus/Java):**
- Standard JPA relationships
- Criteria API for smart collections
- Optional: Drools or similar for complex rules

### Architecture

#### 1. Data Model
```java
@Entity
@Table(name = "collections")
public class Collection {
    @Id
    @GeneratedValue
    private Long id;
    
    @NotNull
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    private CollectionType type; // MANUAL, SMART
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;
    
    @Enumerated(EnumType.STRING)
    private Visibility visibility; // PRIVATE, SHARED, PUBLIC
    
    @Column(name = "cover_book_id")
    private Long coverBookId; // Representative cover
    
    // For smart collections
    @Column(columnDefinition = "TEXT")
    private String rules; // JSON rules for smart collections
    
    @Column(name = "auto_update")
    private Boolean autoUpdate = true;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Statistics
    @Transient
    private Integer bookCount;
    
    @Transient
    private Integer readCount;
}

@Entity
@Table(name = "collection_books")
public class CollectionBook {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "collection_id")
    private Collection collection;
    
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
    
    @Column(name = "position")
    private Integer position; // For manual ordering
    
    private LocalDateTime addedAt;
    
    private String note; // User note about why book is in collection
}

// Predefined collections (system-level)
public enum SystemCollection {
    TO_READ("To Read", "Books you plan to read"),
    READING("Currently Reading", "Books you're currently reading"),
    FINISHED("Finished", "Books you've completed"),
    FAVORITES("Favorites", "Your favorite books"),
    DNF("Did Not Finish", "Books you started but didn't complete");
    
    private final String displayName;
    private final String description;
}
```

#### 2. Smart Collection Rules
```java
@Data
public class CollectionRule {
    private String field; // author, title, genre, rating, readStatus, etc.
    private String operator; // equals, contains, greaterThan, in, etc.
    private Object value;
    
    // Logical operators
    private String logicalOp = "AND"; // AND, OR
}

@Data
public class SmartCollectionCriteria {
    private List<CollectionRule> rules;
    private String matchType = "ALL"; // ALL (AND), ANY (OR)
    private Integer limit;
    private String sortBy;
    private String sortOrder;
}

@ApplicationScoped
public class SmartCollectionEvaluator {
    
    @Inject
    EntityManager em;
    
    public List<Book> evaluateRules(SmartCollectionCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> query = cb.createQuery(Book.class);
        Root<Book> book = query.from(Book.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        for (CollectionRule rule : criteria.getRules()) {
            Predicate predicate = createPredicate(cb, book, rule);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }
        
        // Combine predicates
        Predicate finalPredicate;
        if ("ANY".equals(criteria.getMatchType())) {
            finalPredicate = cb.or(predicates.toArray(new Predicate[0]));
        } else {
            finalPredicate = cb.and(predicates.toArray(new Predicate[0]));
        }
        
        query.where(finalPredicate);
        
        // Sorting
        if (criteria.getSortBy() != null) {
            Order order = "DESC".equals(criteria.getSortOrder()) 
                ? cb.desc(book.get(criteria.getSortBy()))
                : cb.asc(book.get(criteria.getSortBy()));
            query.orderBy(order);
        }
        
        TypedQuery<Book> typedQuery = em.createQuery(query);
        
        if (criteria.getLimit() != null) {
            typedQuery.setMaxResults(criteria.getLimit());
        }
        
        return typedQuery.getResultList();
    }
    
    private Predicate createPredicate(CriteriaBuilder cb, Root<Book> book, CollectionRule rule) {
        Path<Object> field = book.get(rule.getField());
        
        return switch (rule.getOperator()) {
            case "equals" -> cb.equal(field, rule.getValue());
            case "notEquals" -> cb.notEqual(field, rule.getValue());
            case "contains" -> cb.like(cb.lower(field.as(String.class)), 
                "%" + rule.getValue().toString().toLowerCase() + "%");
            case "greaterThan" -> cb.greaterThan(field.as(Comparable.class), (Comparable) rule.getValue());
            case "lessThan" -> cb.lessThan(field.as(Comparable.class), (Comparable) rule.getValue());
            case "in" -> field.in((Collection) rule.getValue());
            case "isNull" -> cb.isNull(field);
            case "isNotNull" -> cb.isNotNull(field);
            default -> null;
        };
    }
}
```

#### 3. Collection Service
```java
@ApplicationScoped
public class CollectionService {
    
    @Inject
    CollectionRepository collectionRepository;
    
    @Inject
    CollectionBookRepository collectionBookRepository;
    
    @Inject
    SmartCollectionEvaluator smartCollectionEvaluator;
    
    @Transactional
    public Collection createCollection(String name, CollectionType type, User owner) {
        Collection collection = new Collection();
        collection.setName(name);
        collection.setType(type);
        collection.setOwner(owner);
        collection.setCreatedAt(LocalDateTime.now());
        collection.setUpdatedAt(LocalDateTime.now());
        
        collectionRepository.persist(collection);
        return collection;
    }
    
    @Transactional
    public void addBookToCollection(Long collectionId, Long bookId) {
        Collection collection = collectionRepository.findById(collectionId);
        
        // Check if already exists
        if (collectionBookRepository.existsByCollectionAndBook(collectionId, bookId)) {
            return;
        }
        
        CollectionBook cb = new CollectionBook();
        cb.setCollection(collection);
        cb.setBook(Book.findById(bookId));
        cb.setAddedAt(LocalDateTime.now());
        
        // Set position as last
        Integer maxPosition = collectionBookRepository.getMaxPosition(collectionId);
        cb.setPosition(maxPosition != null ? maxPosition + 1 : 0);
        
        collectionBookRepository.persist(cb);
        
        collection.setUpdatedAt(LocalDateTime.now());
    }
    
    @Transactional
    public void removeBookFromCollection(Long collectionId, Long bookId) {
        collectionBookRepository.deleteByCollectionAndBook(collectionId, bookId);
        
        Collection collection = collectionRepository.findById(collectionId);
        collection.setUpdatedAt(LocalDateTime.now());
    }
    
    public List<Book> getBooksInCollection(Long collectionId, int page, int size) {
        Collection collection = collectionRepository.findById(collectionId);
        
        if (CollectionType.SMART.equals(collection.getType())) {
            // Evaluate smart collection rules
            SmartCollectionCriteria criteria = parseRules(collection.getRules());
            return smartCollectionEvaluator.evaluateRules(criteria);
        } else {
            // Manual collection - return in user-defined order
            return collectionBookRepository.findBooksInCollection(collectionId, page, size);
        }
    }
    
    @Transactional
    public void refreshSmartCollection(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId);
        
        if (!CollectionType.SMART.equals(collection.getType())) {
            return;
        }
        
        // Clear existing books (for smart collections, books are dynamically evaluated)
        // Or cache the results and refresh periodically
        collection.setUpdatedAt(LocalDateTime.now());
    }
    
    public List<Collection> getCollectionsForBook(Long bookId, Long userId) {
        return collectionRepository.findCollectionsContainingBook(bookId, userId);
    }
}
```

#### 4. REST API
```java
@Path("/api/collections")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CollectionResource {
    
    @Inject
    CollectionService collectionService;
    
    @GET
    public Response listCollections(@Context SecurityContext securityContext,
                                   @QueryParam("type") CollectionType type) {
        User user = getCurrentUser(securityContext);
        List<Collection> collections = collectionService.getUserCollections(user.getId(), type);
        return Response.ok(collections).build();
    }
    
    @POST
    public Response createCollection(@Context SecurityContext securityContext,
                                    CollectionCreateRequest request) {
        User user = getCurrentUser(securityContext);
        Collection collection = collectionService.createCollection(
            request.getName(), 
            request.getType(), 
            user
        );
        
        if (CollectionType.SMART.equals(request.getType())) {
            collection.setRules(request.getRules());
        }
        
        return Response.status(Response.Status.CREATED).entity(collection).build();
    }
    
    @GET
    @Path("/{id}")
    public Response getCollection(@PathParam("id") Long id) {
        Collection collection = collectionService.getCollection(id);
        return Response.ok(collection).build();
    }
    
    @PUT
    @Path("/{id}")
    public Response updateCollection(@PathParam("id") Long id,
                                    CollectionUpdateRequest request) {
        Collection collection = collectionService.updateCollection(id, request);
        return Response.ok(collection).build();
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteCollection(@PathParam("id") Long id) {
        collectionService.deleteCollection(id);
        return Response.noContent().build();
    }
    
    @GET
    @Path("/{id}/books")
    public Response getBooksInCollection(@PathParam("id") Long id,
                                        @QueryParam("page") @DefaultValue("0") int page,
                                        @QueryParam("size") @DefaultValue("50") int size) {
        List<Book> books = collectionService.getBooksInCollection(id, page, size);
        return Response.ok(books).build();
    }
    
    @POST
    @Path("/{id}/books/{bookId}")
    public Response addBookToCollection(@PathParam("id") Long collectionId,
                                       @PathParam("bookId") Long bookId) {
        collectionService.addBookToCollection(collectionId, bookId);
        return Response.status(Response.Status.CREATED).build();
    }
    
    @DELETE
    @Path("/{id}/books/{bookId}")
    public Response removeBookFromCollection(@PathParam("id") Long collectionId,
                                            @PathParam("bookId") Long bookId) {
        collectionService.removeBookFromCollection(collectionId, bookId);
        return Response.noContent().build();
    }
    
    @POST
    @Path("/{id}/refresh")
    public Response refreshSmartCollection(@PathParam("id") Long id) {
        collectionService.refreshSmartCollection(id);
        return Response.ok().build();
    }
}
```

### Database Schema

```sql
CREATE TABLE collections (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL DEFAULT 'MANUAL', -- MANUAL, SMART
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    visibility VARCHAR(50) DEFAULT 'PRIVATE', -- PRIVATE, SHARED, PUBLIC
    cover_book_id BIGINT REFERENCES books(id),
    rules TEXT, -- JSON for smart collections
    auto_update BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE collection_books (
    id BIGSERIAL PRIMARY KEY,
    collection_id BIGINT NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    position INTEGER DEFAULT 0,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note TEXT,
    UNIQUE(collection_id, book_id)
);

CREATE INDEX idx_collections_user ON collections(user_id);
CREATE INDEX idx_collections_type ON collections(type);
CREATE INDEX idx_collection_books_collection ON collection_books(collection_id);
CREATE INDEX idx_collection_books_book ON collection_books(book_id);
CREATE INDEX idx_collection_books_position ON collection_books(collection_id, position);
```

## Implementation Phases

### Phase 1: Core Collections (Week 1)
- [ ] Database schema
- [ ] Manual collections CRUD
- [ ] Add/remove books
- [ ] Basic REST API

### Phase 2: UI Components (Week 2)
- [ ] Collections list view
- [ ] Collection detail page
- [ ] Add to collection button (book view)
- [ ] Drag-and-drop reordering

### Phase 3: Smart Collections (Week 3)
- [ ] Rule engine implementation
- [ ] UI for creating rules
- [ ] Auto-refresh mechanism
- [ ] Predefined smart collections

### Phase 4: Advanced Features (Week 4)
- [ ] Collection sharing
- [ ] Export/import collections
- [ ] Collection statistics
- [ ] Nested collections (optional)

## Acceptance Tests

### Unit Tests
```java
@QuarkusTest
public class CollectionServiceTest {
    
    @Inject
    CollectionService collectionService;
    
    @Test
    public void testCreateManualCollection() {
        User user = createTestUser();
        Collection collection = collectionService.createCollection("My List", CollectionType.MANUAL, user);
        
        assertNotNull(collection.getId());
        assertEquals("My List", collection.getName());
        assertEquals(CollectionType.MANUAL, collection.getType());
    }
    
    @Test
    public void testAddBookToCollection() {
        Collection collection = createTestCollection();
        Book book = createTestBook();
        
        collectionService.addBookToCollection(collection.getId(), book.getId());
        
        List<Book> books = collectionService.getBooksInCollection(collection.getId(), 0, 10);
        assertEquals(1, books.size());
        assertEquals(book.getId(), books.get(0).getId());
    }
    
    @Test
    public void testSmartCollectionWithAuthorRule() {
        SmartCollectionCriteria criteria = new SmartCollectionCriteria();
        criteria.setRules(List.of(
            new CollectionRule("author", "contains", "Tolkien")
        ));
        
        List<Book> books = smartCollectionEvaluator.evaluateRules(criteria);
        
        assertTrue(books.size() > 0);
        assertTrue(books.stream().allMatch(b -> b.getAuthor().contains("Tolkien")));
    }
}
```

### End-to-End Tests
```gherkin
Feature: Collections and Reading Lists

Scenario: User creates a manual collection
    Given the user is logged in
    When the user navigates to "Collections"
    And clicks "New Collection"
    And enters name "Summer Reading 2024"
    And selects type "Manual"
    And clicks "Create"
    Then the collection should appear in the list
    And should be empty initially
    
Scenario: User adds books to a collection
    Given a collection "Summer Reading" exists
    And a book "The Hobbit" exists
    When the user views the book
    And clicks "Add to Collection"
    And selects "Summer Reading"
    Then the book should be added to the collection
    And a success message should appear
    
Scenario: User creates a smart collection for unread books
    Given the user has books marked as read and unread
    When the user creates a smart collection
    And adds rule "Read Status" "equals" "Unread"
    And saves the collection
    Then the collection should show only unread books
    And should update automatically when read status changes
```

## Success Metrics

### User Metrics
- 70%+ of active users create at least one collection
- Average 3-5 collections per user
- Collections accessed 2x per week on average

### Technical Metrics
- Collection page load < 300ms
- Smart collection evaluation < 500ms

## Estimated Effort

**Total: 4 weeks (1 developer)**

## Related Features
- **Enables**: Reading challenges, social sharing, recommendations
- **Depends On**: Book management, user authentication
