# Feature: Advanced Search and Filtering

## Priority: HIGH (Tier 1)
**ROI Score: 84/100** (High Impact, Medium Effort)

**Found in:** Calibre-Web, Kavita, Komga, Audiobookshelf

## Why Implement This Feature

### User Value
- **Find Books Fast**: Quickly locate specific books in large libraries
- **Complex Queries**: "Books by Tolkien published after 1950 unread fantasy"
- **Discovery**: Find books by metadata they didn't know they had
- **Saved Searches**: Reuse common search queries

### Business Value
- **Large Library Support**: Essential for users with 1000+ books
- **Power User Feature**: Differentiates from simple solutions
- **Reduces Friction**: Users find what they want immediately

## Implementation Strategy

### Architecture
```java
@ApplicationScoped
public class SearchService {
    
    public SearchResult search(SearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> query = cb.createQuery(Book.class);
        Root<Book> book = query.from(Book.class);
        
        List<Predicate> predicates = buildPredicates(cb, book, criteria);
        query.where(cb.and(predicates.toArray(new Predicate[0])));
        
        // Apply sorting
        applySorting(cb, query, book, criteria.getSortBy(), criteria.getSortOrder());
        
        return executeSearch(query, criteria.getPage(), criteria.getSize());
    }
}

@Data
public class SearchCriteria {
    private String query; // Full-text search
    private List<String> authors;
    private List<String> genres;
    private List<String> tags;
    private String publisher;
    private Integer yearFrom;
    private Integer yearTo;
    private List<String> formats;
    private ReadingStatus readStatus;
    private Float ratingMin;
    private Boolean hasCover;
    private String language;
    private String series;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;
}
```

### Full-Text Search Integration
```xml
<!-- Hibernate Search / Elasticsearch -->
<dependency>
    <groupId>org.hibernate.search</groupId>
    <artifactId>hibernate-search-mapper-orm</artifactId>
</dependency>
<dependency>
    <groupId>org.hibernate.search</groupId>
    <artifactId>hibernate-search-backend-elasticsearch</artifactId>
</dependency>
```

```java
@Entity
@Indexed
public class Book {
    
    @FullTextField(analyzer = "english")
    private String title;
    
    @FullTextField
    private String author;
    
    @FullTextField(analyzer = "english")
    private String description;
    
    @KeywordField
    private List<String> genres;
    
    @KeywordField
    private List<String> tags;
}
```

### REST API
```java
@GET
@Path("/api/search")
public Response search(@BeanParam SearchCriteria criteria) {
    SearchResult result = searchService.search(criteria);
    return Response.ok(result).build();
}

@GET
@Path("/api/search/suggestions")
public Response getSuggestions(@QueryParam("q") String query) {
    List<String> suggestions = searchService.getSuggestions(query);
    return Response.ok(suggestions).build();
}
```

## Acceptance Tests

```gherkin
Feature: Advanced Search

Scenario: Search by author and genre
    Given the library contains fantasy books by multiple authors
    When I search for author "Tolkien" AND genre "Fantasy"
    Then results should only show Tolkien's fantasy books
    
Scenario: Search with year range
    Given books from various decades exist
    When I search for books published between 1950 and 1960
    Then results should only show books in that range
```

## Estimated Effort
**Total: 3 weeks (1 developer)**
