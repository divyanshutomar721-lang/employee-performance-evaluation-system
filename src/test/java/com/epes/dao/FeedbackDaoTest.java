package com.epes.dao;

import com.epes.dao.impl.FeedbackDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.model.Employee;
import com.epes.model.Feedback;
import com.epes.model.Manager;
import com.epes.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link FeedbackDao} operations.
 */
class FeedbackDaoTest extends BaseDaoTest {

    @Test
    @DisplayName("Feedback CRUD and findByEmployee ordered newest first")
    void testFeedbackCrud() throws Exception {
        UserDao userDao = new UserDaoImpl(connectionFactory);
        FeedbackDao feedbackDao = new FeedbackDaoImpl(connectionFactory);

        Manager manager = new Manager();
        manager.setEmail("fb.manager@epes.local");
        manager.setFullName("Feedback Manager");
        User savedManager = userDao.create(connection, manager);

        Employee employee = new Employee();
        employee.setEmail("fb.employee@epes.local");
        employee.setFullName("Feedback Employee");
        User savedEmployee = userDao.create(connection, employee);

        // Create Feedback 1
        Feedback fb1 = new Feedback();
        fb1.setEmployeeId(savedEmployee.getId());
        fb1.setManagerId(savedManager.getId());
        fb1.setComments("Great teamwork on the release!");
        fb1.setRating(5);
        fb1.setCreatedAt(LocalDateTime.now().minusDays(2));
        Feedback saved1 = feedbackDao.create(connection, fb1);
        assertNotNull(saved1.getId());

        // Create Feedback 2 (newer)
        Feedback fb2 = new Feedback();
        fb2.setEmployeeId(savedEmployee.getId());
        fb2.setManagerId(savedManager.getId());
        fb2.setComments("Solid sprint velocity.");
        fb2.setRating(4);
        fb2.setCreatedAt(LocalDateTime.now());
        Feedback saved2 = feedbackDao.create(connection, fb2);
        assertNotNull(saved2.getId());

        // Find by employee (newest first)
        List<Feedback> empFeedback = feedbackDao.findByEmployee(connection, savedEmployee.getId());
        assertEquals(2, empFeedback.size());
        assertEquals(saved2.getId(), empFeedback.get(0).getId());

        // Find by manager
        List<Feedback> mgrFeedback = feedbackDao.findByManager(connection, savedManager.getId());
        assertEquals(2, mgrFeedback.size());

        // Find by ID
        Optional<Feedback> byId = feedbackDao.findById(connection, saved1.getId());
        assertTrue(byId.isPresent());
        assertEquals(5, byId.get().getRating());

        // Delete
        feedbackDao.delete(connection, saved1.getId());
        Optional<Feedback> deleted = feedbackDao.findById(connection, saved1.getId());
        assertTrue(deleted.isEmpty());
    }
}
