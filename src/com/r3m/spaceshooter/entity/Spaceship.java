package com.r3m.spaceshooter.entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Abstract base for anything that flies around on screen as a "ship" —
 * shared position, size, image, and bounding box. Movement and shooting
 * behavior are left entirely to subclasses (PlayerSpaceship reads WASD
 * input, EnemySpaceship variants run their own AI) — that's the
 * polymorphism seam: update() and render() mean something different for
 * every concrete ship type, but GameController can treat any Spaceship
 * uniformly wherever it doesn't need to care which kind it is.
 */
public abstract class Spaceship {
    protected double x;
    protected double y;
    protected final int width;
    protected final int height;
    protected final BufferedImage image;

    protected Spaceship(BufferedImage image, double x, double y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /** Each concrete ship type defines its own movement/input/AI logic here. */
    public abstract void update(double deltaTime);

    /**
     * Default rendering draws the sprite as-is. Subclasses can override this
     * for custom visuals — e.g. PlayerSpaceship overrides it to blink while
     * invulnerable, and enemy ships without a sprite asset draw a shape instead.
     */
    public void render(Graphics2D graphics) {
        if (image != null) {
            graphics.drawImage(image, (int) x, (int) y, width, height, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public double getCenterX() { return x + width / 2.0; }
    public double getCenterY() { return y + height / 2.0; }
}
