package com.epes.filter;

import com.epes.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AccessPolicy unit tests")
class AccessPolicyTest {

    @Test
    @DisplayName("isPublicPath identifies public static and landing pages")
    void testIsPublicPath() {
        assertTrue(AccessPolicy.isPublicPath("/"));
        assertTrue(AccessPolicy.isPublicPath("/index.jsp"));
        assertTrue(AccessPolicy.isPublicPath("/login"));
        assertTrue(AccessPolicy.isPublicPath("/health"));
        assertTrue(AccessPolicy.isPublicPath("/error"));
        assertTrue(AccessPolicy.isPublicPath("/assets/css/style.css"));
        assertTrue(AccessPolicy.isPublicPath("/assets/js/app.js"));

        assertFalse(AccessPolicy.isPublicPath("/admin/dashboard"));
        assertFalse(AccessPolicy.isPublicPath("/manager/dashboard"));
        assertFalse(AccessPolicy.isPublicPath("/employee/dashboard"));
        assertFalse(AccessPolicy.isPublicPath("/secret"));
    }

    @Test
    @DisplayName("requiredRoleFor returns correct role by path prefix")
    void testRequiredRoleFor() {
        assertEquals(Role.ADMIN, AccessPolicy.requiredRoleFor("/admin/dashboard"));
        assertEquals(Role.ADMIN, AccessPolicy.requiredRoleFor("/admin/users"));
        assertEquals(Role.ADMIN, AccessPolicy.requiredRoleFor("/admin"));

        assertEquals(Role.MANAGER, AccessPolicy.requiredRoleFor("/manager/dashboard"));
        assertEquals(Role.MANAGER, AccessPolicy.requiredRoleFor("/manager/teams"));
        assertEquals(Role.MANAGER, AccessPolicy.requiredRoleFor("/manager"));

        assertEquals(Role.EMPLOYEE, AccessPolicy.requiredRoleFor("/employee/dashboard"));
        assertEquals(Role.EMPLOYEE, AccessPolicy.requiredRoleFor("/employee/goals"));
        assertEquals(Role.EMPLOYEE, AccessPolicy.requiredRoleFor("/employee"));

        assertNull(AccessPolicy.requiredRoleFor("/home"));
        assertNull(AccessPolicy.requiredRoleFor("/login"));
        assertNull(AccessPolicy.requiredRoleFor("/health"));
    }

    @Test
    @DisplayName("Path normalisation handles doubled slashes, query strings and rejects traversal")
    void testPathNormalisation() {
        // Doubled slashes
        assertEquals(Role.ADMIN, AccessPolicy.requiredRoleFor("//admin/dashboard"));

        // Query strings stripped
        assertEquals(Role.ADMIN, AccessPolicy.requiredRoleFor("/admin/dashboard?tab=active"));
        assertTrue(AccessPolicy.isPublicPath("/login?redirect=/home"));

        // Directory traversal '..' rejected
        assertNull(AccessPolicy.requiredRoleFor("/admin/../manager/x"));
        assertFalse(AccessPolicy.isPublicPath("/assets/../WEB-INF/web.xml"));

        // Non-prefix /adminx must not match Role.ADMIN
        assertNull(AccessPolicy.requiredRoleFor("/adminx/foo"));
        assertNull(AccessPolicy.requiredRoleFor("/managers/dashboard"));
        assertNull(AccessPolicy.requiredRoleFor("/employees"));
    }

    @Test
    @DisplayName("dashboardPathFor returns correct role dashboards")
    void testDashboardPathFor() {
        assertEquals("/admin/dashboard", AccessPolicy.dashboardPathFor(Role.ADMIN));
        assertEquals("/manager/dashboard", AccessPolicy.dashboardPathFor(Role.MANAGER));
        assertEquals("/employee/dashboard", AccessPolicy.dashboardPathFor(Role.EMPLOYEE));
    }
}
