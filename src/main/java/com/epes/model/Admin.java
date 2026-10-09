package com.epes.model;

import java.time.LocalDateTime;

/**
 * Domain model representing an Administrator user.
 */
public class Admin extends User {

    /** Default constructor. */
    public Admin() {
        setRole(Role.ADMIN);
    }

    /**
     * Parameterized constructor for Admin.
     *
     * @param id        user identifier
     * @param email     user email
     * @param fullName  user full name
     * @param active    account status
     * @param createdAt creation timestamp
     */
    public Admin(Long id, String email, String fullName, boolean active, LocalDateTime createdAt) {
        super(id, email, fullName, Role.ADMIN, active, createdAt);
    }

    @Override
    public String getDashboardPath() {
        return "/admin/dashboard";
    }
}
