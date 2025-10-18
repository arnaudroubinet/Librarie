import { TestBed } from '@angular/core/testing';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { SearchComponent } from './search.component';
import { BookService } from '../services/book.service';
import { SearchService } from '../services/search.service';
import { MatSnackBar } from '@angular/material/snack-bar';

describe('SearchComponent', () => {
  let component: SearchComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchComponent],
      providers: [
        provideRouter([]),
        provideAnimations(),
        provideHttpClient(),
        BookService,
        SearchService,
        MatSnackBar
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(SearchComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have searchForm initialized with all controls', () => {
    expect(component.searchForm).toBeTruthy();
    expect(component.searchForm.get('quickSearch')).toBeTruthy();
    expect(component.searchForm.get('title')).toBeTruthy();
    expect(component.searchForm.get('authors')).toBeTruthy();
    expect(component.searchForm.get('series')).toBeTruthy();
    expect(component.searchForm.get('publisher')).toBeTruthy();
    expect(component.searchForm.get('language')).toBeTruthy();
    expect(component.searchForm.get('formats')).toBeTruthy();
    expect(component.searchForm.get('publishedAfter')).toBeTruthy();
    expect(component.searchForm.get('publishedBefore')).toBeTruthy();
    expect(component.searchForm.get('sortBy')).toBeTruthy();
    expect(component.searchForm.get('sortDirection')).toBeTruthy();
  });

  it('should update form control values programmatically', () => {
    component.searchForm.patchValue({
      title: 'Test Book Title',
      authors: 'Test Author',
      series: 'Test Series'
    });
    
    expect(component.searchForm.get('title')?.value).toBe('Test Book Title');
    expect(component.searchForm.get('authors')?.value).toBe('Test Author');
    expect(component.searchForm.get('series')?.value).toBe('Test Series');
  });

  it('should clear all form values when clearForm is called', () => {
    // Set some values
    component.searchForm.patchValue({
      title: 'Test',
      authors: 'Author Name',
      series: 'Series Name',
      publisher: 'Publisher Name'
    });
    
    // Clear the form
    component.clearForm();
    
    // Check that values are reset
    expect(component.searchForm.get('title')?.value).toBe('');
    expect(component.searchForm.get('authors')?.value).toBe('');
    expect(component.searchForm.get('series')?.value).toBe('');
    expect(component.searchForm.get('publisher')?.value).toBe('');
    expect(component.searchForm.get('sortBy')?.value).toBe('title');
    expect(component.searchForm.get('sortDirection')?.value).toBe('asc');
  });
});
