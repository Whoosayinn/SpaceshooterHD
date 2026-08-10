package com.r3m.spaceshooter.entity;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Shared behavior for every enemy ship type: flies straight from right to
 * left and decides each frame whether to fire at the player. Concrete
 * subclasses (GruntEnemyShip, EliteEnemyShip) only need to define their own
 * speed, reload time, appearance, and shooting rule via decideToShoot() —
 * this base class handles movement, cooldown timing, and screen-edge cleanup.
 */
public abstract class EnemySpaceship extends Spaceship {
    private final double speed;
    private final double shootCooldownSeconds;
    /** Random source available to subclass shooting strategies. */
    protected final Random random;

    private double shootCooldownRemaining = 0.0;
    private boolean wantsToShoot = false;
    private double playerYReference;

    /**
     * Initializes shared enemy movement and firing state.
     *
     * @param image enemy sprite; may be {@code null}
     * @param x initial horizontal position
     * @param y initial vertical position
     * @param width rendered width in pixels
     * @param height rendered height in pixels
     * @param speed leftward movement speed in pixels per second
     * @param shootCooldownSeconds minimum delay between shots
     * @param random random source for subclass AI
     */
    protected EnemySpaceship(BufferedImage image, double x, double y, int width, int height,
                              double speed, double shootCooldownSeconds, Random random) {
        // image may be null — subclasses fall back to drawing a shape if so
        super(image, x, y, width, height);
        this.speed = speed;
        this.shootCooldownSeconds = shootCooldownSeconds;
        this.random = random;
    }

    /**
     * Supplies the player's vertical center for the next AI update.
     *
     * @param playerCenterY player's vertical center coordinate
     */
    public void setPlayerYReference(double playerCenterY) {
        this.playerYReference = playerCenterY;
    }

    @Override
    public void update(double deltaTime) {
        x -= speed * deltaTime;

        shootCooldownRemaining = Math.max(0.0, shootCooldownRemaining - deltaTime);
        wantsToShoot = false;

        if (shootCooldownRemaining <= 0.0 && decideToShoot(getCenterY(), playerYReference)) {
            wantsToShoot = true;
            shootCooldownRemaining = shootCooldownSeconds;
        }
    }

    /**
     * Each enemy type decides differently whether to fire this frame.
     * @param enemyCenterY this ship's current vertical center
     * @param playerCenterY the player's current vertical center
     * @return {@code true} when the enemy should request a shot
     */
    protected abstract boolean decideToShoot(double enemyCenterY, double playerCenterY);

    /**
     * Returns and clears the pending shoot request.
     *
     * @return {@code true} when the controller should spawn a bullet
     */
    public boolean consumeShootRequest() {
        boolean result = wantsToShoot;
        wantsToShoot = false;
        return result;
    }

    /**
     * Tests whether the ship has passed the left removal boundary.
     *
     * @return {@code true} when {@code x < -10}
     */
    public boolean isPastLeftEdge() {
        return x < -10;
    }
}
