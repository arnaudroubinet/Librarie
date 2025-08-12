-- V1__Initial_schema.sql
-- Initial database schema for Librarie library management system
-- Based on PostgreSQL 16 optimized design with UUID primary keys

-- Create UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Languages table (reference data) - BCP 47 standard support
CREATE TABLE languages (
    code VARCHAR(35) PRIMARY KEY,
    name TEXT NOT NULL,
    rtl BOOLEAN DEFAULT FALSE
);

CREATE TABLE publishers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    website_url TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Core content tables
CREATE TABLE books (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    title_sort TEXT NOT NULL,
    isbn TEXT,
    path TEXT NOT NULL,
    file_size BIGINT,
    file_hash VARCHAR(64),
    has_cover BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    publication_date DATE,
    language_code VARCHAR(35) REFERENCES languages(code),
    publisher_id UUID REFERENCES publishers(id),
    metadata JSONB DEFAULT '{}',
    search_vector TEXT
);

CREATE TABLE authors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    sort_name TEXT NOT NULL,
    bio JSONB,
    birth_date DATE,
    death_date DATE,
    website_url TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE original_works (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    title_sort TEXT NOT NULL,
    description TEXT,
    first_publication DATE,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE original_work_external_ids (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    original_work_id UUID NOT NULL REFERENCES original_works(id) ON DELETE CASCADE,
    type TEXT NOT NULL,
    value TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(original_work_id, type, value)
);

CREATE TABLE series (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    sort_name TEXT NOT NULL,
    description TEXT,
    image_path TEXT,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE tags (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    color VARCHAR(7),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Users table for OIDC integration
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    oidc_origin_name TEXT NOT NULL,
    oidc_subject TEXT NOT NULL,
    public_name TEXT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uk_users_oidc UNIQUE (oidc_origin_name, oidc_subject)
);

CREATE TABLE formats (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    format_type TEXT NOT NULL,
    file_path TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    quality_score INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Many-to-many relationship tables
CREATE TABLE original_work_authors (
    original_work_id UUID REFERENCES original_works(id) ON DELETE CASCADE,
    author_id UUID REFERENCES authors(id) ON DELETE CASCADE,
    role TEXT DEFAULT 'author',
    PRIMARY KEY (original_work_id, author_id, role)
);

CREATE TABLE book_original_works (
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    original_work_id UUID REFERENCES original_works(id) ON DELETE CASCADE,
    relationship_type TEXT DEFAULT 'PRIMARY' CHECK (relationship_type IN ('PRIMARY', 'COLLECTION', 'ANTHOLOGY', 'ADAPTATION', 'TRANSLATION', 'EXCERPT')),
    order_index INTEGER DEFAULT 0,
    PRIMARY KEY (book_id, original_work_id, relationship_type)
);

CREATE TABLE book_series (
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    series_id UUID REFERENCES series(id) ON DELETE CASCADE,
    series_index DECIMAL(10,2) DEFAULT 1.0,
    PRIMARY KEY (book_id, series_id)
);

CREATE TABLE book_tags (
    book_id UUID REFERENCES books(id) ON DELETE CASCADE,
    tag_id UUID REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, tag_id)
);

-- User activity tables
CREATE TABLE ratings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE reading_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    book_id UUID NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    format_id UUID REFERENCES formats(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_id TEXT NOT NULL,
    progress_cfi TEXT,
    progress_percent DECIMAL(5,2) CHECK (progress_percent >= 0 AND progress_percent <= 100),
    last_read_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE user_preferences (
    user_subject TEXT PRIMARY KEY,
    display_name TEXT,
    language_preference VARCHAR(35) REFERENCES languages(code),
    timezone TEXT DEFAULT 'UTC',
    preferences JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    last_login TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE download_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    format_id UUID NOT NULL REFERENCES formats(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- System tables
CREATE TABLE import_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    status TEXT NOT NULL CHECK (status IN ('pending', 'running', 'completed', 'failed')),
    source_path TEXT NOT NULL,
    books_imported INTEGER DEFAULT 0,
    books_failed INTEGER DEFAULT 0,
    error_log TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ
);

CREATE TABLE system_config (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_books_title_sort ON books USING btree(title_sort);
CREATE INDEX idx_books_created_at ON books USING btree(created_at);
CREATE INDEX idx_books_language ON books USING btree(language_code);
CREATE INDEX idx_books_publisher ON books USING btree(publisher_id);
CREATE INDEX idx_books_file_hash ON books USING hash(file_hash);
CREATE INDEX idx_books_search ON books USING btree(search_vector);
CREATE INDEX idx_books_metadata ON books USING gin(metadata);

CREATE UNIQUE INDEX idx_authors_name ON authors(name);
CREATE INDEX idx_authors_sort ON authors USING btree(sort_name);

CREATE INDEX idx_original_works_title_sort ON original_works USING btree(title_sort);
CREATE INDEX idx_original_works_created_at ON original_works USING btree(created_at);
CREATE INDEX idx_original_works_metadata ON original_works USING gin(metadata);

CREATE INDEX idx_original_work_external_ids_work ON original_work_external_ids(original_work_id);
CREATE INDEX idx_original_work_external_ids_type ON original_work_external_ids(type);
CREATE INDEX idx_original_work_external_ids_value ON original_work_external_ids(value);

CREATE UNIQUE INDEX idx_series_name ON series(name);
CREATE INDEX idx_series_sort ON series USING btree(sort_name);
CREATE INDEX idx_series_image_path ON series(image_path);

CREATE UNIQUE INDEX idx_tags_name ON tags(name);

CREATE UNIQUE INDEX idx_publishers_name ON publishers(name);

CREATE INDEX idx_formats_book ON formats(book_id);
CREATE INDEX idx_formats_type ON formats(format_type);

CREATE INDEX idx_original_work_authors_work ON original_work_authors(original_work_id);
CREATE INDEX idx_original_work_authors_author ON original_work_authors(author_id);

CREATE INDEX idx_book_original_works_book ON book_original_works(book_id);
CREATE INDEX idx_book_original_works_work ON book_original_works(original_work_id);

CREATE INDEX idx_book_series_book ON book_series(book_id);
CREATE INDEX idx_book_series_series ON book_series(series_id);
CREATE INDEX idx_book_series_index ON book_series(series_index);

CREATE INDEX idx_book_tags_book ON book_tags(book_id);
CREATE INDEX idx_book_tags_tag ON book_tags(tag_id);

CREATE INDEX idx_ratings_book ON ratings(book_id);
CREATE INDEX idx_ratings_user ON ratings(user_id);
CREATE UNIQUE INDEX idx_ratings_book_user ON ratings(book_id, user_id);

CREATE INDEX idx_reading_progress_user ON reading_progress(user_id);
CREATE INDEX idx_reading_progress_book ON reading_progress(book_id);
CREATE INDEX idx_reading_progress_last_read ON reading_progress(last_read_at);
CREATE UNIQUE INDEX idx_reading_progress_unique ON reading_progress(book_id, user_id, device_id);

CREATE INDEX idx_user_preferences_last_login ON user_preferences(last_login);

CREATE INDEX idx_download_history_user ON download_history(user_id);
CREATE INDEX idx_download_history_format ON download_history(format_id);
CREATE INDEX idx_download_history_date ON download_history(created_at);

CREATE INDEX idx_import_jobs_status ON import_jobs(status);
CREATE INDEX idx_import_jobs_created ON import_jobs(created_at);

-- Insert reference data with full locale codes
INSERT INTO languages VALUES 
    ('en-US', 'English (United States)', FALSE),
    ('en-GB', 'English (United Kingdom)', FALSE),
    ('fr-FR', 'French (France)', FALSE),
    ('fr-CA', 'French (Canada)', FALSE),
    ('es-ES', 'Spanish (Spain)', FALSE),
    ('es-MX', 'Spanish (Mexico)', FALSE),
    ('de-DE', 'German (Germany)', FALSE),
    ('it-IT', 'Italian (Italy)', FALSE),
    ('pt-PT', 'Portuguese (Portugal)', FALSE),
    ('pt-BR', 'Portuguese (Brazil)', FALSE),
    ('ru-RU', 'Russian (Russia)', FALSE),
    ('zh-CN', 'Chinese (Simplified)', FALSE),
    ('zh-TW', 'Chinese (Traditional)', FALSE),
    ('ja-JP', 'Japanese (Japan)', FALSE),
    ('ko-KR', 'Korean (South Korea)', FALSE),
    ('ar-SA', 'Arabic (Saudi Arabia)', TRUE),
    ('he-IL', 'Hebrew (Israel)', TRUE);

-- Insert default system configuration
INSERT INTO system_config VALUES 
    ('library_path', '/data/library', 'Root path for book storage'),
    ('max_file_size', '100MB', 'Maximum upload file size'),
    ('allowed_formats', 'epub,pdf,mobi,azw3', 'Comma-separated list of allowed formats');