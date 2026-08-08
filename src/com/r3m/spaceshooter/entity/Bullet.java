package com.r3m.spaceshooter.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * A single bullet fired by the player. Travels left-to-right at a fixed
 * speed. No image asset — drawn directly as a small glowing rectangle.
 */
public class Bullet {
    private static final int WIDTH = 12;
    private static final int HEIGHT = 4;

    private final double velocityX;
    private double x;
    private double y;

    public Bullet(double x, double y, double velocityX) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
    }

    public void update(double deltaTime) {
        x += velocityX * deltaTime;
    }

    public void render(Graphics2D graphics) {
        graphics.setColor(Color.YELLOW);
        graphics.fillRect((int) x, (int) y, WIDTH, HEIGHT);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, WIDTH, HEIGHT);
    }

    public boolean isPastRightEdge(int panelWidth) {
        return x > panelWidth;
    }

    public static int getHeight() {
        return HEIGHT;
    }
}