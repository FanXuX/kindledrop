package io.kindledrop.engine.download;

import java.nio.file.Path;

public record DownloadResult(
        Path file,
        String fileName,
        long bytes,
        String contentType
) {}
