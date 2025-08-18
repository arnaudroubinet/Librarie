package org.motpassants.infrastructure.adapter.out.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import org.motpassants.domain.port.out.ConfigurationPort;
import org.motpassants.domain.port.out.DemoDataPort;
import org.motpassants.domain.port.out.LoggingPort;
import org.motpassants.domain.port.out.SecureFileProcessingPort;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.UUID;

/**
 * Infrastructure DemoData adapter: reads CSV files from the data folder and inserts rows via repository ports.
 */
@ApplicationScoped
public class DemoDataJdbcAdapter implements DemoDataPort {
    @Inject ConfigurationPort configurationPort;
    @Inject SecureFileProcessingPort secureFileProcessingPort;
    @Inject LoggingPort log;
    @Inject javax.sql.DataSource dataSource;
    @Inject UserTransaction utx;

    @Override
    @Transactional(Transactional.TxType.NEVER)
    public void seed() {
        try {
            if (!configurationPort.isDemoEnabled()) return;
            if (countBooks() > 0) return; // already seeded

            Path dataDir = resolveDataDir();
            if (dataDir == null) { log.warn("Demo data directory not found; expected ./data"); return; }

            Map<String, String> authorIdToName = loadAuthors(dataDir.resolve("authors.csv"));
            Map<String, UUID> seriesUuidToId = loadSeries(dataDir.resolve("series.csv"));
            loadBooks(dataDir.resolve("books.csv"), authorIdToName, seriesUuidToId);
        } catch (Exception e) {
            log.error("Demo seed failed; continuing", e);
        }
    }

    // Defer image downloads until after we commit DB batches (tuple: [url, folder, subFolder, id])
    private void processDownloads(List<Object[]> tasks) {
        if (tasks == null || tasks.isEmpty()) return;
        for (Object[] t : tasks) {
            try { downloadImageToAssets((String) t[0], (String) t[1], (String) t[2], (UUID) t[3]); } catch (Exception ignored) {}
        }
        tasks.clear();
    }

    private long countBooks() {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement("SELECT COUNT(*) FROM books");
             var rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (Exception e) { return 0L; }
    }

