package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.ValidationException;
import com.epes.model.Criterion;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.model.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CriterionService}.
 */
class CriterionServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Manager creates, updates, deactivates, and lists criteria")
    void testCriterionCrudAndOwnership() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Criterion Manager", Role.MANAGER, true, null));
        User otherManager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Other Manager", Role.MANAGER, true, null));

        // Create
        Criterion c = appContext.getCriterionService().create(manager, "Code Quality", "Clean code standards", 40);
        assertNotNull(c.getId());
        assertEquals(40, c.getWeight());

        // Update
        Criterion updated = appContext.getCriterionService().update(manager, c.getId(), "Refined Code Quality", "Updated", 50);
        assertEquals(50, updated.getWeight());

        // Other manager cannot modify it
        assertThrows(AuthorizationException.class, () ->
                appContext.getCriterionService().update(otherManager, c.getId(), "Hacked", "Desc", 30));

        // Deactivate
        appContext.getCriterionService().deactivate(manager, c.getId());

        List<Criterion> activeOnly = appContext.getCriterionService().listMine(manager, true);
        assertTrue(activeOnly.isEmpty());

        List<Criterion> all = appContext.getCriterionService().listMine(manager, false);
        assertEquals(1, all.size());

        // Delete
        appContext.getCriterionService().delete(manager, c.getId());
        assertTrue(appContext.getCriterionService().listMine(manager, false).isEmpty());
    }

    @Test
    @DisplayName("Invalid weights or duplicate names throw ValidationException")
    void testValidationConstraints() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Constraint Manager", Role.MANAGER, true, null));

        // Weight < 1
        assertThrows(ValidationException.class, () ->
                appContext.getCriterionService().create(manager, "Bad Weight", "Desc", 0));

        // Weight > 100
        assertThrows(ValidationException.class, () ->
                appContext.getCriterionService().create(manager, "Bad Weight", "Desc", 101));

        // Duplicate name
        appContext.getCriterionService().create(manager, "Unique Name", "Desc", 50);
        assertThrows(ValidationException.class, () ->
                appContext.getCriterionService().create(manager, "Unique Name", "Desc", 30));
    }
}
