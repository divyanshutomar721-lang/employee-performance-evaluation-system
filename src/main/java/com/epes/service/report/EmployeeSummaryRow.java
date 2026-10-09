package com.epes.service.report;

import com.epes.model.RocketStage;

/**
 * Summary row for an individual employee inside a team report.
 */
public class EmployeeSummaryRow {

    private final Long employeeId;
    private final String employeeName;
    private final double averageScore;
    private final RocketStage latestStage;
    private final int goalCompletionPercent;

    /**
     * Constructs an EmployeeSummaryRow.
     *
     * @param employeeId            employee identifier
     * @param employeeName          employee full name
     * @param averageScore          average evaluation score
     * @param latestStage           most recent evaluation stage tier
     * @param goalCompletionPercent goal completion percentage (0-100)
     */
    public EmployeeSummaryRow(Long employeeId, String employeeName, double averageScore,
                              RocketStage latestStage, int goalCompletionPercent) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.averageScore = averageScore;
        this.latestStage = latestStage;
        this.goalCompletionPercent = goalCompletionPercent;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public RocketStage getLatestStage() {
        return latestStage;
    }

    public int getGoalCompletionPercent() {
        return goalCompletionPercent;
    }
}
