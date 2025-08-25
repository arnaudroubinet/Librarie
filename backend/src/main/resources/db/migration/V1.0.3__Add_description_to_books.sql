-- V1.0.3__Add_description_to_books.sql
-- Add missing description column to books table

ALTER TABLE books ADD COLUMN description TEXT;

-- Create index for full-text search on description if needed
CREATE INDEX idx_books_description_gin ON books USING gin(to_tsvector('english', COALESCE(description, '')));