    private Path resolveDataDir() {
        List<Path> candidates = List.of(
                Paths.get("data"),
                Paths.get("..", "data"),
                Paths.get("..", "..", "data")
        );
        for (Path p : candidates) {
            try {
                Path abs = p.toAbsolutePath().normalize();
                if (Files.isDirectory(abs)) return abs;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Map<String, String> loadAuthors(Path csvPath) throws Exception {
        Map<String, String> idToName = new HashMap<>();
        if (csvPath == null || !Files.exists(csvPath)) return idToName;
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        if (lines.isEmpty()) return idToName;
        String[] header = splitCsvLine(lines.get(0));
        Map<String, Integer> idx = headerIndex(header);
    int batch = 0;
    int inserted = 0;
    boolean txActive = false;
    List<Object[]> pendingDownloads = new ArrayList<>();
    try {
    for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] cols = splitCsvLine(line);
            String csvId = get(cols, idx.get("author_uuid"));
            String name = get(cols, idx.get("name"));
            String bio = get(cols, idx.get("biography"));
            String birth = get(cols, idx.get("birth_date"));
            String death = get(cols, idx.get("death_date"));
            String website = get(cols, idx.get("official_website"));
            String pic = get(cols, idx.get("picture_url"));

            if (name == null || name.isBlank()) continue;
            idToName.putIfAbsent(csvId, name);
    UUID existing = findAuthorByName(name);
        if (existing != null) continue;

            LocalDate birthDate = parseIsoDate(birth);
            LocalDate deathDate = parseIsoDate(death);
    UUID id = parseUuid(csvId);
    if (id == null) { log.warn("Skipping author without valid UUID: " + name); continue; }
    String bioJson = (bio == null || bio.isBlank()) ? null : toJson(Map.of("en", bio));
    Map<String, Object> metaMap = new HashMap<>();
    if (csvId != null && !csvId.isBlank()) metaMap.put("sourceId", csvId);
    if (pic != null && !pic.isBlank()) metaMap.put("imageUrl", pic);
    String metadata = metaMap.isEmpty() ? null : toJson(metaMap);
        boolean hasPic = pic != null && !pic.isBlank();
            if (!txActive) { utx.begin(); txActive = true; }
            try (var conn = dataSource.getConnection();
                var ps = conn.prepareStatement("INSERT INTO authors (id, name, sort_name, has_picture, bio, birth_date, death_date, website_url, metadata, created_at, updated_at) VALUES (?,?,?,?,CAST(? AS JSONB),?,?,?,CAST(? AS JSONB), NOW(), NOW())")) {
            ps.setObject(1, id);
            ps.setString(2, name);
            ps.setString(3, name);
            ps.setBoolean(4, hasPic);
            ps.setString(5, bioJson);
            if (birthDate != null) ps.setObject(6, birthDate); else ps.setNull(6, java.sql.Types.DATE);
            if (deathDate != null) ps.setObject(7, deathDate); else ps.setNull(7, java.sql.Types.DATE);
            ps.setString(8, website);
                ps.setString(9, metadata);
            ps.executeUpdate();
            } catch (Exception e) {
                log.error("Author insert failed at line " + (i + 1) + " for '" + name + "' (id=" + csvId + ")", e);
                try { if (txActive) { utx.rollback(); txActive = false; batch = 0; } } catch (Exception ignore) {}
                continue;
            }
            if (hasPic) pendingDownloads.add(new Object[] { pic, "authors", "pictures", id });
            inserted++;
            if (inserted % 10 == 0) { log.warn("Inserted " + inserted + " authors so far"); }
            batch++;
            if (batch >= 100) {
                try { utx.commit(); processDownloads(pendingDownloads); } catch (Exception e) { log.error("Author batch commit failed at line " + (i + 1), e); try { utx.rollback(); } catch (Exception ignore) {} finally { pendingDownloads.clear(); }
                }
                txActive = false; batch = 0;
            }
        }
        } finally {
            try { if (txActive) { utx.commit(); } } catch (Exception e) { log.error("Author final commit failed", e); try { utx.rollback(); } catch (Exception ignored) {} } finally { processDownloads(pendingDownloads); }
        }
        return idToName;
    }

    private Map<String, UUID> loadSeries(Path csvPath) throws Exception {
        Map<String, UUID> uuidToId = new HashMap<>();
        if (csvPath == null || !Files.exists(csvPath)) return uuidToId;
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        if (lines.isEmpty()) return uuidToId;
        String[] header = splitCsvLine(lines.get(0));
        Map<String, Integer> idx = headerIndex(header);
    int batch = 0;
    int inserted = 0;
    boolean txActive = false;
    List<Object[]> pendingDownloads = new ArrayList<>();
    try {
    for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] cols = splitCsvLine(line);
            String csvId = get(cols, idx.get("series_uuid"));
            String name = get(cols, idx.get("series_name"));
            String description = get(cols, idx.get("description"));
            String cover = get(cols, idx.get("cover_url"));
            if (name == null || name.isBlank()) continue;
    UUID seriesId = findSeriesByName(name);
        if (seriesId == null) {
    seriesId = parseUuid(csvId);
    if (seriesId == null) { log.warn("Skipping series without valid UUID: " + name); continue; }
    boolean hasPic = cover != null && !cover.isBlank();
    Map<String, Object> metaMap = new HashMap<>();
    if (csvId != null && !csvId.isBlank()) metaMap.put("sourceId", csvId);
    if (cover != null && !cover.isBlank()) metaMap.put("seriesImageUrl", cover);
    String meta = metaMap.isEmpty() ? null : toJson(metaMap);
            if (!txActive) { utx.begin(); txActive = true; }
            try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement("INSERT INTO series (id, name, sort_name, description, has_picture, book_count, metadata, created_at, updated_at) VALUES (?,?,?,?,?,0,CAST(? AS JSONB), NOW(), NOW())")) {
            ps.setObject(1, seriesId);
            ps.setString(2, name);
            ps.setString(3, name);
            ps.setString(4, description);
            ps.setBoolean(5, hasPic);
            ps.setString(6, meta);
            ps.executeUpdate();
            } catch (Exception e) {
                log.error("Series insert failed at line " + (i + 1) + " for '" + name + "' (id=" + csvId + ")", e);
                try { if (txActive) { utx.rollback(); txActive = false; batch = 0; } } catch (Exception ignore) {}
                continue;
            }
            if (hasPic) pendingDownloads.add(new Object[] { cover, "series", "covers", seriesId });
            inserted++;
            if (inserted % 10 == 0) { log.warn("Inserted " + inserted + " series so far"); }
            batch++;
            if (batch >= 100) {
                try { utx.commit(); processDownloads(pendingDownloads); } catch (Exception e) { log.error("Series batch commit failed at line " + (i + 1), e); try { utx.rollback(); } catch (Exception ignore) {} finally { pendingDownloads.clear(); }
                }
                txActive = false; batch = 0;
            }
        }
            if (csvId != null && !csvId.isBlank() && seriesId != null) uuidToId.putIfAbsent(csvId, seriesId);
        }
        } finally {
            try { if (txActive) { utx.commit(); } } catch (Exception e) { log.error("Series final commit failed", e); try { utx.rollback(); } catch (Exception ignored) {} } finally { processDownloads(pendingDownloads); }
        }
        return uuidToId;
    }

    private void loadBooks(Path csvPath, Map<String, String> authorIds, Map<String, UUID> seriesUuidToId) throws Exception {
        if (csvPath == null || !Files.exists(csvPath)) return;
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        if (lines.isEmpty()) return;
        String[] header = splitCsvLine(lines.get(0));
        Map<String, Integer> idx = headerIndex(header);
    int batch = 0;
    int inserted = 0;
    boolean txActive = false;
    List<Object[]> pendingDownloads = new ArrayList<>();
    try {
    for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] cols = splitCsvLine(line);
            String csvId = get(cols, idx.get("book_uuid"));
            String title = get(cols, idx.get("title"));
            String year = get(cols, idx.get("year"));
            String cover = get(cols, idx.get("cover_url"));
            String synopsis = get(cols, idx.get("synopsis"));
            String isbn = get(cols, idx.get("isbn"));
        // String language = get(cols, idx.get("language")); // language_code left NULL to avoid FK issues
            String seriesUuid = get(cols, idx.get("series_uuid"));
            String seriesName = get(cols, idx.get("series_name"));
            String seriesIndexStr = get(cols, idx.get("series_index"));
            if (title == null || title.isBlank()) continue;
        if (isbn != null && !isbn.isBlank() && findBookByIsbn(isbn) != null) continue;
            String relPath = "/demo/books/" + sanitizeFileName(csvId != null && !csvId.isBlank() ? csvId : title) + ".epub";
        if (findBookByPath(relPath) != null) continue;
    UUID id = parseUuid(csvId);
    if (id == null) { log.warn("Skipping book without valid UUID: " + title); continue; }
        String titleSort = title;
        String description = blankToNull(synopsis);
            Integer pubYear = parseYear(year);
        LocalDate pubDate = pubYear == null ? null : LocalDate.of(pubYear, 1, 1);
        long fileSize = (long) (500_000 + Math.random() * 2_000_000);
        String fileHash = generateRandomHash();
        boolean hasCover = true;
            ensureDemoBookFileExists(relPath);
            if (!txActive) { utx.begin(); txActive = true; }
            try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement("INSERT INTO books (id, title, title_sort, isbn, path, file_size, file_hash, has_cover, created_at, updated_at, publication_date, metadata) VALUES (?,?,?,?,?,?,?,?, NOW(), NOW(), ?, CAST(? AS JSONB))")) {
            ps.setObject(1, id);
            ps.setString(2, title);
            ps.setString(3, titleSort);
            if (isbn != null && !isbn.isBlank()) ps.setString(4, isbn); else ps.setNull(4, java.sql.Types.VARCHAR);
            ps.setString(5, relPath);
            ps.setLong(6, fileSize);
            ps.setString(7, fileHash);
            ps.setBoolean(8, hasCover);
            if (pubDate != null) ps.setObject(9, pubDate); else ps.setNull(9, java.sql.Types.DATE);
            ps.setString(10, description == null ? null : toJson(Map.of("synopsis", description)));
            ps.executeUpdate();
            } catch (Exception e) {
                log.error("Book insert failed at line " + (i + 1) + " for '" + title + "' (id=" + csvId + ")", e);
                try { if (txActive) { utx.rollback(); txActive = false; batch = 0; } } catch (Exception ignore) {}
                continue;
            }
            if (cover != null && !cover.isBlank()) pendingDownloads.add(new Object[] { cover, "books", "covers", id });
            try {
                UUID targetSeriesId = null;
        if (seriesUuid != null && !seriesUuid.isBlank()) targetSeriesId = seriesUuidToId.get(seriesUuid);
                if (targetSeriesId == null && seriesName != null && !seriesName.isBlank()) {
            targetSeriesId = findSeriesByName(seriesName);
                }
        if (targetSeriesId != null) {
                    Double sIdx = null;
                    if (seriesIndexStr != null && !seriesIndexStr.isBlank()) {
                        try { sIdx = Double.valueOf(seriesIndexStr.trim().replace(",", ".")); } catch (Exception ignored) {}
                    }
            linkBookToSeries(id, targetSeriesId, sIdx);
                }
            } catch (Exception e) { log.warn("Book-series link failed at line " + (i + 1) + " for book '" + title + "' : " + e.getMessage()); }
            inserted++;
            if (inserted % 10 == 0) { log.warn("Inserted " + inserted + " books so far"); }
            batch++;
            if (batch >= 100) {
                try { utx.commit(); processDownloads(pendingDownloads); } catch (Exception e) { log.error("Book batch commit failed at line " + (i + 1), e); try { utx.rollback(); } catch (Exception ignore) {} finally { pendingDownloads.clear(); }
                }
                txActive = false; batch = 0;
            }
        }
        } finally {
            try { if (txActive) { utx.commit(); } } catch (Exception e) { log.error("Book final commit failed", e); try { utx.rollback(); } catch (Exception ignored) {} } finally { processDownloads(pendingDownloads); }
        }
    }

    private String generateRandomHash() { String chars = "0123456789abcdef"; Random r = new Random(); StringBuilder sb = new StringBuilder(); for (int i = 0; i < 64; i++) sb.append(chars.charAt(r.nextInt(chars.length()))); return sb.toString(); }
    private Map<String, Integer> headerIndex(String[] header) { Map<String, Integer> idx = new HashMap<>(); for (int i = 0; i < header.length; i++) idx.put(header[i].trim(), i); return idx; }
    private String[] splitCsvLine(String line) { List<String> parts = new ArrayList<>(); StringBuilder cur = new StringBuilder(); boolean inQuotes = false; for (int i = 0; i < line.length(); i++) { char c = line.charAt(i); if (c == '"') { inQuotes = !inQuotes; } else if (c == ';' && !inQuotes) { parts.add(cur.toString().trim().replaceAll("^\"|\"$", "")); cur.setLength(0); } else { cur.append(c); } } parts.add(cur.toString().trim().replaceAll("^\"|\"$", "")); return parts.toArray(new String[0]); }
    private String get(String[] arr, Integer i) { if (arr == null || i == null) return null; return i >= 0 && i < arr.length ? arr[i] : null; }
    private LocalDate parseIsoDate(String s) { try { if (s == null || s.isBlank()) return null; return LocalDate.parse(s); } catch (Exception ignored) { return null; } }
    private Integer parseYear(String s) { try { if (s == null) return null; String digits = s.trim(); if (digits.length() > 4) digits = digits.substring(0, 4); return Integer.parseInt(digits); } catch (Exception ignored) { return null; } }
    private String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
    private String sanitizeFileName(String s) { if (s == null || s.isBlank()) return "book"; return s.replaceAll("[^a-zA-Z0-9._-]", "_"); }
    private UUID parseUuid(String s) { try { if (s == null || s.isBlank()) return null; return UUID.fromString(s.trim()); } catch (Exception ignored) { return null; } }
    private String toJson(Map<String, ?> map) {
        if (map == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, ?> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escapeJson(e.getKey())).append('"').append(':');
            Object v = e.getValue();
            if (v == null) sb.append("null"); else sb.append('"').append(escapeJson(String.valueOf(v))).append('"');
        }
        sb.append('}');
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return null;
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private UUID findAuthorByName(String name) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement("SELECT id FROM authors WHERE LOWER(name) = LOWER(?)")) {
            ps.setString(1, name);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Object v = rs.getObject(1);
                return (v instanceof UUID u) ? u : UUID.fromString(String.valueOf(v));
            }
        } catch (Exception e) { return null; }
    }

    private UUID findSeriesByName(String name) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement("SELECT id FROM series WHERE LOWER(name) = LOWER(?)")) {
            ps.setString(1, name);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Object v = rs.getObject(1);
                return (v instanceof UUID u) ? u : UUID.fromString(String.valueOf(v));
            }
        } catch (Exception e) { return null; }
    }

    private UUID findBookByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) return null;
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement("SELECT id FROM books WHERE isbn = ?")) {
            ps.setString(1, isbn);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Object v = rs.getObject(1);
                return (v instanceof UUID u) ? u : UUID.fromString(String.valueOf(v));
            }
        } catch (Exception e) { return null; }
    }

    private UUID findBookByPath(String path) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement("SELECT id FROM books WHERE path = ?")) {
            ps.setString(1, path);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Object v = rs.getObject(1);
                return (v instanceof UUID u) ? u : UUID.fromString(String.valueOf(v));
            }
        } catch (Exception e) { return null; }
    }

    private void linkBookToSeries(UUID bookId, UUID seriesId, Double seriesIndex) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement("INSERT INTO book_series (book_id, series_id, series_index) VALUES (?,?,?) ON CONFLICT (book_id, series_id) DO UPDATE SET series_index = EXCLUDED.series_index");
             var ps2 = conn.prepareStatement("UPDATE series s SET book_count = (SELECT COUNT(*) FROM book_series bs WHERE bs.series_id = s.id) WHERE s.id = ?")) {
            ps.setObject(1, bookId);
            ps.setObject(2, seriesId);
            ps.setObject(3, seriesIndex == null ? 1.0 : seriesIndex);
            ps.executeUpdate();
            ps2.setObject(1, seriesId);
            ps2.executeUpdate();
        } catch (Exception ignored) {}
    }

    private void downloadImageToAssets(String url, String folder, String subFolder, UUID id) {
        try {
            if (url == null || url.isBlank() || id == null) return;
            String baseDir = configurationPort.getStorageConfig().getBaseDir();
            Path base = secureFileProcessingPort.sanitizePath(baseDir, ".");
            Path targetDir = base.resolve(folder).resolve(subFolder);
            try { Files.createDirectories(targetDir); } catch (Exception ignored) {}
            Path target = targetDir.resolve(id.toString());
            // If the file already exists and has content, do not fetch again
            try {
                if (Files.exists(target)) {
                    long size = Files.size(target);
                    if (size > 0) return; // asset already present
                    // Remove zero-length placeholder if any
                    try { Files.deleteIfExists(target); } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}

            // Try to download the image bytes
            try {
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    final int MAX_IMAGE_BYTES = 5_000_000; // 5 MB safety limit
                    HttpClient client = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(5))
                            .followRedirects(HttpClient.Redirect.NORMAL)
                            .build();
                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofSeconds(10))
                            .header("User-Agent", "Librarie-DemoSeed/1.0")
                            .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                            .GET()
                            .build();
                    HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
                    int sc = resp.statusCode();
                    String ct = resp.headers().firstValue("content-type").orElse("").toLowerCase();
                    if (sc >= 200 && sc < 300 && (ct.startsWith("image/") || ct.isEmpty())) {
                        byte[] bytes = resp.body();
                        if (bytes != null && bytes.length > 0 && bytes.length <= MAX_IMAGE_BYTES) {
                            Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                        }
                    }
                }
            } catch (Exception ignored) { /* fall through to placeholder */ }
            // Do not create empty files when there is no content; leave absent if download failed
        } catch (Exception ignored) {}
    }

    private void ensureDemoBookFileExists(String relativePath) {
        try {
            if (relativePath == null || relativePath.isBlank()) return;
            String baseDir = configurationPort.getStorageConfig().getBaseDir();
            String safeRel = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            Path safeTarget = secureFileProcessingPort.sanitizePath(baseDir, safeRel);
            try { Files.createDirectories(safeTarget.getParent()); } catch (Exception ignored) {}
            if (!Files.exists(safeTarget)) {
                byte[] minimalEpub = new byte[] { 'P','K',3,4,20,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0 };
                Files.write(safeTarget, minimalEpub, StandardOpenOption.CREATE_NEW);
            }
        } catch (Exception ignored) {}
    }
}
