package com.r3m.spaceshooter.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * The basic enemy type. Flies straight, fires whenever it's roughly lined
 * up with the player, and otherwise takes occasional random pot-shots —
 * this matches the shooting rule from the design notes: shoot if aligned
 * with the player's lane, else about a 1-in-4 chance per opportunity.
 */
public class GruntEnemyShip extends EnemySpaceship {
    private static final double SPEED = 150.0;
    private static final double SHOOT_COOLDOWN_SECONDS = 1.0;
    private static final int ALIGNMENT_TOLERANCE_PIXELS = 20;
    private static final double RANDOM_SHOOT_CHANCE = 0.25; // ~1-4 out of 10

    public GruntEnemyShip(BufferedImage image, double x, double y, int width, int height, Random random) {
        super(image, x, y, width, height, SPEED, SHOOT_COOLDOWN_SECONDS, random);
    }

    @Override
    protected boolean decideToShoot(double enemyCenterY, double playerCenterY) {
        boolean alignedWithPlayer = Math.abs(enemyCenterY - playerCenterY) <= ALIGNMENT_TOLERANCE_PIXELS;
        return alignedWithPlayer || random.nextDouble() < RANDOM_SHOOT_CHANCE;
    }

    @Override
    public void render(Graphics2D graphics) {
        if (image != null) {
            // sprite faces right by default in most asset packs — flip it
            // horizontally so it visually faces its direction of travel (left)
            graphics.drawImage(image, (int) x + width, (int) y, -width, height, null);
            return;
        }

        // fallback if the image failed to load — a plain shape so the
        // enemy is still visible and playable rather than invisible
        graphics.setColor(Color.RED.darker());
        int[] xPoints = {(int) x, (int) x, (int) x - width};
        int[] yPoints = {(int) y, (int) y + height, (int) y + height / 2};
        graphics.fillPolygon(xPoints, yPoints, 3);
    }
}
