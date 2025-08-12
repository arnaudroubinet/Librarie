-- V2__Add_series_image_path.sql
-- Add image_path column to series table

ALTER TABLE series ADD COLUMN image_path TEXT;

-- Add index for performance
CREATE INDEX idx_series_image_path ON series(image_path);