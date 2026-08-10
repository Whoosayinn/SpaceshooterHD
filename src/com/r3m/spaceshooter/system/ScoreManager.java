package com.r3m.spaceshooter.system;


/** Maintains the current non-negative game score. */
public class ScoreManager {
    private int score = 0;

    /** Creates a score manager initialized to zero. */
    public ScoreManager() {
    }

    /**
     * Adds points to the score.
     *
     * @param points amount to add
     */
    public void addScore(int points) {
        score += points;
    }

    /**
     * Subtracts a non-negative amount without allowing the score below zero.
     *
     * @param points amount to subtract; negative values are ignored
     */
    public void subtractScore(int points) {
        score = Math.max(0, score - Math.max(0, points));
    }

    /**
     * Returns the current score.
     *
     * @return current score
     */
    public int getScore() {
        return score;
    }

    /** Resets the score to zero. */
    public void reset() {
        score = 0;
    }
}
