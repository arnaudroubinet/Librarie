package org.motpassants.domain.port.in;

import org.motpassants.domain.core.model.BookAnomaly;

import java.util.List;

/**
 * Use case for scanning and retrieving book data anomalies.
 */
public interface BookAnomalyUseCase {
    /**
     * Scan the database and storage for anomalies.
     */
    List<BookAnomaly> scanAnomalies();
}
