package com.epes.service;

/**
 * Data transfer object representing a criterion rating and comment submitted
 * during evaluation creation.
 */
public class ScoreInput {

    private int rating;
    private String comment;

    /** Default constructor. */
    public ScoreInput() {
    }

    /**
     * Constructs a ScoreInput.
     *
     * @param rating  criterion rating (1-5)
     * @param comment criterion feedback commentary
     */
    public ScoreInput(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
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
}
