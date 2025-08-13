export interface Author {
  id: string;
  name: string;
  sortName?: string;
  bio?: Record<string, string>;
  birthDate?: string;
  deathDate?: string;
  websiteUrl?: string;
  metadata?: Record<string, any>;
  createdAt?: string;
  updatedAt?: string;
}