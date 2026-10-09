package com.epes.dao;

import com.epes.dao.impl.CriterionDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.model.Criterion;
import com.epes.model.Manager;
import com.epes.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CriterionDao} operations.
 */
class CriterionDaoTest extends BaseDaoTest {

    @Test
    @DisplayName("Create criterion, find by manager with active filter, update and delete")
    void testCriterionCrud() throws Exception {
        UserDao userDao = new UserDaoImpl(connectionFactory);
        CriterionDao criterionDao = new CriterionDaoImpl(connectionFactory);

        Manager manager = new Manager();
        manager.setEmail("criterion.mgr@epes.local");
        manager.setFullName("Criterion Manager");
        User savedManager = userDao.create(connection, manager);

        // Create active criterion
        Criterion c1 = new Criterion();
        c1.setManagerId(savedManager.getId());
        c1.setName("Technical Depth");
        c1.setDescription("Code architecture and quality");
        c1.setWeight(40);
        c1.setActive(true);

        Criterion saved1 = criterionDao.create(connection, c1);
        assertNotNull(saved1.getId());

        // Create second criterion and deactivate it
        Criterion c2 = new Criterion();
        c2.setManagerId(savedManager.getId());
        c2.setName("Communication");
        c2.setDescription("Collaboration and clarity");
        c2.setWeight(20);
        c2.setActive(true);
        Criterion saved2 = criterionDao.create(connection, c2);

        criterionDao.deactivate(connection, saved2.getId());

        // Find active only
        List<Criterion> activeList = criterionDao.findByManager(connection, savedManager.getId(), true);
        assertEquals(1, activeList.size());
        assertEquals("Technical Depth", activeList.get(0).getName());

        // Find all
        List<Criterion> allList = criterionDao.findByManager(connection, savedManager.getId(), false);
        assertEquals(2, allList.size());

        // Update
        saved1.setWeight(50);
        saved1.setName("Advanced Technical Depth");
        Criterion updated = criterionDao.update(connection, saved1);
        assertEquals(50, updated.getWeight());

        // Delete
        criterionDao.delete(connection, saved2.getId());
        Optional<Criterion> deleted = criterionDao.findById(connection, saved2.getId());
        assertTrue(deleted.isEmpty());
    }
}
