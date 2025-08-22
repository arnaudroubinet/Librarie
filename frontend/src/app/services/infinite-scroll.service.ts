import { Injectable, signal, computed } from '@angular/core';
import { Observable } from 'rxjs';

export interface InfiniteScrollItem {
  id: string;
  [key: string]: any;
}

export interface PageResponse<T extends InfiniteScrollItem> {
  content: T[];
  nextCursor?: string;
  hasNext?: boolean;
  limit: number;
  totalCount?: number;
}

export interface AlphabeticalSeparator {
  type: 'separator';
  letter: string;
  id: string; // unique identifier for separator
}

export type ItemWithSeparator<T extends InfiniteScrollItem> = T | AlphabeticalSeparator;

export interface InfiniteScrollState<T extends InfiniteScrollItem> {
  items: ItemWithSeparator<T>[];
  loading: boolean;
  hasMore: boolean;
  error: string | null;
  nextCursor?: string;
}

@Injectable({
  providedIn: 'root'
})
export class InfiniteScrollService {

  /**
   * Creates an infinite scroll state manager
   */
  createInfiniteScrollState<T extends InfiniteScrollItem>(
    loadDataFn: (cursor?: string, limit?: number) => Observable<PageResponse<T>>,
    options: {
      limit?: number;
      enableAlphabeticalSeparators?: boolean;
      sortProperty?: keyof T; // property to use for alphabetical sorting (e.g., 'sortName')
      limitProvider?: () => number; // dynamic limit per load
    } = {}
  ) {
    const { 
      limit = 20, 
      enableAlphabeticalSeparators = false, 
      sortProperty,
      limitProvider
    } = options;

    // Internal state
    const _items = signal<ItemWithSeparator<T>[]>([]);
    const _loading = signal(false);
    const _hasMore = signal(true);
    const _error = signal<string | null>(null);
    const _nextCursor = signal<string | undefined>(undefined);

    // Public readonly state
    const items = computed(() => _items());
    const loading = computed(() => _loading());
    const hasMore = computed(() => _hasMore());
    const error = computed(() => _error());
  // Consider empty state only when there's no error; otherwise show dedicated error UI
  const isEmpty = computed(() => _items().length === 0 && !_loading() && !_error());

    // Helper function to get the first letter of a property
    const getFirstLetter = (item: T): string => {
      if (!sortProperty) return '';
      const value = item[sortProperty] as string;
      return value ? value.charAt(0).toUpperCase() : '';
    };

    // Helper function to add alphabetical separators
    const addAlphabeticalSeparators = (newItems: T[]): ItemWithSeparator<T>[] => {
      if (!enableAlphabeticalSeparators || !sortProperty) {
        return newItems;
      }

      const existingItems = _items();
      const result: ItemWithSeparator<T>[] = [];
      let lastLetter = '';

      // Get the last letter from existing items
      if (existingItems.length > 0) {
        const lastItem = existingItems[existingItems.length - 1];
        if (lastItem.type !== 'separator') {
          lastLetter = getFirstLetter(lastItem as T);
        }
      }

      for (const item of newItems) {
        const currentLetter = getFirstLetter(item);
        
        if (currentLetter && currentLetter !== lastLetter) {
          // Add separator for new letter
          result.push({
            type: 'separator',
            letter: currentLetter,
            id: `separator-${currentLetter}-${Date.now()}`
          } as AlphabeticalSeparator);
          lastLetter = currentLetter;
        }
        
        result.push(item);
      }

      return result;
    };

    // Load more data
    const loadMore = () => {
      if (_loading() || !_hasMore()) {
        return;
      }

      _loading.set(true);
  _error.set(null);

  const effectiveLimit = typeof limitProvider === 'function' ? limitProvider() : limit;
    loadDataFn(_nextCursor(), effectiveLimit).subscribe({
        next: (response) => {
          const processedItems = addAlphabeticalSeparators(response.content);
          _items.update(existing => [...existing, ...processedItems]);
          _nextCursor.set(response.nextCursor);
          // Drive hasMore solely from nextCursor presence to be resilient across endpoints
          _hasMore.set(!!response.nextCursor);
          _loading.set(false);
        },
        error: (err) => {
          // Friendly messaging for backend-down or proxy failures during dev
          let msg = 'Failed to load items. Please try again.';
          try {
            const status = (err && err.status) || 0;
            const url: string = (err && err.url) || '';
            if (status === 0) {
              msg = 'Backend is not reachable. Start the backend and retry.';
            } else if (status >= 500 && url.includes('/v1/')) {
              msg = 'Server error while loading data. Is the backend running?';
            }
          } catch {}
          _error.set(msg);
          _loading.set(false);
          // Log once per failure type to reduce noise
          try { console.warn('Infinite scroll load error:', err); } catch {}
        }
      });
    };

    // Reset and load initial data
    const reset = () => {
      _items.set([]);
      _nextCursor.set(undefined);
      _hasMore.set(true);
      _error.set(null);
      loadMore();
    };

    // Initialize with first load
    reset();

    return {
      // State (readonly)
      items,
      loading,
      hasMore,
      error,
      isEmpty,
      
      // Actions
      loadMore,
      reset
    };
  }

  /**
   * Utility function to check if an item is a separator
   */
  isSeparator<T extends InfiniteScrollItem>(item: ItemWithSeparator<T>): item is AlphabeticalSeparator {
    return (item as any).type === 'separator';
  }
}