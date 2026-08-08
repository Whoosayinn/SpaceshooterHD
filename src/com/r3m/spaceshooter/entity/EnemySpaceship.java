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
    protected final Random random;

    private double shootCooldownRemaining = 0.0;
    private boolean wantsToShoot = false;
    private double playerYReference;

    protected EnemySpaceship(BufferedImage image, double x, double y, int width, int height,
                              double speed, double shootCooldownSeconds, Random random) {
        // image may be null — subclasses fall back to drawing a shape if so
        super(image, x, y, width, height);
        this.speed = speed;
        this.shootCooldownSeconds = shootCooldownSeconds;
        this.random = random;
    }

    /** GameController calls this each frame before update() so the AI knows where the player is. */
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
     */
    protected abstract boolean decideToShoot(double enemyCenterY, double playerCenterY);

    /** GameController checks this right after update() to know whether to spawn a bullet. */
    public boolean consumeShootRequest() {
        boolean result = wantsToShoot;
        wantsToShoot = false;
        return result;
    }

    /** Per spec: remove once it passes the left edge (x < 0, with a small buffer). */
    public boolean isPastLeftEdge() {
        return x < -10;
    }
}
