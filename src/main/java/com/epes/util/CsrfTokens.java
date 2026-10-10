package com.epes.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF token generation and constant-time comparison utility.
 *
 * <p>Tokens are 32 random bytes encoded as URL-safe Base64 (no padding).
 * Matching uses {@link MessageDigest#isEqual(byte[], byte[])} to prevent
 * timing side-channels.</p>
 */
public final class CsrfTokens {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTE_LENGTH = 32;

    private CsrfTokens() {
        // Utility class — prevent instantiation
    }

    /**
     * Generates a new cryptographically random CSRF token.
     *
     * @return URL-safe Base64-encoded token string
     */
    public static String newToken() {
        byte[] bytes = new byte[TOKEN_BYTE_LENGTH];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Compares an expected token with an actual token in constant time.
     *
     * @param expected the session-stored token (may be null)
     * @param actual   the form-submitted token (may be null)
     * @return true if both are non-null and equal
     */
    public static boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = actual.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}
