package com.r3m.spaceshooter.entity;

import java.awt.Color;
import java.awt.Graphics2D;

/** * Represents a small background star that moves from right to left * across the game screen. * * <p>A star has a position, movement speed, and visual size. Its * horizontal position is updated over time using the elapsed time * provided to {@link #update(double)}.</p> * * <p>The star can be rendered as a white circle using * {@link #render(Graphics2D)} and can be checked to determine whether * it has moved beyond the left edge of the screen using * {@link #isPastLeftEdge()}.</p> */
public class Star {
    private double x;
    private final double y;
    private final double speed;
    private final int size;

    /** * Creates a new star with the specified position, speed, and size. * * @param x the initial horizontal position of the star * @param y the vertical position of the star * @param speed the horizontal movement speed of the star * @param size the diameter of the star in pixels */
    /**
     * Creates a background star.
     *
     * @param x initial horizontal position
     * @param y vertical position
     * @param speed leftward speed in pixels per second
     * @param size rendered diameter in pixels
     */
    public Star(double x, double y, double speed, int size) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.size = size;
    }

    /** * Updates the star's horizontal position based on the elapsed time. * * <p>The star moves from right to left. The amount of movement * is calculated by multiplying the star's speed by the elapsed * time.</p> * * @param deltaTime the elapsed time since the previous update, * typically measured in seconds */
    /** Advances the star leftward.
     * @param deltaTime elapsed frame time in seconds */
    public void update(double deltaTime) {
        x -= speed * deltaTime;
    }

    /** * Renders the star as a white circle on the specified graphics context. * * @param graphics the {@link Graphics2D} object used to draw the star */
    /** Draws the star.
     * @param graphics destination graphics context */
    public void render(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.fillOval((int) x, (int) y, size, size);
    }

    /** * Determines whether the star has moved beyond the left edge * of the game screen. * * <p>The star is considered past the left edge when its rightmost * point is more than 10 pixels beyond the left side of the screen.</p> * * @return {@code true} if the star has moved past the left edge; * {@code false} otherwise */
    /** Tests the removal boundary.
     * @return whether the star is beyond the left boundary */
    public boolean isPastLeftEdge() {
        return x + size < -10;
    }
}
