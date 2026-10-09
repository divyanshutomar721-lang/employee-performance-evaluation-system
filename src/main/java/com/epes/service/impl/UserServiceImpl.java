package com.epes.service.impl;

import com.epes.dao.UserDao;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.DuplicateEntityException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.model.UserFactory;
import com.epes.service.UserService;
import com.epes.util.Validator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of {@link UserService} enforcing administrative authorization,
 * input validation, and safety invariants for administrator accounts.
 */
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    /**
     * Default constructor using standard DAO.
     *
     * @throws DataAccessException if DAO fails initialization
     */
    public UserServiceImpl() throws DataAccessException {
        this(new UserDaoImpl());
    }

    /**
     * Constructor with explicit {@link UserDao}.
     *
     * @param userDao user DAO
     */
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User createUser(User actor, String email, String fullName, Role role)
            throws AuthorizationException, ValidationException, DataAccessException {
        requireAdmin(actor);

        var collector = Validator.newCollector();
        String normalizedEmail = Validator.requireEmail(email, "Email", collector.getErrors());
        collector.checkNotBlank(fullName, "Full name")
                .checkMaxLength(fullName, 150, "Full name")
                .checkNotNull(role, "Role");
        collector.throwIfAny();

        User newUser = UserFactory.create(null, normalizedEmail, fullName.trim(), role, true, LocalDateTime.now());
        try {
            return userDao.create(newUser);
        } catch (DuplicateEntityException e) {
            throw new ValidationException("Email is already registered: " + normalizedEmail);
        }
    }

    @Override
    public List<User> listUsers(User actor) throws AuthorizationException, DataAccessException {
        requireAdmin(actor);
        return userDao.findAll();
    }

    @Override
    public List<User> listByRole(User actor, Role role) throws AuthorizationException, ValidationException, DataAccessException {
        requireAdmin(actor);
        if (role == null) {
            throw new ValidationException("Role cannot be null");
        }
        return userDao.findByRole(role);
    }

    @Override
    public User changeRole(User actor, Long userId, Role newRole)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, ValidationException, DataAccessException {
        requireAdmin(actor);
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }
        if (newRole == null) {
            throw new ValidationException("Role cannot be null");
        }

        User target = userDao.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Self-demotion protection
        if (actor.getId() != null && actor.getId().equals(userId) && newRole != Role.ADMIN) {
            throw new BusinessRuleException("An administrator cannot demote their own account");
        }

        // Last active admin protection
        if (target.getRole() == Role.ADMIN && newRole != Role.ADMIN && target.isActive()) {
            ensureNotLastActiveAdmin(userId);
        }

        target.setRole(newRole);
        User updated = UserFactory.create(target.getId(), target.getEmail(), target.getFullName(), newRole, target.isActive(), target.getCreatedAt());
        return userDao.update(updated);
    }

    @Override
    public void setActive(User actor, Long userId, boolean active)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, DataAccessException {
        requireAdmin(actor);
        if (userId == null) {
            throw new ValidationException("User ID cannot be null");
        }

        User target = userDao.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // Self-deactivation protection
        if (!active && actor.getId() != null && actor.getId().equals(userId)) {
            throw new BusinessRuleException("An administrator cannot deactivate their own account");
        }

        // Last active admin protection
        if (!active && target.getRole() == Role.ADMIN && target.isActive()) {
            ensureNotLastActiveAdmin(userId);
        }

        target.setActive(active);
        userDao.update(target);
    }

    private void requireAdmin(User actor) {
        if (actor == null || actor.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Only administrators can perform this action");
        }
    }

    private void ensureNotLastActiveAdmin(Long targetUserId) throws DataAccessException {
        List<User> admins = userDao.findByRole(Role.ADMIN);
        long activeAdmins = admins.stream().filter(User::isActive).count();
        if (activeAdmins <= 1) {
            throw new BusinessRuleException("Cannot demote or deactivate the last remaining active administrator");
        }
    }
}
