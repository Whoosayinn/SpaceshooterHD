package com.r3m.spaceshooter.entity;

import com.r3m.spaceshooter.system.InputManager;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * The player-controlled ship. Reads WASD from InputManager and moves with
 * inertia (accelerates, keeps drifting, slows via friction — feels like ice
 * rather than snapping to a fixed speed). After taking a hit it becomes
 * briefly invulnerable and blinks on screen so the player gets clear
 * "you got hit, you're safe for a moment" feedback.
 */
public class PlayerSpaceship extends Spaceship {
    private static final double MAX_SPEED = 400;
    private static final double ACCELERATION = 1200;
    private static final double FRICTION = 0.98;
    private static final double STOP_THRESHOLD = 0.1;

    private static final int HITBOX_INSET = 8;
    private static final int STARTING_LIVES = 3;
    private static final double INVULNERABILITY_SECONDS = 1.0;

    // how often the blink toggles on/off while invulnerable, in seconds
    // smaller = faster flicker
    private static final double BLINK_INTERVAL_SECONDS = 0.1;

    private final InputManager inputManager;
    private int panelWidth;
    private int panelHeight;

    private double velocityX = 0;
    private double velocityY = 0;

    private int lives = STARTING_LIVES;
    private double invulnerabilityRemaining = 0.0;

    public PlayerSpaceship(BufferedImage image, double x, double y, int width, int height,
                            InputManager inputManager, int panelWidth, int panelHeight) {
        super(image, x, y, width, height);
        this.inputManager = inputManager;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
    }

    /** GameController calls this whenever the window is resized. */
    public void setViewportSize(int panelWidth, int panelHeight) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
    }

    @Override
    public void update(double deltaTime) {
        invulnerabilityRemaining = Math.max(0.0, invulnerabilityRemaining - deltaTime);

        double dx = 0; // horizontal direction: negative = left, positive = right
        double dy = 0; // vertical direction: negative = up, positive = down

        if (inputManager.isAPressed()) dx--;
        if (inputManager.isDPressed()) dx++;
        if (inputManager.isWPressed()) dy--;
        if (inputManager.isSPressed()) dy++;

        // normalize so diagonal movement isn't faster than straight movement
        double length = Math.sqrt(dx * dx + dy * dy);

        if (length != 0) {
            dx /= length;
            dy /= length;

            velocityX += dx * ACCELERATION * deltaTime;
            velocityY += dy * ACCELERATION * deltaTime;

            double currentSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (currentSpeed > MAX_SPEED) {
                velocityX = (velocityX / currentSpeed) * MAX_SPEED;
                velocityY = (velocityY / currentSpeed) * MAX_SPEED;
            }
        } else {
            // no key held — apply friction so the ship gradually coasts to a stop
            velocityX *= FRICTION;
            velocityY *= FRICTION;
            if (Math.abs(velocityX) < STOP_THRESHOLD) velocityX = 0;
            if (Math.abs(velocityY) < STOP_THRESHOLD) velocityY = 0;
        }

        x += velocityX * deltaTime;
        y += velocityY * deltaTime;

        // clamp to screen bounds
        x = Math.max(0, Math.min(x, panelWidth - width));
        y = Math.max(0, Math.min(y, panelHeight - height));
    }

    @Override
    public void render(Graphics2D graphics) {
        // while invulnerable, skip drawing on alternating blink intervals —
        // this is what creates the flicker effect instead of a solid sprite
        if (invulnerabilityRemaining > 0.0) {
            int blinkStep = (int) (invulnerabilityRemaining / BLINK_INTERVAL_SECONDS);
            if (blinkStep % 2 == 0) {
                return; // skip this frame entirely — ship "disappears" briefly
            }
        }
        super.render(graphics);
    }

    /** Smaller than the full sprite bounds so near-miss grazes don't count as hits. */
    public Rectangle getHitbox() {
        return new Rectangle(
            (int) x + HITBOX_INSET,
            (int) y + HITBOX_INSET,
            width - HITBOX_INSET * 2,
            height - HITBOX_INSET * 2
        );
    }

    public void takeHit() {
        if (invulnerabilityRemaining > 0.0 || lives == 0) {
            return;
        }
        lives--;
        invulnerabilityRemaining = INVULNERABILITY_SECONDS;
    }

    public int getLives() {
        return lives;
    }

    public boolean isAlive() {
        return lives > 0;
    }
}
