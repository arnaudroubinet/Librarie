export interface Author {
  id: string;
  name: string;
  sortName?: string;
  bio?: Record<string, string>; // Map of language to biography text
  birthDate?: string; // ISO date string
  deathDate?: string; // ISO date string
  websiteUrl?: string;
  metadata?: Record<string, any>;
  createdAt?: string; // ISO datetime string
  updatedAt?: string; // ISO datetime string
}

export interface AuthorPageResponse {
  content: Author[];
  nextCursor?: string;
  previousCursor?: string;
  limit: number;
  hasNext?: boolean;
  hasPrevious?: boolean;
  totalCount?: number;
}

export interface AuthorRequest {
  name: string;
  sortName?: string;
  bio?: Record<string, string>;
  birthDate?: string;
  deathDate?: string;
  websiteUrl?: string;
  metadata?: Record<string, any>;
}