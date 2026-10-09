package com.epes.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing an employee performance goal.
 */
public class Goal {

    private Long id;
    private Long employeeId;
    private Long setBy;
    private GoalType goalType;
    private String description;
    private LocalDate targetDate;
    private GoalStatus status = GoalStatus.NOT_STARTED;
    private int progressPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Default constructor. */
    public Goal() {
    }

    /**
     * Constructs a Goal entity.
     *
     * @param id              goal identifier
     * @param employeeId      assigned employee identifier
     * @param setBy           assigner user identifier (manager or self)
     * @param goalType        goal type classification
     * @param description     description of the goal
     * @param targetDate      target completion date
     * @param status          lifecycle status
     * @param progressPercent current progress percentage (0-100)
     * @param createdAt       creation timestamp
     * @param updatedAt       last update timestamp
     */
    public Goal(Long id, Long employeeId, Long setBy, GoalType goalType, String description,
                LocalDate targetDate, GoalStatus status, int progressPercent,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.setBy = setBy;
        this.goalType = goalType;
        this.description = description;
        this.targetDate = targetDate;
        this.status = status != null ? status : GoalStatus.NOT_STARTED;
        this.progressPercent = progressPercent;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Long getSetBy() {
        return setBy;
    }

    public void setSetBy(Long setBy) {
        this.setBy = setBy;
    }

    public GoalType getGoalType() {
        return goalType;
    }

    public void setGoalType(GoalType goalType) {
        this.goalType = goalType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goal goal = (Goal) o;
        return Objects.equals(id, goal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", setBy=" + setBy +
                ", goalType=" + goalType +
                ", targetDate=" + targetDate +
                ", status=" + status +
                ", progressPercent=" + progressPercent +
                '}';
    }
}
