-- V2__Entity_restructuring.sql
-- Migration for PR feedback: entity restructuring and field renaming

-- Update languages table to support BCP 47 standard (longer codes)
ALTER TABLE languages ALTER COLUMN code TYPE VARCHAR(35);

-- Update original_work_external_ids table structure
ALTER TABLE original_work_external_ids RENAME COLUMN identifier_type TO type;
ALTER TABLE original_work_external_ids RENAME COLUMN identifier_value TO value;
ALTER TABLE original_work_external_ids DROP COLUMN updated_at;

-- Update original_work_authors table - remove order_index
ALTER TABLE original_work_authors DROP COLUMN order_index;

-- Update original_works table - rename first_publication_date to first_publication
ALTER TABLE original_works RENAME COLUMN first_publication_date TO first_publication;

-- Update tags table - remove category column
ALTER TABLE tags DROP COLUMN category;

-- Update users table - rename fields as per feedback
ALTER TABLE users RENAME COLUMN oidc_origin TO oidc_origin_name;
ALTER TABLE users DROP COLUMN oidc_origin_url;
ALTER TABLE users RENAME COLUMN username TO public_name;

-- Update constraints to match new column names
ALTER TABLE users DROP CONSTRAINT uk_users_oidc;
ALTER TABLE users ADD CONSTRAINT uk_users_oidc UNIQUE (oidc_origin_name, oidc_subject);

-- Update unique index
DROP INDEX IF EXISTS idx_users_username;
CREATE UNIQUE INDEX idx_users_public_name ON users(public_name);

-- Update the relationship_type column in book_original_works to use enum values
-- First update existing data to use uppercase values
UPDATE book_original_works SET relationship_type = 'PRIMARY' WHERE relationship_type = 'primary';
UPDATE book_original_works SET relationship_type = 'COLLECTION' WHERE relationship_type = 'collection';
UPDATE book_original_works SET relationship_type = 'ANTHOLOGY' WHERE relationship_type = 'anthology';
UPDATE book_original_works SET relationship_type = 'ADAPTATION' WHERE relationship_type = 'adaptation';
UPDATE book_original_works SET relationship_type = 'TRANSLATION' WHERE relationship_type = 'translation';
UPDATE book_original_works SET relationship_type = 'EXCERPT' WHERE relationship_type = 'excerpt';

-- Add constraint to ensure only valid enum values
ALTER TABLE book_original_works ADD CONSTRAINT check_relationship_type 
    CHECK (relationship_type IN ('PRIMARY', 'COLLECTION', 'ANTHOLOGY', 'ADAPTATION', 'TRANSLATION', 'EXCERPT'));

-- Update indexes that reference the renamed columns
DROP INDEX IF EXISTS idx_original_work_external_ids_type;
DROP INDEX IF EXISTS idx_original_work_external_ids_value;
CREATE INDEX idx_original_work_external_ids_type ON original_work_external_ids(type);
CREATE INDEX idx_original_work_external_ids_value ON original_work_external_ids(value);