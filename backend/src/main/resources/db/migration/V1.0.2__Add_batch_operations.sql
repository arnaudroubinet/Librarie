-- Add batch operations table for tracking bulk operations on books
-- Version: 1.0.2
-- Description: Creates table for batch edit and delete operations with results tracking

CREATE TABLE batch_operations (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL CHECK (type IN ('EDIT', 'DELETE')),
    book_ids TEXT NOT NULL, -- JSON array of book UUIDs
    edit_request TEXT, -- JSON object for edit operations, null for delete operations
    user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'PARTIAL', 'FAILED', 'CANCELLED')),
    results TEXT NOT NULL DEFAULT '[]', -- JSON array of operation results
    error_message TEXT
);

-- Create indexes for efficient querying
CREATE INDEX idx_batch_operations_user_id ON batch_operations(user_id);
CREATE INDEX idx_batch_operations_created_at ON batch_operations(created_at);
CREATE INDEX idx_batch_operations_status ON batch_operations(status);
CREATE INDEX idx_batch_operations_user_created ON batch_operations(user_id, created_at DESC);