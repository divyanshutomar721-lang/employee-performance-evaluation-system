package com.epes.model;

import java.time.LocalDateTime;

/**
 * Domain model representing a Manager user.
 */
public class Manager extends User {

    /** Default constructor. */
    public Manager() {
        setRole(Role.MANAGER);
    }

    /**
     * Parameterized constructor for Manager.
     *
     * @param id        user identifier
     * @param email     user email
     * @param fullName  user full name
     * @param active    account status
     * @param createdAt creation timestamp
     */
    public Manager(Long id, String email, String fullName, boolean active, LocalDateTime createdAt) {
        super(id, email, fullName, Role.MANAGER, active, createdAt);
    }

    @Override
    public String getDashboardPath() {
        return "/manager/dashboard";
    }
}
