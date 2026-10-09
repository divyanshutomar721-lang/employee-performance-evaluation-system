package com.epes.model;

import java.util.Objects;

/**
 * Domain model representing an individual criterion score snapshot inside an evaluation.
 */
public class EvaluationScore {

    private Long id;
    private Long evaluationId;
    private Long criterionId;
    private String criterionName;
    private int weight;
    private int rating;
    private String comment;

    /** Default constructor. */
    public EvaluationScore() {
    }

    /**
     * Constructs an EvaluationScore snapshot.
     *
     * @param id            score record identifier
     * @param evaluationId  parent evaluation identifier
     * @param criterionId   source criterion identifier (optional)
     * @param criterionName snapshot name of criterion
     * @param weight        snapshot weight of criterion
     * @param rating        rating score (1-5)
     * @param comment       feedback remarks on this criterion
     */
    public EvaluationScore(Long id, Long evaluationId, Long criterionId, String criterionName,
                           int weight, int rating, String comment) {
        this.id = id;
        this.evaluationId = evaluationId;
        this.criterionId = criterionId;
        this.criterionName = criterionName;
        this.weight = weight;
        this.rating = rating;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEvaluationId() {
        return evaluationId;
    }

    public void setEvaluationId(Long evaluationId) {
        this.evaluationId = evaluationId;
    }

    public Long getCriterionId() {
        return criterionId;
    }

    public void setCriterionId(Long criterionId) {
        this.criterionId = criterionId;
    }

    public String getCriterionName() {
        return criterionName;
    }

    public void setCriterionName(String criterionName) {
        this.criterionName = criterionName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationScore that = (EvaluationScore) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EvaluationScore{" +
                "id=" + id +
                ", evaluationId=" + evaluationId +
                ", criterionName='" + criterionName + '\'' +
                ", weight=" + weight +
                ", rating=" + rating +
                '}';
    }
}
