package com.epes.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CsrfTokens unit tests")
class CsrfTokensTest {

    @Test
    @DisplayName("newToken generates non-empty, unique, high-entropy tokens")
    void testNewTokenUniquenessAndLength() {
        String token1 = CsrfTokens.newToken();
        String token2 = CsrfTokens.newToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(token1.length() >= 40, "Token length should be at least 40 characters for 32 bytes base64");
        assertNotEquals(token1, token2, "Two generated tokens should be unique");
    }

    @Test
    @DisplayName("matches returns true for identical tokens and false for differences or nulls")
    void testMatches() {
        String token = CsrfTokens.newToken();

        assertTrue(CsrfTokens.matches(token, token));
        assertFalse(CsrfTokens.matches(token, CsrfTokens.newToken()));
        assertFalse(CsrfTokens.matches(token, null));
        assertFalse(CsrfTokens.matches(null, token));
        assertFalse(CsrfTokens.matches(null, null));
        assertFalse(CsrfTokens.matches(token, ""));
        assertFalse(CsrfTokens.matches(token, token + "extra"));
    }
}
