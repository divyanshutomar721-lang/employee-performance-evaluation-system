package com.epes.dao;

import com.epes.dao.impl.GoalDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.model.Employee;
import com.epes.model.Goal;
import com.epes.model.GoalStatus;
import com.epes.model.GoalType;
import com.epes.model.Manager;
import com.epes.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link GoalDao} operations.
 */
class GoalDaoTest extends BaseDaoTest {

    @Test
    @DisplayName("Goal CRUD and findDueBefore non-completed goals")
    void testGoalCrudAndDueBefore() throws Exception {
        UserDao userDao = new UserDaoImpl(connectionFactory);
        GoalDao goalDao = new GoalDaoImpl(connectionFactory);

        Manager manager = new Manager();
        manager.setEmail("goal.mgr@epes.local");
        manager.setFullName("Goal Manager");
        User savedManager = userDao.create(connection, manager);

        Employee employee = new Employee();
        employee.setEmail("goal.emp@epes.local");
        employee.setFullName("Goal Employee");
        User savedEmployee = userDao.create(connection, employee);

        // Create Goal 1: Manager assigned, due in 3 days, IN_PROGRESS
        Goal g1 = new Goal();
        g1.setEmployeeId(savedEmployee.getId());
        g1.setSetBy(savedManager.getId());
        g1.setGoalType(GoalType.MANAGER_ASSIGNED);
        g1.setDescription("Complete microservices refactoring");
        g1.setTargetDate(LocalDate.now().plusDays(3));
        g1.setStatus(GoalStatus.IN_PROGRESS);
        g1.setProgressPercent(40);

        Goal savedGoal1 = goalDao.create(connection, g1);
        assertNotNull(savedGoal1.getId());

        // Create Goal 2: Personal goal, due in 10 days, COMPLETED
        Goal g2 = new Goal();
        g2.setEmployeeId(savedEmployee.getId());
        g2.setSetBy(savedEmployee.getId());
        g2.setGoalType(GoalType.PERSONAL);
        g2.setDescription("Read Java Concurrency in Practice");
        g2.setTargetDate(LocalDate.now().plusDays(10));
        g2.setStatus(GoalStatus.COMPLETED);
        g2.setProgressPercent(100);

        Goal savedGoal2 = goalDao.create(connection, g2);
        assertNotNull(savedGoal2.getId());

        // Find by employee
        List<Goal> empGoals = goalDao.findByEmployee(connection, savedEmployee.getId());
        assertEquals(2, empGoals.size());

        // Find by setBy
        List<Goal> mgrAssigned = goalDao.findBySetBy(connection, savedManager.getId());
        assertEquals(1, mgrAssigned.size());

        // Find due before 5 days from now (should only include g1, since g2 is COMPLETED)
        List<Goal> dueGoals = goalDao.findDueBefore(connection, LocalDate.now().plusDays(5));
        assertTrue(dueGoals.stream().anyMatch(g -> g.getId().equals(savedGoal1.getId())));
        assertFalse(dueGoals.stream().anyMatch(g -> g.getId().equals(savedGoal2.getId())));

        // Update Goal 1
        savedGoal1.setProgressPercent(80);
        savedGoal1.setStatus(GoalStatus.IN_PROGRESS);
        savedGoal1.setDescription("Complete microservices refactoring v2");
        Goal updated = goalDao.update(connection, savedGoal1);
        assertEquals(80, updated.getProgressPercent());
        assertEquals("Complete microservices refactoring v2", updated.getDescription());

        // Delete Goal 2
        goalDao.delete(connection, savedGoal2.getId());
        Optional<Goal> deleted = goalDao.findById(connection, savedGoal2.getId());
        assertTrue(deleted.isEmpty());
    }
}
