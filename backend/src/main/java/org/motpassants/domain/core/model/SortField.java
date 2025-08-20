package org.motpassants.domain.core.model;

/**
 * Enumeration of sortable fields for books.
 * This provides a secure DSL to prevent SQL injection by restricting
 * sorting to only predefined, safe database columns.
 */
public enum SortField {
    UPDATED_AT("updated_at"),
    TITLE_SORT("title_sort"),
    PUBLICATION_DATE("publication_date"),
    SORT_NAME("sort_name");

    private final String columnName;

    SortField(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Get the database column name for this sort field.
     * @return the database column name
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Determines if this field represents a timestamp type for cursor parsing.
     * @return true if this field should be parsed as a timestamp, false if as a string
     */
    public boolean isTimestampField() {
        return this == UPDATED_AT || this == PUBLICATION_DATE;
    }

    /**
     * Parse a string value into a SortField enum.
     * @param value the string value to parse
     * @return the corresponding SortField
     * @throws IllegalArgumentException if the value is not a valid sort field
     */
    public static SortField fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Sort field cannot be null or empty");
        }
        
        String normalized = value.trim().toUpperCase();
        try {
            return SortField.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort field: " + value + 
                ". Allowed values are: UPDATED_AT, TITLE_SORT, PUBLICATION_DATE");
        }
    }
}