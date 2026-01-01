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

        if (req.smtp() == null) {
            return new SendResponse(false, resolved.url(), resolved.fileName(), 0, "Missing SMTP config in request.");
        }

        String password = req.smtp().password();
        if (password == null || password.isBlank()) {
            // allow env var fallback
            password = System.getenv("KINDLEDROP_SMTP_PASS");
        }
        if (password == null || password.isBlank()) {
            return new SendResponse(false, resolved.url(), resolved.fileName(), 0,
                    "Missing SMTP password. Provide in request.smtp.password or env KINDLEDROP_SMTP_PASS.");
        }

        Path downloadedFile = null;
        Path downloadedDir = null;

        try {
            var result = downloader.downloadToTemp(URI.create(resolved.url()), resolved.fileName(), maxBytes);
            downloadedFile = result.file();
            downloadedDir = downloadedFile.getParent();

            var smtp = new KindleMailSender.Smtp(
                    req.smtp().host(),
                    req.smtp().port() > 0 ? req.smtp().port() : 587,
                    req.smtp().user(),
                    req.smtp().from(),
                    req.smtp().useStartTLS(),
                    req.smtp().useSSL(),
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
