package com.epes.service.report;

import com.epes.model.RocketStage;
import com.epes.model.Team;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Team-wide performance evaluation summary report.
 */
public class TeamReport {

    private final Team team;
    private final List<EmployeeSummaryRow> rows;
    private final Map<RocketStage, Long> stageDistribution;
    private final double teamAverageScore;

    /**
     * Constructs a TeamReport.
     *
     * @param team              team entity
     * @param rows              summary rows per employee
     * @param stageDistribution count of employees across each RocketStage tier
     * @param teamAverageScore  overall average score across the team
     */
    public TeamReport(Team team, List<EmployeeSummaryRow> rows,
                      Map<RocketStage, Long> stageDistribution, double teamAverageScore) {
        this.team = team;
        this.rows = rows != null ? Collections.unmodifiableList(rows) : Collections.emptyList();
        this.stageDistribution = stageDistribution != null ? Collections.unmodifiableMap(new EnumMap<>(stageDistribution)) : Collections.emptyMap();
        this.teamAverageScore = teamAverageScore;
    }

    public Team getTeam() {
        return team;
    }

    public List<EmployeeSummaryRow> getRows() {
        return rows;
    }

    public Map<RocketStage, Long> getStageDistribution() {
        return stageDistribution;
    }

    public double getTeamAverageScore() {
        return teamAverageScore;
    }
}
