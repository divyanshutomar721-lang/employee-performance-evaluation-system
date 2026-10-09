package com.epes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing an organizational team.
 */
public class Team {

    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private LocalDateTime createdAt;
    private List<TeamMember> members = new ArrayList<>();

    /** Default constructor. */
    public Team() {
    }

    /**
     * Constructs a Team with core attributes.
     *
     * @param id          team identifier
     * @param name        team name
     * @param description description of the team
     * @param managerId   id of the manager heading the team
     * @param createdAt   creation timestamp
     */
    public Team(Long id, String name, String description, Long managerId, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TeamMember> getMembers() {
        return members;
    }

    public void setMembers(List<TeamMember> members) {
        this.members = members != null ? members : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", managerId=" + managerId +
                ", createdAt=" + createdAt +
                ", membersCount=" + members.size() +
                '}';
    }
}
