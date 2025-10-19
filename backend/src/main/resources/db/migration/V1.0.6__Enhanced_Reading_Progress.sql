-- V1.0.6__Enhanced_Reading_Progress.sql
-- Enhance reading progress table with status tracking and multi-device sync support

-- Add new columns for enhanced reading progress tracking
ALTER TABLE reading_progress 
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'READING',
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS finished_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS sync_version BIGINT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS notes TEXT;

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_reading_progress_status ON reading_progress(status);
CREATE INDEX IF NOT EXISTS idx_reading_progress_last_read_desc ON reading_progress(last_read_at DESC);

-- Update existing records to have default status based on progress
UPDATE reading_progress 
SET status = CASE 
    WHEN progress_percent >= 100 THEN 'FINISHED'
    WHEN progress_percent > 0 THEN 'READING'
    ELSE 'UNREAD'
END
WHERE status IS NULL OR status = 'READING';

-- Set started_at for existing reading progress records
UPDATE reading_progress 
SET started_at = created_at
WHERE started_at IS NULL AND (status = 'READING' OR status = 'FINISHED');

-- Set finished_at for completed books
UPDATE reading_progress 
SET finished_at = last_read_at
WHERE finished_at IS NULL AND status = 'FINISHED';

-- Ensure sync_version is set for existing records
UPDATE reading_progress 
SET sync_version = 1
WHERE sync_version IS NULL;

-- Add comment to describe the table
COMMENT ON TABLE reading_progress IS 'Tracks user reading progress with status, timestamps, and multi-device sync support';
COMMENT ON COLUMN reading_progress.status IS 'Reading status: UNREAD, READING, FINISHED, or DNF (Did Not Finish)';
COMMENT ON COLUMN reading_progress.started_at IS 'Timestamp when user started reading the book';
COMMENT ON COLUMN reading_progress.finished_at IS 'Timestamp when user finished reading the book';
COMMENT ON COLUMN reading_progress.sync_version IS 'Version counter for conflict resolution in multi-device sync';
COMMENT ON COLUMN reading_progress.notes IS 'Optional user notes about their reading progress';
