package com.epes.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing feedback submitted by a manager to an employee.
 */
public class Feedback {

    private Long id;
    private Long employeeId;
    private Long managerId;
    private Long evaluationId;
    private String comments;
    private int rating;
    private LocalDateTime createdAt;

    /** Default constructor. */
    public Feedback() {
    }

    /**
     * Constructs a Feedback record.
     *
     * @param id           feedback identifier
     * @param employeeId   recipient employee identifier
     * @param managerId    authoring manager identifier
     * @param evaluationId related evaluation identifier (optional)
     * @param comments     feedback commentary
     * @param rating       numeric rating (1-5)
     * @param createdAt    creation timestamp
     */
    public Feedback(Long id, Long employeeId, Long managerId, Long evaluationId,
                    String comments, int rating, LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.managerId = managerId;
        this.evaluationId = evaluationId;
        this.comments = comments;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
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
        Feedback feedback = (Feedback) o;
        return Objects.equals(id, feedback.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", managerId=" + managerId +
                ", evaluationId=" + evaluationId +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                '}';
    }
}
