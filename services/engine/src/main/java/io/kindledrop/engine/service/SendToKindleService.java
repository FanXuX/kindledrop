package io.kindledrop.engine.service;

import io.kindledrop.engine.api.SendRequest;
import io.kindledrop.engine.api.SendResponse;
import io.kindledrop.engine.download.SecureDownloader;
import io.kindledrop.engine.mail.KindleMailSender;
import io.kindledrop.engine.resolve.GitHubLinkResolver;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SendToKindleService {

    private final GitHubLinkResolver resolver = new GitHubLinkResolver();
    private final SecureDownloader downloader = new SecureDownloader();
    private final KindleMailSender mailer = new KindleMailSender();

    public SendResponse send(SendRequest req) throws Exception {
        var resolved = resolver.resolve(req.url());

        long maxBytes = 30L * 1024 * 1024; // default 30MB
        if (req.limits() != null && req.limits().maxBytes() > 0) {
            maxBytes = req.limits().maxBytes();
        }

        if (req.dryRun()) {
            return new SendResponse(true, resolved.url(), resolved.fileName(), 0, "Dry run: resolved + validated.");
        }

        // determine SMTP config: prefer request-level config, otherwise fall back to environment / .env
        var smtpConfig = req.smtp();

        String host = null;
        int port = 587;
        String user = null;
        String from = null;
        boolean useStartTLS = true;
        boolean useSSL = false;
        String password = null;

        if (smtpConfig != null) {
            host = smtpConfig.host();
            port = smtpConfig.port() > 0 ? smtpConfig.port() : port;
            user = smtpConfig.user();
            from = smtpConfig.from();
            useStartTLS = smtpConfig.useStartTLS();
            useSSL = smtpConfig.useSSL();
            password = smtpConfig.password();
        }

        // env/system fallback helpers
        final class Helpers {
            String envOrProp(String key, String current) {
                if (current != null && !current.isBlank()) return current;
                String v = System.getenv(key);
                if (v != null && !v.isBlank()) return v;
                v = System.getProperty(key);
                return (v == null || v.isBlank()) ? null : v;
            }
        }
        var helpers = new Helpers();

        // read from environment or .env-loaded system properties
        host = helpers.envOrProp("KINDLEDROP_SMTP_HOST", host);
        String portStr = helpers.envOrProp("KINDLEDROP_SMTP_PORT", null);
        if (portStr != null) {
            try { port = Integer.parseInt(portStr); } catch (NumberFormatException ignored) {}
        }
        user = helpers.envOrProp("KINDLEDROP_SMTP_USER", user);
        from = helpers.envOrProp("KINDLEDROP_SMTP_FROM", from);
        String starttlsStr = helpers.envOrProp("KINDLEDROP_SMTP_STARTTLS", null);
        if (starttlsStr != null) {
            useStartTLS = Boolean.parseBoolean(starttlsStr);
        }
        String sslStr = helpers.envOrProp("KINDLEDROP_SMTP_SSL", null);
        if (sslStr != null) {
            useSSL = Boolean.parseBoolean(sslStr);
        }
        password = helpers.envOrProp("KINDLEDROP_SMTP_PASS", password);

        // validate required fields are present
        if (host == null || host.isBlank() || user == null || user.isBlank() || from == null || from.isBlank() || password == null || password.isBlank()) {
            return new SendResponse(false, resolved.url(), resolved.fileName(), 0,
                    "Missing SMTP config. Provide SMTP via request or set KINDLEDROP_SMTP_HOST, KINDLEDROP_SMTP_USER, KINDLEDROP_SMTP_FROM and KINDLEDROP_SMTP_PASS environment variables (or a local .env).");
        }

        Path downloadedFile = null;
        Path downloadedDir = null;

        try {
            var result = downloader.downloadToTemp(URI.create(resolved.url()), resolved.fileName(), maxBytes);
            downloadedFile = result.file();
            downloadedDir = downloadedFile.getParent();

                var smtp = new KindleMailSender.Smtp(
                    host,
                    port,
                    user,
                    from,
                    useStartTLS,
                    useSSL,
                    password
                );

            String subject = "KindleDrop: " + result.fileName();
            String body = "Sent by KindleDrop.\n\nSource: " + req.url() + "\nResolved: " + resolved.url();

            mailer.sendWithAttachment(
                    smtp,
                    req.kindleEmail(),
                    subject,
                    body,
                    downloadedFile,
                    result.fileName()
            );

            return new SendResponse(true, resolved.url(), result.fileName(), result.bytes(), "Sent to Kindle.");
        } finally {
            // cleanup temp files
            if (downloadedFile != null) {
                try { Files.deleteIfExists(downloadedFile); } catch (Exception ignored) {}
            }
            if (downloadedDir != null) {
                try {
                    Files.walk(downloadedDir)
                            .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                            .forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) {} });
                } catch (Exception ignored) {}
            }
        }
    }
}
