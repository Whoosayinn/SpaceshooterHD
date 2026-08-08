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
    private double x;
    private double y;

    public Bullet(double x, double y, double velocityX, double velocityY) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void update(double deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
    }

    public void render(Graphics2D graphics) {
        graphics.setColor(Color.YELLOW);
        graphics.fillRect((int) x, (int) y, WIDTH, HEIGHT);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, WIDTH, HEIGHT);
    }

    // now checks all four edges, since angled bullets can leave through
    // the top or bottom of the screen, not just the right side
    public boolean isOffScreen(int panelWidth, int panelHeight) {
        return x > panelWidth || x < -WIDTH || y < -HEIGHT || y > panelHeight;
    }

    public static int getHeight() {
        return HEIGHT;
    }
}