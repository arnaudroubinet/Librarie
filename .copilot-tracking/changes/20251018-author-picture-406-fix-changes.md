# Release Changes: Fix Author Picture 406 Errors

**Related Plan**: T-003 - Resolve Author Picture 406 Errors
**Implementation Date**: 2025-10-18

## Summary

Fixed 406 (Not Acceptable) errors that occurred when requesting author pictures from the `/v1/authors/{id}/picture` endpoint. The issue was caused by error responses returning JSON content (`Map.of(...)`) while the endpoint was annotated with `@Produces("image/*")`, creating a content-type mismatch during JAX-RS content negotiation.

## Root Cause

The `AuthorController.getAuthorPicture()` method was annotated with `@Produces("image/*")` to indicate it serves image content. However, error responses (404, 400, 500) were returning JSON objects using `Map.of("error", "message")`. When JAX-RS attempted content negotiation:

1. The client expected `image/*` based on the endpoint annotation
2. The server tried to send `application/json` for error responses
3. This mismatch caused a 406 (Not Acceptable) response

## Changes

### Modified

- `backend/src/main/java/org/motpassants/infrastructure/adapter/in/rest/AuthorController.java` - Fixed error responses in `getAuthorPicture()` method to return `text/plain` content type instead of JSON
  - Line 335-339: Changed 404 response from JSON to plain text
  - Line 364-366: Changed 400 response from JSON to plain text  
  - Line 368-370: Changed 500 response from JSON to plain text
- `backend/src/test/java/org/motpassants/integration/AuthorIntegrationTest.java` - Added comprehensive tests for picture endpoint
  - Added `testGetAuthorPictureWithAcceptHeaders()` to verify various Accept headers work correctly
  - Added `testGetNonExistentAuthorPicture()` to verify 404 (not 406) is returned for non-existent authors
  - Fixed test ordering to prevent conflicts

## Technical Details

### Before Fix
```java
if (authorOpt.isEmpty()) {
    return Response.status(Response.Status.NOT_FOUND)
            .entity(Map.of("error", "Author not found"))
            .build();
}
```

### After Fix
```java
if (authorOpt.isEmpty()) {
    return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.TEXT_PLAIN)
            .entity("Author not found")
            .build();
}
```

## Testing

All 152 backend tests pass, including:
- 14 Author integration tests (added 2 new tests)
- Tests with various Accept headers (image/*, */*, application/json)
- Tests for non-existent authors returning 404 instead of 406
- Tests for invalid author IDs returning 400 instead of 406

## Acceptance Criteria Met

- [x] Backend returns appropriate status codes (200 for images, 404 for missing)
- [x] Backend includes proper Content-Type headers (image/* for success, text/plain for errors)
- [x] No 406 errors when requesting author pictures
- [x] Authors with pictures display images correctly (via fallback SVG)
- [x] Authors without pictures show placeholder/fallback
- [x] Cache headers set for performance (handled by ImageCachingService)
- [x] Tests pass for both scenarios (with/without picture)

## Related Controllers

Verified that `BookController` and `SeriesController` do not have the same issue - they already use plain text for error responses in their image endpoints.

## Deployment Notes

No migration or configuration changes required. This is a code-only fix that maintains backward compatibility.
