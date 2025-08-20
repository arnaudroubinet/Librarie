package org.motpassants.domain.core.model;

/**
 * Enumeration of sort directions.
 * This provides a secure DSL component to ensure only valid
 * sort directions can be used in database queries.
 */
public enum SortDirection {
    ASC("ASC"),
    DESC("DESC");

    private final String sqlKeyword;

    SortDirection(String sqlKeyword) {
        this.sqlKeyword = sqlKeyword;
    }

    /**
     * Get the SQL keyword for this sort direction.
     * @return the SQL keyword (ASC or DESC)
     */
    public String getSqlKeyword() {
        return sqlKeyword;
    }

    /**
     * Parse a string value into a SortDirection enum.
     * @param value the string value to parse
     * @return the corresponding SortDirection
     * @throws IllegalArgumentException if the value is not a valid sort direction
     */
    public static SortDirection fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Sort direction cannot be null or empty");
        }
        
        String normalized = value.trim().toUpperCase();
        try {
            return SortDirection.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort direction: " + value + 
                ". Allowed values are: ASC, DESC");
        }
    }
}