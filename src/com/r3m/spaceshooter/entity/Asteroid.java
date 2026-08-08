package com.r3m.spaceshooter.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** A single asteroid that travels left and can bounce vertically. */
public class Asteroid {
    private static final int SIZE = 72;
    private static final int MIN_SPLITTABLE_SIZE = 48;

    private final BufferedImage image;
    private final double velocityX;
    private final int diameter;
    private double velocityY;
    private double x;
    private double y;
    private double asteroidCollisionGraceRemaining;

    public Asteroid(
        BufferedImage image,
        double x,
        double y,
        double velocityX,
        double velocityY
    ) {
        this(image, x, y, velocityX, velocityY, SIZE, 0.0);
    }

    public Asteroid(
        BufferedImage image,
        double x,
        double y,
        double velocityX,
        double velocityY,
        int diameter,
        double asteroidCollisionGraceSeconds
    ) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.diameter = diameter;
        this.asteroidCollisionGraceRemaining = asteroidCollisionGraceSeconds;
    }

    public void update(double deltaTime, int panelHeight) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
        asteroidCollisionGraceRemaining = Math.max(
            0.0,
            asteroidCollisionGraceRemaining - deltaTime
        );

        if (y < 0) {
            y = 0;
            velocityY = Math.abs(velocityY);
        } else if (y + diameter > panelHeight) {
            y = Math.max(0, panelHeight - diameter);
            velocityY = -Math.abs(velocityY);
        }
    }

    public void render(Graphics2D graphics) {
        graphics.drawImage(image, (int) x, (int) y, diameter, diameter, null);
    }

    public Rectangle getBounds() {
        int hitboxInset = Math.max(3, diameter / 9);
        return new Rectangle(
            (int) x + hitboxInset,
            (int) y + hitboxInset,
            diameter - hitboxInset * 2,
            diameter - hitboxInset * 2
        );
    }

    public boolean isPastLeftEdge() {
        return x + diameter < 0;
    }

    public boolean canCollideWithAsteroids() {
        return asteroidCollisionGraceRemaining <= 0.0;
    }

    public boolean canSplit() {
        return diameter >= MIN_SPLITTABLE_SIZE;
    }

    public int getFragmentSize() {
        return diameter / 2;
    }

    public int getDiameter() {
        return diameter;
    }

    public double getCenterX() {
        return x + diameter / 2.0;
    }

    public double getCenterY() {
        return y + diameter / 2.0;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public static int getSize() {
        return SIZE;
    }
}
