# Feature Specification: OPDS Feed Support

**Feature ID:** F003  
**Priority:** Medium  
**Effort:** Medium (3-4 weeks)  
**Competitors:** Calibre-Web, Kavita, Komga

## Overview

OPDS (Open Publication Distribution System) catalog implementation for integration with e-reader apps like Kindle, Kobo, Moon+ Reader, etc.

## Why It Matters

- Industry standard for ebook distribution
- Enables integration with 100+ e-reader apps
- No need to build native apps for basic functionality
- Users can use their preferred reading app

## User Stories

### US-001: Add Library to E-Reader
**As a** user  
**I want** to add my Librarie library to my e-reader app  
**So that** I can browse and download books natively

**Acceptance:**
- Generate OPDS catalog URL
- E-reader app can discover library
- Browse books by category, author, series
- Download books directly to e-reader

### US-002: Authentication with OPDS
**As a** user  
**I want** secure access to my OPDS feed  
**So that** only I can access my books

**Acceptance:**
- Basic authentication support
- Token-based authentication
- Per-user OPDS URLs with embedded auth

## Architecture

### OPDS Versions

Support both OPDS 1.2 (Atom) and OPDS 2.0 (JSON):

- **OPDS 1.2**: Widely supported, XML/Atom format
- **OPDS 2.0**: Modern JSON format, better for web

### Feed Structure

```
/opds/
  catalog.xml         # Root catalog
  /new                # Recently added
  /popular            # Most downloaded
  /authors            # Browse by author
    /{id}             # Author's books
  /series             # Browse by series
    /{id}             # Series books
  /search?q={query}   # Search
  /book/{id}          # Book acquisition
```

## Data Model

No new entities needed. Expose existing books via OPDS format.

## API Endpoints

```
GET /opds/catalog.xml
Response: OPDS root catalog

<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns="http://www.w3.org/2005/Atom"
      xmlns:opds="http://opds-spec.org/2010/catalog">
  <id>urn:uuid:librarie-root</id>
  <title>Librarie Library</title>
  <updated>2025-10-18T12:00:00Z</updated>
  
  <entry>
    <title>Recently Added</title>
    <link rel="subsection" 
          href="/opds/new" 
          type="application/atom+xml;profile=opds-catalog"/>
  </entry>
  
  <entry>
    <title>Browse by Author</title>
    <link rel="subsection" 
          href="/opds/authors" 
          type="application/atom+xml;profile=opds-catalog"/>
  </entry>
</feed>

---

GET /opds/book/{id}
Response: Book entry with acquisition links

<entry>
  <title>The Great Gatsby</title>
  <id>urn:uuid:book-123</id>
  <author><name>F. Scott Fitzgerald</name></author>
  <published>1925-04-10</published>
  <summary>A classic American novel...</summary>
  
  <link rel="http://opds-spec.org/image" 
        href="/api/v1/books/123/cover" 
        type="image/jpeg"/>
  
  <link rel="http://opds-spec.org/acquisition" 
        href="/api/v1/books/123/download?format=epub" 
        type="application/epub+zip"/>
</entry>

---

GET /opds/search?q={query}
Response: Search results in OPDS format
```

### Authentication

```
# Option 1: Basic Auth (widely supported)
Authorization: Basic base64(username:password)

# Option 2: Token-based (more secure)
GET /opds/catalog.xml?token=<user-specific-token>

# Option 3: OAuth (for advanced clients)
Authorization: Bearer <oauth-token>
```

## Implementation Tasks

### Phase 1: OPDS 1.2 (2 weeks)

1. **OPDS Resource Class** (2 days)
   - Create OPDSCatalogResource
   - Implement root catalog
   - Navigation feeds (new, popular, authors, series)

2. **Book Acquisition** (2 days)
   - Book entry generation
   - Download links with format selection
   - Cover art integration

3. **Search** (2 days)
   - OpenSearch description document
   - Search results in OPDS format
   - Faceted search support

4. **Authentication** (1 day)
   - Basic auth integration
   - Token generation endpoint
   - Security annotations

5. **Testing** (3 days)
   - Test with popular OPDS readers
   - Validation against OPDS spec
   - Performance testing

### Phase 2: OPDS 2.0 (1 week)

6. **JSON Feeds** (3 days)
   - Parallel JSON endpoints
   - Content negotiation (Accept header)
   - JSON schema validation

7. **Documentation** (2 days)
   - User guide for adding to readers
   - API documentation
   - Example URLs

## Test Plan

### Manual Testing with OPDS Clients

Test with popular e-reader apps:
- **iOS**: KyBook, Marvin, Chunky Reader
- **Android**: Moon+ Reader, FBReader, ReadEra
- **Desktop**: Calibre
- **Web**: Aldiko Online

### Automated Tests

```java
@Test
void shouldGenerateValidOPDSRootCatalog() {
    given()
        .auth().basic("user", "password")
    .when()
        .get("/opds/catalog.xml")
    .then()
        .statusCode(200)
        .contentType("application/atom+xml;profile=opds-catalog")
        .body("feed.title", equalTo("Librarie Library"));
}

@Test
void shouldReturnBookAcquisitionLinks() {
    given()
        .auth().basic("user", "password")
    .when()
        .get("/opds/book/{id}", testBookId)
    .then()
        .statusCode(200)
        .body("entry.link.find { it.@rel == 'http://opds-spec.org/acquisition' }", notNullValue());
}

@Test
void shouldSupportContentNegotiation() {
    given()
        .header("Accept", "application/opds+json")
    .when()
        .get("/opds/catalog")
    .then()
        .contentType(containsString("application/opds+json"));
}
```

### OPDS Validator

Use online validator: https://opds-validator.herokuapp.com/

## Dependencies

- **JAXB** (for XML generation) - already in Quarkus
- **Jackson** (for JSON) - already in project
- Optional: **Apache Abdera** (OPDS-specific library)

## Success Metrics

- **Integration**: 80% of users successfully add OPDS to their e-reader
- **Usage**: 25% of book downloads via OPDS clients
- **Compatibility**: Works with top 10 OPDS readers
- **Performance**: Catalog load time < 500ms

## Rollout

1. **Alpha**: Document OPDS URL format, test internally
2. **Beta**: Share with power users, gather compatibility feedback
3. **GA**: Announce in documentation, add setup wizard

## Future Enhancements

- OPDS Page Streaming Extension (for comics)
- OPDS Subscriptions (for series)
- OPDS Shelf Extension (reading lists)
- Advanced faceted search

---
