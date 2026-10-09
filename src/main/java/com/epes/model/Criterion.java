package com.epes.model;

import java.util.Objects;

/**
 * Domain model representing an evaluation criterion configured by a manager.
 */
public class Criterion {

    private Long id;
    private Long managerId;
    private String name;
    private String description;
    private int weight;
    private boolean active = true;

    /** Default constructor. */
    public Criterion() {
    }

    /**
     * Constructs a Criterion instance.
     *
     * @param id          criterion identifier
     * @param managerId   creator/owner manager identifier
     * @param name        criterion name
     * @param description criterion description
     * @param weight      relative evaluation weight (1-100)
     * @param active      active status flag
     */
    public Criterion(Long id, Long managerId, String name, String description, int weight, boolean active) {
        this.id = id;
        this.managerId = managerId;
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Criterion criterion = (Criterion) o;
        return Objects.equals(id, criterion.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Criterion{" +
                "id=" + id +
                ", managerId=" + managerId +
                ", name='" + name + '\'' +
                ", weight=" + weight +
                ", active=" + active +
                '}';
    }
}
