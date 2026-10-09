package com.epes.service.impl;

import com.epes.dao.CriterionDao;
import com.epes.dao.impl.CriterionDaoImpl;
import com.epes.exception.AuthorizationException;
import com.epes.exception.DataAccessException;
import com.epes.exception.DuplicateEntityException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Criterion;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.service.CriterionService;
import com.epes.util.Validator;

import java.util.List;

/**
 * Implementation of {@link CriterionService} enforcing manager ownership and weight bounds.
 */
public class CriterionServiceImpl implements CriterionService {

    private final CriterionDao criterionDao;

    /** Default constructor. */
    public CriterionServiceImpl() throws DataAccessException {
        this(new CriterionDaoImpl());
    }

    /** Parameterized constructor. */
    public CriterionServiceImpl(CriterionDao criterionDao) {
        this.criterionDao = criterionDao;
    }

    @Override
    public Criterion create(User actor, String name, String description, int weight)
            throws AuthorizationException, ValidationException, DataAccessException {
        requireManager(actor);

        var collector = Validator.newCollector();
        collector.checkNotBlank(name, "Criterion name")
                .checkMaxLength(name, 100, "Criterion name")
                .checkRange(weight, 1, 100, "Weight");
        collector.throwIfAny();

        Criterion criterion = new Criterion(null, actor.getId(), name.trim(), description, weight, true);
        try {
            return criterionDao.create(criterion);
        } catch (DuplicateEntityException e) {
            throw new ValidationException("A criterion with this name already exists for your account: " + name);
        }
    }

    @Override
    public Criterion update(User actor, Long criterionId, String name, String description, int weight)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException {
        requireManager(actor);
        Criterion criterion = requireCriterionOwner(actor, criterionId);

        var collector = Validator.newCollector();
        collector.checkNotBlank(name, "Criterion name")
                .checkMaxLength(name, 100, "Criterion name")
                .checkRange(weight, 1, 100, "Weight");
        collector.throwIfAny();

        criterion.setName(name.trim());
        criterion.setDescription(description);
        criterion.setWeight(weight);

        try {
            return criterionDao.update(criterion);
        } catch (DuplicateEntityException e) {
            throw new ValidationException("A criterion with this name already exists for your account: " + name);
        }
    }

    @Override
    public void deactivate(User actor, Long criterionId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        requireManager(actor);
        requireCriterionOwner(actor, criterionId);
        criterionDao.deactivate(criterionId);
    }

    @Override
    public void delete(User actor, Long criterionId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        requireManager(actor);
        requireCriterionOwner(actor, criterionId);
        criterionDao.delete(criterionId);
    }

    @Override
    public List<Criterion> listMine(User actor, boolean activeOnly)
            throws AuthorizationException, DataAccessException {
        requireManager(actor);
        return criterionDao.findByManager(actor.getId(), activeOnly);
    }

    private void requireManager(User actor) {
        if (actor == null || actor.getRole() != Role.MANAGER) {
            throw new AuthorizationException("Only managers can manage criteria");
        }
    }

    private Criterion requireCriterionOwner(User actor, Long criterionId) throws DataAccessException {
        if (criterionId == null) {
            throw new ValidationException("Criterion ID cannot be null");
        }
        Criterion c = criterionDao.findById(criterionId)
                .orElseThrow(() -> new EntityNotFoundException("Criterion not found with ID: " + criterionId));
        if (!c.getManagerId().equals(actor.getId())) {
            throw new AuthorizationException("You do not own this criterion");
        }
        return c;
    }
}
