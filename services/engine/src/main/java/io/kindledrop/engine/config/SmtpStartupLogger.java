package io.kindledrop.engine.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class SmtpStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(SmtpStartupLogger.class);

    private static String envOrProp(String key) {
        String v = System.getenv(key);
        if (v != null && !v.isBlank()) return v;
        v = System.getProperty(key);
        return (v == null || v.isBlank()) ? null : v;
    }

    private static String maskUser(String user) {
        if (user == null || user.isBlank()) return "<missing>";
        int at = user.indexOf('@');
        if (at <= 1) return "*".repeat(Math.max(1, Math.min(3, user.length())));
        String local = user.substring(0, at);
        String domain = user.substring(at + 1);
        String visible = local.length() > 1 ? local.substring(0, 1) + "***" : "***";
        return visible + "@" + domain;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        try {
            String host = envOrProp("KINDLEDROP_SMTP_HOST");
            String port = envOrProp("KINDLEDROP_SMTP_PORT");
            String user = envOrProp("KINDLEDROP_SMTP_USER");
            String from = envOrProp("KINDLEDROP_SMTP_FROM");
            String pass = envOrProp("KINDLEDROP_SMTP_PASS");
            String starttls = envOrProp("KINDLEDROP_SMTP_STARTTLS");
            String ssl = envOrProp("KINDLEDROP_SMTP_SSL");

            boolean configured = host != null && !host.isBlank() && user != null && !user.isBlank() && from != null && !from.isBlank() && pass != null && !pass.isBlank();

            if (configured) {
                log.info("SMTP configuration present: host='{}', port={}, user='{}', from='{}', startTLS={}, ssl={}, passwordPresent={}",
                        host,
                        port == null ? "587" : port,
                        maskUser(user),
                        maskUser(from),
                        starttls == null ? "true" : starttls,
                        ssl == null ? "false" : ssl,
                        true
                );
            } else {
                log.warn("SMTP configuration incomplete or missing. To send mails set KINDLEDROP_SMTP_HOST, KINDLEDROP_SMTP_USER, KINDLEDROP_SMTP_FROM and KINDLEDROP_SMTP_PASS (or provide SMTP in request). If you're running locally, use a local .env and load it into the shell before starting the app.");
            }
        } catch (Exception e) {
            // don't fail startup because of logging
            log.debug("Exception while checking SMTP configuration", e);
        }
    }
}
