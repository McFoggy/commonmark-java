package org.commonmark.renderer.html;

import org.apache.commons.text.translate.NumericEntityUnescaper;
import org.apache.commons.text.translate.NumericEntityUnescaper.OPTION;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows http, https and mailto protocols for url.
 * Also allows protocol relative urls, and relative urls.
 * Implementation based on https://github.com/OWASP/java-html-sanitizer/blob/f07e44b034a45d94d6fd010279073c38b6933072/src/main/java/org/owasp/html/FilterUrlByProtocolAttributePolicy.java
 */
public class DefaultUrlSanitizer implements UrlSanitizer {

    // Unescaper used to decode HTML entities.
    private static final NumericEntityUnescaper NUMERIC_ENTITY_UNESCAPER =
            new NumericEntityUnescaper(OPTION.semiColonOptional);

    private Set<String> protocols;

    public DefaultUrlSanitizer() {
        this(Arrays.asList("http", "https", "mailto"));
    }

    public DefaultUrlSanitizer(Collection<String> protocols) {
        this.protocols = new HashSet<>(protocols);
    }

    @Override
    public String sanitizeImageUrl(String url) {
        return sanitizeLinkUrl(url);
    }

    @Override
    public String sanitizeLinkUrl(String url) {
        url = NUMERIC_ENTITY_UNESCAPER.translate(url);
        url = stripHtmlSpaces(url);

        protocol_loop:
        for (int i = 0, n = url.length(); i < n; ++i) {
            switch (url.charAt(i)) {
                case '/':
                case '#':
                case '?':  // No protocol.
                    break protocol_loop;
                case ':':
                    String protocol = url.substring(0, i).toLowerCase();
                    if (!protocols.contains(protocol)) {
                        return "";
                    }
                    break protocol_loop;
            }
        }
        return url;
    }

    private boolean isHtmlSpace(int ch) {
        switch (ch) {
            case ' ':
            case '\t':
            case '\n':
            case '\u000c':
            case '\r':
                return true;
            default:
                return false;
        }
    }

    private String stripHtmlSpaces(String s) {
        int i = 0, n = s.length();
        for (; n > i; --n) {
            if (!isHtmlSpace(s.charAt(n - 1))) {
                break;
            }
        }
        for (; i < n; ++i) {
            if (!isHtmlSpace(s.charAt(i))) {
                break;
            }
        }
        if (i == 0 && n == s.length()) {
            return s;
        }
        return s.substring(i, n);
    }
}
