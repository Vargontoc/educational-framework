package es.vargontoc.educational.framework.shared.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

public final class TokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private TokenGenerator() {
    }

    public static String generateRawToken() {
        var tokenBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(tokenBytes);
    }

    public static String hashToken(String rawToken) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
