package com.epes.service;

import com.epes.exception.AuthenticationException;
import com.epes.exception.DataAccessException;
import com.epes.model.User;

/**
 * Authentication service interface.
 */
public interface AuthService {

    /**
     * Authenticates a user by email address.
     *
     * @param email email of the user attempting authentication
     * @return the authenticated active {@link User}
     * @throws AuthenticationException if authentication fails, account is inactive, or login is disabled
     * @throws DataAccessException     if a database error occurs
     */
    User authenticate(String email) throws AuthenticationException, DataAccessException;
}
