package com.epes.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing an employee performance evaluation.
 */
public class Evaluation {

    private Long id;
    private Long employeeId;
    private Long managerId;
    private Long teamId;
    private String periodLabel;
    private BigDecimal overallScore;
    private RocketStage overallStage;
    private String summaryComment;
    private LocalDateTime createdAt;
    private List<EvaluationScore> scores = new ArrayList<>();

    /** Default constructor. */
    public Evaluation() {
    }

    /**
     * Constructs an Evaluation record.
     *
     * @param id             evaluation identifier
     * @param employeeId     reviewed employee identifier
     * @param managerId      reviewing manager identifier
     * @param teamId         associated team identifier (optional)
     * @param periodLabel    period identifier (e.g. "Q3 2026")
     * @param overallScore   weighted aggregate score (0-100)
     * @param overallStage   performance stage tier
     * @param summaryComment overall evaluation remarks
     * @param createdAt      creation timestamp
     */
    public Evaluation(Long id, Long employeeId, Long managerId, Long teamId, String periodLabel,
                      BigDecimal overallScore, RocketStage overallStage, String summaryComment,
                      LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.managerId = managerId;
        this.teamId = teamId;
        this.periodLabel = periodLabel;
        this.overallScore = overallScore;
        this.overallStage = overallStage;
        this.summaryComment = summaryComment;
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

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(BigDecimal overallScore) {
        this.overallScore = overallScore;
    }

    public RocketStage getOverallStage() {
        return overallStage;
    }

    public void setOverallStage(RocketStage overallStage) {
        this.overallStage = overallStage;
    }

    public String getSummaryComment() {
        return summaryComment;
    }

    public void setSummaryComment(String summaryComment) {
        this.summaryComment = summaryComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<EvaluationScore> getScores() {
        return scores;
    }

    public void setScores(List<EvaluationScore> scores) {
        this.scores = scores != null ? scores : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evaluation that = (Evaluation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", managerId=" + managerId +
                ", teamId=" + teamId +
                ", periodLabel='" + periodLabel + '\'' +
                ", overallScore=" + overallScore +
                ", overallStage=" + overallStage +
                ", createdAt=" + createdAt +
                ", scoresCount=" + scores.size() +
                '}';
    }
}
