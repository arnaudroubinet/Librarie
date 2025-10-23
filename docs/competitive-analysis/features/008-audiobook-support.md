# Feature: Complete Audiobook Support

## Priority: CRITICAL (Tier 1 - Differentiator)
**ROI Score: 94/100** (Very High Impact, High Effort)

**Found in:** Audiobookshelf, Booksonic-Air  
**Competitive Advantage:** Unified ebook + audiobook management

## Why Implement This Feature

### User Value
- **Single Library**: Ebooks and audiobooks in one place
- **Chapter Navigation**: Jump to specific chapters
- **Playback Controls**: Speed, sleep timer, bookmarks
- **Progress Sync**: Resume on any device
- **Offline Mode**: Download for offline listening

### Business Value
- **Market Differentiation**: No competitor excels at both ebooks AND audiobooks
- **Audience Expansion**: Audiobook listeners are growing 20% YoY
- **Sticky Feature**: Audio users consume 3-5x more content
- **Competitive Moat**: Technical complexity creates barrier

### Technical Value
- **Format Complexity**: M4B, MP3, AAC, FLAC support required
- **Transcoding**: Convert formats for compatibility
- **Chapter Metadata**: Parse and display chapter markers

## Implementation Strategy

### Technology Stack

**Audio Processing:**
```xml
<dependency>
    <groupId>org.jaudiotagger</groupId>
    <artifactId>jaudiotagger</artifactId>
    <version>3.0.1</version>
</dependency>
```

**FFmpeg Integration (for transcoding):**
```java
@ApplicationScoped
public class AudioTranscoder {
    
    @Inject
    @ConfigProperty(name = "ffmpeg.path")
    String ffmpegPath;
    
    public Path transcode(Path input, AudioFormat targetFormat) {
        ProcessBuilder pb = new ProcessBuilder(
            ffmpegPath,
            "-i", input.toString(),
            "-codec:a", targetFormat.getCodec(),
            "-b:a", targetFormat.getBitrate(),
            outputPath.toString()
        );
        
        Process process = pb.start();
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            throw new TranscodingException("FFmpeg failed");
        }
        
        return outputPath;
    }
}
```

### Data Model
```java
@Entity
@Table(name = "audiobooks")
public class Audiobook extends Book {
    
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    @Column(name = "audio_format")
    private String audioFormat; // M4B, MP3, etc.
    
    @Column(name = "bitrate")
    private Integer bitrate;
    
    @Column(name = "sample_rate")
    private Integer sampleRate;
    
    @Column(name = "narrator")
    private String narrator;
    
    @OneToMany(mappedBy = "audiobook", cascade = CascadeType.ALL)
    private List<AudiobookChapter> chapters;
    
    @Column(name = "has_chapters")
    private Boolean hasChapters;
}

@Entity
@Table(name = "audiobook_chapters")
public class AudiobookChapter {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Audiobook audiobook;
    
    private String title;
    private Integer chapterNumber;
    private Long startTimeMs;
    private Long endTimeMs;
    private Long durationMs;
}

@Entity
@Table(name = "audiobook_playback_sessions")
public class AudiobookPlaybackSession {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User user;
    
    @ManyToOne
    private Audiobook audiobook;
    
    private Long currentPositionMs;
    private Float playbackSpeed;
    private LocalDateTime lastPlayedAt;
    private Boolean completed;
}
```

### REST API
```java
@Path("/api/audiobooks")
public class AudiobookResource {
    
    @GET
    @Path("/{id}/stream")
    @Produces("audio/mpeg")
    public Response streamAudio(@PathParam("id") Long id,
                               @HeaderParam("Range") String range) {
        // Implement range requests for seeking
        Audiobook audiobook = audiobookService.findById(id);
        
        if (range != null) {
            return buildRangeResponse(audiobook, range);
        }
        
        return Response.ok(audiobook.getAudioStream())
            .header("Accept-Ranges", "bytes")
            .header("Content-Length", audiobook.getFileSizeBytes())
            .build();
    }
    
    @POST
    @Path("/{id}/transcode")
    public Response requestTranscode(@PathParam("id") Long id,
                                    @QueryParam("format") String format) {
        audiobookService.queueTranscode(id, format);
        return Response.accepted().build();
    }
    
    @GET
    @Path("/{id}/chapters")
    public Response getChapters(@PathParam("id") Long id) {
        List<AudiobookChapter> chapters = audiobookService.getChapters(id);
        return Response.ok(chapters).build();
    }
    
    @POST
    @Path("/{id}/playback/position")
    public Response savePlaybackPosition(@PathParam("id") Long id,
                                        PlaybackPositionRequest request,
                                        @Context SecurityContext ctx) {
        User user = getCurrentUser(ctx);
        audiobookService.savePosition(user.getId(), id, request.getPositionMs());
        return Response.ok().build();
    }
}
```

