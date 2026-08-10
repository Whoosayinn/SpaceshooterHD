package com.r3m.spaceshooter.core;

import com.r3m.spaceshooter.entity.Asteroid;
import com.r3m.spaceshooter.entity.Bullet;
import com.r3m.spaceshooter.entity.EliteEnemyShip;
import com.r3m.spaceshooter.entity.EnemySpaceship;
import com.r3m.spaceshooter.entity.Explosion;
import com.r3m.spaceshooter.entity.GruntEnemyShip;
import com.r3m.spaceshooter.entity.PlayerSpaceship;
import com.r3m.spaceshooter.entity.PowerUp;
import com.r3m.spaceshooter.entity.Star;
import com.r3m.spaceshooter.system.AssetManager;
import com.r3m.spaceshooter.system.CollisionManager;
import com.r3m.spaceshooter.system.InputManager;
import com.r3m.spaceshooter.system.ScoreManager;
import com.r3m.spaceshooter.system.SpriteSheet;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
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

	// --- GAME STATE --- //
    private enum GameState {
        MENU,
        INSTRUCTIONS,
        CONTROLS,
        GAME_OVER,
        PLAYING
    }

    private volatile GameState gameState = GameState.MENU;
    private volatile double menuAnimationTime;
    private volatile double gameOverFade;
    private static final double GAME_OVER_FADE_SECONDS = 1.6;

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
    
    // --- RENDERING --- //
    private final MenuRenderer menuRenderer;

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
    private final List<Star> stars = new ArrayList<>();
    private final Random random = new Random();
    private final BufferedImage asteroidImage;
    private double asteroidSpawnTimer = ASTEROID_SPAWN_INTERVAL;

    private static final int STAR_COUNT = 120;
    private static final double STAR_MIN_SPEED = 60.0;
    private static final double STAR_SPEED_RANGE = 180.0;

    // --- BULLET STATE ---
    private static final double BULLET_SPEED = 700.0;
    private static final double SHOOT_COOLDOWN_SECONDS = 0.25;
    private static final int BULLET_SCORE_VALUE = 10;
    private static final int DAMAGE_SCORE_PENALTY = 15;

    private final List<Bullet> bullets = new ArrayList<>();
    private double shootCooldownRemaining = 0.0;

    // --- POWER-UP STATE ---
    private static final double POWERUP_DROP_CHANCE = 0.05;
    private static final double SPREAD_SHOT_DURATION_SECONDS = 8.0;
    private static final double SPREAD_ANGLE_DEGREES = 15.0;
    private static final int POWER_UP_SCORE_VALUE = 20;

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

     // load player sprite sheet
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
        
        this.menuRenderer = new MenuRenderer(
                player, asteroidImage, panelWidth, panelHeight
            );
        
        createInitialStars();
    }

    /**
     * Update — runs every frame (~60 times per second).
     * This is the LOGIC step: move things, check rules, update state.
     * No drawing happens here — only data changes.
     *
     * @param deltaTime seconds since the last frame (e.g. 0.01666 at 60fps)
     *                  multiply all movement by this to keep speed
     *                  consistent regardless of frame rate
     */
    public void update(double deltaTime) {
        updateStars(deltaTime);

        if (gameState == GameState.GAME_OVER) {
            menuAnimationTime += deltaTime;
            gameOverFade = Math.min(
                1.0,
                gameOverFade + deltaTime / GAME_OVER_FADE_SECONDS
            );
            handleWindowMenuInput();
            return;
        }

        if (gameState != GameState.PLAYING) {
            menuAnimationTime += deltaTime;
            handleWindowMenuInput();
            return;
        }

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

        if (!player.isAlive()) {
            gameOverFade = 0;
            gameState = GameState.GAME_OVER;
        }
    }

    public void setViewportSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        panelWidth = width;
        panelHeight = height;
        player.setViewportSize(width, height);
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

    public void beginGameplay() {
        player.reset(200, panelHeight / 2.0 - PLAYER_HEIGHT / 2.0);
        scoreManager.reset();
        asteroidSpawnTimer = 0;
        enemySpawnTimer = 0;
        shootCooldownRemaining = 0;
        spreadShotRemaining = 0;

        synchronized (asteroids) { asteroids.clear(); }
        synchronized (explosions) { explosions.clear(); }
        synchronized (bullets) { bullets.clear(); }
        synchronized (enemyBullets) { enemyBullets.clear(); }
        synchronized (enemies) { enemies.clear(); }
        synchronized (powerUps) { powerUps.clear(); }

        gameOverFade = 0;
        gameState = GameState.PLAYING;
    }

    public void showMainMenu() {
        gameState = GameState.MENU;
    }

    public void showInstructions() {
        gameState = GameState.INSTRUCTIONS;
    }

    public void showControls() {
        gameState = GameState.CONTROLS;
    }

    private void handleWindowMenuInput() {
        int choice = inputManager.consumeMenuChoice();
        if (choice == InputManager.NO_MENU_CHOICE) {
            return;
        }

        if (gameState == GameState.GAME_OVER) {
            switch (choice) {
                case 1 -> showMainMenu();
                case 2 -> beginGameplay();
                default -> { }
            }
            return;
        }

        if (gameState == GameState.MENU) {
            switch (choice) {
                case 1 -> beginGameplay();
                case 2 -> showInstructions();
                case 3 -> showControls();
                case 4 -> System.exit(0);
                default -> { }
            }
        } else if (choice == 0) {
            showMainMenu();
        }
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
                    scoreManager.addScore(POWER_UP_SCORE_VALUE);
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


    /**
     * Render — runs every frame right after update().
     * Reads game state and draws it — never modifies state.
     *
     * @param g the Graphics2D paintbrush
     */
    public void render(Graphics2D g) {

        // background and stars — always drawn regardless of state
        menuRenderer.renderSpaceBackground(g);
        synchronized (stars) {
            for (Star star : stars) star.render(g);
        }

        // menu screens delegate entirely to MenuRenderer
        switch (gameState) {
            case MENU         -> { menuRenderer.renderMainMenu(g, menuAnimationTime);    return; }
            case INSTRUCTIONS -> { menuRenderer.renderInstructionsPage(g);               return; }
            case CONTROLS     -> { menuRenderer.renderControlsPage(g);                   return; }
            default           -> { /* fall through to gameplay rendering */ }
        }

        // gameplay rendering
        player.render(g);
        synchronized (asteroids)    { for (Asteroid a      : asteroids)    a.render(g); }
        synchronized (enemies)      { for (EnemySpaceship e: enemies)      e.render(g); }
        synchronized (bullets)      { for (Bullet b        : bullets)      b.render(g); }
        synchronized (enemyBullets) { for (Bullet b        : enemyBullets) b.render(g); }
        synchronized (explosions)   { for (Explosion e     : explosions)   e.render(g); }
        synchronized (powerUps)     { for (PowerUp p       : powerUps)     p.render(g); }

        // HUD
        g.setColor(Color.WHITE);
        g.drawString("Score: " + scoreManager.getScore(), 10, 20);
        g.drawString("Lives: " + player.getLives(),       10, 38);

        if (spreadShotRemaining > 0.0) {
            g.setColor(new Color(80, 220, 255));
            g.drawString(
                "Spread Shot: " + String.format("%.1f", spreadShotRemaining) + "s",
                10, 56
            );
        }

        // game over overlay delegates to MenuRenderer
        if (gameState == GameState.GAME_OVER) {
            menuRenderer.renderGameOverScreen(
                g, gameOverFade, menuAnimationTime, scoreManager.getScore()
            );
        }
    }

    // FIX: removed getPlayerBounds() — call player.getHitbox() directly
    // it was a single-line wrapper with no added value

    private void damagePlayer() {
        int livesBefore = player.getLives();
        player.takeHit();
        if (player.getLives() < livesBefore) {
            scoreManager.subtractScore(DAMAGE_SCORE_PENALTY);
        }
    }
}

