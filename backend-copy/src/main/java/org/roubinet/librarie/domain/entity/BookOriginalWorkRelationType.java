package org.roubinet.librarie.domain.entity;

/**
 * Enum representing the possible relationship types between a book and an original work.
 */
public enum BookOriginalWorkRelationType {
    PRIMARY("primary"),
    COLLECTION("collection"),
    ANTHOLOGY("anthology"),
    ADAPTATION("adaptation"),
    TRANSLATION("translation"),
    EXCERPT("excerpt");

    private final String value;

    BookOriginalWorkRelationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}