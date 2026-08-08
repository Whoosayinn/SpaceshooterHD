package com.r3m.spaceshooter.entity;

import java.awt.Color;
import java.awt.Graphics2D;

/** A small background star that travels from right to left. */
public class Star {
    private double x;
    private final double y;
    private final double speed;
    private final int size;

    public Star(double x, double y, double speed, int size) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.size = size;
    }

    public void update(double deltaTime) {
        x -= speed * deltaTime;
    }

    public void render(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.fillOval((int) x, (int) y, size, size);
    }

    public boolean isPastLeftEdge() {
        return x + size < -10;
    }
}
