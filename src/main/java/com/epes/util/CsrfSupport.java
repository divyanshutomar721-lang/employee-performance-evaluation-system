package com.epes.util;

import com.epes.config.AppConstants;
import jakarta.servlet.http.HttpSession;

/**
 * Helper for lazily creating and retrieving CSRF tokens from the session.
 */
public final class CsrfSupport {

    private CsrfSupport() {
        // Utility class — prevent instantiation
    }

    /**
     * Returns the CSRF token for the given session, creating one if absent.
     *
     * @param session the HTTP session (must not be null)
     * @return the CSRF token string
     */
    public static String tokenFor(HttpSession session) {
        String token = (String) session.getAttribute(AppConstants.SESSION_CSRF);
        if (token == null) {
            token = CsrfTokens.newToken();
            session.setAttribute(AppConstants.SESSION_CSRF, token);
        }
        return token;
    }
}
