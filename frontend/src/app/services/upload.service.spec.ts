import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpEvent, HttpEventType, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { UploadService } from './upload.service';
import { UploadConfig, UploadResult, ValidationResult, UploadProgress } from '../models/upload.model';
import { environment } from '../../environments/environment';

describe('UploadService', () => {
  let service: UploadService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/api/upload`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UploadService]
    });
    service = TestBed.inject(UploadService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getUploadConfig', () => {
    it('should get upload configuration', () => {
      const mockConfig: UploadConfig = {
        maxFileSize: 104857600,
        allowedExtensions: ['epub', 'pdf', 'cbz', 'cbr']
      };

      service.getUploadConfig().subscribe(config => {
        expect(config).toEqual(mockConfig);
      });

      const req = httpMock.expectOne(`${baseUrl}/config`);
      expect(req.request.method).toBe('GET');
      req.flush(mockConfig);
    });
  });

  describe('uploadBook', () => {
    it('should upload a book file', () => {
      const mockFile = new File(['test content'], 'test.epub', { type: 'application/epub+zip' });
      const mockResult: UploadResult = {
        status: 'SUCCESS',
        message: 'Book uploaded successfully',
        bookId: 'book-123',
        fileName: 'test.epub',
        fileSize: 1024,
        fileHash: 'abc123'
      };

      service.uploadBook(mockFile).subscribe(result => {
        expect(result).toEqual(mockResult);
      });

      const req = httpMock.expectOne(`${baseUrl}/book`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeInstanceOf(FormData);
      req.flush(mockResult);
    });
  });

  describe('uploadBookWithProgress', () => {
    it('should upload book with progress tracking', () => {
      const mockFile = new File(['test content'], 'test.epub', { type: 'application/epub+zip' });
      const mockResult: UploadResult = {
        status: 'SUCCESS',
        message: 'Book uploaded successfully',
        bookId: 'book-123'
      };

      const progressEvents: (UploadProgress | UploadResult)[] = [];

      service.uploadBookWithProgress(mockFile).subscribe(event => {
        progressEvents.push(event);
      });

      const req = httpMock.expectOne(`${baseUrl}/book`);
      expect(req.request.method).toBe('POST');
      expect(req.request.reportProgress).toBe(true);

      // Simulate progress event
      const progressEvent: HttpProgressEvent = {
        type: HttpEventType.UploadProgress,
        loaded: 50,
        total: 100
      };
      req.event(progressEvent);

      // Simulate completion
      const responseEvent: HttpResponse<UploadResult> = new HttpResponse({
        body: mockResult,
        status: 200
      });
      req.event(responseEvent);

      expect(progressEvents.length).toBe(2);
      expect(progressEvents[0]).toEqual({
        loaded: 50,
        total: 100,
        percentage: 50
      });
      expect(progressEvents[1]).toEqual(mockResult);
    });

    it('should handle progress without total size', () => {
      const mockFile = new File(['test content'], 'test.epub', { type: 'application/epub+zip' });

      let progressReceived = false;
      service.uploadBookWithProgress(mockFile).subscribe(event => {
        if ('percentage' in event) {
          progressReceived = true;
          expect(event.percentage).toBe(0); // Should be 0 when no total size
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/book`);

      // Simulate progress event without total
      const progressEvent: HttpProgressEvent = {
        type: HttpEventType.UploadProgress,
        loaded: 50
      };
      req.event(progressEvent);

      // Verify we received progress and didn't crash
      req.flush({ status: 'SUCCESS', message: 'Done' });
      expect(progressReceived).toBe(true);
    });
  });

  describe('validateFile', () => {
    it('should validate a file', () => {
      const mockFile = new File(['test content'], 'test.epub', { type: 'application/epub+zip' });
      const mockValidation: ValidationResult = {
        isValid: true,
        errors: [],
        fileName: 'test.epub',
        fileSize: 1024,
        fileExtension: 'epub'
      };

      service.validateFile(mockFile).subscribe(result => {
        expect(result).toEqual(mockValidation);
      });

      const req = httpMock.expectOne(`${baseUrl}/validate`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toBeInstanceOf(FormData);
      req.flush(mockValidation);
    });
  });

  describe('isFileExtensionAllowed', () => {
    it('should return true for allowed extensions', () => {
      const allowedExtensions = ['epub', 'pdf', 'cbz'];
      
      expect(service.isFileExtensionAllowed('book.epub', allowedExtensions)).toBe(true);
      expect(service.isFileExtensionAllowed('book.EPUB', allowedExtensions)).toBe(true);
      expect(service.isFileExtensionAllowed('document.pdf', allowedExtensions)).toBe(true);
    });

    it('should return false for disallowed extensions', () => {
      const allowedExtensions = ['epub', 'pdf', 'cbz'];
      
      expect(service.isFileExtensionAllowed('book.txt', allowedExtensions)).toBe(false);
      expect(service.isFileExtensionAllowed('book.docx', allowedExtensions)).toBe(false);
    });

    it('should return false for files without extensions', () => {
      const allowedExtensions = ['epub', 'pdf', 'cbz'];
      
      expect(service.isFileExtensionAllowed('book', allowedExtensions)).toBe(false);
    });
  });

  describe('isFileSizeAllowed', () => {
    it('should return true for files within size limit', () => {
      const maxSize = 1024 * 1024; // 1MB
      
      expect(service.isFileSizeAllowed(500 * 1024, maxSize)).toBe(true);
      expect(service.isFileSizeAllowed(maxSize, maxSize)).toBe(true);
    });

    it('should return false for files exceeding size limit', () => {
      const maxSize = 1024 * 1024; // 1MB
      
      expect(service.isFileSizeAllowed(2 * 1024 * 1024, maxSize)).toBe(false);
    });
  });

  describe('formatFileSize', () => {
    it('should format file sizes correctly', () => {
      expect(service.formatFileSize(0)).toBe('0 Bytes');
      expect(service.formatFileSize(1024)).toBe('1 KB');
      expect(service.formatFileSize(1024 * 1024)).toBe('1 MB');
      expect(service.formatFileSize(1536)).toBe('1.5 KB');
      expect(service.formatFileSize(1024 * 1024 * 1024)).toBe('1 GB');
    });
  });
});