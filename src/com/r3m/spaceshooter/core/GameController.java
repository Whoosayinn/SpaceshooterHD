package com.r3m.spaceshooter.core;

import com.r3m.spaceshooter.entity.Asteroid;
import com.r3m.spaceshooter.entity.Explosion;
import com.r3m.spaceshooter.entity.Star;
import com.r3m.spaceshooter.system.AssetManager;
import com.r3m.spaceshooter.system.CollisionManager;
import com.r3m.spaceshooter.system.InputManager;
import com.r3m.spaceshooter.system.ScoreManager;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
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
        PLAYING
    }

    private GameState gameState = GameState.MENU;
	
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
    private double menuAnimationTime;

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

        if (gameState == GameState.MENU) {
            menuAnimationTime += deltaTime;
            boolean buttonClicked = inputManager.consumePrimaryClick()
                && getStartButtonBounds().contains(
                    inputManager.getMouseX(),
                    inputManager.getMouseY()
                );

            if (inputManager.isSpacePressed() || buttonClicked) {
                startNewGame();
            }
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

    private void startNewGame() {
        gameState = GameState.PLAYING;
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
    }

    private Rectangle getStartButtonBounds() {
        int width = Math.min(390, Math.max(260, panelWidth - 80));
        int height = 76;
        int x = (panelWidth - width) / 2;
        int y = (int) (panelHeight * 0.68) - height / 2;
        return new Rectangle(x, y, width, height);
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

        if (gameState == GameState.MENU) {
            renderMainMenu(g);
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

    private void renderMainMenu(Graphics2D g) {
        int titleSize = Math.max(48, Math.min(92, panelWidth / 15));
        int titleBaseline = panelHeight / 3;

        // Small studio-style heading.
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(120, 190, 230));
        drawCenteredString(g, "R3M  //  INTERSTELLAR COMMAND", titleBaseline - titleSize);

        // Layered text creates a neon glow without needing another image asset.
        Font titleFont = new Font("SansSerif", Font.BOLD | Font.ITALIC, titleSize);
        g.setFont(titleFont);
        drawGlowText(g, "SPACE SHOOTER", titleBaseline);

        g.setFont(new Font("SansSerif", Font.BOLD, Math.max(18, titleSize / 3)));
        g.setColor(new Color(100, 225, 255));
        drawCenteredString(g, "—  H D  —", titleBaseline + titleSize / 2);

        renderMenuSpaceship(g);
        renderStartButton(g);

        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.setColor(new Color(145, 165, 190));
        drawCenteredString(g, "W A S D   TO MOVE     •     ESC   TO EXIT", (int) (panelHeight * 0.84));

        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.setColor(new Color(70, 115, 145));
        drawCenteredString(g, "SYSTEM READY  //  AWAITING PILOT", panelHeight - 32);
    }

    private void drawGlowText(Graphics2D g, String text, int baseline) {
        int x = (panelWidth - g.getFontMetrics().stringWidth(text)) / 2;
        Composite oldComposite = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
        g.setColor(new Color(0, 210, 255));
        for (int offset = 10; offset >= 2; offset -= 2) {
            g.drawString(text, x - offset, baseline);
            g.drawString(text, x + offset, baseline);
            g.drawString(text, x, baseline - offset);
            g.drawString(text, x, baseline + offset);
        }

        g.setComposite(oldComposite);
        g.setColor(new Color(235, 252, 255));
        g.drawString(text, x, baseline);
    }

    private void renderMenuSpaceship(Graphics2D g) {
        if (spaceshipImage == null) {
            return;
        }

        int shipWidth = 105;
        int shipHeight = 92;
        int shipX = (panelWidth - shipWidth) / 2;
        int shipY = (int) (panelHeight * 0.49 + Math.sin(menuAnimationTime * 2.0) * 7);

        Composite oldComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
        g.setColor(new Color(0, 210, 255));
        g.fillOval(shipX - 35, shipY + 20, shipWidth + 70, shipHeight / 2);
        g.setComposite(oldComposite);
        g.drawImage(spaceshipImage, shipX, shipY, shipWidth, shipHeight, null);
    }

    private void renderStartButton(Graphics2D g) {
        Rectangle button = getStartButtonBounds();
        boolean hovered = button.contains(inputManager.getMouseX(), inputManager.getMouseY());
        double wave = (Math.sin(menuAnimationTime * 3.0) + 1.0) / 2.0;
        int fillAlpha = hovered ? 105 : 45 + (int) (wave * 30);

        Composite oldComposite = g.getComposite();
        Stroke oldStroke = g.getStroke();

        // Outer glow and dark glass-like button body.
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hovered ? 0.28f : 0.16f));
        g.setColor(new Color(0, 220, 255));
        g.fillRoundRect(button.x - 9, button.y - 9, button.width + 18, button.height + 18, 28, 28);
        g.setComposite(oldComposite);

        g.setColor(new Color(12, 80, 120, fillAlpha));
        g.fillRoundRect(button.x, button.y, button.width, button.height, 22, 22);

        g.setStroke(new BasicStroke(hovered ? 3.0f : 2.0f));
        g.setColor(hovered ? new Color(150, 245, 255) : new Color(45, 205, 240));
        g.drawRoundRect(button.x, button.y, button.width, button.height, 22, 22);

        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        int textBaseline = button.y + (button.height + g.getFontMetrics().getAscent()) / 2 - 4;
        drawCenteredString(g, "PRESS TO START", textBaseline);

        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(135, 210, 230));
        drawCenteredString(g, "CLICK  OR  PRESS  SPACE", button.y + button.height + 22);

        g.setStroke(oldStroke);
    }

    private void drawCenteredString(Graphics2D g, String text, int baseline) {
        int x = (panelWidth - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, baseline);
    }
}
