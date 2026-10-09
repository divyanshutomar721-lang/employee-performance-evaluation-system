package com.epes.service.report;

import java.util.Collections;
import java.util.List;

/**
 * Manager-level aggregated overview covering all managed teams and employees.
 */
public class ManagerOverview {

    private final List<TeamReport> teams;
    private final int totalEmployees;
    private final double overallAverage;

    /**
     * Constructs a ManagerOverview.
     *
     * @param teams          list of team reports managed by the manager
     * @param totalEmployees distinct count of employees across all managed teams
     * @param overallAverage overall average score across all evaluations in managed teams
     */
    public ManagerOverview(List<TeamReport> teams, int totalEmployees, double overallAverage) {
        this.teams = teams != null ? Collections.unmodifiableList(teams) : Collections.emptyList();
        this.totalEmployees = totalEmployees;
        this.overallAverage = overallAverage;
    }

    public List<TeamReport> getTeams() {
        return teams;
    }

    public int totalEmployees() {
        return totalEmployees;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }

    public double getOverallAverage() {
        return overallAverage;
    }
}
