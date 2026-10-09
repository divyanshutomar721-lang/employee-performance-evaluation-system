package com.epes.service;

import com.epes.exception.AuthenticationException;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.service.impl.TemporaryEmailAuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link AuthService}.
 */
class AuthServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Authenticate existing active user successfully")
    void testAuthenticateActiveUser() throws Exception {
        User admin = appContext.getUserDao().create(com.epes.model.UserFactory.create(
                null, randomEmail(), "Suite Admin", Role.ADMIN, true, null));

        String email = randomEmail();
        User employee = appContext.getUserService().createUser(admin, email, "Alice Employee", Role.EMPLOYEE);

        User authenticated = appContext.getAuthService().authenticate(email);
        assertNotNull(authenticated);
        assertEquals(employee.getId(), authenticated.getId());
        assertEquals(email.toLowerCase(), authenticated.getEmail());
    }

    @Test
    @DisplayName("Authenticate unknown user throws AuthenticationException")
    void testUnknownUserThrows() {
        assertThrows(AuthenticationException.class, () ->
                appContext.getAuthService().authenticate("nonexistent-" + randomEmail()));
    }

    @Test
    @DisplayName("Authenticate deactivated user throws AuthenticationException")
    void testDeactivatedUserThrows() throws Exception {
        User admin = appContext.getUserDao().create(com.epes.model.UserFactory.create(
                null, randomEmail(), "Suite Admin", Role.ADMIN, true, null));

        String email = randomEmail();
        User employee = appContext.getUserService().createUser(admin, email, "Deactivated User", Role.EMPLOYEE);
        appContext.getUserService().setActive(admin, employee.getId(), false);

        assertThrows(AuthenticationException.class, () ->
                appContext.getAuthService().authenticate(email));
    }

    @Test
    @DisplayName("Temporary login disabled throws AuthenticationException")
    void testDisabledTempLogin() throws Exception {
        User admin = appContext.getUserDao().create(com.epes.model.UserFactory.create(
                null, randomEmail(), "Suite Admin", Role.ADMIN, true, null));

        AuthService disabledAuth = new TemporaryEmailAuthService(appContext.getUserDao(), false);
        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                disabledAuth.authenticate(admin.getEmail()));
        assertTrue(ex.getMessage().contains("disabled"));
    }
}
