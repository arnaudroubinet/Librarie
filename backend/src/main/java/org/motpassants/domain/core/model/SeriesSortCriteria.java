package org.motpassants.domain.core.model;

public class SeriesSortCriteria {
    private final SortField field;
    private final SortDirection direction;

    public static final SeriesSortCriteria DEFAULT = new SeriesSortCriteria(SortField.UPDATED_AT, SortDirection.DESC);

    public SeriesSortCriteria(SortField field, SortDirection direction) {
        if (field == null) throw new IllegalArgumentException("Sort field cannot be null");
        if (direction == null) throw new IllegalArgumentException("Sort direction cannot be null");
        this.field = field;
        this.direction = direction;
    }

    public static SeriesSortCriteria of(String fieldStr, String directionStr) {
        SortField field = SortField.fromString(fieldStr);
        SortDirection direction = SortDirection.fromString(directionStr);
        return new SeriesSortCriteria(field, direction);
    }

    public SortField getField() { return field; }
    public SortDirection getDirection() { return direction; }

    public String toSqlOrderClause() { return field.getColumnName() + " " + direction.getSqlKeyword(); }
}
