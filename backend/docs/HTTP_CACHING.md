# HTTP Caching Implementation

## Overview

This implementation adds comprehensive HTTP caching for all static assets in the Librarie application, including book covers, author pictures, and EPUB resources. The caching strategy uses industry-standard HTTP headers to reduce bandwidth usage by over 90% for repeated requests.

## Features Implemented

### 1. Cache-Control Headers
- **Header Value**: `public, max-age=86400`
- **Duration**: 1 day (86400 seconds)
- **Scope**: All static assets (images, SVG fallbacks, EPUB resources)
- **Benefits**: Allows browser and proxy caching, significantly reducing server load

### 2. ETag Support
- **Type**: Strong ETags using SHA-256 hashing
- **Purpose**: Enables conditional requests and cache validation
- **Response**: Returns `304 Not Modified` when content hasn't changed
- **Bandwidth Savings**: >90% reduction for unchanged resources

### 3. Last-Modified Support
- **Source**: File modification timestamps
- **Purpose**: Fallback caching mechanism
- **Headers**: Works with `If-Modified-Since` requests
- **Response**: Returns `304 Not Modified` for unchanged files

### 4. Vary Header
- **Value**: `Accept-Encoding`
- **Purpose**: Ensures correct cache key management
- **Benefit**: Prevents serving wrong content encoding

## Endpoints with HTTP Caching

1. **Book Covers**: `GET /v1/books/{id}/cover`
2. **Author Pictures**: `GET /v1/authors/{id}/picture`
3. **EPUB Resources**: `GET /v1/books/{id}/resources/{path}`

## Testing

Run caching tests:
```bash
./mvnw test -Dtest=HttpCachingIntegrationTest
```

## Measuring Bandwidth Reduction

### Using Chrome DevTools

1. Open Network tab in DevTools
2. Disable cache to see initial load
3. Note Size column (e.g., "125 KB")
4. Enable cache and reload
5. Size now shows "(from cache)" with minimal transfer

### Using curl

Initial request:
```bash
curl -I http://localhost:8080/v1/books/{id}/cover
```

Cached request:
```bash
curl -I -H 'If-None-Match: "ETAG_VALUE"' http://localhost:8080/v1/books/{id}/cover
```

Expected result: `304 Not Modified` with no body transfer

## Configuration

Cache duration is set in `ImageCachingService.java`:
```java
private static final int CACHE_MAX_AGE_SECONDS = 86400; // 1 day
```

## References

- [HTTP Caching (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Caching)
- [RFC 7232 - Conditional Requests](https://tools.ietf.org/html/rfc7232)
