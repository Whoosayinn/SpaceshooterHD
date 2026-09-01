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
    /** Horizontal position in pixels. */
    protected double x;
    /** Vertical position in pixels. */
    protected double y;
    /** Rendered width in pixels. */
    protected final int width;
    /** Rendered height in pixels. */
    protected final int height;
    /** Sprite image, or {@code null} when a subclass draws another way. */
    protected final BufferedImage image;

    /**
     * Initializes shared ship geometry and appearance.
     *
     * @param image ship sprite; may be {@code null}
     * @param x initial horizontal position
     * @param y initial vertical position
     * @param width rendered width in pixels
     * @param height rendered height in pixels
     */
    protected Spaceship(BufferedImage image, double x, double y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Advances movement or AI state.
     *
     * @param deltaTime elapsed frame time in seconds
     */
    public abstract void update(double deltaTime);

    /**
     * Default rendering draws the sprite as-is. Subclasses can override this
     * for custom visuals — e.g. PlayerSpaceship overrides it to blink while
     * invulnerable, and enemy ships without a sprite asset draw a shape instead.
     *
     * @param graphics destination graphics context
     */
    public void render(Graphics2D graphics) {
        if (image != null) {
            graphics.drawImage(image, (int) x, (int) y, width, height, null);
        }
    }

    /** Returns the collision bounds.
     * @return rectangular ship bounds */
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    /** Returns the horizontal position.
     * @return horizontal position in pixels */
    public double getX() { return x; }
    /** Returns the vertical position.
     * @return vertical position in pixels */
    public double getY() { return y; }
    /** Returns the rendered width.
     * @return rendered width in pixels */
    public int getWidth() { return width; }
    /** Returns the rendered height.
     * @return rendered height in pixels */
    public int getHeight() { return height; }
    /** Returns the horizontal center.
     * @return horizontal center coordinate */
    public double getCenterX() { return x + width / 2.0; }
    /** Returns the vertical center.
     * @return vertical center coordinate */
    public double getCenterY() { return y + height / 2.0; }
}
