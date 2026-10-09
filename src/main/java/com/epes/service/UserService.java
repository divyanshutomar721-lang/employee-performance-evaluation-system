package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Role;
import com.epes.model.User;

import java.util.List;

/**
 * Service interface for user management and administration operations.
 */
public interface UserService {

    /**
     * Creates a new user in the system. Restricted to administrators.
     *
     * @param actor    authenticated user performing the operation
     * @param email    user email address
     * @param fullName user display name
     * @param role     assigned system role
     * @return created user
     * @throws AuthorizationException if actor is not an admin
     * @throws ValidationException    if email format, name, or role is invalid or duplicated
     * @throws DataAccessException    if a database error occurs
     */
    User createUser(User actor, String email, String fullName, Role role)
            throws AuthorizationException, ValidationException, DataAccessException;

    /**
     * Lists all registered users. Restricted to administrators.
     *
     * @param actor authenticated user performing the operation
     * @return list of all users
     * @throws AuthorizationException if actor is not an admin
     * @throws DataAccessException    if a database error occurs
     */
    List<User> listUsers(User actor) throws AuthorizationException, DataAccessException;

    /**
     * Lists all users with a specific role. Restricted to administrators.
     *
     * @param actor authenticated user performing the operation
     * @param role  role filter
     * @return list of users matching role
     * @throws AuthorizationException if actor is not an admin
     * @throws ValidationException    if role is null
     * @throws DataAccessException    if a database error occurs
     */
    List<User> listByRole(User actor, Role role) throws AuthorizationException, ValidationException, DataAccessException;

    /**
     * Changes a user's system role. Restricted to administrators.
     * Protects against self-demotion and demoting the last active administrator.
     *
     * @param actor   authenticated user performing the operation
     * @param userId  target user identifier
     * @param newRole new role to assign
     * @return updated user
     * @throws AuthorizationException  if actor is not an admin
     * @throws EntityNotFoundException if target user does not exist
     * @throws BusinessRuleException   if attempting self-demotion or demoting the last admin
     * @throws ValidationException     if inputs are invalid
     * @throws DataAccessException     if a database error occurs
     */
    User changeRole(User actor, Long userId, Role newRole)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, ValidationException, DataAccessException;

    /**
     * Activates or deactivates a user account. Restricted to administrators.
     * Protects against self-deactivation and deactivating the last active administrator.
     *
     * @param actor  authenticated user performing the operation
     * @param userId target user identifier
     * @param active desired account active status
     * @throws AuthorizationException  if actor is not an admin
     * @throws EntityNotFoundException if target user does not exist
     * @throws BusinessRuleException   if attempting self-deactivation or deactivating the last admin
     * @throws DataAccessException     if a database error occurs
     */
    void setActive(User actor, Long userId, boolean active)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, DataAccessException;
}
