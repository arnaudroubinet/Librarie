package org.motpassants.infrastructure.media;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.motpassants.domain.port.out.SecureFileProcessingPort;
import org.motpassants.infrastructure.config.LibrarieConfigProperties;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for caching and serving images with security validation.
 */
@ApplicationScoped
public class ImageCachingService {
    
    private static final Logger LOG = Logger.getLogger(ImageCachingService.class);
    private static final Pattern OG_IMAGE_PATTERN = Pattern.compile(
        "<meta\\s+(?:property|name)\\s*=\\s*\\\"og:image\\\"\\s+content\\s*=\\s*\\\"([^\\\"]+)\\\"",
        Pattern.CASE_INSENSITIVE
    );
    
    private final LibrarieConfigProperties config;
    private final SecureFileProcessingPort secureFileProcessingPort;
    private final ConcurrentMap<String, byte[]> imageCache = new ConcurrentHashMap<>();
    
    @Inject
    public ImageCachingService(LibrarieConfigProperties config, 
                              SecureFileProcessingPort secureFileProcessingPort) {
        this.config = config;
        this.secureFileProcessingPort = secureFileProcessingPort;
    }
    
    /**
     * Gets image bytes from cache or loads from file system.
     */
    public byte[] getImage(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        
        // Check cache first
        byte[] cachedImage = imageCache.get(relativePath);
        if (cachedImage != null) {
            LOG.debug("Serving image from cache: " + relativePath);
            return cachedImage;
        }
        
        // Load from file system
        try {
            Path imagePath = secureFileProcessingPort.sanitizePath(
                config.storage().baseDir(), relativePath);
            
            if (!Files.exists(imagePath)) {
                LOG.warn("Image file not found: " + imagePath);
                return null;
            }
            
            if (!secureFileProcessingPort.isValidImageFile(imagePath)) {
                LOG.warn("Invalid image file: " + imagePath);
                return null;
            }
            
            byte[] imageBytes = Files.readAllBytes(imagePath);
            
            // Cache the image if it's not too large (max 1MB)
            if (imageBytes.length <= 1024 * 1024) {
                imageCache.put(relativePath, imageBytes);
                LOG.debug("Cached image: " + relativePath);
            }
            
            return imageBytes;
            
        } catch (IOException e) {
            LOG.error("Error reading image file: " + relativePath, e);
            return null;
        } catch (SecurityException e) {
            LOG.warn("Security violation accessing image: " + relativePath, e);
            return null;
        }
    }
    
