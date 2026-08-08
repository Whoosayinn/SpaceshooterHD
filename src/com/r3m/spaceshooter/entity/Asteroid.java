package com.r3m.spaceshooter.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** A single asteroid that travels left and can bounce vertically. */
public class Asteroid {
    private static final int SIZE = 72;
    private static final int HITBOX_INSET = 8;

    private final BufferedImage image;
    private final double velocityX;
    private double velocityY;
    private double x;
    private double y;

    public Asteroid(
        BufferedImage image,
        double x,
        double y,
        double velocityX,
        double velocityY
    ) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public void update(double deltaTime, int panelHeight) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;

        if (y < 0) {
            y = 0;
            velocityY = Math.abs(velocityY);
        } else if (y + SIZE > panelHeight) {
            y = panelHeight - SIZE;
            velocityY = -Math.abs(velocityY);
        }
    }

    public void render(Graphics2D graphics) {
        graphics.drawImage(image, (int) x, (int) y, SIZE, SIZE, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(
            (int) x + HITBOX_INSET,
            (int) y + HITBOX_INSET,
            SIZE - HITBOX_INSET * 2,
            SIZE - HITBOX_INSET * 2
        );
    }

    public boolean isPastLeftEdge() {
        return x + SIZE < 0;
    }

    public static int getSize() {
        return SIZE;
    }
}
