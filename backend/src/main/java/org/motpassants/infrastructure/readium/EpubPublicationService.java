package org.motpassants.infrastructure.readium;

import org.motpassants.domain.core.model.Book;
import org.motpassants.infrastructure.config.LibrarieConfigProperties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Minimal EPUB parser to expose publication info (spine/resources) for Readium-style web serving.
 * Note: lightweight, no external deps, supports EPUB2/3 OPF basics.
 */
@ApplicationScoped
public class EpubPublicationService {

    private final LibrarieConfigProperties config;

    @Inject
    public EpubPublicationService(LibrarieConfigProperties config) {
        this.config = config;
    }

    public Optional<PublicationInfo> openPublication(Book book) {
        try {
            if (book.getPath() == null || !book.getPath().toLowerCase().endsWith(".epub")) {
                return Optional.empty();
            }
            Path basePath = Paths.get(config.storage().baseDir()).normalize();
            String stored = book.getPath();
            String relative = stored.startsWith("/") || stored.startsWith("\\") ? stored.substring(1) : stored;
            Path epubPath = basePath.resolve(relative).normalize();
            if (!epubPath.startsWith(basePath) || !Files.exists(epubPath)) {
                return Optional.empty();
            }
            return openPublication(epubPath);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Open publication by absolute path (no storage base constraints). */
    public Optional<PublicationInfo> openPublication(Path epubPath) {
        try {
            if (epubPath == null || !Files.exists(epubPath) || !epubPath.toString().toLowerCase().endsWith(".epub")) {
                return Optional.empty();
            }
            try (ZipFile zip = new ZipFile(epubPath.toFile())) {
                String opfPath = locateOpfPath(zip);
                if (opfPath == null) {
                    return Optional.empty();
                }
                OpfData opf = parseOpf(zip, opfPath);
                PublicationInfo info = new PublicationInfo();
                info.setEpubFile(epubPath);
                info.setOpfPath(opfPath);
                info.setTitle(opf.title);
                info.setLanguage(opf.language);
                info.setSpineResourceHrefs(opf.spineHrefs);
                info.setManifestHrefToMediaType(opf.hrefToType);
                info.setOpfDir(opf.opfDir);
                info.setManifestIdToHref(opf.idToHref);
                info.setManifestIdToProperties(opf.idToProperties);
                info.setNcxId(opf.ncxId);
                return Optional.of(info);
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<ZipEntry> getEntry(PublicationInfo pub, String entryPathInZip) {
        try (ZipFile zip = new ZipFile(pub.getEpubFile().toFile())) {
            ZipEntry entry = zip.getEntry(entryPathInZip);
            if (entry == null) return Optional.empty();
            // We cannot return ZipEntry with closed ZipFile; use stream method instead.
            return Optional.of(entry);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public InputStream openEntryStream(PublicationInfo pub, String entryPathInZip) throws Exception {
        ZipFile zip = new ZipFile(pub.getEpubFile().toFile());
        ZipEntry entry = zip.getEntry(entryPathInZip);
        if (entry == null) {
            zip.close();
            throw new IllegalArgumentException("Resource not found in EPUB: " + entryPathInZip);
        }
        // The caller must ensure to close the returned InputStream; we wrap to also close the ZipFile when stream closes.
        InputStream is = zip.getInputStream(entry);
        return new java.io.FilterInputStream(is) {
            @Override
            public void close() throws java.io.IOException {
                try {
                    super.close();
                } finally {
                    zip.close();
                }
            }
        };
    }

    public String buildZipPath(String opfDir, String href) {
        if (href == null) return null;
        String normalizedHref = href.replace("\\", "/");
        if (opfDir == null || opfDir.isBlank() || opfDir.equals("/")) {
            return normalizedHref.startsWith("/") ? normalizedHref.substring(1) : normalizedHref;
        }
        String dir = opfDir.endsWith("/") ? opfDir : opfDir + "/";
        String path = dir + normalizedHref;
        // Remove any leading slash
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private String locateOpfPath(ZipFile zip) throws Exception {
        ZipEntry container = zip.getEntry("META-INF/container.xml");
        if (container == null) return null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        try (InputStream is = zip.getInputStream(container)) {
            Document doc = builder.parse(is);
            XPath xp = XPathFactory.newInstance().newXPath();
            // Try with namespace-agnostic search
            String expr = "/*[local-name()='container']/*[local-name()='rootfiles']/*[local-name()='rootfile']/@full-path";
            String path = (String) xp.evaluate(expr, doc, XPathConstants.STRING);
            return (path != null && !path.isBlank()) ? path : null;
        }
    }

    private OpfData parseOpf(ZipFile zip, String opfPath) throws Exception {
        ZipEntry opfEntry = zip.getEntry(opfPath);
        if (opfEntry == null) throw new IllegalStateException("OPF not found: " + opfPath);
        String opfDir = opfPath.contains("/") ? opfPath.substring(0, opfPath.lastIndexOf('/')) : "";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc;
        try (InputStream is = zip.getInputStream(opfEntry)) {
            doc = builder.parse(is);
        }
        XPath xp = XPathFactory.newInstance().newXPath();

        String title = (String) xp.evaluate("/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='title'][1]/text()", doc, XPathConstants.STRING);
        String language = (String) xp.evaluate("/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='language'][1]/text()", doc, XPathConstants.STRING);

        // manifest map id -> href & media-type & properties
        Map<String, String> idToHref = new LinkedHashMap<>();
        Map<String, String> hrefToType = new LinkedHashMap<>();
        Map<String, String> idToProperties = new LinkedHashMap<>();
        NodeList items = (NodeList) xp.evaluate("/*[local-name()='package']/*[local-name()='manifest']/*[local-name()='item']", doc, XPathConstants.NODESET);
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            String id = getAttr(item, "id");
            String href = getAttr(item, "href");
            String mediaType = getAttr(item, "media-type");
            String properties = getAttr(item, "properties");
            if (id != null && href != null) {
                idToHref.put(id, href);
                if (mediaType != null) hrefToType.put(href, mediaType);
                if (properties != null) idToProperties.put(id, properties);
            }
        }

        // spine idrefs -> hrefs
        List<String> spineHrefs = new ArrayList<>();
        NodeList itemrefs = (NodeList) xp.evaluate("/*[local-name()='package']/*[local-name()='spine']/*[local-name()='itemref']", doc, XPathConstants.NODESET);
        for (int i = 0; i < itemrefs.getLength(); i++) {
            Node itemref = itemrefs.item(i);
            String idref = getAttr(itemref, "idref");
            if (idref != null) {
                String href = idToHref.get(idref);
                if (href != null) {
                    spineHrefs.add(href);
                }
            }
        }

        // EPUB2 NCX id (spine @toc)
        String ncxId = (String) xp.evaluate("/*[local-name()='package']/*[local-name()='spine']/@toc", doc, XPathConstants.STRING);

        OpfData data = new OpfData();
        data.title = title;
        data.language = language;
        data.spineHrefs = spineHrefs;
        data.hrefToType = hrefToType;
        data.opfDir = opfDir;
        data.idToHref = idToHref;
        data.idToProperties = idToProperties;
        data.ncxId = (ncxId != null && !ncxId.isBlank()) ? ncxId : null;
        return data;
    }

    private static String getAttr(Node node, String name) {
        if (node == null || node.getAttributes() == null) return null;
        Node a = node.getAttributes().getNamedItem(name);
        return a != null ? a.getNodeValue() : null;
    }

    public String guessContentType(String hrefOrName) {
        if (hrefOrName == null) return "application/octet-stream";
        String lower = hrefOrName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".xhtml") || lower.endsWith(".html") || lower.endsWith(".htm")) return "application/xhtml+xml";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".ttf")) return "font/ttf";
        if (lower.endsWith(".otf")) return "font/otf";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".mp4")) return "video/mp4";
        return "application/octet-stream";
    }

    private static class OpfData {
        String title;
        String language;
        List<String> spineHrefs;
        Map<String, String> hrefToType;
        String opfDir;
    Map<String, String> idToHref;
    Map<String, String> idToProperties;
    String ncxId;
    }

    public static class PublicationInfo {
        private Path epubFile;
        private String opfPath;
        private String opfDir;
        private String title;
        private String language;
        private List<String> spineResourceHrefs;
        private Map<String, String> manifestHrefToMediaType;
    private Map<String, String> manifestIdToHref;
    private Map<String, String> manifestIdToProperties;
    private String ncxId;

        public Path getEpubFile() { return epubFile; }
        public void setEpubFile(Path epubFile) { this.epubFile = epubFile; }
        public String getOpfPath() { return opfPath; }
        public void setOpfPath(String opfPath) { this.opfPath = opfPath; }
        public String getOpfDir() { return opfDir; }
        public void setOpfDir(String opfDir) { this.opfDir = opfDir; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
        public List<String> getSpineResourceHrefs() { return spineResourceHrefs; }
        public void setSpineResourceHrefs(List<String> spineResourceHrefs) { this.spineResourceHrefs = spineResourceHrefs; }
        public Map<String, String> getManifestHrefToMediaType() { return manifestHrefToMediaType; }
        public void setManifestHrefToMediaType(Map<String, String> manifestHrefToMediaType) { this.manifestHrefToMediaType = manifestHrefToMediaType; }
        public Map<String, String> getManifestIdToHref() { return manifestIdToHref; }
        public void setManifestIdToHref(Map<String, String> manifestIdToHref) { this.manifestIdToHref = manifestIdToHref; }
        public Map<String, String> getManifestIdToProperties() { return manifestIdToProperties; }
        public void setManifestIdToProperties(Map<String, String> manifestIdToProperties) { this.manifestIdToProperties = manifestIdToProperties; }
        public String getNcxId() { return ncxId; }
        public void setNcxId(String ncxId) { this.ncxId = ncxId; }
    }

    /** Basic metadata extracted from OPF. */
    public static class CoreMetadata {
        public String title;
        public String language;
        public List<String> creators = java.util.List.of();
        public String publisher;
        public String identifier;
        public String isbn;
        public String description;
        public String date;
        public List<String> subjects = java.util.List.of();
    }

    /** Extract common metadata fields from OPF. */
    public Optional<CoreMetadata> extractCoreMetadata(PublicationInfo pub) {
        try (ZipFile zip = new ZipFile(pub.getEpubFile().toFile())) {
            ZipEntry opfEntry = zip.getEntry(pub.getOpfPath());
            if (opfEntry == null) return Optional.empty();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc;
            try (InputStream is = zip.getInputStream(opfEntry)) { doc = builder.parse(is); }
            XPath xp = XPathFactory.newInstance().newXPath();
            CoreMetadata cm = new CoreMetadata();
            cm.title = text(xp, "/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='title'][1]/text()", doc);
            cm.language = text(xp, "/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='language'][1]/text()", doc);
            cm.publisher = text(xp, "/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='publisher'][1]/text()", doc);
            cm.description = text(xp, "/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='description'][1]/text()", doc);
            cm.date = text(xp, "/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='date'][1]/text()", doc);
            // creators
            NodeList creators = (NodeList) xp.evaluate("/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='creator']/text()", doc, XPathConstants.NODESET);
            java.util.List<String> cList = new java.util.ArrayList<>();
            for (int i = 0; i < creators.getLength(); i++) cList.add(creators.item(i).getNodeValue());
            cm.creators = java.util.List.copyOf(cList);
            // identifiers
            NodeList ids = (NodeList) xp.evaluate("/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='identifier']/text()", doc, XPathConstants.NODESET);
            if (ids.getLength() > 0) {
                cm.identifier = ids.item(0).getNodeValue();
                String id = cm.identifier != null ? cm.identifier.toLowerCase(Locale.ROOT) : null;
                if (id != null) {
                    String onlyDigits = id.replaceAll("[^0-9xX]", "");
                    if (onlyDigits.length() == 13 || onlyDigits.length() == 10) cm.isbn = onlyDigits;
                    if (id.startsWith("urn:isbn:") || id.startsWith("isbn:")) cm.isbn = id.replaceFirst("^.*isbn:?", "");
                }
            }
            // subjects
            NodeList subs = (NodeList) xp.evaluate("/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='subject']/text()", doc, XPathConstants.NODESET);
            java.util.List<String> sList = new java.util.ArrayList<>();
            for (int i = 0; i < subs.getLength(); i++) sList.add(subs.item(i).getNodeValue());
            cm.subjects = java.util.List.copyOf(sList);
            return Optional.of(cm);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Try to find the cover image href inside the zip using OPF hints. */
    public Optional<String> findCoverImageZipPath(PublicationInfo pub) {
        try (ZipFile zip = new ZipFile(pub.getEpubFile().toFile())) {
            ZipEntry opfEntry = zip.getEntry(pub.getOpfPath());
            if (opfEntry == null) return Optional.empty();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc;
            try (InputStream is = zip.getInputStream(opfEntry)) { doc = builder.parse(is); }
            XPath xp = XPathFactory.newInstance().newXPath();
            // EPUB3 cover-image property
            if (pub.getManifestIdToProperties() != null && pub.getManifestIdToHref() != null) {
                for (var e : pub.getManifestIdToProperties().entrySet()) {
                    String props = e.getValue();
                    if (props != null && props.contains("cover-image")) {
                        String href = pub.getManifestIdToHref().get(e.getKey());
                        if (href != null) return Optional.of(buildZipPath(pub.getOpfDir(), href));
                    }
                }
            }
            // EPUB2 meta name="cover" content="id"
            String coverId = text(xp, "/*[local-name()='package']/*[local-name()='metadata']/*[local-name()='meta'][@name='cover']/@content", doc);
            if (coverId != null && pub.getManifestIdToHref() != null) {
                String href = pub.getManifestIdToHref().get(coverId);
                if (href != null) return Optional.of(buildZipPath(pub.getOpfDir(), href));
            }
            // Fallback: first jpg/png in manifest
            if (pub.getManifestHrefToMediaType() != null) {
                for (var e : pub.getManifestHrefToMediaType().entrySet()) {
                    String type = e.getValue();
                    if (type != null && (type.equalsIgnoreCase("image/jpeg") || type.equalsIgnoreCase("image/png"))) {
                        return Optional.of(buildZipPath(pub.getOpfDir(), e.getKey()));
                    }
                }
            }
        } catch (Exception ignore) { }
        return Optional.empty();
    }

    /** Read a zip entry to bytes. */
    public Optional<byte[]> readEntryBytes(PublicationInfo pub, String entryPathInZip) {
        try (ZipFile zip = new ZipFile(pub.getEpubFile().toFile())) {
            ZipEntry entry = zip.getEntry(entryPathInZip);
            if (entry == null) return Optional.empty();
            try (InputStream is = zip.getInputStream(entry)) {
                return Optional.of(is.readAllBytes());
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String text(XPath xp, String expr, Document doc) throws Exception {
        String s = (String) xp.evaluate(expr, doc, XPathConstants.STRING);
        return (s != null && !s.isBlank()) ? s : null;
    }

    /**
     * Fallback: try to find an image for the first page by scanning spine items.
     * - If a spine item is an image type, use it directly
     * - Else parse first XHTML spine items to find first <img src> and resolve to a zip path
     */
    public Optional<String> findFirstPageImageZipPath(PublicationInfo pub) {
        try (ZipFile zip = new ZipFile(pub.getEpubFile().toFile())) {
            // Direct image in spine
            if (pub.getSpineResourceHrefs() != null && pub.getManifestHrefToMediaType() != null) {
                for (String href : pub.getSpineResourceHrefs()) {
                    String type = pub.getManifestHrefToMediaType().get(href);
                    if (type != null && type.toLowerCase(Locale.ROOT).startsWith("image/")) {
                        return Optional.of(buildZipPath(pub.getOpfDir(), href));
                    }
                }
            }
            // Parse first few XHTMLs for <img>
            int checked = 0;
            if (pub.getSpineResourceHrefs() != null) {
                for (String href : pub.getSpineResourceHrefs()) {
                    if (checked++ > 5) break; // limit work
                    String type = pub.getManifestHrefToMediaType() != null ? pub.getManifestHrefToMediaType().get(href) : null;
                    if (type == null || (!type.contains("xhtml") && !type.contains("html"))) continue;
                    String zipPath = buildZipPath(pub.getOpfDir(), href);
                    ZipEntry entry = zip.getEntry(zipPath);
                    if (entry == null) continue;
                    String html;
                    try (InputStream is = zip.getInputStream(entry)) { html = new String(is.readAllBytes()); }
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("<img[^>]+src=\\\"([^\\\"]+)\\\"", java.util.regex.Pattern.CASE_INSENSITIVE).matcher(html);
                    if (m.find()) {
                        String imgHref = m.group(1);
                        String baseDir = zipPath.contains("/") ? zipPath.substring(0, zipPath.lastIndexOf('/')) : "";
                        String resolved = resolveZipPath(baseDir, imgHref);
                        return Optional.of(resolved);
                    }
                }
            }
        } catch (Exception ignore) {}
        return Optional.empty();
    }

    /**
     * Extract TOC links (zip-relative paths, may include fragments) from EPUB3 nav or EPUB2 NCX.
     */
    public List<String> extractTocLinks(PublicationInfo pub) throws Exception {
        List<String> results = new ArrayList<>();
        try (ZipFile zip = new ZipFile(pub.getEpubFile().toFile())) {
            // EPUB3 nav (properties contains 'nav')
            String navHref = null;
            if (pub.getManifestIdToProperties() != null && pub.getManifestIdToHref() != null) {
                for (var e : pub.getManifestIdToProperties().entrySet()) {
                    String id = e.getKey();
                    String props = e.getValue();
                    if (props != null && props.contains("nav")) {
                        navHref = pub.getManifestIdToHref().get(id);
                        if (navHref != null) break;
                    }
                }
            }
            if (navHref != null) {
                String navZipPath = buildZipPath(pub.getOpfDir(), navHref);
                ZipEntry navEntry = zip.getEntry(navZipPath);
                if (navEntry != null) {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(true);
                    DocumentBuilder builder = dbf.newDocumentBuilder();
                    Document doc;
                    try (InputStream is = zip.getInputStream(navEntry)) { doc = builder.parse(is); }
                    XPath xp = XPathFactory.newInstance().newXPath();
                    // Links under nav[epub:type~='toc'] or role='doc-toc'
                    NodeList hrefs = (NodeList) xp.evaluate(
                        "//*[local-name()='nav' and (contains(@*[local-name()='type'], 'toc') or @role='doc-toc')]//*[local-name()='a']/@href",
                        doc, XPathConstants.NODESET);
                    String baseDir = navZipPath.contains("/") ? navZipPath.substring(0, navZipPath.lastIndexOf('/')) : "";
                    for (int i = 0; i < hrefs.getLength(); i++) {
                        String href = hrefs.item(i).getNodeValue();
                        results.add(resolveZipPath(baseDir, href));
                    }
                }
            }

            // EPUB2 NCX fallback
            if (results.isEmpty()) {
                String ncxId = pub.getNcxId();
                String ncxHref = null;
                if (ncxId != null && pub.getManifestIdToHref() != null) {
                    ncxHref = pub.getManifestIdToHref().get(ncxId);
                } else if (pub.getManifestHrefToMediaType() != null) {
                    for (var e : pub.getManifestHrefToMediaType().entrySet()) {
                        if ("application/x-dtbncx+xml".equalsIgnoreCase(e.getValue())) { ncxHref = e.getKey(); break; }
                    }
                }
                if (ncxHref != null) {
                    String ncxZipPath = buildZipPath(pub.getOpfDir(), ncxHref);
                    ZipEntry ncxEntry = zip.getEntry(ncxZipPath);
                    if (ncxEntry != null) {
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setNamespaceAware(true);
                        DocumentBuilder builder = dbf.newDocumentBuilder();
                        Document doc;
                        try (InputStream is = zip.getInputStream(ncxEntry)) { doc = builder.parse(is); }
                        XPath xp = XPathFactory.newInstance().newXPath();
                        NodeList hrefs = (NodeList) xp.evaluate("/*[local-name()='ncx']/*[local-name()='navMap']//*[local-name()='content']/@src", doc, XPathConstants.NODESET);
                        String baseDir = ncxZipPath.contains("/") ? ncxZipPath.substring(0, ncxZipPath.lastIndexOf('/')) : "";
                        for (int i = 0; i < hrefs.getLength(); i++) {
                            String href = hrefs.item(i).getNodeValue();
                            results.add(resolveZipPath(baseDir, href));
                        }
                    }
                }
            }
        }
        // Deduplicate while preserving order
        LinkedHashSet<String> set = new LinkedHashSet<>(results);
        return new ArrayList<>(set);
    }

    private String resolveZipPath(String baseDir, String href) {
        if (href == null) return null;
        String fragment = null;
        int hash = href.indexOf('#');
        if (hash >= 0) { fragment = href.substring(hash); href = href.substring(0, hash); }
        String normalized = href.replace("\\", "/");
        java.nio.file.Path base = baseDir == null || baseDir.isBlank() ? java.nio.file.Paths.get("") : java.nio.file.Paths.get(baseDir);
        java.nio.file.Path resolved = base.resolve(normalized).normalize();
        String path = resolved.toString().replace('\\', '/');
        if (path.startsWith("/")) path = path.substring(1);
        return fragment != null ? path + fragment : path;
    }
}
