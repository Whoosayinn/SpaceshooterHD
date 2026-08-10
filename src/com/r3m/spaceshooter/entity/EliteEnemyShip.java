package com.r3m.spaceshooter.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * A faster, more precise enemy type: only fires when it's actually lined up
 * with the player (no random pot-shots), but reloads quicker and closes the
 * distance faster than a grunt — a sharper, more deliberate threat rather
 * than a spray-and-pray one.
 */
public class EliteEnemyShip extends EnemySpaceship {
    private static final double SPEED = 230.0;
    private static final double SHOOT_COOLDOWN_SECONDS = 0.6;
    private static final int ALIGNMENT_TOLERANCE_PIXELS = 14;

    /**
     * Creates an elite enemy ship.
     *
     * @param image enemy sprite; may be {@code null}
     * @param x initial horizontal position
     * @param y initial vertical position
     * @param width rendered width in pixels
     * @param height rendered height in pixels
     * @param random random source shared with enemy AI
     */
    public EliteEnemyShip(BufferedImage image, double x, double y, int width, int height, Random random) {
        super(image, x, y, width, height, SPEED, SHOOT_COOLDOWN_SECONDS, random);
    }

    @Override
    protected boolean decideToShoot(double enemyCenterY, double playerCenterY) {
        return Math.abs(enemyCenterY - playerCenterY) <= ALIGNMENT_TOLERANCE_PIXELS;
    }

    @Override
    public void render(Graphics2D graphics) {
        if (image != null) {
            graphics.drawImage(image, (int) x + width, (int) y, -width, height, null);
            return;
        }

        graphics.setColor(Color.MAGENTA.darker());
        int[] xPoints = {(int) x, (int) x, (int) x - width};
        int[] yPoints = {(int) y, (int) y + height, (int) y + height / 2};
        graphics.fillPolygon(xPoints, yPoints, 3);
    }
}
