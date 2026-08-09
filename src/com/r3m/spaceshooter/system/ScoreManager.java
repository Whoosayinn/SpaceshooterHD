package com.r3m.spaceshooter.system;


public class ScoreManager {
    private int score = 0;

    public void addScore(int points) {
        score += points;
    }

    public void subtractScore(int points) {
        score = Math.max(0, score - Math.max(0, points));
    }

    public int getScore() {
        return score;
    }

    public void reset() {
        score = 0;
    }
}
