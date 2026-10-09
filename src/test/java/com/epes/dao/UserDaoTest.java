package com.epes.dao;

import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.DuplicateEntityException;
import com.epes.model.Admin;
import com.epes.model.Employee;
import com.epes.model.Manager;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.model.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link UserDao} operations.
 */
class UserDaoTest extends BaseDaoTest {

    @Test
    @DisplayName("Create user, find by ID, find by email, update and soft deactivate")
    void testUserCrudAndDeactivate() throws Exception {
        UserDao userDao = new UserDaoImpl(connectionFactory);

        Employee emp = new Employee();
        emp.setEmail("test.emp1@epes.local");
        emp.setFullName("Test Employee One");
        emp.setActive(true);
        emp.setCreatedAt(LocalDateTime.now());

        User created = userDao.create(connection, emp);
        assertNotNull(created.getId());
        assertEquals("test.emp1@epes.local", created.getEmail());
        assertEquals("/employee/dashboard", created.getDashboardPath());

        // Find by ID
        Optional<User> byId = userDao.findById(connection, created.getId());
        assertTrue(byId.isPresent());
        assertEquals("Test Employee One", byId.get().getFullName());
        assertInstanceOf(Employee.class, byId.get());

        // Find by Email (case-insensitive)
        Optional<User> byEmail = userDao.findByEmail(connection, "TEST.EMP1@EPES.LOCAL");
        assertTrue(byEmail.isPresent());
        assertEquals(created.getId(), byEmail.get().getId());

        // Update
        byId.get().setFullName("Updated Employee Name");
        User updated = userDao.update(connection, byId.get());
        assertEquals("Updated Employee Name", updated.getFullName());

        // Deactivate
        userDao.deactivate(connection, created.getId());
        Optional<User> deactivated = userDao.findById(connection, created.getId());
        assertTrue(deactivated.isPresent());
        assertFalse(deactivated.get().isActive());
    }

    @Test
    @DisplayName("Creating user with duplicate email throws DuplicateEntityException")
    void testDuplicateEmailThrowsException() throws Exception {
        UserDao userDao = new UserDaoImpl(connectionFactory);

        Manager m1 = new Manager();
        m1.setEmail("duplicate.check@epes.local");
        m1.setFullName("Manager One");
        userDao.create(connection, m1);

        Manager m2 = new Manager();
        m2.setEmail("DUPLICATE.CHECK@epes.local");
        m2.setFullName("Manager Two");

        assertThrows(DuplicateEntityException.class, () -> userDao.create(connection, m2));
    }

    @Test
    @DisplayName("Test user polymorphism and role counts")
    void testPolymorphismAndCounts() throws Exception {
        UserDao userDao = new UserDaoImpl(connectionFactory);

        Admin admin = new Admin();
        admin.setEmail("admin.test@epes.local");
        admin.setFullName("Test Admin");
        userDao.create(connection, admin);
        assertEquals("/admin/dashboard", admin.getDashboardPath());

        Manager manager = new Manager();
        manager.setEmail("manager.test@epes.local");
        manager.setFullName("Test Manager");
        userDao.create(connection, manager);
        assertEquals("/manager/dashboard", manager.getDashboardPath());

        List<User> managers = userDao.findByRole(connection, Role.MANAGER);
        assertFalse(managers.isEmpty());
        assertTrue(managers.stream().anyMatch(u -> u.getEmail().equals("manager.test@epes.local")));

        long adminCount = userDao.countByRole(connection, Role.ADMIN);
        assertTrue(adminCount >= 1);
    }
}
