package org.roubinet.librarie.infrastructure.media;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class ImageCachingService {

    private static final Pattern OG_IMAGE_PATTERN = Pattern.compile(
        "<meta\\s+(?:property|name)\\s*=\\s*\\\"og:image\\\"\\s+content\\s*=\\s*\\\"([^\\\"]+)\\\"",
        Pattern.CASE_INSENSITIVE
    );

    public Response serveLocalFirstStrongETag(
            Request httpRequest,
            Path storageBaseDir,
            String folder,
            String subFolder,
            String id,
            String remoteUrl,
            byte[] fallbackSvg) {
        try {
            Path targetDir = storageBaseDir.resolve(folder).resolve(subFolder);
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
                    } catch (Exception ignored) {}
                    var ft = Files.getLastModifiedTime(localFile);
                    Date lastMod = new Date(ft.toMillis());
                    EntityTag strong = new EntityTag(sha256Hex(bytes));
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

            // Fallback SVG (default generic if not provided)
            byte[] svg = fallbackSvg != null ? fallbackSvg : getGenericFailoverSvg();
            EntityTag strong = new EntityTag(sha256Hex(svg));
            return Response.ok(svg, "image/svg+xml")
                .tag(strong)
                .header(jakarta.ws.rs.core.HttpHeaders.CACHE_CONTROL, "public, max-age=0, must-revalidate")
                .header(jakarta.ws.rs.core.HttpHeaders.VARY, "Accept-Encoding")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Internal server error: " + e.getMessage()).build();
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

    private static byte[] getGenericFailoverSvg() {
        String svg = """
                <svg xmlns='http://www.w3.org/2000/svg' width='320' height='480' viewBox='0 0 320 480'>
                    <defs>
                        <linearGradient id='g' x1='0' y1='0' x2='1' y2='1'>
                            <stop offset='0%' stop-color='#f0f0f0'/>
                            <stop offset='100%' stop-color='#d8d8d8'/>
                        </linearGradient>
                    </defs>
                    <rect width='100%' height='100%' fill='url(#g)'/>
                    <g fill='#9a9a9a'>
                        <rect x='60' y='120' width='200' height='20' rx='4'/>
                        <rect x='80' y='160' width='160' height='14' rx='4'/>
                        <rect x='80' y='200' width='160' height='14' rx='4'/>
                        <rect x='80' y='240' width='160' height='14' rx='4'/>
                    </g>
                </svg>
        """;
        return svg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private record ImageData(byte[] bytes, String contentType) {}
}
