# Metadata Search Integration Guide

This guide shows how to integrate the metadata search feature into the book detail view.

## Overview

The metadata search feature allows users to:
1. Search for book metadata from Google Books and Open Library
2. Preview multiple results with confidence scores
3. Select and apply metadata to update the book

## Components

### MetadataService (`src/app/services/metadata.service.ts`)
Service for fetching metadata from the backend API.

**Methods:**
- `searchByIsbn(isbn: string): Observable<MetadataSearchResult[]>`
- `searchByTitleAndAuthor(title: string, author?: string): Observable<MetadataSearchResult[]>`

### MetadataSearchComponent (`src/app/components/metadata-search.component.ts`)
Standalone component for searching and selecting metadata.

**Inputs:**
- `bookIsbn?: string` - Pre-fill ISBN search
- `bookTitle?: string` - Pre-fill title search

**Outputs:**
- `metadataSelected: EventEmitter<MetadataSearchResult>` - Emitted when user applies metadata
- `cancelled: EventEmitter<void>` - Emitted when user cancels

## Integration with Book Detail Component

### Step 1: Add Metadata Search Button

Add a button to trigger metadata search in the book detail template:

```typescript
<!-- In book-detail.component.ts template -->
<div class="book-actions">
  <button mat-raised-button color="primary" (click)="showMetadataSearch = true">
    <iconify-icon icon="material-symbols:search"></iconify-icon>
    Fetch Metadata
  </button>
</div>
```

### Step 2: Add Metadata Search Component

Add the metadata search component with a dialog or expandable section:

```typescript
<!-- Metadata Search Section -->
@if (showMetadataSearch) {
  <div class="metadata-search-section">
    <app-metadata-search
      [bookIsbn]="book()?.isbn"
      [bookTitle]="book()?.title"
      (metadataSelected)="applyMetadata($event)"
      (cancelled)="showMetadataSearch = false">
    </app-metadata-search>
  </div>
}
```

### Step 3: Import Required Dependencies

Update the component imports:

```typescript
import { MetadataSearchComponent } from './metadata-search.component';
import { MetadataSearchResult } from '../services/metadata.service';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MetadataSearchComponent, // Add this
    ...MATERIAL_MODULES
  ],
  // ... rest of component
})
```

### Step 4: Add Component Logic

Add the following to your book-detail.component.ts:

```typescript
export class BookDetailComponent implements OnInit {
  // ... existing properties ...
  
  showMetadataSearch = false;

  /**
   * Apply selected metadata to the book
   */
  applyMetadata(metadata: MetadataSearchResult) {
    const bookId = this.book()?.id;
    if (!bookId) return;

    // Build update request from metadata
    const updateRequest = {
      title: metadata.title,
      description: metadata.description,
      isbn: metadata.isbn13 || metadata.isbn10,
      pageCount: metadata.pageCount,
      publicationDate: metadata.publishedDate,
      publicationYear: metadata.publishedDate ? 
        new Date(metadata.publishedDate).getFullYear() : undefined,
      language: metadata.language,
      metadata: {
        subtitle: metadata.subtitle,
        categories: metadata.categories,
        average_rating: metadata.averageRating,
        ratings_count: metadata.ratingsCount,
        metadata_source: metadata.source,
        provider_book_id: metadata.providerBookId,
        confidence_score: metadata.confidenceScore
      }
    };

    // Update book using existing BookService
    this.bookService.updateBook(bookId, updateRequest).subscribe({
      next: (updatedBook) => {
        this.book.set(updatedBook);
        this.showMetadataSearch = false;
        this.snackBar.open('Metadata applied successfully', 'Close', {
          duration: 3000
        });
      },
      error: (err) => {
        console.error('Error applying metadata:', err);
        this.snackBar.open('Failed to apply metadata', 'Close', {
          duration: 3000
        });
      }
    });
  }
}
```

### Step 5: Add Styling

Add CSS for the metadata search section:

```css
.metadata-search-section {
  margin: 20px 0;
  padding: 20px;
  background: var(--surface-color);
  border-radius: 8px;
  border: 1px solid var(--border-color);
}

.book-actions {
  display: flex;
  gap: 10px;
  margin: 20px 0;
}
```

## API Endpoints Used

The metadata service calls these backend endpoints:

- `GET /api/metadata/search/isbn/{isbn}` - Search by ISBN
- `GET /api/metadata/search/title?title=...&author=...` - Search by title/author

Book updates are performed via:
- `PUT /api/v1/books/{id}` - Update book (existing BookService)

## Cover Image Download (Future Enhancement)

To download cover images from metadata:

1. Add a checkbox in the metadata search component for "Download cover image"
2. When applying metadata, if checked:
   - Download image from `metadata.coverImageUrl`
   - Upload to book using `POST /api/v1/books/{id}/cover` (existing endpoint)

Example:

```typescript
if (downloadCover && metadata.coverImageUrl) {
  // Fetch the image
  fetch(metadata.coverImageUrl)
    .then(res => res.blob())
    .then(blob => {
      const formData = new FormData();
      formData.append('file', blob, 'cover.jpg');
      
      // Upload using existing cover upload endpoint
      return this.bookService.uploadCover(bookId, formData);
    })
    .then(() => {
      this.snackBar.open('Cover image downloaded', 'Close', { duration: 3000 });
    })
    .catch(err => {
      console.error('Error downloading cover:', err);
    });
}
```

## Testing

To test the metadata search:

1. Navigate to a book detail page
2. Click "Fetch Metadata" button
3. Search by ISBN or title
4. Review results from multiple providers
5. Select a result and click "Apply Selected Metadata"
6. Verify the book is updated with the new metadata

## Architecture Notes

- The backend MetadataService only handles metadata fetching (no DB operations)
- The frontend handles applying metadata by calling BookService
- This maintains proper separation of concerns and hexagonal architecture
- All metadata updates go through the standard book update flow

## Future Enhancements

1. **Batch Metadata Fetch**: Add ability to fetch metadata for multiple books at once
2. **Auto-fetch on Import**: Automatically search for metadata when importing new books
3. **Metadata History**: Track which metadata sources were used for each book
4. **Custom Providers**: Allow users to add custom metadata providers
5. **Metadata Conflict Resolution**: UI for resolving conflicts when multiple sources provide different data
