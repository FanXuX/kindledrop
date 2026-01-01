package io.kindledrop.engine;

import io.kindledrop.engine.resolve.GitHubLinkResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GitHubLinkResolverTest {

    @Test
    void resolvesBlobToRaw() {
        GitHubLinkResolver r = new GitHubLinkResolver();
        var out = r.resolve("https://github.com/org/repo/blob/main/path/book.pdf");
        assertEquals("https://raw.githubusercontent.com/org/repo/main/path/book.pdf", out.url());
        assertEquals("book.pdf", out.fileName());
    }

    @Test
    void rejectsNonBlob() {
        GitHubLinkResolver r = new GitHubLinkResolver();
        assertThrows(IllegalArgumentException.class, () -> r.resolve("https://github.com/org/repo/tree/main/path"));
    }

    @Test
    void encodesSpacesInPaths() {
        GitHubLinkResolver r = new GitHubLinkResolver();
        var out = r.resolve("https://github.com/org/repo/blob/main/Semantic Web/Programming The Semantic Web.pdf");

        assertTrue(out.url().contains("Semantic%20Web"));
        assertTrue(out.url().contains("Programming%20The%20Semantic%20Web.pdf"));
        assertEquals("Programming The Semantic Web.pdf", out.fileName());
    }
    
}
