package com.epes.service.impl;

import com.epes.dao.UserDao;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.AuthorizationException;
import com.epes.exception.DataAccessException;
import com.epes.model.User;
import com.epes.service.UserLookupService;
import com.epes.service.UserSummary;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link UserLookupService} performing safe user summary projections.
 */
public class UserLookupServiceImpl implements UserLookupService {

    private final UserDao userDao;

    /**
     * Default constructor using standard DAO.
     *
     * @throws DataAccessException if DAO fails initialization
     */
    public UserLookupServiceImpl() throws DataAccessException {
        this(new UserDaoImpl());
    }

    /**
     * Parameterized constructor.
     *
     * @param userDao user DAO
     */
    public UserLookupServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Map<Long, UserSummary> summaries(User actor, Collection<Long> ids)
            throws AuthorizationException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required to look up user summaries");
        }
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, UserSummary> result = new HashMap<>();
        for (Long id : ids) {
            if (id != null && !result.containsKey(id)) {
                userDao.findById(id).ifPresent(user -> result.put(user.getId(), UserSummary.from(user)));
            }
        }
        return result;
    }
}
