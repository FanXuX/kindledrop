package io.kindledrop.engine.api;

public record SendResponse(
        boolean ok,
        String resolvedUrl,
        String fileName,
        long bytes,
        String message
) {}
