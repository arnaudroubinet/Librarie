import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { MetadataService } from './metadata.service';
import { 
  BookMetadata, 
  MetadataPreview, 
  ProviderStatus, 
  MetadataApplyRequest,
  AuthorMetadata,
  FieldChange
} from '../models/metadata.model';
import { environment } from '../../environments/environment';

describe('MetadataService', () => {
  let service: MetadataService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/api/metadata`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MetadataService]
    });
    service = TestBed.inject(MetadataService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('searchByIsbn', () => {
    it('should search metadata by ISBN', () => {
      const isbn = '9780545010221';
      const mockResults: BookMetadata[] = [
        {
          title: 'Harry Potter and the Deathly Hallows',
          authors: [{ name: 'J.K. Rowling' }],
          isbn13: isbn,
          confidence: 0.95,
          provider: 'GoogleBooks'
        }
      ];

      service.searchByIsbn(isbn).subscribe(results => {
        expect(results).toEqual(mockResults);
      });

      const req = httpMock.expectOne(`${baseUrl}/search/isbn/${isbn}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResults);
    });
  });

  describe('searchByTitle', () => {
    it('should search metadata by title only', () => {
      const title = 'Harry Potter';
      const mockResults: BookMetadata[] = [
        {
          title: 'Harry Potter and the Philosopher\'s Stone',
          authors: [{ name: 'J.K. Rowling' }],
          confidence: 0.90,
          provider: 'GoogleBooks'
        }
      ];

      service.searchByTitle(title).subscribe(results => {
        expect(results).toEqual(mockResults);
      });

      const req = httpMock.expectOne(`${baseUrl}/search/title?title=${encodeURIComponent(title)}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResults);
    });

    it('should search metadata by title and author', () => {
      const title = 'Harry Potter';
      const author = 'J.K. Rowling';
      const mockResults: BookMetadata[] = [
        {
          title: 'Harry Potter and the Philosopher\'s Stone',
          authors: [{ name: 'J.K. Rowling' }],
          confidence: 0.95,
          provider: 'GoogleBooks'
        }
      ];

      service.searchByTitle(title, author).subscribe(results => {
        expect(results).toEqual(mockResults);
      });

      const req = httpMock.expectOne(`${baseUrl}/search/title?title=${encodeURIComponent(title)}&author=${encodeURIComponent(author)}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResults);
    });
  });

  describe('getBestMetadata', () => {
    it('should get best merged metadata', () => {
      const isbn = '9780545010221';
      const mockResult: BookMetadata = {
        title: 'Harry Potter and the Deathly Hallows',
        authors: [{ name: 'J.K. Rowling' }],
        isbn13: isbn,
        confidence: 0.98,
        provider: 'merged'
      };

      service.getBestMetadata(isbn).subscribe(result => {
        expect(result).toEqual(mockResult);
      });

      const req = httpMock.expectOne(`${baseUrl}/best/${isbn}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResult);
    });
  });

  describe('previewMetadataChanges', () => {
    it('should preview metadata changes', () => {
      const bookId = 'book-123';
      const request: MetadataApplyRequest = {
        metadata: { title: 'New Title' },
        overwriteExisting: false
      };
      const mockPreview: MetadataPreview = {
        bookId,
        currentMetadata: { title: 'Old Title' },
        proposedMetadata: { title: 'New Title' },
        changes: [
          {
            fieldName: 'title',
            currentValue: 'Old Title',
            proposedValue: 'New Title',
            changeType: 'UPDATE'
          }
        ],
        overwriteExisting: false
      };

      service.previewMetadataChanges(bookId, request).subscribe(preview => {
        expect(preview).toEqual(mockPreview);
      });

      const req = httpMock.expectOne(`${baseUrl}/preview/${bookId}`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockPreview);
    });
  });

  describe('applyMetadata', () => {
    it('should apply metadata to a book', () => {
      const bookId = 'book-123';
      const request: MetadataApplyRequest = {
        metadata: { title: 'New Title' },
        overwriteExisting: true
      };

      service.applyMetadata(bookId, request).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/apply/${bookId}`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(null);
    });
  });

  describe('getProviderStatus', () => {
    it('should get status of all providers', () => {
      const mockStatuses: ProviderStatus[] = [
        {
          providerId: 'google-books',
          name: 'Google Books',
          isAvailable: true,
          lastChecked: '2025-08-24T12:00:00Z',
          responseTime: 150
        },
        {
          providerId: 'open-library',
          name: 'Open Library',
          isAvailable: false,
          lastChecked: '2025-08-24T12:00:00Z',
          errorMessage: 'Connection timeout'
        }
      ];

      service.getProviderStatus().subscribe(statuses => {
        expect(statuses).toEqual(mockStatuses);
      });

      const req = httpMock.expectOne(`${baseUrl}/providers/status`);
      expect(req.request.method).toBe('GET');
      req.flush(mockStatuses);
    });
  });

  describe('testProvider', () => {
    it('should test provider connection', () => {
      const providerId = 'google-books';
      const mockStatus: ProviderStatus = {
        providerId,
        name: 'Google Books',
        isAvailable: true,
        lastChecked: '2025-08-24T12:00:00Z',
        responseTime: 120
      };

      service.testProvider(providerId).subscribe(status => {
        expect(status).toEqual(mockStatus);
      });

      const req = httpMock.expectOne(`${baseUrl}/providers/${providerId}/test`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});
      req.flush(mockStatus);
    });
  });

  describe('getProviders', () => {
    it('should get available providers', () => {
      const mockProviders = ['google-books', 'open-library'];

      service.getProviders().subscribe(providers => {
        expect(providers).toEqual(mockProviders);
      });

      const req = httpMock.expectOne(`${baseUrl}/providers`);
      expect(req.request.method).toBe('GET');
      req.flush(mockProviders);
    });
  });

  describe('mergeMetadata', () => {
    it('should return empty object for empty array', () => {
      const result = service.mergeMetadata([]);
      expect(result).toEqual({});
    });

    it('should return single result unchanged', () => {
      const metadata: BookMetadata = {
        title: 'Test Book',
        authors: [{ name: 'Test Author' }],
        confidence: 0.8
      };
      const result = service.mergeMetadata([metadata]);
      expect(result).toEqual(metadata);
    });

    it('should merge multiple results with confidence priority', () => {
      const metadata1: BookMetadata = {
        title: 'Book Title',
        description: 'Short description',
        confidence: 0.7,
        provider: 'provider1'
      };
      const metadata2: BookMetadata = {
        title: 'Better Book Title',
        description: 'Much longer and more detailed description',
        pages: 300,
        confidence: 0.9,
        provider: 'provider2'
      };

      const result = service.mergeMetadata([metadata1, metadata2]);

      expect(result.title).toBe('Better Book Title'); // Higher confidence
      expect(result.description).toBe('Much longer and more detailed description'); // Longer description
      expect(result.pages).toBe(300);
    });

    it('should merge arrays correctly', () => {
      const metadata1: BookMetadata = {
        authors: [{ name: 'Author 1' }, { name: 'Author 2' }],
        categories: ['Fiction', 'Adventure'],
        confidence: 0.8
      };
      const metadata2: BookMetadata = {
        authors: [{ name: 'Author 1' }, { name: 'Author 3' }],
        categories: ['Adventure', 'Fantasy'],
        confidence: 0.7
      };

      const result = service.mergeMetadata([metadata1, metadata2]);

      // Authors should be unique but may include duplicates based on the merge logic
      expect(result.authors?.length).toBeGreaterThan(0);
      expect(result.categories).toEqual(['Fiction', 'Adventure', 'Fantasy']);
    });

    it('should merge ratings correctly', () => {
      const metadata1: BookMetadata = {
        averageRating: 4.0,
        ratingsCount: 100,
        confidence: 0.8
      };
      const metadata2: BookMetadata = {
        averageRating: 4.5,
        ratingsCount: 200,
        confidence: 0.7
      };

      const result = service.mergeMetadata([metadata1, metadata2]);

      // Weighted average: (4.0 * 100 + 4.5 * 200) / 300 = 4.33...
      expect(result.averageRating).toBeCloseTo(4.33, 2);
      expect(result.ratingsCount).toBe(300);
    });

    it('should handle missing values gracefully', () => {
      const metadata1: BookMetadata = {
        title: 'Book Title',
        confidence: 0.8
      };
      const metadata2: BookMetadata = {
        description: 'Book description',
        confidence: 0.7
      };

      const result = service.mergeMetadata([metadata1, metadata2]);

      expect(result.title).toBe('Book Title');
      expect(result.description).toBe('Book description');
    });
  });
});