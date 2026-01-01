package io.kindledrop.engine.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendRequest(
        @NotBlank String url,
        @NotBlank @Email String kindleEmail,
        boolean dryRun,
        SmtpConfig smtp,
        Limits limits
) {
    public record SmtpConfig(
            @NotBlank String host,
            int port,
            @NotBlank String user,
            @NotBlank String from,
            boolean useStartTLS,
            boolean useSSL,
            String password
    ) {}

    public record Limits(
            long maxBytes
    ) {}
}
