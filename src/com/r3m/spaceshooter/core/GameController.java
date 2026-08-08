package com.r3m.spaceshooter.core;

import com.r3m.spaceshooter.entity.Asteroid;
import com.r3m.spaceshooter.entity.Bullet;
import com.r3m.spaceshooter.entity.EliteEnemyShip;
import com.r3m.spaceshooter.entity.EnemySpaceship;
import com.r3m.spaceshooter.entity.Explosion;
import com.r3m.spaceshooter.entity.GruntEnemyShip;
import com.r3m.spaceshooter.entity.PlayerSpaceship;
import com.r3m.spaceshooter.entity.PowerUp;
import com.r3m.spaceshooter.system.AssetManager;
import com.r3m.spaceshooter.system.CollisionManager;
import com.r3m.spaceshooter.system.InputManager;
import com.r3m.spaceshooter.system.ScoreManager;
import com.r3m.spaceshooter.system.SpriteSheet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
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
    private final InputManager inputManager;

    // checks whether two game entities are overlapping
    private final CollisionManager collisionManager;

    // loads and caches image assets from disk
    private final AssetManager assetManager;
    private final ScoreManager scoreManager;

    // --- PLAYER ---
    private static final int PLAYER_WIDTH = 64;
    private static final int PLAYER_HEIGHT = 56;
    private final PlayerSpaceship player;

    private final SpriteSheet playerSpriteSheet;
    
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
    private final Random random = new Random();
    private final BufferedImage asteroidImage;
    private double asteroidSpawnTimer = ASTEROID_SPAWN_INTERVAL;

    // --- BULLET STATE ---
    private static final double BULLET_SPEED = 700.0;
    private static final double SHOOT_COOLDOWN_SECONDS = 0.25;
    private static final int BULLET_SCORE_VALUE = 10;

    private final List<Bullet> bullets = new ArrayList<>();
    private double shootCooldownRemaining = 0.0;

    // --- POWER-UP STATE ---
    private static final double POWERUP_DROP_CHANCE = 0.05;
    private static final double SPREAD_SHOT_DURATION_SECONDS = 8.0;
    private static final double SPREAD_ANGLE_DEGREES = 15.0;

    private final List<PowerUp> powerUps = new ArrayList<>();
    private double spreadShotRemaining = 0.0;

    // --- ENEMY SHIP STATE ---
    private static final double ENEMY_SPAWN_INTERVAL_SECONDS = 2.5;
    private static final int ENEMY_WIDTH = 50;
    private static final int ENEMY_HEIGHT = 36;
    private static final double ELITE_SPAWN_CHANCE = 0.3;
    private static final int ENEMY_KILL_SCORE_VALUE = 25;
    private static final double ENEMY_BULLET_SPEED = 500.0;

    private final List<EnemySpaceship> enemies = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();
    private double enemySpawnTimer = ENEMY_SPAWN_INTERVAL_SECONDS;
    private final BufferedImage gruntImage;
    private final BufferedImage eliteImage;

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
    public GameController(InputManager inputManager, int panelWidth, int panelHeight) {

        this.inputManager = inputManager;

        // store screen dimensions for boundary clamping in update()
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;

        this.collisionManager = new CollisionManager();
        this.assetManager = AssetManager.getInstance();
        this.scoreManager = new ScoreManager();
        
        BufferedImage sheetImage = assetManager.loadImage("/assets/player_sprite.png");
        if (sheetImage != null) {
            // slice the 25-frame sheet into a 5x5 grid
            this.playerSpriteSheet = new SpriteSheet(sheetImage, 5, 5);

            // use the animated constructor — passes spritesheet to PlayerSpaceship
            this.player = new PlayerSpaceship(
                playerSpriteSheet,
                200, 250, PLAYER_WIDTH, PLAYER_HEIGHT,
                inputManager, panelWidth, panelHeight
            );
        } else {
            // fallback — spritesheet didn't load, use static image instead
            this.playerSpriteSheet = null;
            BufferedImage fallbackImage = assetManager.loadImage("/assets/spaceship.png");
            this.player = new PlayerSpaceship(
                fallbackImage,
                200, 250, PLAYER_WIDTH, PLAYER_HEIGHT,
                inputManager, panelWidth, panelHeight
            );
        }
        
        this.asteroidImage = assetManager.loadImage("/assets/asteroid.png");
        this.gruntImage = assetManager.loadImage("/assets/enemy_grunt.png");
        this.eliteImage = assetManager.loadImage("/assets/enemy_elite.png");
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

        player.update(deltaTime);

        shootCooldownRemaining = Math.max(0.0, shootCooldownRemaining - deltaTime);
        spreadShotRemaining = Math.max(0.0, spreadShotRemaining - deltaTime);

        handleShooting();
        updateBullets(deltaTime);
        updateAsteroids(deltaTime);
        checkBulletAsteroidCollisions();
        updatePowerUps(deltaTime);

        updateEnemies(deltaTime);
        updateEnemyBullets(deltaTime);
        checkBulletEnemyCollisions();
        checkEnemyBulletPlayerCollisions();
    }

    public void setViewportSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        panelWidth = width;
        panelHeight = height;
        player.setViewportSize(width, height);
    }

    private void handleShooting() {
        if (!inputManager.isSpacePressed()) return;
        if (shootCooldownRemaining > 0.0) return;
        if (!player.isAlive()) return;

        double bulletX = player.getX() + PLAYER_WIDTH - 8;
        double bulletY = player.getY() + (PLAYER_HEIGHT / 2.0) - (Bullet.getHeight() / 2.0);

        synchronized (bullets) {
            if (spreadShotRemaining > 0.0) {
                double angleRadians = Math.toRadians(SPREAD_ANGLE_DEGREES);
                bullets.add(new Bullet(bulletX, bulletY, BULLET_SPEED, 0));
                bullets.add(new Bullet(
                    bulletX,
                    bulletY,
                    BULLET_SPEED * Math.cos(angleRadians),
                    -BULLET_SPEED * Math.sin(angleRadians)
                ));
                bullets.add(new Bullet(
                    bulletX,
                    bulletY,
                    BULLET_SPEED * Math.cos(angleRadians),
                    BULLET_SPEED * Math.sin(angleRadians)
                ));
            } else {
                bullets.add(new Bullet(bulletX, bulletY, BULLET_SPEED, 0));
            }
        }

        shootCooldownRemaining = SHOOT_COOLDOWN_SECONDS;
    }

    private void updateBullets(double deltaTime) {
        synchronized (bullets) {
            Iterator<Bullet> iterator = bullets.iterator();
            while (iterator.hasNext()) {
                Bullet bullet = iterator.next();
                bullet.update(deltaTime);
                if (bullet.isOffScreen(panelWidth, panelHeight)) {
                    iterator.remove();
                }
            }
        }
    }

    private void checkBulletAsteroidCollisions() {
        synchronized (bullets) {
            synchronized (asteroids) {
                Iterator<Bullet> bulletIterator = bullets.iterator();
                while (bulletIterator.hasNext()) {
                    Bullet bullet = bulletIterator.next();
                    Rectangle bulletBounds = bullet.getBounds();
                    boolean bulletConsumed = false;

                    Iterator<Asteroid> asteroidIterator = asteroids.iterator();
                    while (asteroidIterator.hasNext()) {
                        Asteroid asteroid = asteroidIterator.next();
                        if (collisionManager.isColliding(bulletBounds, asteroid.getBounds())) {
                            createExplosion(asteroid.getCenterX(), asteroid.getCenterY());
                            maybeDropPowerUp(asteroid.getCenterX(), asteroid.getCenterY());
                            asteroidIterator.remove();
                            scoreManager.addScore(BULLET_SCORE_VALUE);
                            bulletConsumed = true;
                            break;
                        }
                    }

                    if (bulletConsumed) {
                        bulletIterator.remove();
                    }
                }
            }
        }
    }

    private void updateEnemies(double deltaTime) {
        synchronized (enemies) {
            enemySpawnTimer += deltaTime;

            if (enemySpawnTimer >= ENEMY_SPAWN_INTERVAL_SECONDS) {
                spawnEnemy();
                enemySpawnTimer = 0.0;
            }

            Iterator<EnemySpaceship> iterator = enemies.iterator();
            while (iterator.hasNext()) {
                EnemySpaceship enemy = iterator.next();

                enemy.setPlayerYReference(player.getCenterY());
                enemy.update(deltaTime);

                if (enemy.consumeShootRequest()) {
                    spawnEnemyBullet(enemy);
                }

                if (enemy.isPastLeftEdge()) {
                    iterator.remove();
                }
            }
        }
    }

    private void spawnEnemy() {
        int maximumY = Math.max(0, panelHeight - ENEMY_HEIGHT);
        double y = maximumY == 0 ? 0 : random.nextInt(maximumY + 1);

        EnemySpaceship enemy = random.nextDouble() < ELITE_SPAWN_CHANCE
            ? new EliteEnemyShip(eliteImage, panelWidth, y, ENEMY_WIDTH, ENEMY_HEIGHT, random)
            : new GruntEnemyShip(gruntImage, panelWidth, y, ENEMY_WIDTH, ENEMY_HEIGHT, random);

        enemies.add(enemy);
    }

    private void spawnEnemyBullet(EnemySpaceship enemy) {
        double bulletX = enemy.getX();
        double bulletY = enemy.getCenterY() - (Bullet.getHeight() / 2.0);

        synchronized (enemyBullets) {
            enemyBullets.add(new Bullet(bulletX, bulletY, -ENEMY_BULLET_SPEED, 0, Color.RED));
        }
    }

    private void updateEnemyBullets(double deltaTime) {
        synchronized (enemyBullets) {
            Iterator<Bullet> iterator = enemyBullets.iterator();
            while (iterator.hasNext()) {
                Bullet bullet = iterator.next();
                bullet.update(deltaTime);
                if (bullet.isOffScreen(panelWidth, panelHeight)) {
                    iterator.remove();
                }
            }
        }
    }

    private void checkBulletEnemyCollisions() {
        synchronized (bullets) {
            synchronized (enemies) {
                Iterator<Bullet> bulletIterator = bullets.iterator();
                while (bulletIterator.hasNext()) {
                    Bullet bullet = bulletIterator.next();
                    Rectangle bulletBounds = bullet.getBounds();
                    boolean bulletConsumed = false;

                    Iterator<EnemySpaceship> enemyIterator = enemies.iterator();
                    while (enemyIterator.hasNext()) {
                        EnemySpaceship enemy = enemyIterator.next();
                        if (collisionManager.isColliding(bulletBounds, enemy.getBounds())) {
                            createExplosion(enemy.getCenterX(), enemy.getCenterY());
                            enemyIterator.remove();
                            scoreManager.addScore(ENEMY_KILL_SCORE_VALUE);
                            bulletConsumed = true;
                            break;
                        }
                    }

                    if (bulletConsumed) {
                        bulletIterator.remove();
                    }
                }
            }
        }
    }

    private void checkEnemyBulletPlayerCollisions() {
        synchronized (enemyBullets) {
            Iterator<Bullet> iterator = enemyBullets.iterator();
            while (iterator.hasNext()) {
                Bullet bullet = iterator.next();
                if (collisionManager.isColliding(bullet.getBounds(), player.getHitbox())) {
                    damagePlayer();
                    iterator.remove();
                }
            }
        }
    }

    private void maybeDropPowerUp(double centerX, double centerY) {
        if (random.nextDouble() >= POWERUP_DROP_CHANCE) {
            return;
        }

        synchronized (powerUps) {
            powerUps.add(new PowerUp(centerX, centerY));
        }
    }

    private void updatePowerUps(double deltaTime) {
        synchronized (powerUps) {
            Iterator<PowerUp> iterator = powerUps.iterator();
            while (iterator.hasNext()) {
                PowerUp powerUp = iterator.next();
                powerUp.update(deltaTime);

                if (powerUp.isPastLeftEdge()) {
                    iterator.remove();
                    continue;
                }

                if (collisionManager.isColliding(getPlayerBounds(), powerUp.getBounds())) {
                    spreadShotRemaining = SPREAD_SHOT_DURATION_SECONDS;
                    iterator.remove();
                }
            }
        }
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
                    createExplosion(asteroid.getCenterX(), asteroid.getCenterY());
                    maybeDropPowerUp(asteroid.getCenterX(), asteroid.getCenterY());
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
                maybeDropPowerUp(
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

    private Polygon getPlayerBounds() {
        return player.getHitbox();
    }

    private void damagePlayer() {
        player.takeHit();
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

        // player handles its own rendering (including blinking while invulnerable)
        player.render(g);

        synchronized (asteroids) {
            for (Asteroid asteroid : asteroids) {
                asteroid.render(g);
            }
        }

        synchronized (enemies) {
            for (EnemySpaceship enemy : enemies) {
                enemy.render(g);
            }
        }

        synchronized (bullets) {
            for (Bullet bullet : bullets) {
                bullet.render(g);
            }
        }

        synchronized (enemyBullets) {
            for (Bullet bullet : enemyBullets) {
                bullet.render(g);
            }
        }

        synchronized (explosions) {
            for (Explosion explosion : explosions) {
                explosion.render(g);
            }
        }

        synchronized (powerUps) {
            for (PowerUp powerUp : powerUps) {
                powerUp.render(g);
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
        g.drawString("Lives: " + player.getLives(), 10, 38);

        if (spreadShotRemaining > 0.0) {
            g.setColor(new java.awt.Color(80, 220, 255));
            g.drawString("Spread Shot: " + String.format("%.1f", spreadShotRemaining) + "s", 10, 56);
        }

        if (!player.isAlive()) {
            g.setColor(Color.WHITE);
            g.drawString("GAME OVER", panelWidth / 2 - 35, panelHeight / 2);
        }
    }
}