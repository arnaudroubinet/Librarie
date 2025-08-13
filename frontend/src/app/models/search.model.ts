import { Book } from './book.model';
import { Series } from './series.model';
import { Author } from './author.model';

export interface UnifiedSearchResult {
  books: Book[];
  series: Series[];
  authors: Author[];
}