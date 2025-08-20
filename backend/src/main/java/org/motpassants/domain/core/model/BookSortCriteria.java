package org.motpassants.domain.core.model;

/**
 * Value object representing book sorting criteria.
 * Encapsulates both the field to sort by and the direction.
 * Provides a default sort order that maintains backward compatibility.
 */
public class BookSortCriteria {
    
    private final SortField field;
    private final SortDirection direction;
    
    /**
     * Default sort criteria: updated_at DESC (most recently updated first).
     * This provides a sensible default that shows users the most recently
     * modified books first, which is useful for library management.
     */
    public static final BookSortCriteria DEFAULT = new BookSortCriteria(SortField.UPDATED_AT, SortDirection.DESC);
    
    /**
     * Create new sort criteria.
     * @param field the field to sort by
     * @param direction the sort direction
     */
    public BookSortCriteria(SortField field, SortDirection direction) {
        if (field == null) {
            throw new IllegalArgumentException("Sort field cannot be null");
        }
        if (direction == null) {
            throw new IllegalArgumentException("Sort direction cannot be null");
        }
        this.field = field;
        this.direction = direction;
    }
    
    /**
     * Create sort criteria from string values.
     * @param fieldStr the field name as string
     * @param directionStr the direction as string
     * @return new BookSortCriteria instance
     */
    public static BookSortCriteria of(String fieldStr, String directionStr) {
        SortField field = SortField.fromString(fieldStr);
        SortDirection direction = SortDirection.fromString(directionStr);
        return new BookSortCriteria(field, direction);
    }
    
    /**
     * Create sort criteria with default direction (DESC).
     * @param field the field to sort by
     * @return new BookSortCriteria instance with DESC direction
     */
    public static BookSortCriteria descending(SortField field) {
        return new BookSortCriteria(field, SortDirection.DESC);
    }
    
    /**
     * Create sort criteria with ascending direction.
     * @param field the field to sort by
     * @return new BookSortCriteria instance with ASC direction
     */
    public static BookSortCriteria ascending(SortField field) {
        return new BookSortCriteria(field, SortDirection.ASC);
    }
    
    public SortField getField() {
        return field;
    }
    
    public SortDirection getDirection() {
        return direction;
    }
    
    /**
     * Get the SQL ORDER BY clause for this sort criteria.
     * This ensures consistent SQL generation across the application.
     * @return the ORDER BY clause (e.g., "updated_at DESC")
     */
    public String toSqlOrderClause() {
        return field.getColumnName() + " " + direction.getSqlKeyword();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookSortCriteria)) return false;
        BookSortCriteria that = (BookSortCriteria) o;
        return field == that.field && direction == that.direction;
    }
    
    @Override
    public int hashCode() {
        return field.hashCode() * 31 + direction.hashCode();
    }
    
    @Override
    public String toString() {
        return "BookSortCriteria{" +
                "field=" + field +
                ", direction=" + direction +
                '}';
    }
}