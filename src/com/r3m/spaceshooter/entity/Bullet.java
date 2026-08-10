package com.r3m.spaceshooter.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * A single bullet fired by the player. Travels in a straight line at
 * whatever angle it was given (velocityX, velocityY) — used both for
 * normal forward shots and for angled spread-shot bullets.
 * No image asset — drawn directly as a small glowing rectangle.
 */
public class Bullet {
    private static final int WIDTH = 12;
    private static final int HEIGHT = 4;

    private final double velocityX;
    private final double velocityY;
    private final Color color;
    private double x;
    private double y;

    /**
     * Creates a yellow bullet.
     *
     * @param x initial horizontal position
     * @param y initial vertical position
     * @param velocityX horizontal velocity in pixels per second
     * @param velocityY vertical velocity in pixels per second
     */
    public Bullet(double x, double y, double velocityX, double velocityY) {
        this(x, y, velocityX, velocityY, Color.YELLOW);
    }

    /**
     * Creates a bullet with the requested color.
     *
     * @param x initial horizontal position
     * @param y initial vertical position
     * @param velocityX horizontal velocity in pixels per second
     * @param velocityY vertical velocity in pixels per second
     * @param color bullet color
     */
    public Bullet(double x, double y, double velocityX, double velocityY, Color color) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.color = color;
    }

    /** Advances the bullet.
     * @param deltaTime elapsed frame time in seconds */
    public void update(double deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
    }

    /** Draws the bullet.
     * @param graphics destination graphics context */
    public void render(Graphics2D graphics) {
        graphics.setColor(color);
        graphics.fillRect((int) x, (int) y, WIDTH, HEIGHT);
    }

    /** Returns the collision bounds.
     * @return rectangular bullet bounds */
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, WIDTH, HEIGHT);
    }

    // now checks all four edges, since angled bullets can leave through
    // the top or bottom of the screen, not just the right side
    /**
     * Tests whether the bullet has left any viewport edge.
     *
     * @param panelWidth viewport width in pixels
     * @param panelHeight viewport height in pixels
     * @return {@code true} when the bullet is outside the viewport
     */
    public boolean isOffScreen(int panelWidth, int panelHeight) {
        return x > panelWidth || x < -WIDTH || y < -HEIGHT || y > panelHeight;
    }

    /** Returns the fixed bullet height.
     * @return bullet height in pixels */
    public static int getHeight() {
        return HEIGHT;
    }
}
