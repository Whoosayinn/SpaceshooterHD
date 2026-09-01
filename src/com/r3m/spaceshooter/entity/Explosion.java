package com.r3m.spaceshooter.entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/** A short-lived radial particle burst used when asteroids collide. */
public class Explosion {
    private static final double DURATION_SECONDS = 0.45;
    private static final int PARTICLE_COUNT = 14;

    private final Particle[] particles = new Particle[PARTICLE_COUNT];
    private double elapsedSeconds;

    /**
     * Creates a randomized radial particle burst.
     *
     * @param centerX horizontal burst center
     * @param centerY vertical burst center
     * @param random random source for particle direction, speed, and size
     */
    public Explosion(double centerX, double centerY, Random random) {
        for (int i = 0; i < particles.length; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double speed = 90.0 + random.nextDouble() * 170.0;
            int size = 3 + random.nextInt(6);

            particles[i] = new Particle(
                centerX,
                centerY,
                Math.cos(angle) * speed,
                Math.sin(angle) * speed,
                size,
                i % 2 == 0 ? new Color(255, 190, 50) : new Color(255, 80, 20)
            );
        }
    }

    /**
     * Advances every particle.
     *
     * @param deltaTime elapsed frame time in seconds
     */
    public void update(double deltaTime) {
        elapsedSeconds += deltaTime;
        for (Particle particle : particles) {
            particle.x += particle.velocityX * deltaTime;
            particle.y += particle.velocityY * deltaTime;
        }
    }

    /**
     * Draws all particles with an age-based fade.
     *
     * @param graphics destination graphics context
     */
    public void render(Graphics2D graphics) {
        float alpha = (float) Math.max(0.0, 1.0 - elapsedSeconds / DURATION_SECONDS);
        Graphics2D effectGraphics = (Graphics2D) graphics.create();
        effectGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        for (Particle particle : particles) {
            effectGraphics.setColor(particle.color);
            effectGraphics.fillOval(
                (int) particle.x - particle.size / 2,
                (int) particle.y - particle.size / 2,
                particle.size,
                particle.size
            );
        }

        effectGraphics.dispose();
    }

    /** Tests whether the effect can be removed.
     * @return whether the display duration has elapsed */
    public boolean isFinished() {
        return elapsedSeconds >= DURATION_SECONDS;
    }

    private static class Particle {
        private double x;
        private double y;
        private final double velocityX;
        private final double velocityY;
        private final int size;
        private final Color color;

        private Particle(
            double x,
            double y,
            double velocityX,
            double velocityY,
            int size,
            Color color
        ) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.size = size;
            this.color = color;
        }
    }
}
