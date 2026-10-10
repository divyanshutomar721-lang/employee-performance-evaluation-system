package com.epes.filter;

import com.epes.model.Role;

/**
 * Pure static utility that centralises URL-level access rules.
 *
 * <p>All methods operate on the path <em>within the application</em>
 * (i.e. after the context path has been stripped). The path is
 * normalised first: the query string is dropped, doubled slashes
 * are collapsed, and ".." segments cause the path to be treated
 * as unrecognised (returns {@code false}/{@code null}).</p>
 */
public final class AccessPolicy {

    private AccessPolicy() {
        // Utility class — prevent instantiation
    }

    // ── Public path check ───────────────────────────────────────────────

    /**
     * Returns {@code true} if the path may be accessed without a session.
     *
     * @param pathWithinApp the request path relative to the context root
     * @return true when no authentication is required
     */
    public static boolean isPublicPath(String pathWithinApp) {
        String p = normalise(pathWithinApp);
        if (p == null) {
            return false;
        }

        return switch (p) {
            case "/", "/index.jsp", "/login", "/health", "/error" -> true;
            default -> p.startsWith("/assets/");
        };
    }

    // ── Role requirement ────────────────────────────────────────────────

    /**
     * Returns the {@link Role} required for the given path, or {@code null}
     * if any authenticated user may access it.
     *
     * @param pathWithinApp the request path relative to the context root
     * @return required role, or null for any-logged-in-user
     */
    public static Role requiredRoleFor(String pathWithinApp) {
        String p = normalise(pathWithinApp);
        if (p == null) {
            return null;
        }

        if (p.equals("/admin") || p.startsWith("/admin/")) {
            return Role.ADMIN;
        } else if (p.equals("/manager") || p.startsWith("/manager/")) {
            return Role.MANAGER;
        } else if (p.equals("/employee") || p.startsWith("/employee/")) {
            return Role.EMPLOYEE;
        }
        return null;
    }

    // ── Dashboard path ──────────────────────────────────────────────────

    /**
     * Returns the dashboard path for the given role.
     *
     * @param role user role
     * @return path within the application (e.g. "/admin/dashboard")
     */
    public static String dashboardPathFor(Role role) {
        return switch (role) {
            case ADMIN    -> "/admin/dashboard";
            case MANAGER  -> "/manager/dashboard";
            case EMPLOYEE -> "/employee/dashboard";
        };
    }

    // ── Path normalisation ──────────────────────────────────────────────

    /**
     * Normalises a request path: strips the query string, collapses
     * doubled slashes, and rejects any path containing "..".
     *
     * @param raw the raw path within the application
     * @return normalised path, or {@code null} if the path is malformed
     */
    static String normalise(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "/";
        }

        // Strip query string
        int q = raw.indexOf('?');
        String p = (q >= 0) ? raw.substring(0, q) : raw;

        // Reject ".." segments (directory traversal)
        if (p.contains("..")) {
            return null;
        }

        // Collapse doubled slashes
        while (p.contains("//")) {
            p = p.replace("//", "/");
        }

        // Ensure leading slash
        if (!p.startsWith("/")) {
            p = "/" + p;
        }

        return p;
    }
}
