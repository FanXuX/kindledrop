package io.kindledrop.engine.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;

public class SecureDownloader {

    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "raw.githubusercontent.com",
            "github.com"
            // Add "objects.githubusercontent.com" later if you support releases assets.
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "epub", "mobi", "azw3");

    private final HttpClient client;

    public SecureDownloader() {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER) // we handle redirects manually to enforce allowlist
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public DownloadResult downloadToTemp(URI uri, String fileNameHint, long maxBytes) throws IOException, InterruptedException {
        validateHost(uri);
        String fileName = sanitizeFileName(fileNameHint);
        validateExtension(fileName);

        // Follow redirects up to 5 times, enforcing allowlist each step.
        URI current = uri;
        HttpResponse<InputStream> resp = null;

        for (int i = 0; i < 6; i++) {
            HttpRequest req = HttpRequest.newBuilder(current)
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .header("User-Agent", "KindleDrop/0.1")
                    .build();

            resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());

            int code = resp.statusCode();
            if (code >= 300 && code < 400) {
                String loc = resp.headers().firstValue("location").orElseThrow(() ->
                        new IOException("Redirect without Location header"));
                current = current.resolve(loc);
                validateHost(current);
                continue;
            }
            break;
        }

        if (resp == null) throw new IOException("No response received.");

        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("Download failed with status " + resp.statusCode());
        }

        long declaredLen = resp.headers().firstValueAsLong("content-length").orElse(-1);
        if (declaredLen > maxBytes) {
            throw new IOException("File too large (content-length " + declaredLen + " bytes). Max allowed is " + maxBytes + " bytes.");
        }

        String contentType = resp.headers().firstValue("content-type").orElse("");

        Path tmpDir = Files.createTempDirectory("kindledrop-");
        Path tmpFile = tmpDir.resolve(fileName);

        long written = 0;
        try (InputStream in = resp.body()) {
            // stream with hard cap
            try (var out = Files.newOutputStream(tmpFile)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) {
                    written += r;
                    if (written > maxBytes) {
                        throw new IOException("File too large (streamed > " + maxBytes + " bytes).");
                    }
                    out.write(buf, 0, r);
                }
            }
        } catch (IOException e) {
            // cleanup temp dir on failure
            safeDeleteRecursive(tmpDir);
            throw e;
        }

        return new DownloadResult(tmpFile, fileName, written, contentType);
    }

    private void validateHost(URI uri) {
        String host = uri.getHost();
        if (host == null || !ALLOWED_HOSTS.contains(host.toLowerCase())) {
            throw new IllegalArgumentException("Blocked host: " + host);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Only https URLs are allowed.");
        }
    }

    private void validateExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx < 0) {
            throw new IllegalArgumentException("File must have an extension (pdf/epub/mobi/azw3).");
        }
        String ext = fileName.substring(idx + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Unsupported file extension: " + ext);
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "document.pdf";
        // remove path separators and control chars
        name = name.replaceAll("[\\/\r\n\t\0]", "_");
        // limit length
        if (name.length() > 150) name = name.substring(name.length() - 150);
        return name;
    }

    private void safeDeleteRecursive(Path dir) {
        try {
            if (Files.notExists(dir)) return;
            Files.walk(dir)
                    .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                    });
        } catch (Exception ignored) {}
    }
}
