package es.vargontoc.educational.framework.shared.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenGeneratorTest {

    @Test
    void generateRawToken_returnsBase64UrlTokenWithoutPadding() {
        var token = TokenGenerator.generateRawToken();

        assertEquals(43, token.length());
        assertTrue(token.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void generateRawToken_returnsDifferentTokens() {
        var first = TokenGenerator.generateRawToken();
        var second = TokenGenerator.generateRawToken();

        assertNotEquals(first, second);
    }

    @Test
    void hashToken_returnsLowercaseSha256Hex() {
        var hash = TokenGenerator.hashToken("raw-token");

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    void hashToken_isNotIdempotent() {
        var firstHash = TokenGenerator.hashToken("raw-token");
        var secondHash = TokenGenerator.hashToken(firstHash);

        assertNotEquals(firstHash, secondHash);
    }
}
