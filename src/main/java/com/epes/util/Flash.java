package com.epes.util;

import com.epes.config.AppConstants;
import jakarta.servlet.http.HttpSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper for session-scoped flash messages (one-time display).
 *
 * <p>Flash messages are stored in the session under {@link AppConstants#SESSION_FLASH}
 * and consumed (removed) the first time they are read.</p>
 */
public final class Flash {

    private Flash() {
        // Utility class — prevent instantiation
    }

    /**
     * Adds a flash message to the session.
     *
     * @param session the HTTP session (must not be null)
     * @param type    message category
     * @param text    display text
     */
    @SuppressWarnings("unchecked")
    public static void add(HttpSession session, FlashMessage.Type type, String text) {
        List<FlashMessage> messages = (List<FlashMessage>) session.getAttribute(AppConstants.SESSION_FLASH);
        if (messages == null) {
            messages = new ArrayList<>();
            session.setAttribute(AppConstants.SESSION_FLASH, messages);
        }
        messages.add(new FlashMessage(type, text));
    }

    /**
     * Removes and returns all pending flash messages from the session.
     *
     * @param session the HTTP session (may be null)
     * @return unmodifiable list of messages (empty if none)
     */
    @SuppressWarnings("unchecked")
    public static List<FlashMessage> consume(HttpSession session) {
        if (session == null) {
            return Collections.emptyList();
        }
        List<FlashMessage> messages = (List<FlashMessage>) session.getAttribute(AppConstants.SESSION_FLASH);
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        session.removeAttribute(AppConstants.SESSION_FLASH);
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }
}
