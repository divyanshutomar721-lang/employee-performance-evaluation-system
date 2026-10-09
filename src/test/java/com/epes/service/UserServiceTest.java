package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.ValidationException;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.model.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link UserService}.
 */
class UserServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Admin creates user successfully and lists users")
    void testAdminCreatesAndListsUsers() throws Exception {
        User admin = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Root Admin", Role.ADMIN, true, null));

        String empEmail = randomEmail();
        User created = appContext.getUserService().createUser(admin, empEmail, "Bob Smith", Role.EMPLOYEE);
        assertNotNull(created.getId());
        assertEquals("Bob Smith", created.getFullName());
        assertEquals(Role.EMPLOYEE, created.getRole());

        List<User> allUsers = appContext.getUserService().listUsers(admin);
        assertTrue(allUsers.stream().anyMatch(u -> u.getId().equals(created.getId())));

        List<User> employees = appContext.getUserService().listByRole(admin, Role.EMPLOYEE);
        assertTrue(employees.stream().anyMatch(u -> u.getId().equals(created.getId())));
    }

    @Test
    @DisplayName("Non-admin attempting user management throws AuthorizationException")
    void testNonAdminRefused() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Lead Manager", Role.MANAGER, true, null));

        assertThrows(AuthorizationException.class, () ->
                appContext.getUserService().createUser(manager, randomEmail(), "Test User", Role.EMPLOYEE));

        assertThrows(AuthorizationException.class, () ->
                appContext.getUserService().listUsers(manager));
    }

    @Test
    @DisplayName("Admin self-demotion and self-deactivation are blocked")
    void testSelfDemotionAndDeactivationBlocked() throws Exception {
        User admin = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Self Admin", Role.ADMIN, true, null));

        // Self demotion
        assertThrows(BusinessRuleException.class, () ->
                appContext.getUserService().changeRole(admin, admin.getId(), Role.EMPLOYEE));

        // Self deactivation
        assertThrows(BusinessRuleException.class, () ->
                appContext.getUserService().setActive(admin, admin.getId(), false));
    }

    @Test
    @DisplayName("Duplicate email registration throws friendly ValidationException")
    void testDuplicateEmail() throws Exception {
        User admin = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Admin User", Role.ADMIN, true, null));

        String email = randomEmail();
        appContext.getUserService().createUser(admin, email, "First User", Role.EMPLOYEE);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                appContext.getUserService().createUser(admin, email, "Second User", Role.EMPLOYEE));
        assertTrue(ex.getMessage().contains("already registered"));
    }
}
