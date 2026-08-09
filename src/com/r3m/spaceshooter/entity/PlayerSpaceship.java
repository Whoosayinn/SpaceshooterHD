package com.r3m.spaceshooter.entity;

import com.r3m.spaceshooter.system.InputManager;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import com.r3m.spaceshooter.system.ShipAnimator;
import com.r3m.spaceshooter.system.SpriteSheet;


/**
 * The player-controlled ship. Reads WASD from InputManager and moves with
 * inertia (accelerates, keeps drifting, slows via friction — feels like ice
 * rather than snapping to a fixed speed). After taking a hit it becomes
 * briefly invulnerable and blinks on screen so the player gets clear
 * "you got hit, you're safe for a moment" feedback.
 */
public class PlayerSpaceship extends Spaceship {
	
	// --- movement constants (unchanged) --- //
    private static final double MAX_SPEED = 400;
    private static final double ACCELERATION = 1200;
    private static final double FRICTION = 0.98;
    private static final double STOP_THRESHOLD = 0.1;

    // --- hitbox / lives / invulnerability (unchanged) --- //
    private static final int HITBOX_INSET = 5;
    private static final int STARTING_LIVES = 3;
    private static final double INVULNERABILITY_SECONDS = 1.0;
    // how often the blink toggles on/off while invulnerable, in seconds
    // smaller = faster flicker
    private static final double BLINK_INTERVAL_SECONDS = 0.1;

    // --- dependencies -- //
    private final InputManager inputManager;
    private final ShipAnimator shipAnimator;
    // spritesheet is optional — if null, falls back to base Spaceship image
    private final SpriteSheet  spriteSheet;

    // --- state --- //
    private int panelWidth;
    private int panelHeight;

    private double velocityX = 0;
    private double velocityY = 0;

    private double bankAngle = 0;
    
    private int lives = STARTING_LIVES;
    private double invulnerabilityRemaining = 0.0;

    /**
     * Constructor without spritesheet — uses the single image from Spaceship base class.
     * Use this if you only have one static ship image.
     */
    public PlayerSpaceship(BufferedImage image, double x, double y, int width, int height,
                            InputManager inputManager, int panelWidth, int panelHeight) {
        super(image, x, y, width, height);
        this.inputManager = inputManager;
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        this.spriteSheet = null;
        this.shipAnimator = new ShipAnimator();
    }

    /**
     * Constructor WITH spritesheet — uses animated 25-frame sprite cycling.
     * Use this when you have SPRITE-2.png loaded and sliced.
     */
    public PlayerSpaceship(SpriteSheet spriteSheet, double x, double y,
                           int width, int height,
                           InputManager inputManager,
                           int panelWidth, int panelHeight) {
        // pass null image to base class — we draw from spriteSheet instead
        super(null, x, y, width, height);
        this.inputManager = inputManager;
        this.panelWidth   = panelWidth;
        this.panelHeight  = panelHeight;
        this.spriteSheet  = spriteSheet;
        this.shipAnimator = new ShipAnimator();
    }
    
    /** GameController calls this whenever the window is resized. */
    public void setViewportSize(int panelWidth, int panelHeight) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
    }

    @Override
    public void update(double deltaTime) {
    	
    	// tick down invulnerability timer
        invulnerabilityRemaining = Math.max(0.0, invulnerabilityRemaining - deltaTime);

        // --- direction input ---
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

            // clamp to MAX_SPEED
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
        // apply velocity to position
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;

        // clamp to screen bounds, zero out velocity on impact
        if (x <= 0)                  { x = 0;                  velocityX = 0; }
        if (x >= panelWidth  - width){ x = panelWidth  - width; velocityX = 0; }
        if (y <= 0)                  { y = 0;                  velocityY = 0; }
        if (y >= panelHeight - height){ y = panelHeight - height; velocityY = 0; }

        // --- sprite animation ---
        // pass current speed magnitude so animator knows which zone to show
        double speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        shipAnimator.update(speed, MAX_SPEED, deltaTime);
    }

    @Override
    public void render(Graphics2D g) {

        // blink effect while invulnerable
        if (invulnerabilityRemaining > 0.0) {
            int blinkStep = (int)(invulnerabilityRemaining / BLINK_INTERVAL_SECONDS);
            if (blinkStep % 2 == 0) return;
        }

        // save original transform — restore after drawing ship
        // so score text and other UI elements are NOT rotated
        AffineTransform originalTransform = g.getTransform();

        // rotate around the center of the ship
        double centerX = x + width  / 2.0;
        double centerY = y + height / 2.0;
        g.rotate(Math.toRadians(bankAngle), centerX, centerY);

        if (spriteSheet != null) {
            // draw the correct animated frame from the spritesheet
            BufferedImage frame = spriteSheet.getFrame(
                shipAnimator.getCurrentFrameIndex()
            );
            g.drawImage(frame, (int) x, (int) y, width, height, null);
        } else {
            // fall back to base class render (single static image)
            super.render(g);
        }

        // restore transform before anything else draws
        g.setTransform(originalTransform);
    }

    /** Draws the current ship frame at a custom location for menu previews. */
    public void renderAt(Graphics2D g, int drawX, int drawY, int drawWidth, int drawHeight) {
        if (spriteSheet != null) {
            BufferedImage frame = spriteSheet.getFrame(shipAnimator.getCurrentFrameIndex());
            g.drawImage(frame, drawX, drawY, drawWidth, drawHeight, null);
        } else if (image != null) {
            g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
        }
    }

    // Hitbox is a triangle //
    public Polygon getHitbox() {
        int left = (int) x + HITBOX_INSET;
        int right = (int) x + width - HITBOX_INSET;
        int top = (int) y + HITBOX_INSET;
        int bottom = (int) y + height - HITBOX_INSET;
        int centerY = (int) y + height / 2;

        return new Polygon(
            new int[] { left, left, right },
            new int[] { top, bottom, centerY },
            3
        );
    }

    public void takeHit() {
        if (invulnerabilityRemaining > 0.0 || lives == 0) {
            return;
        }
        lives--;
        invulnerabilityRemaining = INVULNERABILITY_SECONDS;
    }

    /** Restores the player to a fresh state for a new or replayed game. */
    public void reset(double startX, double startY) {
        x = startX;
        y = startY;
        velocityX = 0;
        velocityY = 0;
        bankAngle = 0;
        lives = STARTING_LIVES;
        invulnerabilityRemaining = 0;
    }

    public int getLives() { return lives; }
    public boolean isAlive() { return lives > 0; }
}
