-- Add raw Readium locator storage alongside percentage progress
ALTER TABLE reading_progress
ADD COLUMN IF NOT EXISTS progress_locator JSONB;

-- Optional: backfill nulls explicitly (no-op default)
UPDATE reading_progress SET progress_locator = NULL WHERE progress_locator IS NULL;

-- No index added for now; locators are opaque JSON for restore only
