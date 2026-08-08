package com.r3m.spaceshooter.core;

import com.r3m.spaceshooter.entity.Asteroid;
import com.r3m.spaceshooter.entity.Explosion;
import com.r3m.spaceshooter.entity.Star;
import com.r3m.spaceshooter.system.AssetManager;
import com.r3m.spaceshooter.system.CollisionManager;
import com.r3m.spaceshooter.system.InputManager;
import com.r3m.spaceshooter.system.ScoreManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

    private enum GameState {
        MENU,
        INSTRUCTIONS,
        CONTROLS,
        PLAYING
    }

    private volatile GameState gameState = GameState.MENU;
    private String menuMessage = "SELECT AN OPTION USING KEYS 1 - 4";
    private volatile double menuAnimationTime;

    // --- SCREEN BOUNDARIES ---

    // width of the game panel in pixels
    // used to clamp entities so they can't move off the right edge
    private volatile int panelWidth;
    
    // height of the game panel in pixels
    // used to clamp entities so they can't move off the bottom edge
    private volatile int panelHeight;

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
    private static final int PLAYER_WIDTH = 64;
    private static final int PLAYER_HEIGHT = 56;
    private static final int PLAYER_HITBOX_INSET = 8;
    private static final int STARTING_LIVES = 3;
    private static final double COLLISION_INVULNERABILITY_SECONDS = 1.0;
    private final BufferedImage spaceshipImage;
    private int playerLives = STARTING_LIVES;
    private double collisionInvulnerabilityRemaining;

    
	 // current velocity — how fast the ship is moving in each direction
	 // persists between frames — this is what creates the inertia effect
	 private double velocityX = 0;
	 private double velocityY = 0;
	 
	// maximum speed the ship can reach in pixels per second
	 private static final double MAX_SPEED = 400;
	 
	// how fast the ship accelerates when a key is held, pixels per second²
	 private static final double ACCELERATION = 1200;

	 // friction multiplier applied every frame when no key is pressed
	 // 0.88 means velocity loses 12% per frame — feels like ice
	 // closer to 1.0 = icier, closer to 0.0 = more grippy
	 private static final double FRICTION = 0.98;

	 // threshold below which velocity snaps to zero
	 // prevents the ship from drifting forever at imperceptibly small speeds
	 private static final double STOP_THRESHOLD = 0.1;
    
    // --- ASTEROID STATE ---

    private static final double ASTEROID_SPAWN_INTERVAL = 0.5;
    private static final double ASTEROID_MIN_SPEED = 110.0;
    private static final double ASTEROID_SPEED_RANGE = 90.0;
    private static final double ASTEROID_MIN_VERTICAL_SPEED = 50.0;
    private static final double ASTEROID_VERTICAL_SPEED_RANGE = 90.0;
    private static final double STRAIGHT_ASTEROID_CHANCE = 0.35;
    private static final double FRAGMENT_COLLISION_GRACE_SECONDS = 0.4;
    private static final double FRAGMENT_MIN_SPREAD_SPEED = 90.0;
    private static final double FRAGMENT_SPREAD_SPEED_RANGE = 70.0;

    private final List<Asteroid> asteroids = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();
    private final List<Star> stars = new ArrayList<>();
    private final Random random = new Random();
    private final BufferedImage asteroidImage;
    private double asteroidSpawnTimer = ASTEROID_SPAWN_INTERVAL;

    private static final int STAR_COUNT = 120;
    private static final double STAR_MIN_SPEED = 60.0;
    private static final double STAR_SPEED_RANGE = 180.0;

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
        this.spaceshipImage = assetManager.loadImage("/assets/spaceship.png");
        this.asteroidImage = assetManager.loadImage("/assets/asteroid.png");
        createInitialStars();
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

        // The starfield keeps moving both in the menu and during gameplay.
        updateStars(deltaTime);

        if (gameState != GameState.PLAYING) {
            menuAnimationTime += deltaTime;
            updateMenu();
            return;
        }

        collisionInvulnerabilityRemaining = Math.max(
            0.0,
            collisionInvulnerabilityRemaining - deltaTime
        );

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
            dx = (dx / length);
            dy = (dy / length);
            
            velocityX += dx * ACCELERATION * deltaTime;
            velocityY += dy * ACCELERATION * deltaTime;
            
            // clamp to max speed
            double currentSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
            if (currentSpeed > MAX_SPEED) {
                velocityX = (velocityX / currentSpeed) * MAX_SPEED;
                velocityY = (velocityY / currentSpeed) * MAX_SPEED;
            }
        } else {
            // no key held — apply friction
            velocityX *= FRICTION;
            velocityY *= FRICTION;

            // snap to zero to prevent infinite drift
            if (Math.abs(velocityX) < STOP_THRESHOLD) velocityX = 0;
            if (Math.abs(velocityY) < STOP_THRESHOLD) velocityY = 0;
        }

        // --- Step 3: Apply movement ---
        // add the calculated displacement to the current position
        // playerX and playerY are doubles so sub-pixel precision is preserved
        playerX += velocityX * deltaTime;
        playerY += velocityY * deltaTime;

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

    public void setViewportSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        panelWidth = width;
        panelHeight = height;
    }

    private void createInitialStars() {
        synchronized (stars) {
            for (int i = 0; i < STAR_COUNT; i++) {
                stars.add(createStar(random.nextInt(Math.max(1, panelWidth))));
            }
        }
    }

    private Star createStar(double x) {
        double y = random.nextInt(Math.max(1, panelHeight));
        double speed = STAR_MIN_SPEED + random.nextDouble() * STAR_SPEED_RANGE;
        int size = 1 + random.nextInt(3);
        return new Star(x, y, speed, size);
    }

    private void updateStars(double deltaTime) {
        synchronized (stars) {
            Iterator<Star> iterator = stars.iterator();
            while (iterator.hasNext()) {
                Star star = iterator.next();
                star.update(deltaTime);

                if (star.isPastLeftEdge()) {
                    iterator.remove();
                }
            }

            while (stars.size() < STAR_COUNT) {
                stars.add(createStar(panelWidth));
            }
        }
    }

    private void updateMenu() {
        int choice = inputManager.consumeMenuChoice();
        if (choice == InputManager.NO_MENU_CHOICE) {
            return;
        }

        if (gameState == GameState.MENU) {
            switch (choice) {
                case 1 -> startNewGame();
                case 2 -> {
                    gameState = GameState.INSTRUCTIONS;
                    menuMessage = "PRESS 0 TO RETURN TO THE MAIN MENU";
                }
                case 3 -> {
                    gameState = GameState.CONTROLS;
                    menuMessage = "PRESS 0 TO RETURN TO THE MAIN MENU";
                }
                case 4 -> System.exit(0);
                default -> menuMessage = "INVALID OPTION - PLEASE CHOOSE 1, 2, 3, OR 4";
            }
            return;
        }

        if (choice == 0) {
            gameState = GameState.MENU;
            menuMessage = "SELECT AN OPTION USING KEYS 1 - 4";
        } else {
            menuMessage = "INVALID OPTION - PRESS 0 TO RETURN";
        }
    }

    private void startNewGame() {
        playerX = 200;
        playerY = panelHeight / 2.0 - PLAYER_HEIGHT / 2.0;
        velocityX = 0;
        velocityY = 0;
        playerLives = STARTING_LIVES;
        collisionInvulnerabilityRemaining = 0;
        asteroidSpawnTimer = 0;
        scoreManager.reset();

        synchronized (asteroids) {
            asteroids.clear();
        }

        synchronized (explosions) {
            explosions.clear();
        }

        gameState = GameState.PLAYING;
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
                asteroid.update(deltaTime, panelHeight);

                if (asteroid.isPastLeftEdge()) {
                    iterator.remove();
                    continue;
                }

                if (collisionManager.isColliding(getPlayerBounds(), asteroid.getBounds())) {
                    iterator.remove();
                    damagePlayer();
                }
            }

            resolveAsteroidCollisions();
        }

        updateExplosions(deltaTime);
    }

    private void resolveAsteroidCollisions() {
        Set<Asteroid> destroyedAsteroids = new HashSet<>();
        List<Asteroid> fragments = new ArrayList<>();

        for (int firstIndex = 0; firstIndex < asteroids.size(); firstIndex++) {
            Asteroid first = asteroids.get(firstIndex);
            if (destroyedAsteroids.contains(first) || !first.canCollideWithAsteroids()) {
                continue;
            }

            for (int secondIndex = firstIndex + 1; secondIndex < asteroids.size(); secondIndex++) {
                Asteroid second = asteroids.get(secondIndex);
                if (destroyedAsteroids.contains(second) || !second.canCollideWithAsteroids()) {
                    continue;
                }

                if (!collisionManager.isColliding(first.getBounds(), second.getBounds())) {
                    continue;
                }

                destroyedAsteroids.add(first);
                destroyedAsteroids.add(second);
                createExplosion(
                    (first.getCenterX() + second.getCenterX()) / 2.0,
                    (first.getCenterY() + second.getCenterY()) / 2.0
                );
                addFragments(first, fragments);
                addFragments(second, fragments);
                break;
            }
        }

        asteroids.removeAll(destroyedAsteroids);
        asteroids.addAll(fragments);
    }

    private void addFragments(Asteroid parent, List<Asteroid> fragments) {
        if (!parent.canSplit()) {
            return;
        }

        int fragmentSize = parent.getFragmentSize();
        for (int direction : new int[] {-1, 1}) {
            double spreadSpeed = FRAGMENT_MIN_SPREAD_SPEED
                + random.nextDouble() * FRAGMENT_SPREAD_SPEED_RANGE;
            double fragmentVelocityX = parent.getVelocityX()
                * (0.9 + random.nextDouble() * 0.2);
            double fragmentVelocityY = parent.getVelocityY() + direction * spreadSpeed;

            fragments.add(new Asteroid(
                asteroidImage,
                parent.getCenterX() - fragmentSize / 2.0,
                parent.getCenterY() - fragmentSize / 2.0,
                fragmentVelocityX,
                fragmentVelocityY,
                fragmentSize,
                FRAGMENT_COLLISION_GRACE_SECONDS
            ));
        }
    }

    private void createExplosion(double centerX, double centerY) {
        synchronized (explosions) {
            explosions.add(new Explosion(centerX, centerY, random));
        }
    }

    private void updateExplosions(double deltaTime) {
        synchronized (explosions) {
            Iterator<Explosion> iterator = explosions.iterator();
            while (iterator.hasNext()) {
                Explosion explosion = iterator.next();
                explosion.update(deltaTime);
                if (explosion.isFinished()) {
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
        double velocityX = -(ASTEROID_MIN_SPEED + random.nextDouble() * ASTEROID_SPEED_RANGE);
        double velocityY = createRandomVerticalVelocity();

        asteroids.add(new Asteroid(asteroidImage, panelWidth, y, velocityX, velocityY));
    }

    private double createRandomVerticalVelocity() {
        if (random.nextDouble() < STRAIGHT_ASTEROID_CHANCE) {
            return 0.0;
        }

        double speed = ASTEROID_MIN_VERTICAL_SPEED
            + random.nextDouble() * ASTEROID_VERTICAL_SPEED_RANGE;
        return random.nextBoolean() ? speed : -speed;
    }

    private Rectangle getPlayerBounds() {
        return new Rectangle(
            (int) playerX + PLAYER_HITBOX_INSET,
            (int) playerY + PLAYER_HITBOX_INSET,
            PLAYER_WIDTH - PLAYER_HITBOX_INSET * 2,
            PLAYER_HEIGHT - PLAYER_HITBOX_INSET * 2
        );
    }

    private void damagePlayer() {
        if (collisionInvulnerabilityRemaining > 0.0 || playerLives == 0) {
            return;
        }

        playerLives--;
        collisionInvulnerabilityRemaining = COLLISION_INVULNERABILITY_SECONDS;
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

        // Background is rendered first so every other object appears above it.
        renderSpaceBackground(g);

        synchronized (stars) {
            for (Star star : stars) {
                star.render(g);
            }
        }

        if (gameState != GameState.PLAYING) {
            renderTextMenu(g);
            return;
        }

        // Draw the player sprite at its current movement position.

        // cast double positions to int — screen pixels must be whole numbers
        if (spaceshipImage != null) {
            g.drawImage(
                spaceshipImage,
                (int) playerX,
                (int) playerY,
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                null
            );
        }

        synchronized (asteroids) {
            for (Asteroid asteroid : asteroids) {
                asteroid.render(g);
            }
        }

        synchronized (explosions) {
            for (Explosion explosion : explosions) {
                explosion.render(g);
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
        g.drawString("Lives: " + playerLives, 10, 38);

        if (playerLives == 0) {
            g.drawString("GAME OVER", panelWidth / 2 - 35, panelHeight / 2);
        }
    }

    private void renderSpaceBackground(Graphics2D g) {
        Paint oldPaint = g.getPaint();
        g.setPaint(new GradientPaint(
            0,
            0,
            new Color(5, 16, 42),
            0,
            panelHeight,
            new Color(0, 2, 12)
        ));
        g.fillRect(0, 0, panelWidth, panelHeight);
        g.setPaint(oldPaint);
    }

    private void renderTextMenu(Graphics2D g) {
        if (gameState == GameState.INSTRUCTIONS) {
            renderInformationPage(
                g,
                "INSTRUCTIONS",
                new String[] {
                    "Pilot your spaceship through the asteroid field.",
                    "Avoid incoming asteroids and survive as long as possible.",
                    "Every collision costs one life. You start with three lives."
                }
            );
            return;
        }

        if (gameState == GameState.CONTROLS) {
            renderInformationPage(
                g,
                "VIEW CONTROLS",
                new String[] {
                    "W  -  MOVE UP",
                    "A  -  MOVE LEFT",
                    "S  -  MOVE DOWN",
                    "D  -  MOVE RIGHT",
                    "ESC  -  EXIT GAME"
                }
            );
            return;
        }

        String[] asciiTitle = {
            " ____  ____   _    ____ _____     ____  _   _  ___   ___ _____ _____ ____    _   _ ____  ",
            "/ ___||  _ \\ / \\  / ___| ____|   / ___|| | | |/ _ \\ / _ \\_   _| ____|  _ \\  | | | |  _ \\ ",
            "\\___ \\| |_) / _ \\| |   |  _|     \\___ \\| |_| | | | | | | || | |  _| | |_) | | |_| | | | |",
            " ___) |  __/ ___ \\ |___| |___     ___) |  _  | |_| | |_| || | | |___|  _ <  |  _  | |_| |",
            "|____/|_| /_/   \\_\\____|_____|   |____/|_| |_|\\___/ \\___/ |_| |_____|_| \\_\\ |_| |_|____/ "
        };

        int asciiSize = Math.max(8, Math.min(22, panelWidth / 70));
        g.setFont(new Font("Monospaced", Font.BOLD, asciiSize));
        g.setColor(new Color(115, 225, 255));
        int y = Math.max(90, panelHeight / 6);
        for (String line : asciiTitle) {
            drawCenteredString(g, line, y);
            y += asciiSize + 4;
        }

        renderAnimatedMenuShip(g, y + 12);

        String[] options = {
            "[1]  START GAME",
            "[2]  INSTRUCTIONS",
            "[3]  VIEW CONTROLS",
            "[4]  EXIT"
        };

        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        int optionY = Math.max(y + 70, panelHeight / 2 + 35);
        for (String option : options) {
            g.setColor(new Color(225, 245, 255));
            drawCenteredString(g, option, optionY);
            optionY += 45;
        }

        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        boolean invalid = menuMessage.startsWith("INVALID");
        int pulse = 175 + (int) (Math.sin(menuAnimationTime * 3.0) * 45);
        g.setColor(invalid ? new Color(255, 110, 110) : new Color(80, pulse, 235));
        drawCenteredString(g, menuMessage, Math.min(panelHeight - 40, optionY + 25));
    }

    private void renderAnimatedMenuShip(Graphics2D g, int y) {
        if (spaceshipImage == null) {
            return;
        }

        int shipWidth = Math.max(62, Math.min(92, panelWidth / 14));
        int shipHeight = shipWidth * PLAYER_HEIGHT / PLAYER_WIDTH;
        int drift = (int) (Math.sin(menuAnimationTime * 1.4) * 28);
        int bob = (int) (Math.sin(menuAnimationTime * 2.2) * 5);
        int shipX = (panelWidth - shipWidth) / 2 + drift;
        int shipY = y + bob;
        int engineY = shipY + shipHeight / 2;
        int trailLength = 32 + (int) ((Math.sin(menuAnimationTime * 7.0) + 1.0) * 8);

        // Soft glow behind the ship.
        g.setColor(new Color(20, 190, 255, 45));
        g.fillOval(shipX - 16, shipY - 8, shipWidth + 32, shipHeight + 16);

        // Animated engine trail.
        g.setColor(new Color(55, 220, 255, 160));
        g.drawLine(shipX - trailLength, engineY, shipX - 5, engineY);
        g.setColor(new Color(120, 235, 255, 80));
        g.drawLine(shipX - trailLength / 2, engineY - 5, shipX - 4, engineY - 5);
        g.drawLine(shipX - trailLength / 2, engineY + 5, shipX - 4, engineY + 5);

        g.drawImage(spaceshipImage, shipX, shipY, shipWidth, shipHeight, null);
    }

    private void renderInformationPage(Graphics2D g, String heading, String[] lines) {
        g.setFont(new Font("Monospaced", Font.BOLD, 38));
        g.setColor(new Color(115, 225, 255));
        drawCenteredString(g, "+=== " + heading + " ===+", panelHeight / 3);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        int y = panelHeight / 3 + 75;
        for (String line : lines) {
            g.setColor(new Color(225, 245, 255));
            drawCenteredString(g, line, y);
            y += 38;
        }

        g.setFont(new Font("Monospaced", Font.BOLD, 15));
        boolean invalid = menuMessage.startsWith("INVALID");
        g.setColor(invalid ? new Color(255, 110, 110) : new Color(100, 190, 220));
        drawCenteredString(g, menuMessage, y + 60);
    }

    private void drawCenteredString(Graphics2D g, String text, int baseline) {
        int x = (panelWidth - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, baseline);
    }

}
