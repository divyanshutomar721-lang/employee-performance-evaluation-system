package com.epes.service.impl;

import com.epes.dao.UserDao;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.AuthenticationException;
import com.epes.exception.DataAccessException;
import com.epes.model.User;
import com.epes.service.AuthService;
import com.epes.util.ConnectionFactory;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Temporary email-based authentication implementation.
 *
 * <p><b>Lifecycle Note:</b>
 * This is a development and testing implementation enabled by the environment variable
 * {@code TEMP_LOGIN_ENABLED} (default: {@code true}). In a subsequent phase, this service
 * will be replaced by OAuth2 / Google Sign-In while preserving the {@link AuthService} contract.
 * Password-less authentication looks up an active user by email and rejects inactive accounts.</p>
 */
public class TemporaryEmailAuthService implements AuthService {

    private static final Logger LOG = Logger.getLogger(TemporaryEmailAuthService.class.getName());

    private final UserDao userDao;
    private final boolean tempLoginEnabled;

    /**
     * Constructs a TemporaryEmailAuthService reading {@code TEMP_LOGIN_ENABLED} from system environment.
     *
     * @throws DataAccessException if DAO initialization fails
     */
    public TemporaryEmailAuthService() throws DataAccessException {
        this(new UserDaoImpl(), parseEnvFlag("TEMP_LOGIN_ENABLED", true));
    }

    /**
     * Constructs a TemporaryEmailAuthService with injected {@link UserDao}.
     *
     * @param userDao user DAO
     */
    public TemporaryEmailAuthService(UserDao userDao) {
        this(userDao, parseEnvFlag("TEMP_LOGIN_ENABLED", true));
    }

    /**
     * Constructs a TemporaryEmailAuthService with explicit configuration flag (useful for testing).
     *
     * @param userDao          user DAO
     * @param tempLoginEnabled whether temporary email login is active
     */
    public TemporaryEmailAuthService(UserDao userDao, boolean tempLoginEnabled) {
        this.userDao = userDao;
        this.tempLoginEnabled = tempLoginEnabled;
        if (this.tempLoginEnabled) {
            LOG.warning("Temporary email authentication is ENABLED. Do not use in production without OAuth2.");
        }
    }

    @Override
    public User authenticate(String email) throws AuthenticationException, DataAccessException {
        if (!tempLoginEnabled) {
            throw new AuthenticationException("Email login is disabled");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new AuthenticationException("Invalid email or inactive account");
        }

        Optional<User> optUser = userDao.findByEmail(email.trim());
        if (optUser.isEmpty() || !optUser.get().isActive()) {
            throw new AuthenticationException("Invalid email or inactive account");
        }

        return optUser.get();
    }

    private static boolean parseEnvFlag(String name, boolean defaultValue) {
        String val = System.getenv(name);
        if (val == null || val.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val.trim());
    }
}
