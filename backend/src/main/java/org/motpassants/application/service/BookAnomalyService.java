package org.motpassants.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.motpassants.domain.core.model.BookAnomaly;
import org.motpassants.domain.port.in.BookAnomalyUseCase;
import org.motpassants.domain.port.out.LoggingPort;

import io.agroal.api.AgroalDataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class BookAnomalyService implements BookAnomalyUseCase {

    @Inject
    AgroalDataSource dataSource;

    @Inject
    LoggingPort loggingPort;

    @Override
    public List<BookAnomaly> scanAnomalies() {
        List<BookAnomaly> anomalies = new ArrayList<>();
        anomalies.addAll(findInvalidLanguageCodes());
        anomalies.addAll(findMissingFiles());
        return anomalies;
    }

    private List<BookAnomaly> findInvalidLanguageCodes() {
        List<BookAnomaly> list = new ArrayList<>();
        String sql = "SELECT b.id, b.language_code FROM books b LEFT JOIN languages l ON b.language_code = l.code WHERE b.language_code IS NOT NULL AND l.code IS NULL";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID id = (UUID) rs.getObject(1);
                String lang = rs.getString(2);
                list.add(new BookAnomaly(id, "INVALID_LANGUAGE_CODE", "Book references a non-existent language code", "language_code=" + lang));
            }
        } catch (Exception e) {
            loggingPort.error("Error scanning invalid language codes", e);
        }
        return list;
    }

    private List<BookAnomaly> findMissingFiles() {
        List<BookAnomaly> list = new ArrayList<>();
        String sql = "SELECT id, path FROM books WHERE path IS NOT NULL";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                UUID id = (UUID) rs.getObject(1);
                String p = rs.getString(2);
                try {
                    if (p == null || p.isBlank() || !Files.exists(Path.of(p))) {
                        list.add(new BookAnomaly(id, "MISSING_FILE", "File path does not exist on disk", p));
                    }
                } catch (Exception ignore) {
                    list.add(new BookAnomaly(id, "MISSING_FILE", "File path check failed", p));
                }
            }
        } catch (Exception e) {
            loggingPort.error("Error scanning missing files", e);
        }
        return list;
    }
}
