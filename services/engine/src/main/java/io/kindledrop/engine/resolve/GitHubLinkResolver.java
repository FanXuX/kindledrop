package io.kindledrop.engine.resolve;

import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class GitHubLinkResolver {

    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "github.com",
            "raw.githubusercontent.com"
            // Add "objects.githubusercontent.com" when you support release assets safely.
    );

    public ResolvedLink resolve(String inputUrl) {
        URL url = parseLenientUrl(inputUrl);

        String host = url.getHost();
        if (host == null || !ALLOWED_HOSTS.contains(host.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported host. Only GitHub file links are allowed.");
        }

        // Decode path so we can safely work with real characters (spaces etc.)
        String decodedPath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);

        // If already raw, normalize/encode segments so downstream URI/HTTP works
        if ("raw.githubusercontent.com".equalsIgnoreCase(host)) {
            String normalized = rebuildUrl(host, decodedPath, url.getQuery());
            return new ResolvedLink(normalized, fileNameFromPath(decodedPath));
        }

        // github.com/<org>/<repo>/blob/<branch>/path/file.ext
        String[] parts = decodedPath.split("/");

        if (parts.length < 6 || !"blob".equals(parts[3])) {
            throw new IllegalArgumentException("Unsupported GitHub URL format. Expected a file 'blob' link.");
        }

        String org = parts[1];
        String repo = parts[2];
        String branch = parts[4];

        // Encode each path segment after the branch
        StringBuilder encodedRest = new StringBuilder();
        for (int i = 5; i < parts.length; i++) {
            encodedRest.append("/").append(encodePathSegment(parts[i]));
        }

        String raw = "https://raw.githubusercontent.com/" + org + "/" + repo + "/" + branch + encodedRest;
        return new ResolvedLink(raw, fileNameFromPath(decodedPath));
    }

    private URL parseLenientUrl(String input) {
        try {
            // URL is more tolerant than URI for user-pasted strings.
            return new URL(input);
        } catch (Exception e) {
            // Try a minimal salvage: replace spaces with %20 and retry
            try {
                return new URL(input.replace(" ", "%20"));
            } catch (Exception e2) {
                throw new IllegalArgumentException("Invalid URL: " + input, e2);
            }
        }
    }

    private String rebuildUrl(String host, String decodedPath, String query) {
        // Rebuild as a properly encoded HTTPS URL
        StringBuilder out = new StringBuilder("https://").append(host);

        String[] segs = decodedPath.split("/");
        for (String seg : segs) {
            if (seg.isEmpty()) continue;
            out.append("/").append(encodePathSegment(seg));
        }

        if (query != null && !query.isBlank()) {
            out.append("?").append(query);
        }
        return out.toString();
    }

    private String encodePathSegment(String s) {
        // URLEncoder is for query params; adapt for path segments.
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String fileNameFromPath(String path) {
        if (path == null || path.isBlank()) return "document";
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    public record ResolvedLink(String url, String fileName) {}
}
