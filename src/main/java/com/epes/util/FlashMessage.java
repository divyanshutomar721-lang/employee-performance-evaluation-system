package com.epes.util;

/**
 * Immutable data object representing a flash message shown to the user once.
 */
public final class FlashMessage {

    /** Visual category of the message, mapped to Bootstrap alert classes. */
    public enum Type {
        /** Operation succeeded. */
        SUCCESS,
        /** Informational notice. */
        INFO,
        /** Non-blocking warning. */
        WARNING,
        /** Blocking error. */
        ERROR
    }

    private final Type type;
    private final String text;

    /**
     * Constructs a new FlashMessage.
     *
     * @param type message category
     * @param text display text
     */
    public FlashMessage(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    /** Returns the message category. */
    public Type getType() {
        return type;
    }

    /** Returns the display text. */
    public String getText() {
        return text;
    }

    /**
     * Returns the Bootstrap alert CSS class suffix for this type.
     *
     * @return e.g. "success", "info", "warning", "danger"
     */
    public String getBootstrapClass() {
        return switch (type) {
            case SUCCESS -> "success";
            case INFO    -> "info";
            case WARNING -> "warning";
            case ERROR   -> "danger";
        };
    }
}
