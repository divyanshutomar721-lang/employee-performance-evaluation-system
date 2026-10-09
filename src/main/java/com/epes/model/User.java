package com.epes.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base abstract domain model representing a system user.
 */
public abstract class User {

    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private boolean active = true;
    private LocalDateTime createdAt;

    /**
     * Default constructor.
     */
    protected User() {
    }

    /**
     * Constructs a User with common properties.
     *
     * @param id        user identifier
     * @param email     user email address
     * @param fullName  user display name
     * @param role      user role
     * @param active    account active status
     * @param createdAt registration timestamp
     */
    protected User(Long id, String email, String fullName, Role role, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    /**
     * Returns the relative URI for this user role's landing dashboard.
     *
     * @return dashboard path string
     */
    public abstract String getDashboardPath();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
