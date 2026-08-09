package com.r3m.spaceshooter.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * A pickup that drifts left across the screen, same as asteroids.
 * Colliding with it grants the player a temporary spread shot.
 * Drawn as a small diamond with an "S" — no image asset needed.
 */
public class PowerUp {
    private static final int SIZE = 28;
    private static final double DRIFT_SPEED = 80.0;

    private double x;
    private double y;

    public PowerUp(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update(double deltaTime) {
        x -= DRIFT_SPEED * deltaTime;
    }

    public void render(Graphics2D graphics) {
        graphics.setColor(new Color(80, 220, 255));
        int[] xPoints = {(int) x + SIZE / 2, (int) x + SIZE, (int) x + SIZE / 2, (int) x};
        int[] yPoints = {(int) y, (int) y + SIZE / 2, (int) y + SIZE, (int) y + SIZE / 2};
        graphics.fillPolygon(xPoints, yPoints, 4);

        graphics.setColor(Color.BLACK);
        graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 14f));
        graphics.drawString("S", (int) x + SIZE / 2 - 4, (int) y + SIZE / 2 + 5);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, SIZE, SIZE);
    }

    public boolean isPastLeftEdge() {
        return x + SIZE < 0;
    }
}