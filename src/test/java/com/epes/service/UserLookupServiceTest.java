package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.model.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link UserLookupService}.
 */
class UserLookupServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Authenticated user can resolve summaries for existing user IDs; missing IDs omitted")
    void testUserLookupSummaries() throws Exception {
        User actor = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Any Employee", Role.EMPLOYEE, true, null));
        User target1 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Alice Target", Role.MANAGER, true, null));
        User target2 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Bob Target", Role.EMPLOYEE, false, null));

        Map<Long, UserSummary> summaries = appContext.getUserLookupService().summaries(
                actor, List.of(target1.getId(), target2.getId(), 999999L)
        );

        assertNotNull(summaries);
        assertEquals(2, summaries.size());
        assertTrue(summaries.containsKey(target1.getId()));
        assertTrue(summaries.containsKey(target2.getId()));
        assertFalse(summaries.containsKey(999999L));

        UserSummary s1 = summaries.get(target1.getId());
        assertEquals("Alice Target", s1.getFullName());
        assertEquals(Role.MANAGER, s1.getRole());
        assertTrue(s1.isActive());

        UserSummary s2 = summaries.get(target2.getId());
        assertEquals("Bob Target", s2.getFullName());
        assertEquals(Role.EMPLOYEE, s2.getRole());
        assertFalse(s2.isActive());
    }

    @Test
    @DisplayName("Unauthenticated actor throws AuthorizationException")
    void testUnauthenticatedActorThrows() {
        assertThrows(AuthorizationException.class, () ->
                appContext.getUserLookupService().summaries(null, List.of(1L, 2L)));
    }

    @Test
    @DisplayName("Empty or null ID collection returns empty map without error")
    void testEmptyIdsReturnsEmptyMap() throws Exception {
        User actor = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Actor User", Role.ADMIN, true, null));

        Map<Long, UserSummary> res1 = appContext.getUserLookupService().summaries(actor, Collections.emptyList());
        assertTrue(res1.isEmpty());

        Map<Long, UserSummary> res2 = appContext.getUserLookupService().summaries(actor, null);
        assertTrue(res2.isEmpty());
    }
}
