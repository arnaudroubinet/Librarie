# Feature: Reading Progress Tracking

## Priority: HIGH (Tier 1)
**ROI Score: 86/100** (High Impact, Low Effort)

**Found in:** Calibre-Web, Kavita, Komga, Audiobookshelf

## Why Implement This Feature

### User Value
- **Resume Reading**: Pick up exactly where they left off
- **Multi-Device Sync**: Continue reading on any device
- **Reading Statistics**: See how much they've read, time spent
- **Motivation**: Visual progress encourages completion
- **History**: Track reading patterns over time

### Business Value
- **Engagement**: Users with progress tracking read 3x more
- **Retention**: Core feature for serious readers
- **Competitive Parity**: All competitors have this

### Technical Value
- **Simple Implementation**: Just tracking page/position
- **Foundation**: Enables recommendations, reading goals
- **Low Cost**: Minimal storage, simple queries

## Implementation Strategy

### Data Model
```java
@Entity
@Table(name = "reading_progress")
public class ReadingProgress {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User user;
    
    @ManyToOne
    private Book book;
    
    private Integer currentPage;
    private Integer totalPages;
    private Float progressPercent;
    
    @Enumerated(EnumType.STRING)
    private ReadingStatus status; // UNREAD, READING, FINISHED, DNF
    
    private LocalDateTime startedAt;
    private LocalDateTime lastReadAt;
    private LocalDateTime finishedAt;
    
    private Integer readingTimeMinutes; // Total time spent
    
    // For web reader position tracking
    private String cfiPosition; // EPUB CFI (Canonical Fragment Identifier)
    private String scrollPosition; // For PDF/other formats
}

public enum ReadingStatus {
    UNREAD,
    READING,
    FINISHED,
    DNF // Did Not Finish
}
```

### REST API
```java
@Path("/api/books/{bookId}/progress")
public class ReadingProgressResource {
    
    @GET
    public Response getProgress(@PathParam("bookId") Long bookId,
                               @Context SecurityContext ctx) {
        User user = getCurrentUser(ctx);
        ReadingProgress progress = progressService.getProgress(user.getId(), bookId);
        return Response.ok(progress).build();
    }
    
    @POST
    public Response updateProgress(@PathParam("bookId") Long bookId,
                                  ProgressUpdateRequest request,
                                  @Context SecurityContext ctx) {
        User user = getCurrentUser(ctx);
        progressService.updateProgress(user.getId(), bookId, request);
        return Response.ok().build();
    }
    
    @POST
    @Path("/mark-finished")
    public Response markFinished(@PathParam("bookId") Long bookId,
                                @Context SecurityContext ctx) {
        User user = getCurrentUser(ctx);
        progressService.markFinished(user.getId(), bookId);
        return Response.ok().build();
    }
}
```

### Database Schema
```sql
CREATE TABLE reading_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    current_page INTEGER,
    total_pages INTEGER,
    progress_percent FLOAT,
    status VARCHAR(20) DEFAULT 'UNREAD',
    started_at TIMESTAMP,
    last_read_at TIMESTAMP,
    finished_at TIMESTAMP,
    reading_time_minutes INTEGER DEFAULT 0,
    cfi_position TEXT,
    scroll_position TEXT,
    UNIQUE(user_id, book_id)
);

CREATE INDEX idx_reading_progress_user ON reading_progress(user_id);
CREATE INDEX idx_reading_progress_status ON reading_progress(status);
CREATE INDEX idx_reading_progress_last_read ON reading_progress(last_read_at DESC);
```

## Acceptance Tests

```gherkin
Feature: Reading Progress Tracking

Scenario: User starts reading a book
    Given a book exists in the library
    And the user has not read it yet
    When the user opens the book to page 10
    Then progress should be saved as "READING"
    And current page should be 10
    And started_at timestamp should be set
    
Scenario: User resumes reading on different device
    Given the user was reading "Book A" on page 50
    When the user opens "Book A" on a different device
    Then the reader should open to page 50
    And show "Resume from page 50" option
```

## Estimated Effort
**Total: 2 weeks (1 developer)**
