package com.r3m.spaceshooter.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** A single asteroid that travels horizontally across the playfield. */
public class Asteroid {
    private static final int SIZE = 72;

    private final BufferedImage image;
    private final double speed;
    private double x;
    private final double y;

    public Asteroid(BufferedImage image, double x, double y, double speed) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.speed = speed;
    }

    public void update(double deltaTime) {
        x += speed * deltaTime;
    }

    public void render(Graphics2D graphics) {
        graphics.drawImage(image, (int) x, (int) y, SIZE, SIZE, null);
    }

    public boolean isPastRightEdge(int panelWidth) {
        return x > panelWidth;
    }

    public static int getSize() {
        return SIZE;
    }
}
