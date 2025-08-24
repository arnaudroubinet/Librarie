import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of, throwError, BehaviorSubject } from 'rxjs';
import { MetadataEditorComponent } from './metadata-editor.component';
import { MetadataService } from '../services/metadata.service';
import { BookService } from '../services/book.service';
import { Book } from '../models/book.model';
import { BookMetadata, ProviderStatus, MetadataApplyRequest } from '../models/metadata.model';

describe('MetadataEditorComponent', () => {
  let component: MetadataEditorComponent;
  let fixture: ComponentFixture<MetadataEditorComponent>;
  let metadataServiceSpy: jasmine.SpyObj<MetadataService>;
  let bookServiceSpy: jasmine.SpyObj<BookService>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;
  let route: ActivatedRoute;

  const mockBook: Book = {
    id: 'book-123',
    title: 'Test Book',
    isbn13: '9780123456789',
    description: 'Test description',
    language: 'en-US',
    publicationDate: '2023-01-01',
    publisher: 'Test Publisher',
    pageCount: 300,
    fileSize: 1024000,
    path: '/path/to/book.epub',
    coverUrl: 'http://example.com/cover.jpg',
    createdAt: '2023-01-01T00:00:00Z'
  };

  const mockMetadata: BookMetadata = {
    title: 'Updated Test Book',
    authors: [{ name: 'Updated Author' }],
    isbn13: '9780123456789',
    description: 'Updated description',
    language: 'en-US',
    publishedDate: '2023-02-01',
    publisher: 'Updated Publisher',
    pages: 350,
    confidence: 0.95,
    provider: 'GoogleBooks'
  };

  const mockProviderStatus: ProviderStatus[] = [
    {
      providerId: 'google-books',
      name: 'Google Books',
      isAvailable: true,
      lastChecked: '2025-08-24T12:00:00Z',
      responseTime: 150
    }
  ];

  beforeEach(async () => {
    const metadataSpy = jasmine.createSpyObj('MetadataService', [
      'searchByIsbn',
      'searchByTitle',
      'previewMetadataChanges',
      'applyMetadata',
      'getProviderStatus',
      'testProvider'
    ]);
    const bookSpy = jasmine.createSpyObj('BookService', ['getBook', 'getBookDetails', 'clearCache']);
    const snackBarSpyObj = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [MetadataEditorComponent, NoopAnimationsModule],
      providers: [
        { provide: MetadataService, useValue: metadataSpy },
        { provide: BookService, useValue: bookSpy },
        { provide: MatSnackBar, useValue: snackBarSpyObj },
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ id: 'book-123' })
          }
        }
      ]
    }).compileComponents();

    metadataServiceSpy = TestBed.inject(MetadataService) as jasmine.SpyObj<MetadataService>;
    bookServiceSpy = TestBed.inject(BookService) as jasmine.SpyObj<BookService>;
    snackBarSpy = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;
    route = TestBed.inject(ActivatedRoute);

    // Set up default spy behaviors
    bookServiceSpy.getBookDetails.and.returnValue(of(mockBook));
    metadataServiceSpy.getProviderStatus.and.returnValue(of(mockProviderStatus));
    metadataServiceSpy.searchByIsbn.and.returnValue(of([mockMetadata]));
    metadataServiceSpy.searchByTitle.and.returnValue(of([mockMetadata]));
    snackBarSpy.open.and.returnValue({} as any);

    fixture = TestBed.createComponent(MetadataEditorComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load book and provider status on init', () => {
    bookServiceSpy.getBookDetails = jasmine.createSpy().and.returnValue(of(mockBook));
    metadataServiceSpy.getProviderStatus.and.returnValue(of(mockProviderStatus));
    
    fixture.detectChanges();
    
    expect(bookServiceSpy.getBookDetails).toHaveBeenCalledWith('book-123');
    expect(component.book()).toEqual(mockBook);
  });

  it('should handle book loading error', () => {
    bookServiceSpy.getBookDetails.and.returnValue(throwError(() => new Error('Book not found')));
    
    const component2 = new MetadataEditorComponent(
      route, 
      metadataServiceSpy, 
      bookServiceSpy, 
      snackBarSpy
    );
    
    // Simulate route change to trigger book loading
    const paramsSubject = new BehaviorSubject({ id: 'book-123' });
    (route as any).params = paramsSubject.asObservable();
    
    component2.ngOnInit();
    
    expect(snackBarSpy.open).toHaveBeenCalledWith('Failed to load book details', 'Close', { duration: 3000 });
  });

  it('should search metadata by ISBN', () => {
    component.book.set(mockBook);
    component.searchIsbn = '9780123456789';
    
    component.searchByIsbn();
    
    expect(metadataServiceSpy.searchByIsbn).toHaveBeenCalledWith('9780123456789');
    expect(component.isSearching()).toBe(false);
    expect(component.searchResults()).toEqual([mockMetadata]);
  });

  it('should search metadata by title and author', () => {
    component.book.set(mockBook);
    component.searchTitle = 'Test Book';
    component.searchAuthor = 'Test Author';
    
    component.searchByTitle();
    
    expect(metadataServiceSpy.searchByTitle).toHaveBeenCalledWith('Test Book', 'Test Author');
    expect(component.isSearching()).toBe(false);
    expect(component.searchResults()).toEqual([mockMetadata]);
  });

  it('should handle search errors', () => {
    metadataServiceSpy.searchByIsbn.and.returnValue(throwError(() => new Error('Search failed')));
    
    component.searchIsbn = '9780123456789';
    component.searchByIsbn();
    
    expect(component.isSearching()).toBe(false);
    expect(snackBarSpy.open).toHaveBeenCalledWith('Search failed', 'Close', { duration: 3000 });
  });

  it('should apply metadata from search results', () => {
    metadataServiceSpy.applyMetadata.and.returnValue(of(void 0));
    
    component.bookId.set('book-123');
    component.book.set(mockBook);
    
    component.applyMetadata(mockMetadata);
    
    expect(metadataServiceSpy.applyMetadata).toHaveBeenCalledWith('book-123', {
      metadata: mockMetadata,
      overwriteExisting: false
    });
    expect(bookServiceSpy.clearCache).toHaveBeenCalled();
    expect(snackBarSpy.open).toHaveBeenCalledWith('Metadata updated successfully', 'Close', { duration: 3000 });
  });

  it('should clear preview', () => {
    component.preview.set({
      bookId: 'book-123',
      currentMetadata: mockBook,
      proposedMetadata: mockMetadata,
      changes: [],
      overwriteExisting: false
    });
    
    component.clearPreview();
    
    expect(component.preview()).toBeNull();
  });

  describe('basic functionality', () => {
    it('should handle book properties', () => {
      const bookWithTitle: Book = { ...mockBook, title: 'Test Book' };
      const bookWithoutTitle: Book = { ...mockBook, title: '' };
      
      component.book.set(bookWithTitle);
      expect(component.book()?.title).toBe('Test Book');
      
      component.book.set(bookWithoutTitle);
      expect(component.book()?.title).toBe('');
    });

    it('should handle ISBN properties', () => {
      const bookWithIsbn: Book = { ...mockBook, isbn13: '9780123456789' };
      const bookWithoutIsbn: Book = { ...mockBook, isbn13: undefined };
      
      component.book.set(bookWithIsbn);
      expect(component.book()?.isbn13).toBe('9780123456789');
      
      component.book.set(bookWithoutIsbn);
      expect(component.book()?.isbn13).toBeUndefined();
    });
  });

  describe('component state', () => {
    it('should manage search results', () => {
      component.searchResults.set([mockMetadata]);
      expect(component.searchResults().length).toBe(1);
      
      component.searchResults.set([]);
      expect(component.searchResults().length).toBe(0);
    });

    it('should manage manual metadata editing', () => {
      component.manualMetadata = { title: 'New Title' };
      expect(component.manualMetadata.title).toBe('New Title');
    });

    it('should manage overwrite settings', () => {
      component.overwriteExisting = true;
      expect(component.overwriteExisting).toBe(true);
      
      component.overwriteExisting = false;
      expect(component.overwriteExisting).toBe(false);
    });
  });
});