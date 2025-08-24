import { ComponentFixture, TestBed, fakeAsync, tick, flushMicrotasks } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { of, throwError, Observable } from 'rxjs';
import { UploadComponent } from './upload.component';
import { UploadService } from '../services/upload.service';
import { BookService } from '../services/book.service';
import { UploadConfig, UploadResult, UploadProgress } from '../models/upload.model';

describe('UploadComponent', () => {
  let component: UploadComponent;
  let fixture: ComponentFixture<UploadComponent>;
  let uploadServiceSpy: jasmine.SpyObj<UploadService>;
  let bookServiceSpy: jasmine.SpyObj<BookService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;

  const mockConfig: UploadConfig = {
    maxFileSize: 104857600, // 100MB
    allowedExtensions: ['epub', 'pdf', 'mobi', 'azw', 'azw3', 'fb2', 'txt', 'rtf', 'doc', 'docx']
  };

  beforeEach(async () => {
    const uploadSpy = jasmine.createSpyObj('UploadService', [
      'getUploadConfig',
      'uploadBookWithProgress',
      'isFileExtensionAllowed',
      'isFileSizeAllowed',
      'formatFileSize'
    ]);
    const bookSpy = jasmine.createSpyObj('BookService', ['clearCache']);
    const routerSpyObj = jasmine.createSpyObj('Router', ['navigate']);
    const snackBarSpyObj = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [UploadComponent, NoopAnimationsModule],
      providers: [
        { provide: UploadService, useValue: uploadSpy },
        { provide: BookService, useValue: bookSpy },
        { provide: Router, useValue: routerSpyObj },
        { provide: MatSnackBar, useValue: snackBarSpyObj }
      ]
    }).compileComponents();

    uploadServiceSpy = TestBed.inject(UploadService) as jasmine.SpyObj<UploadService>;
    bookServiceSpy = TestBed.inject(BookService) as jasmine.SpyObj<BookService>;
    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    snackBarSpy = TestBed.inject(MatSnackBar) as jasmine.SpyObj<MatSnackBar>;

    // Set up default spy behaviors
    uploadServiceSpy.getUploadConfig.and.returnValue(of(mockConfig));
    uploadServiceSpy.formatFileSize.and.callFake((bytes: number) => `${bytes} bytes`);
    uploadServiceSpy.isFileExtensionAllowed.and.returnValue(true);
    uploadServiceSpy.isFileSizeAllowed.and.returnValue(true);
    snackBarSpy.open.and.returnValue({} as any);

    fixture = TestBed.createComponent(UploadComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load upload config on init', () => {
    fixture.detectChanges();
    
    expect(uploadServiceSpy.getUploadConfig).toHaveBeenCalled();
    expect(component.config()).toEqual(mockConfig);
  });

  it('should handle config load error', () => {
    // Spy on console.error to prevent test framework failures
    spyOn(console, 'error');
    
    // Reset and recreate component with error scenario
    uploadServiceSpy.getUploadConfig.and.returnValue(throwError(() => new Error('Failed to load')));
    
    const component2 = new UploadComponent(uploadServiceSpy, bookServiceSpy, snackBarSpy, routerSpy);
    component2.ngOnInit();
    
    expect(console.error).toHaveBeenCalledWith('Failed to load upload config:', jasmine.any(Error));
    expect(snackBarSpy.open).toHaveBeenCalledWith('Failed to load upload configuration', 'Close', { duration: 3000 });
  });

  describe('drag and drop', () => {
    it('should handle drag over', () => {
      const event = new DragEvent('dragover');
      spyOn(event, 'preventDefault');
      spyOn(event, 'stopPropagation');
      
      component.onDragOver(event);
      
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(component.isDragOver()).toBe(true);
    });

    it('should handle drag leave', () => {
      component.isDragOver.set(true);
      const event = new DragEvent('dragleave');
      spyOn(event, 'preventDefault');
      spyOn(event, 'stopPropagation');
      
      component.onDragLeave(event);
      
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(component.isDragOver()).toBe(false);
    });

    it('should handle file drop', () => {
      const mockFile = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      const dataTransfer = {
        files: [mockFile]
      } as any;
      const event = new DragEvent('drop');
      Object.defineProperty(event, 'dataTransfer', { value: dataTransfer });
      spyOn(event, 'preventDefault');
      spyOn(event, 'stopPropagation');
      component.config.set(mockConfig);
      
      // Call the actual onDrop method
      component.onDrop(event);
      
      expect(event.preventDefault).toHaveBeenCalled();
      expect(event.stopPropagation).toHaveBeenCalled();
      expect(component.isDragOver()).toBe(false);
      expect(component.uploadQueue().length).toBe(1);
      expect(component.uploadQueue()[0].file.name).toBe('test.epub');
    });
  });

  describe('file selection', () => {
    it('should handle file input change', () => {
      const mockFile = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      const event = {
        target: {
          files: [mockFile],
          value: 'test-path'
        }
      };
      
      component.config.set(mockConfig);
      
      // Call the onFileSelected method as would happen on file input change
      component.onFileSelected(event);
      
      expect(event.target.value).toBe(''); // Should reset input
    });

    it('should validate files before adding to queue', () => {
      const validFile = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      const invalidExtensionFile = new File(['content'], 'test.xyz', { type: 'application/unknown' }); // Use an invalid extension
      const tooLargeFile = new File(['content'], 'large.epub', { type: 'application/epub+zip' });
      
      // Configure validation spies to use the actual service logic
      uploadServiceSpy.isFileExtensionAllowed.and.callFake((fileName: string, allowedExtensions: string[]) => {
        const extension = fileName.split('.').pop()?.toLowerCase();
        return extension ? allowedExtensions.includes(extension) : false;
      });
      uploadServiceSpy.isFileSizeAllowed.and.callFake((fileSize: number, maxFileSize: number) => {
        return fileSize <= maxFileSize;
      });
      uploadServiceSpy.formatFileSize.and.returnValue('100 MB');
      
      // Mock file sizes - make large file exceed the max size  
      Object.defineProperty(validFile, 'size', { value: 1000000 }); // 1MB - valid
      Object.defineProperty(tooLargeFile, 'size', { value: 200000000 }); // 200MB - too large
      
      component.config.set(mockConfig);
      // Call the public method that would trigger file validation
      const event = {
        target: {
          files: [validFile, invalidExtensionFile, tooLargeFile],
          value: 'test-path'
        }
      };
      
      component.onFileSelected(event);
      
      expect(component.uploadQueue().length).toBe(1);
      expect(component.uploadQueue()[0].file.name).toBe('test.epub');
      // Verify snackBar was called with validation errors
      expect(snackBarSpy.open).toHaveBeenCalledWith(
        jasmine.stringContaining('test.xyz: Invalid file type'), 
        'Close', 
        { duration: 5000 }
      );
    });

    it('should prevent duplicate files in queue', () => {
      const file1 = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      const file2 = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      
      // Ensure both files have the same size for duplicate detection
      Object.defineProperty(file1, 'size', { value: 1000000 });
      Object.defineProperty(file2, 'size', { value: 1000000 });
      
      // Set up validation spies to allow EPUB files
      uploadServiceSpy.isFileExtensionAllowed.and.returnValue(true);
      uploadServiceSpy.isFileSizeAllowed.and.returnValue(true);
      
      component.config.set(mockConfig);
      
      // Add first file
      const event1 = {
        target: {
          files: [file1],
          value: 'test-path'
        }
      };
      component.onFileSelected(event1);
      
      // Try to add duplicate file
      const event2 = {
        target: {
          files: [file2],
          value: 'test-path'
        }
      };
      component.onFileSelected(event2);
      
      expect(component.uploadQueue().length).toBe(1);
      expect(snackBarSpy.open).toHaveBeenCalledWith(
        'test.epub: Already in upload queue',
        'Close',
        { duration: 5000 }
      );
    });
  });

  describe('upload functionality', () => {
    it('should upload all pending files', fakeAsync(() => {
      const mockFile = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      const uploadResult: UploadResult = {
        status: 'SUCCESS',
        message: 'Upload successful',
        bookId: 'book-123',
        fileName: 'test.epub'
      };

      // Mock the upload service to immediately return just the result (not progress)
      uploadServiceSpy.uploadBookWithProgress.and.returnValue(of(uploadResult));
      
      component.config.set(mockConfig);
      component.uploadQueue.set([{
        file: mockFile,
        progress: 0,
        status: 'pending'
      }]);
      
      component.uploadAll();
      
      // Allow time for all async operations including setTimeout calls
      tick(); // Process the observable
      tick(); // Process the setTimeout(resolve, 0) 
      fixture.detectChanges(); // Force change detection
      
      expect(uploadServiceSpy.uploadBookWithProgress).toHaveBeenCalledWith(mockFile);
      expect(bookServiceSpy.clearCache).toHaveBeenCalled();
      expect(component.isUploading()).toBe(false);
      expect(component.uploadQueue()[0].status).toBe('success');
    }));

    it('should handle upload errors', fakeAsync(() => {
      const mockFile = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      const error = { error: { message: 'Upload failed' } };

      uploadServiceSpy.uploadBookWithProgress.and.returnValue(throwError(() => error));
      
      component.config.set(mockConfig);
      component.uploadQueue.set([{
        file: mockFile,
        progress: 0,
        status: 'pending'
      }]);
      
      component.uploadAll();
      
      // Allow time for all async operations including setTimeout calls
      tick(); // Process the observable error
      tick(); // Process the setTimeout(resolve, 0) 
      fixture.detectChanges(); // Force change detection
      
      expect(component.uploadQueue()[0].status).toBe('error');
      expect(component.uploadQueue()[0].error).toBe('Upload failed');
      expect(snackBarSpy.open).toHaveBeenCalledWith(
        'Upload failed: test.epub',
        'Close',
        { duration: 3000 }
      );
    }));

    it('should handle duplicate upload result', fakeAsync(() => {
      const mockFile = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      const uploadResult: UploadResult = {
        status: 'DUPLICATE',
        message: 'File already exists',
        fileHash: 'abc123'
      };

      uploadServiceSpy.uploadBookWithProgress.and.returnValue(of(uploadResult));
      
      component.config.set(mockConfig);
      component.uploadQueue.set([{
        file: mockFile,
        progress: 0,
        status: 'pending'
      }]);
      
      component.uploadAll();
      
      // Allow time for all async operations including setTimeout calls
      tick(); // Process the observable
      tick(); // Process the setTimeout(resolve, 0) 
      fixture.detectChanges(); // Force change detection
      
      expect(component.uploadQueue()[0].status).toBe('duplicate');
      expect(snackBarSpy.open).toHaveBeenCalledWith(
        'Duplicate file detected: test.epub',
        'Close',
        { duration: 3000 }
      );
    }));
  });

  describe('queue management', () => {
    it('should remove item from queue', () => {
      const mockFile = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      const item = {
        file: mockFile,
        progress: 0,
        status: 'pending' as const
      };
      
      component.uploadQueue.set([item]);
      component.removeFromQueue(item);
      
      expect(component.uploadQueue().length).toBe(0);
    });

    it('should clear entire queue', () => {
      const mockFile = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      component.uploadQueue.set([{
        file: mockFile,
        progress: 0,
        status: 'pending'
      }]);
      
      component.clearQueue();
      
      expect(component.uploadQueue().length).toBe(0);
    });

    it('should check if queue has valid files', () => {
      expect(component.hasValidFiles()).toBe(false);
      
      const mockFile = new File(['content'], 'test.epub', { type: 'application/epub+zip' });
      component.uploadQueue.set([{
        file: mockFile,
        progress: 0,
        status: 'pending'
      }]);
      
      expect(component.hasValidFiles()).toBe(true);
      
      component.uploadQueue.update(queue => 
        queue.map(item => ({ ...item, status: 'success' as const }))
      );
      
      expect(component.hasValidFiles()).toBe(false);
    });
  });

  describe('utility methods', () => {
    it('should navigate to book view', () => {
      const bookId = 'book-123';
      component.viewBook(bookId);
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/books', bookId]);
    });

    it('should format file size', () => {
      uploadServiceSpy.formatFileSize.and.returnValue('1 MB');
      const result = component.formatFileSize(1048576);
      expect(result).toBe('1 MB');
      expect(uploadServiceSpy.formatFileSize).toHaveBeenCalledWith(1048576);
    });

    it('should return correct status icons', () => {
  expect(component.getStatusIcon('pending')).toBe('material-symbols:schedule');
  expect(component.getStatusIcon('uploading')).toBe('material-symbols:cloud-upload');
  expect(component.getStatusIcon('success')).toBe('material-symbols:check-circle');
  expect(component.getStatusIcon('duplicate')).toBe('material-symbols:content-copy');
  expect(component.getStatusIcon('error')).toBe('material-symbols:error');
  expect(component.getStatusIcon('unknown')).toBe('material-symbols:help');
    });

    it('should return correct status icon classes', () => {
      expect(component.getStatusIconClass('pending')).toBe('status-pending');
      expect(component.getStatusIconClass('uploading')).toBe('status-uploading');
      expect(component.getStatusIconClass('success')).toBe('status-success');
      expect(component.getStatusIconClass('duplicate')).toBe('status-duplicate');
      expect(component.getStatusIconClass('error')).toBe('status-error');
      expect(component.getStatusIconClass('unknown')).toBe('');
    });
  });

  describe('computed properties', () => {
    it('should calculate overall progress', () => {
      component.uploadQueue.set([
        { file: new File([''], 'test1.epub'), progress: 50, status: 'uploading' },
        { file: new File([''], 'test2.epub'), progress: 75, status: 'uploading' },
        { file: new File([''], 'test3.epub'), progress: 25, status: 'uploading' }
      ]);
      
      expect(component.overallProgress()).toBe(50); // (50 + 75 + 25) / 3 = 50
    });

    it('should return 0 progress for empty queue', () => {
      component.uploadQueue.set([]);
      expect(component.overallProgress()).toBe(0);
    });
  });
});