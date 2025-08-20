package org.motpassants.domain.core.model;

public class AuthorSortCriteria {
    private final SortField field;
    private final SortDirection direction;

    public static final AuthorSortCriteria DEFAULT = new AuthorSortCriteria(SortField.SORT_NAME, SortDirection.ASC);

    public AuthorSortCriteria(SortField field, SortDirection direction) {
        if (field == null) throw new IllegalArgumentException("Sort field cannot be null");
        if (direction == null) throw new IllegalArgumentException("Sort direction cannot be null");
        this.field = field;
        this.direction = direction;
    }

    public static AuthorSortCriteria of(String fieldStr, String directionStr) {
        if (fieldStr == null && directionStr == null) return DEFAULT;
        SortField field = SortField.fromString(fieldStr == null ? "SORT_NAME" : fieldStr);
        SortDirection direction = SortDirection.fromString(directionStr == null ? "ASC" : directionStr);
        return new AuthorSortCriteria(field, direction);
    }

    public SortField getField() { return field; }
    public SortDirection getDirection() { return direction; }

    public String toSqlOrderClause() { return field.getColumnName() + " " + direction.getSqlKeyword(); }
}
