package org.motpassants.domain.core.model;

import java.util.List;

/**
 * Domain model for paginated results.
 * This represents a page of results with pagination metadata.
 */
public class Page<T> {
    private final List<T> content;
    private final PaginationMetadata metadata;

    public Page(List<T> content, PaginationMetadata metadata) {
        this.content = content;
        this.metadata = metadata;
    }

    public List<T> getContent() {
        return content;
    }

    public PaginationMetadata getMetadata() {
        return metadata;
    }

    /**
     * Pagination metadata for domain operations.
     */
    public static class PaginationMetadata {
        private final int page;
        private final int size;
        private final long totalElements;

        public PaginationMetadata(int page, int size, long totalElements) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
        }

        public int getPage() {
            return page;
        }

        public int getSize() {
            return size;
        }

        public long getTotalElements() {
            return totalElements;
        }

        public int getTotalPages() {
            return (int) Math.ceil((double) totalElements / size);
        }

        public boolean hasNext() {
            return page < getTotalPages() - 1;
        }

        public boolean hasPrevious() {
            return page > 0;
        }
    }
}