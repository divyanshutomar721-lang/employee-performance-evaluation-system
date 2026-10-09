package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Criterion;
import com.epes.model.User;

import java.util.List;

/**
 * Service interface for managing evaluation criteria configured by managers.
 */
public interface CriterionService {

    /**
     * Creates a new criterion. Restricted to managers.
     */
    Criterion create(User actor, String name, String description, int weight)
            throws AuthorizationException, ValidationException, DataAccessException;

    /**
     * Updates an existing criterion. Restricted to the owning manager.
     */
    Criterion update(User actor, Long criterionId, String name, String description, int weight)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException;

    /**
     * Deactivates a criterion (soft delete). Restricted to the owning manager.
     */
    void deactivate(User actor, Long criterionId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Hard-deletes a criterion. Restricted to the owning manager.
     */
    void delete(User actor, Long criterionId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Lists criteria created by the acting manager.
     */
    List<Criterion> listMine(User actor, boolean activeOnly)
            throws AuthorizationException, DataAccessException;
}
