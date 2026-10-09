package com.epes.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing an employee's membership and role in a team.
 */
public class TeamMember {

    private Long teamId;
    private Long employeeId;
    private String roleInTeam;
    private LocalDateTime joinedAt;

    /** Default constructor. */
    public TeamMember() {
    }

    /**
     * Constructs a TeamMember membership association.
     *
     * @param teamId     team identifier
     * @param employeeId employee user identifier
     * @param roleInTeam functional role inside the team
     * @param joinedAt   joining timestamp
     */
    public TeamMember(Long teamId, Long employeeId, String roleInTeam, LocalDateTime joinedAt) {
        this.teamId = teamId;
        this.employeeId = employeeId;
        this.roleInTeam = roleInTeam;
        this.joinedAt = joinedAt;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getRoleInTeam() {
        return roleInTeam;
    }

    public void setRoleInTeam(String roleInTeam) {
        this.roleInTeam = roleInTeam;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamMember that = (TeamMember) o;
        return Objects.equals(teamId, that.teamId) && Objects.equals(employeeId, that.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, employeeId);
    }

    @Override
    public String toString() {
        return "TeamMember{" +
                "teamId=" + teamId +
                ", employeeId=" + employeeId +
                ", roleInTeam='" + roleInTeam + '\'' +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
