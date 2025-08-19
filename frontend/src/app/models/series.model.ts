export interface Series {
  id: string;
  name: string;
  sortName?: string;
  description?: string;
  imagePath?: string;
  hasPicture?: boolean;
  metadata?: Record<string, any>;
  createdAt?: string;
  updatedAt?: string;
  bookCount: number;
}

export interface SeriesPageResponse {
  content: Series[];
  nextCursor?: string;
  previousCursor?: string;
  limit: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
  totalCount?: number;
}