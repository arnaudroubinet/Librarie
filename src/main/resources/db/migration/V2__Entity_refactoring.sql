-- V2__Entity_refactoring.sql
-- Major entity refactoring for hexagonal architecture compliance
-- Adds User entity, removes BookPublisher, updates relationships

-- Add users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    oidc_origin TEXT NOT NULL,
    oidc_origin_url TEXT NOT NULL,
    oidc_subject TEXT NOT NULL,
    username TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uk_users_oidc UNIQUE (oidc_origin, oidc_subject)
);

-- Add updated_at columns to tables that don't have them
ALTER TABLE authors ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE series ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE tags ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE publishers ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();
ALTER TABLE formats ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- Rename last_modified to updated_at where it exists
ALTER TABLE original_works RENAME COLUMN last_modified TO updated_at;
ALTER TABLE books RENAME COLUMN last_modified TO updated_at;

-- Add direct publisher relationship to books
ALTER TABLE books ADD COLUMN publisher_id UUID REFERENCES publishers(id);

-- Update user references to use user_id instead of user_subject
ALTER TABLE ratings ADD COLUMN user_id UUID REFERENCES users(id);
ALTER TABLE reading_progress ADD COLUMN user_id UUID REFERENCES users(id);
ALTER TABLE download_history ADD COLUMN user_id UUID REFERENCES users(id);

-- Add updated_at to reading_progress if not exists
ALTER TABLE reading_progress ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ DEFAULT NOW();

-- Rename download_history.downloaded_at to created_at and add updated_at
ALTER TABLE download_history RENAME COLUMN downloaded_at TO created_at;
ALTER TABLE download_history ADD COLUMN updated_at TIMESTAMPTZ DEFAULT NOW();

-- Remove format relationship from download_history (can be obtained by transitivity)
ALTER TABLE download_history DROP COLUMN IF EXISTS format_id;

-- Drop book_publishers table (replaced by direct relationship)
DROP TABLE IF EXISTS book_publishers;

-- After migrating to user_id, drop old user_subject columns
-- (This should be done after data migration)
-- ALTER TABLE ratings DROP COLUMN user_subject;
-- ALTER TABLE reading_progress DROP COLUMN user_subject;
-- ALTER TABLE download_history DROP COLUMN user_subject;

-- Add comments for future enhancements
COMMENT ON COLUMN download_history.ip_address IS 'TODO: Implement encryption for GDPR compliance';
COMMENT ON COLUMN authors.bio IS 'TODO: Externalize by language for multilingual support';
COMMENT ON COLUMN authors.website_url IS 'TODO: Externalize by language for multilingual support';

-- Add constraint to ensure book can only be in one series
ALTER TABLE book_series ADD CONSTRAINT uk_book_series_book_id UNIQUE (book_id);

-- Fix column type mismatch for schema validation (PostgreSQL types to standard types)
ALTER TABLE books ALTER COLUMN file_hash TYPE VARCHAR(64);
ALTER TABLE books ALTER COLUMN language_code TYPE VARCHAR(2);
ALTER TABLE languages ALTER COLUMN code TYPE VARCHAR(2);
ALTER TABLE tags ALTER COLUMN color TYPE VARCHAR(7);
ALTER TABLE download_history ALTER COLUMN ip_address TYPE VARCHAR(45); -- IPv6 compatible

-- Drop GIN index on search_vector before changing column type
DROP INDEX IF EXISTS idx_books_search;
ALTER TABLE books ALTER COLUMN search_vector TYPE TEXT;
-- Recreate as btree index for text search (can be improved later with full-text search)
CREATE INDEX idx_books_search ON books USING btree(search_vector);

-- Create indexes for new relationships
CREATE INDEX idx_books_publisher_id ON books(publisher_id);
CREATE INDEX idx_ratings_user_id ON ratings(user_id);
CREATE INDEX idx_reading_progress_user_id ON reading_progress(user_id);
CREATE INDEX idx_download_history_user_id ON download_history(user_id);
CREATE INDEX idx_users_oidc ON users(oidc_origin, oidc_subject);
CREATE INDEX idx_users_username ON users(username);