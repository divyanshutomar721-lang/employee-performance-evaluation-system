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
    private final int evaluationCount;

    /**
     * Constructs an EmployeeSummaryRow (5-parameter overload).
     */
    public EmployeeSummaryRow(Long employeeId, String employeeName, double averageScore,
                              RocketStage latestStage, int goalCompletionPercent) {
        this(employeeId, employeeName, averageScore, latestStage, goalCompletionPercent, averageScore > 0.0 ? 1 : 0);
    }

    /**
     * Constructs an EmployeeSummaryRow with evaluation count.
     */
    public EmployeeSummaryRow(Long employeeId, String employeeName, double averageScore,
                              RocketStage latestStage, int goalCompletionPercent, int evaluationCount) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.averageScore = averageScore;
        this.latestStage = latestStage;
        this.goalCompletionPercent = goalCompletionPercent;
        this.evaluationCount = evaluationCount;
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

    public int getEvaluationCount() {
        return evaluationCount;
    }

    public boolean hasEvaluations() {
        return evaluationCount > 0;
    }
}

