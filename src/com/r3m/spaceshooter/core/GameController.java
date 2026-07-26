package com.r3m.spaceshooter.core;

import com.r3m.spaceshooter.system.AssetManager;
import com.r3m.spaceshooter.system.CollisionManager;
import com.r3m.spaceshooter.system.InputManager;
import com.r3m.spaceshooter.system.ScoreManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GameController {
	
    private final int panelWidth;
    private final int panelHeight;

    InputManager inputManager;
    CollisionManager collisionManager;
    AssetManager assetManager;
    ScoreManager scoreManager;

    // placeholder player position — replace with a proper Player class later
    private double playerX = 200, playerY = 250;
    private static final int PLAYER_SPEED = 300; //pixels per second

    public GameController(InputManager inputManager, int panelWidth, int panelHeight) {
        this.inputManager = inputManager;
        // make sure that gameController method take a hold of keyboard's input
        this.panelWidth = panelWidth;
        // passing through the horizontal boundary of screen
        this.panelHeight = panelHeight;
     // passing through the vertical boundary of screen

        this.collisionManager = new CollisionManager();
        this.assetManager = AssetManager.getInstance();
        // 
        this.scoreManager = new ScoreManager();
    }

    public void update(double deltaTime) {
    	// Runs every single frame. 60 times per second, basically the logic step of the program
    	// move things, check rules, updates state.
    	// Step 1: build raw direction vector
        double dx = 0;
        double dy = 0;

        if (inputManager.isAPressed())  dx--;
        if (inputManager.isDPressed()) dx++;
        if (inputManager.isWPressed())  dy--;  // y decreases going up in Java2D
        if (inputManager.isSPressed())  dy++;

        // Step 2: normalize so diagonal speed = straight speed
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length != 0) {
            dx = (dx / length) * PLAYER_SPEED * deltaTime;
            dy = (dy / length) * PLAYER_SPEED * deltaTime;
        }

        // Step 3: apply movement
        playerX += dx;
        playerY += dy;

        // Step 4: clamp to screen bounds (all 4 edges now)
        playerX = Math.max(0, Math.min(playerX, panelWidth - 32));
        playerY = Math.max(0, Math.min(playerY, panelHeight - 32));
        
        // later: update bullets/enemies, call collisionManager.isColliding(...)
    }

    // What is graphics g? a class that has many functions to draw objects on screen
    
    public void render(Graphics2D g) {
    	// runs every frame, right after update. REceives Graphics g object
        g.setColor(Color.GREEN);
        g.fillRect((int)playerX, (int)playerY, 32, 32); // placeholder ship
        // ^^ Creates player placeholder ship ^^

        g.setColor(Color.WHITE);
        g.drawString("Score: " + scoreManager.getScore(), 10, 20);
        // Draws text on screen at position (10,20), gets score from getScore()
        // method every frame
    }
}