package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.DataAccessException;
import com.epes.model.User;

import java.util.Collection;
import java.util.Map;

/**
 * Service interface for securely resolving user display information without exposing sensitive internals.
 */
public interface UserLookupService {

    /**
     * Resolves user summaries for a given collection of user identifiers.
     *
     * <p>Accessible by any authenticated user (actor != null). Returns only {@link UserSummary}
     * objects, never internal or sensitive fields. Missing or non-existent identifiers are simply
     * absent from the returned map.</p>
     *
     * @param actor authenticated user performing the lookup
     * @param ids   collection of user IDs to resolve
     * @return map of user ID to resolved {@link UserSummary}
     * @throws AuthorizationException if actor is unauthenticated (null)
     * @throws DataAccessException    if a database error occurs
     */
    Map<Long, UserSummary> summaries(User actor, Collection<Long> ids)
            throws AuthorizationException, DataAccessException;
}
