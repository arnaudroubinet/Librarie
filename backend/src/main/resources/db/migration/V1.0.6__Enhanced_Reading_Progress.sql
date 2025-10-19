-- V1.0.6__Enhanced_Reading_Progress.sql
-- Enhanced reading progress tracking with reading status, timestamps, and statistics support

-- Add reading status enum support
ALTER TABLE reading_progress 
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'READING' CHECK (status IN ('UNREAD', 'READING', 'FINISHED', 'DNF')),
    ADD COLUMN IF NOT EXISTS started_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS finished_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS sync_version BIGINT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS notes TEXT;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_reading_progress_status ON reading_progress(status);
CREATE INDEX IF NOT EXISTS idx_reading_progress_started_at ON reading_progress(started_at DESC) WHERE started_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_reading_progress_finished_at ON reading_progress(finished_at DESC) WHERE finished_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_reading_progress_user_status ON reading_progress(user_id, status);

-- Backfill started_at for existing records with READING status
UPDATE reading_progress 
SET started_at = created_at
WHERE started_at IS NULL 
  AND progress_percent > 0;

-- Backfill finished_at for records that are complete
UPDATE reading_progress 
SET finished_at = last_read_at,
    status = 'FINISHED'
WHERE finished_at IS NULL 
  AND progress_percent >= 100;

-- Comment on new columns
COMMENT ON COLUMN reading_progress.status IS 'Reading status: UNREAD, READING, FINISHED, DNF (Did Not Finish)';
COMMENT ON COLUMN reading_progress.started_at IS 'Timestamp when user started reading this book';
COMMENT ON COLUMN reading_progress.finished_at IS 'Timestamp when user finished reading this book';
COMMENT ON COLUMN reading_progress.sync_version IS 'Version for optimistic locking in multi-device sync';
COMMENT ON COLUMN reading_progress.notes IS 'User notes or annotations about their reading progress';