### Chapter Parsing
```java
@ApplicationScoped
public class ChapterParser {
    
    public List<AudiobookChapter> parseChapters(Path audioFile) {
        AudioFile af = AudioFileIO.read(audioFile.toFile());
        Tag tag = af.getTag();
        
        // M4B chapter extraction
        if ("m4b".equalsIgnoreCase(getExtension(audioFile))) {
            return parseM4BChapters(tag);
        }
        
        // MP3 ID3 chapters
        if ("mp3".equalsIgnoreCase(getExtension(audioFile))) {
            return parseMP3Chapters(tag);
        }
        
        return Collections.emptyList();
    }
    
    private List<AudiobookChapter> parseM4BChapters(Tag tag) {
        // Parse MP4 chapter atoms
        List<AudiobookChapter> chapters = new ArrayList<>();
        
        String chaptersData = tag.getFirst(FieldKey.CUSTOM1); // Chapter data
        if (chaptersData != null) {
            // Parse chapter timestamps and titles
            // Format varies by tagger
        }
        
        return chapters;
    }
}
```

### Database Schema
```sql
CREATE TABLE audiobooks (
    id BIGINT PRIMARY KEY REFERENCES books(id) ON DELETE CASCADE,
    duration_seconds BIGINT,
    audio_format VARCHAR(10),
    bitrate INTEGER,
    sample_rate INTEGER,
    narrator VARCHAR(255),
    has_chapters BOOLEAN DEFAULT FALSE
);

CREATE TABLE audiobook_chapters (
    id BIGSERIAL PRIMARY KEY,
    audiobook_id BIGINT REFERENCES audiobooks(id) ON DELETE CASCADE,
    title VARCHAR(500),
    chapter_number INTEGER,
    start_time_ms BIGINT,
    end_time_ms BIGINT,
    duration_ms BIGINT
);

CREATE TABLE audiobook_playback_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    audiobook_id BIGINT REFERENCES audiobooks(id) ON DELETE CASCADE,
    current_position_ms BIGINT DEFAULT 0,
    playback_speed FLOAT DEFAULT 1.0,
    last_played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed BOOLEAN DEFAULT FALSE,
    UNIQUE(user_id, audiobook_id)
);

CREATE INDEX idx_audiobook_chapters_book ON audiobook_chapters(audiobook_id);
CREATE INDEX idx_audiobook_playback_user ON audiobook_playback_sessions(user_id);
```

## Implementation Phases

### Phase 1: Core Audio Support (Week 1-2)
- [ ] Audiobook entity and database schema
- [ ] File upload and metadata extraction
- [ ] Basic streaming endpoint
- [ ] Duration calculation

### Phase 2: Chapter Support (Week 3)
- [ ] Chapter parsing (M4B, MP3)
- [ ] Chapter API endpoints
- [ ] Chapter navigation UI

### Phase 3: Playback Features (Week 4-5)
- [ ] Playback position tracking
- [ ] Speed control
- [ ] Sleep timer
- [ ] Bookmarks

### Phase 4: Transcoding (Week 6-8)
- [ ] FFmpeg integration
- [ ] Format conversion queue
- [ ] Quality presets
- [ ] Progress tracking

## Acceptance Tests

```gherkin
Feature: Audiobook Playback

Scenario: User uploads M4B audiobook
    Given user has an M4B audiobook file
    When user uploads the file
    Then metadata should be extracted
    And chapters should be parsed
    And duration should be calculated
    And cover art should be extracted
    
Scenario: User plays audiobook with chapters
    Given an audiobook with chapters exists
    When user starts playback
    Then audio should stream correctly
    When user clicks chapter 3
    Then playback should jump to chapter 3 start
    
Scenario: User resumes audiobook on different device
    Given user was listening on Device A at 45:30
    When user opens same audiobook on Device B
    Then playback should offer to resume at 45:30
```

### Performance Tests
```java
@Test
public void testAudioStreamingPerformance() {
    // Test streaming 100MB file
    long start = System.currentTimeMillis();
    
    given()
        .when()
        .get("/api/audiobooks/1/stream")
    .then()
        .statusCode(200)
        .time(lessThan(2000L), TimeUnit.MILLISECONDS);
}

@Test
public void testChapterParsingSpeed() {
    Path audioFile = createTestM4BFile();
    
    long start = System.currentTimeMillis();
    List<AudiobookChapter> chapters = chapterParser.parseChapters(audioFile);
    long duration = System.currentTimeMillis() - start;
    
    assertTrue(duration < 500); // Should parse in <500ms
    assertTrue(chapters.size() > 0);
}
```

## Success Metrics

### User Metrics
- 40%+ of users add at least one audiobook
- Average listening time 5+ hours/week
- Audiobook completion rate 60%+

### Technical Metrics
- Audio streaming latency < 100ms
- Chapter parsing < 1 second
- Transcode queue processing < 5 min per file

## Estimated Effort

**Total: 8-10 weeks (2 developers)**
- Backend: 6-8 weeks
- Frontend player: 4-6 weeks
- Testing & polish: 2 weeks

## Related Features
- **Depends On**: File upload, streaming infrastructure
- **Enables**: Podcast support, playlist management
- **Integrates With**: Collections, progress tracking, OPDS