    /**
     * Serve image using a local-first strategy with strong ETag/Last-Modified and optional remote hydration.
     * If a local file exists at baseDir/folder/subFolder/id, it is served with conditional GET support.
     * If not and a remoteUrl is provided, the service attempts to resolve the image (supports og:image from HTML),
     * stores it locally, and serves it. If all fails, returns the provided fallback SVG (image/svg+xml).
     */
    public Response serveLocalFirstStrongETag(
            Request httpRequest,
            Path storageBaseDir,
            String folder,
            String subFolder,
            String id,
            String remoteUrl,
            byte[] fallbackSvg) {
        try {
            Path safeBase = secureFileProcessingPort.sanitizePath(storageBaseDir.toString(), ".");
            Path targetDir = safeBase.resolve(folder).resolve(subFolder);
            try { Files.createDirectories(targetDir); } catch (Exception ignored) {}
            Path localFile = targetDir.resolve(id);

            // Serve from disk with Last-Modified and strong ETag
            if (Files.exists(localFile) && Files.isRegularFile(localFile)) {
                var ft = Files.getLastModifiedTime(localFile);
                Date lastMod = new Date(ft.toMillis());

                Response.ResponseBuilder preDate = httpRequest.evaluatePreconditions(lastMod);
                if (preDate != null) {
                    return preDate
                        .lastModified(lastMod)
                        .header(jakarta.ws.rs.core.HttpHeaders.CACHE_CONTROL, "public, max-age=0, must-revalidate")
                        .header(jakarta.ws.rs.core.HttpHeaders.VARY, "Accept-Encoding")
                        .build();
                }

                byte[] bytes = Files.readAllBytes(localFile);
                String mime = Files.probeContentType(localFile);
                if (mime == null || !mime.startsWith("image/")) mime = "image/jpeg";
                EntityTag strong = new EntityTag(sha256Hex(bytes));

                Response.ResponseBuilder pre = httpRequest.evaluatePreconditions(lastMod, strong);
                if (pre != null) {
                    return pre
                        .tag(strong)
                        .lastModified(lastMod)
                        .header(jakarta.ws.rs.core.HttpHeaders.CACHE_CONTROL, "public, max-age=0, must-revalidate")
                        .header(jakarta.ws.rs.core.HttpHeaders.VARY, "Accept-Encoding")
                        .build();
                }
                return Response.ok(bytes, mime)
                    .tag(strong)
                    .lastModified(lastMod)
                    .header(jakarta.ws.rs.core.HttpHeaders.CACHE_CONTROL, "public, max-age=0, must-revalidate")
                    .header(jakarta.ws.rs.core.HttpHeaders.VARY, "Accept-Encoding")
                    .build();
            }

            // Populate from remote if URL provided
            if (remoteUrl != null && (remoteUrl.startsWith("http://") || remoteUrl.startsWith("https://"))) {
                Optional<ImageData> img = resolveImage(remoteUrl);
                if (img.isPresent()) {
                    byte[] bytes = img.get().bytes;
                    String contentType = img.get().contentType != null && img.get().contentType.toLowerCase().startsWith("image/")
                        ? img.get().contentType
                        : "image/jpeg";
                    try {
                        Files.write(localFile, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    } catch (Exception e) {
                        LOG.debug("Failed writing hydrated image locally: " + localFile + ", " + e.getMessage());
                    }
                    var ft = Files.getLastModifiedTime(localFile);
                    Date lastMod = new Date(ft.toMillis());
                    EntityTag strong = new EntityTag(sha256Hex(bytes));
                    // Evaluate preconditions (rare on first fetch, but safe)
                    Response.ResponseBuilder pre = httpRequest.evaluatePreconditions(lastMod, strong);
                    if (pre != null) {
                        return pre
                            .tag(strong)
                            .lastModified(lastMod)
                            .header(jakarta.ws.rs.core.HttpHeaders.CACHE_CONTROL, "public, max-age=0, must-revalidate")
                            .header(jakarta.ws.rs.core.HttpHeaders.VARY, "Accept-Encoding")
                            .build();
                    }
                    return Response.ok(bytes, contentType)
                        .tag(strong)
                        .lastModified(lastMod)
                        .header(jakarta.ws.rs.core.HttpHeaders.CACHE_CONTROL, "public, max-age=0, must-revalidate")
                        .header(jakarta.ws.rs.core.HttpHeaders.VARY, "Accept-Encoding")
                        .build();
                }
            }

            // If no fallback requested, return 404 to let frontend render its own placeholder
            if (fallbackSvg == null) {
                return Response.status(Response.Status.NOT_FOUND)
                    .header(jakarta.ws.rs.core.HttpHeaders.CACHE_CONTROL, "public, max-age=0, must-revalidate")
                    .header(jakarta.ws.rs.core.HttpHeaders.VARY, "Accept-Encoding")
                    .build();
            }

            // Fallback SVG with ETag preconditions
            byte[] svg = fallbackSvg;
            EntityTag strong = new EntityTag(sha256Hex(svg));
            Response.ResponseBuilder pre = httpRequest.evaluatePreconditions(strong);
            if (pre != null) {
                return pre
                    .tag(strong)
                    .header(jakarta.ws.rs.core.HttpHeaders.CACHE_CONTROL, "public, max-age=0, must-revalidate")
                    .header(jakarta.ws.rs.core.HttpHeaders.VARY, "Accept-Encoding")
                    .build();
            }
            return Response.ok(svg, "image/svg+xml")
                .tag(strong)
                .header(jakarta.ws.rs.core.HttpHeaders.CACHE_CONTROL, "public, max-age=0, must-revalidate")
                .header(jakarta.ws.rs.core.HttpHeaders.VARY, "Accept-Encoding")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage())
                .build();
        }
    }

    private Optional<ImageData> resolveImage(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        HttpResponse<byte[]> resp = fetch(client, url);
        String contentType = resp.headers().firstValue("content-type").orElse("").toLowerCase();
        if (contentType.startsWith("image/")) {
            return Optional.of(new ImageData(resp.body(), contentType));
        }
        if (contentType.contains("text/html")) {
            String html = new String(resp.body());
            Matcher m = OG_IMAGE_PATTERN.matcher(html);
            if (m.find()) {
                String ogImage = m.group(1);
                if (ogImage.startsWith("//")) ogImage = "https:" + ogImage;
                HttpResponse<byte[]> imgResp = fetch(client, ogImage);
                String imgType = imgResp.headers().firstValue("content-type").orElse("image/jpeg");
                if (imgType.toLowerCase().startsWith("image/")) {
                    return Optional.of(new ImageData(imgResp.body(), imgType));
                }
            }
        }
        return Optional.empty();
    }

    private HttpResponse<byte[]> fetch(HttpClient client, String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .header("User-Agent", "Librarie/1.0 (+https://localhost)")
            .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
            .header("Accept-Encoding", "gzip, deflate, br")
            .GET()
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofByteArray());
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "len-" + (data == null ? 0 : data.length);
        }
    }

    private record ImageData(byte[] bytes, String contentType) {}

    /**
     * Gets the MIME type for an image file.
     */
    public String getImageMimeType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }
        
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFilename.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFilename.endsWith(".bmp")) {
            return "image/bmp";
        }
        
        return "application/octet-stream";
    }
    
    /**
     * Clears the image cache.
     */
    public void clearCache() {
        imageCache.clear();
        LOG.info("Image cache cleared");
    }
    
    /**
     * Gets cache statistics.
     */
    public CacheStats getCacheStats() {
        return new CacheStats(imageCache.size());
    }
    
    public static class CacheStats {
        private final int size;
        
        public CacheStats(int size) {
            this.size = size;
        }
        
        public int getSize() {
            return size;
        }
    }
}