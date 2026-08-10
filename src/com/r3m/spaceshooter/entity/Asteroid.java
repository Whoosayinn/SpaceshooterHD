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

    /**
     * Creates a standard-size asteroid with no collision grace period.
     *
     * @param image asteroid sprite
     * @param x initial horizontal position
     * @param y initial vertical position
     * @param velocityX horizontal velocity in pixels per second
     * @param velocityY vertical velocity in pixels per second
     */
    public Asteroid(
        BufferedImage image,
        double x,
        double y,
        double velocityX,
        double velocityY
    ) {
        this(image, x, y, velocityX, velocityY, SIZE, 0.0);
    }

    /**
     * Creates an asteroid with an explicit diameter and collision grace period.
     *
     * @param image asteroid sprite
     * @param x initial horizontal position
     * @param y initial vertical position
     * @param velocityX horizontal velocity in pixels per second
     * @param velocityY vertical velocity in pixels per second
     * @param diameter rendered diameter in pixels
     * @param asteroidCollisionGraceSeconds delay before asteroid collisions apply
     */
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

    /**
     * Advances the asteroid and bounces it off the viewport's vertical edges.
     *
     * @param deltaTime elapsed frame time in seconds
     * @param panelHeight viewport height in pixels
     */
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

    /**
     * Draws the asteroid sprite.
     *
     * @param graphics destination graphics context
     */
    public void render(Graphics2D graphics) {
        graphics.drawImage(image, (int) x, (int) y, diameter, diameter, null);
    }

    /** Returns the asteroid's collision bounds.
     * @return inset rectangular collision bounds */
    public Rectangle getBounds() {
        int hitboxInset = Math.max(3, diameter / 9);
        return new Rectangle(
            (int) x + hitboxInset,
            (int) y + hitboxInset,
            diameter - hitboxInset * 2,
            diameter - hitboxInset * 2
        );
    }

    /** Tests the left removal boundary.
     * @return whether the entire asteroid is left of the viewport */
    public boolean isPastLeftEdge() {
        return x + diameter < 0;
    }

    /** Tests collision readiness.
     * @return whether the fragment collision grace period has elapsed */
    public boolean canCollideWithAsteroids() {
        return asteroidCollisionGraceRemaining <= 0.0;
    }

    /** Tests whether fragments can be created.
     * @return whether the asteroid is large enough to split */
    public boolean canSplit() {
        return diameter >= MIN_SPLITTABLE_SIZE;
    }

    /** Returns the child-fragment diameter.
     * @return diameter to use for child fragments */
    public int getFragmentSize() {
        return diameter / 2;
    }

    /** Returns this asteroid's size.
     * @return asteroid diameter in pixels */
    public int getDiameter() {
        return diameter;
    }

    /** Returns the horizontal center.
     * @return horizontal center coordinate */
    public double getCenterX() {
        return x + diameter / 2.0;
    }

    /** Returns the vertical center.
     * @return vertical center coordinate */
    public double getCenterY() {
        return y + diameter / 2.0;
    }

    /** Returns horizontal speed and direction.
     * @return horizontal velocity in pixels per second */
    public double getVelocityX() {
        return velocityX;
    }

    /** Returns vertical speed and direction.
     * @return vertical velocity in pixels per second */
    public double getVelocityY() {
        return velocityY;
    }

    /** Returns the standard asteroid size.
     * @return standard asteroid diameter in pixels */
    public static int getSize() {
        return SIZE;
    }
}
