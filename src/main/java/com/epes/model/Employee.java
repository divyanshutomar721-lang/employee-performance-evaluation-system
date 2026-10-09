package com.epes.model;

import java.time.LocalDateTime;

/**
 * Domain model representing an Employee user.
 */
public class Employee extends User {

    /** Default constructor. */
    public Employee() {
        setRole(Role.EMPLOYEE);
    }

    /**
     * Parameterized constructor for Employee.
     *
     * @param id        user identifier
     * @param email     user email
     * @param fullName  user full name
     * @param active    account status
     * @param createdAt creation timestamp
     */
    public Employee(Long id, String email, String fullName, boolean active, LocalDateTime createdAt) {
        super(id, email, fullName, Role.EMPLOYEE, active, createdAt);
    }

    @Override
    public String getDashboardPath() {
        return "/employee/dashboard";
    }
}
