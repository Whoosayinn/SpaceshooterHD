package com.r3m.spaceshooter.core;

import com.r3m.spaceshooter.entity.Asteroid;
import com.r3m.spaceshooter.system.AssetManager;
import com.r3m.spaceshooter.system.CollisionManager;
import com.r3m.spaceshooter.system.InputManager;
import com.r3m.spaceshooter.system.ScoreManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameController {
	/**
	 * GameController is the brain of the game.
	 * It owns all game logic — movement, collision, scoring —
	 * and delegates to specialized manager classes for each responsibility.
	 *
	 * It receives deltaTime from GamePanel every frame and uses it
	 * to make all movement frame-rate independent.
	 *
	 * This class follows the Single Responsibility principle:
	 * it orchestrates game logic but does NOT manage the window,
	 * the game loop, or keyboard events directly.
	 */
	
    // --- SCREEN BOUNDARIES ---

    // width of the game panel in pixels
    // used to clamp entities so they can't move off the right edge
    private final int panelWidth;
    
    // height of the game panel in pixels
    // used to clamp entities so they can't move off the bottom edge
    private final int panelHeight;

    // --- SYSTEM MANAGERS ---
    // all private and final — encapsulation means nothing outside
    // this class can access or replace these manager objects directly

    // reads keyboard state — which keys are currently held down
    InputManager inputManager;
    
    // checks whether two game entities are overlapping
    CollisionManager collisionManager;
    
    // loads and caches image assets from disk
    AssetManager assetManager;
    ScoreManager scoreManager;

    // --- PLAYER STATE ---
    // temporary placeholder — will be replaced by a proper Player class later
    // stored as double for sub-pixel precision, preventing stuttery movement

    // horizontal and vertical position of the player in pixels from the left edge
    private double playerX = 200, playerY = 250;
    
    // movement speed in pixels per second
    // declared as double because it's multiplied by deltaTime (a double) every frame
    // at 60fps: 500 * 0.01666 = ~8.3 pixels per frame
    private static final int PLAYER_SPEED = 500;

    // --- ASTEROID STATE ---

    private static final double ASTEROID_SPAWN_INTERVAL = 1.5;
    private static final double ASTEROID_MIN_SPEED = 110.0;
    private static final double ASTEROID_SPEED_RANGE = 90.0;

    private final List<Asteroid> asteroids = new ArrayList<>();
    private final Random random = new Random();
    private final BufferedImage asteroidImage;
    private double asteroidSpawnTimer = ASTEROID_SPAWN_INTERVAL;

    public GameController(InputManager inputManager, int panelWidth, int panelHeight) {
        /**
         * Constructor — initializes all managers and stores screen boundaries.
         * Called once by GamePanel when the game starts.
         *
         * @param inputManager the shared InputManager that receives keyboard events
         * @param panelWidth   the width of the game surface in pixels
         * @param panelHeight  the height of the game surface in pixels
         */
    	
        // store the shared InputManager — this is the SAME object
        // that GamePanel attached to addKeyListener()
        // so key presses flow: keyboard → InputManager → here → player movement
        this.inputManager = inputManager;
        
        // store screen dimensions for boundary clamping in update()
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        
        this.collisionManager = new CollisionManager();
        this.assetManager = AssetManager.getInstance();
        this.scoreManager = new ScoreManager();
        this.asteroidImage = assetManager.loadImage("/assets/asteroid.png");
    }

    public void update(double deltaTime) {
        /**
         * Update — runs every frame (~60 times per second).
         * This is the LOGIC step: move things, check rules, update state.
         * No drawing happens here — only data changes.
         *
         * @param deltaTime seconds since the last frame (e.g. 0.01666 at 60fps)
         *                  multiply all movement by this to keep speed
         *                  consistent regardless of frame rate
         */
    	
        double dx = 0; // horizontal direction: negative = left, positive = right
        double dy = 0; // vertical direction: negative = up, positive = down

        // A key moves left — subtracts from dx
        if (inputManager.isAPressed()) dx--;

        // D key moves right — adds to dx
        if (inputManager.isDPressed()) dx++;

        // W key moves up — subtracts from dy
        // in Java2D, y=0 is the TOP of the screen, so going up means decreasing y
        if (inputManager.isWPressed()) dy--;

        // S key moves down — adds to dy
        if (inputManager.isSPressed()) dy++;


        // --- Step 2: Normalize the direction vector ---
        // without normalization, diagonal movement (dx=1, dy=1) would travel
        // at speed √2 ≈ 1.41x faster than straight movement (dx=1, dy=0)
        // normalization scales the vector to length 1.0 regardless of direction

        // calculate the actual length of the direction vector
        // Pythagorean theorem: length = √(dx² + dy²)
        double length = Math.sqrt(dx * dx + dy * dy);
        
        if (length != 0) {
            // divide each component by length to normalize to 1.0
            // then multiply by PLAYER_SPEED to apply intended speed
            // then multiply by deltaTime to make it frame-rate independent
            // result: player always moves at exactly PLAYER_SPEED pixels/second
            dx = (dx / length) * PLAYER_SPEED * deltaTime;
            dy = (dy / length) * PLAYER_SPEED * deltaTime;
        }

        // --- Step 3: Apply movement ---
        // add the calculated displacement to the current position
        // playerX and playerY are doubles so sub-pixel precision is preserved
        playerX += dx;
        playerY += dy;

        // --- Step 4: Clamp to screen bounds ---
        // prevents the player from moving outside the visible game area

        // Math.max(0, ...) stops the player at the left/top edge (can't go below 0)
        // Math.min(..., panelWidth - 32) stops at the right edge
        // the - 32 accounts for the player's own width so the RIGHT edge of the
        // ship stays on screen, not just the left edge
        playerX = Math.max(0, Math.min(playerX, panelWidth - 32));
        
        // same clamping for vertical — - 32 accounts for player height
        playerY = Math.max(0, Math.min(playerY, panelHeight - 32));

        updateAsteroids(deltaTime);
        
        // --- Future additions go here ---
        // update bullets: move them upward, remove if off screen
        // update enemies: move them downward, spawn new ones
        // collisionManager.isColliding(player, enemy) → handle damage
        // collisionManager.isColliding(bullet, enemy) → handle destruction + score
    }

    private void updateAsteroids(double deltaTime) {
        synchronized (asteroids) {
            asteroidSpawnTimer += deltaTime;

            if (asteroidSpawnTimer >= ASTEROID_SPAWN_INTERVAL) {
                spawnAsteroid();
                asteroidSpawnTimer = 0.0;
            }

            Iterator<Asteroid> iterator = asteroids.iterator();
            while (iterator.hasNext()) {
                Asteroid asteroid = iterator.next();
                asteroid.update(deltaTime);
                if (asteroid.isPastRightEdge(panelWidth)) {
                    iterator.remove();
                }
            }
        }
    }

    private void spawnAsteroid() {
        if (asteroidImage == null) {
            return;
        }

        int maximumY = Math.max(0, panelHeight - Asteroid.getSize());
        double y = maximumY == 0 ? 0 : random.nextInt(maximumY + 1);
        double speed = ASTEROID_MIN_SPEED + random.nextDouble() * ASTEROID_SPEED_RANGE;
        asteroids.add(new Asteroid(asteroidImage, -Asteroid.getSize(), y, speed));
    }

    // What is graphics g? a class that has many functions to draw objects on screen
    			       //^^^^^^^^^
    public void render(Graphics2D g) {
        /**
         * Render — runs every frame right after update().
         * Receives the Graphics2D object from GamePanel's paintComponent()
         * and draws the current game state to screen.
         *
         * This method should NEVER modify game state — only read and draw.
         * All logic belongs in update(), all drawing belongs here.
         *
         * @param g the Graphics2D paintbrush — use it to draw shapes, images, text
         */
    	
        // set the drawing color to green for the placeholder ship
        // every draw call after this uses green until setColor is called again
        g.setColor(Color.GREEN);

        // draw the placeholder ship as a filled green rectangle
        // cast double positions to int — screen pixels must be whole numbers
        // the fractional part is preserved in playerX/playerY for smooth movement
        // only discarded at the last moment here when handing to the screen
        // arguments: x position, y position, width, height
        g.fillOval((int) playerX, (int) playerY, 48, 32);

        synchronized (asteroids) {
            for (Asteroid asteroid : asteroids) {
                asteroid.render(g);
            }
        }

        // switch color to white for the score text
        g.setColor(Color.WHITE);

        // draw the score string at position (10, 20) — top left corner
        // 10px from left edge, 20px from top edge
        // scoreManager.getScore() is called fresh every frame
        // so as soon as score changes, it shows up immediately next render
        // string concatenation: "Score: " + 0 = "Score: 0"
        g.drawString("Score: " + scoreManager.getScore(), 10, 20);
    }
}
