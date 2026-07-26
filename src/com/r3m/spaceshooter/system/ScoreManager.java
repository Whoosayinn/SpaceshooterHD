package com.r3m.spaceshooter.system;


public class ScoreManager {
    private int score = 0;

    public void addScore(int points) {
        score += points;
    }

    public int getScore() {
        return score;
    }

    public void reset() {
        score = 0;
    }
}