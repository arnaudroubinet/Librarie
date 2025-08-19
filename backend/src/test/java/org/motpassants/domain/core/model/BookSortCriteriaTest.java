package org.motpassants.domain.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BookSortCriteria and related sorting enums.
 */
class BookSortCriteriaTest {

    @Test
    void testSortFieldEnumValues() {
        assertEquals("updated_at", SortField.UPDATED_AT.getColumnName());
        assertEquals("title_sort", SortField.TITLE_SORT.getColumnName());
        assertEquals("publication_date", SortField.PUBLICATION_DATE.getColumnName());
    }

    @Test
    void testSortFieldFromString() {
        assertEquals(SortField.UPDATED_AT, SortField.fromString("UPDATED_AT"));
        assertEquals(SortField.TITLE_SORT, SortField.fromString("title_sort"));
        assertEquals(SortField.PUBLICATION_DATE, SortField.fromString("Publication_Date"));
        
        assertThrows(IllegalArgumentException.class, () -> SortField.fromString("invalid"));
        assertThrows(IllegalArgumentException.class, () -> SortField.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> SortField.fromString(""));
    }

    @Test
    void testSortDirectionEnumValues() {
        assertEquals("ASC", SortDirection.ASC.getSqlKeyword());
        assertEquals("DESC", SortDirection.DESC.getSqlKeyword());
    }

    @Test
    void testSortDirectionFromString() {
        assertEquals(SortDirection.ASC, SortDirection.fromString("ASC"));
        assertEquals(SortDirection.DESC, SortDirection.fromString("desc"));
        
        assertThrows(IllegalArgumentException.class, () -> SortDirection.fromString("invalid"));
        assertThrows(IllegalArgumentException.class, () -> SortDirection.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> SortDirection.fromString(""));
    }

    @Test
    void testBookSortCriteriaConstruction() {
        BookSortCriteria criteria = new BookSortCriteria(SortField.TITLE_SORT, SortDirection.ASC);
        assertEquals(SortField.TITLE_SORT, criteria.getField());
        assertEquals(SortDirection.ASC, criteria.getDirection());
        assertEquals("title_sort ASC", criteria.toSqlOrderClause());
    }

    @Test
    void testBookSortCriteriaFactoryMethods() {
        BookSortCriteria criteria1 = BookSortCriteria.of("UPDATED_AT", "DESC");
        assertEquals(SortField.UPDATED_AT, criteria1.getField());
        assertEquals(SortDirection.DESC, criteria1.getDirection());
        
        BookSortCriteria criteria2 = BookSortCriteria.ascending(SortField.PUBLICATION_DATE);
        assertEquals(SortField.PUBLICATION_DATE, criteria2.getField());
        assertEquals(SortDirection.ASC, criteria2.getDirection());
        
        BookSortCriteria criteria3 = BookSortCriteria.descending(SortField.TITLE_SORT);
        assertEquals(SortField.TITLE_SORT, criteria3.getField());
        assertEquals(SortDirection.DESC, criteria3.getDirection());
    }

    @Test
    void testDefaultSortCriteria() {
        assertEquals(SortField.UPDATED_AT, BookSortCriteria.DEFAULT.getField());
        assertEquals(SortDirection.DESC, BookSortCriteria.DEFAULT.getDirection());
        assertEquals("updated_at DESC", BookSortCriteria.DEFAULT.toSqlOrderClause());
    }

    @Test
    void testBookSortCriteriaValidation() {
        assertThrows(IllegalArgumentException.class, 
            () -> new BookSortCriteria(null, SortDirection.ASC));
        assertThrows(IllegalArgumentException.class, 
            () -> new BookSortCriteria(SortField.TITLE_SORT, null));
    }

    @Test
    void testEqualsAndHashCode() {
        BookSortCriteria criteria1 = new BookSortCriteria(SortField.TITLE_SORT, SortDirection.ASC);
        BookSortCriteria criteria2 = new BookSortCriteria(SortField.TITLE_SORT, SortDirection.ASC);
        BookSortCriteria criteria3 = new BookSortCriteria(SortField.TITLE_SORT, SortDirection.DESC);
        
        assertEquals(criteria1, criteria2);
        assertEquals(criteria1.hashCode(), criteria2.hashCode());
        assertNotEquals(criteria1, criteria3);
    }
}