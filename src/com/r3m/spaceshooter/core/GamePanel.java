package com.r3m.spaceshooter.core;

import com.r3m.spaceshooter.system.InputManager;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;


public class GamePanel extends JPanel implements Runnable {
	
	// SCREEN SETTINGS
    private static final int SCREEN_WIDTH = 640;
    private static final int SCREEN_HEIGHT = 480;
    private static final int TARGET_FPS = 60;
    private static final long NANOSECONDS_PER_FRAME = 1_000_000_000L / TARGET_FPS;

    private Thread gameThread;
    private boolean running = false;
    
    private final GameController gameController;
    private final InputManager inputManager;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        // ALlows GUI receive keyboard focus
        setDoubleBuffered(true);

        this.inputManager = new InputManager();
        //instantiation of inputManager class
        addKeyListener(inputManager);
        // allow the specified key listener to 
        
        // pass the SAME object into gameController
        gameController = new GameController(inputManager, SCREEN_WIDTH, SCREEN_HEIGHT);
		
    }

    public void startGame() {
        requestFocusInWindow();
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGame() {
    	running = false;
    	try {
    		gameThread.join(); // wait for thread to fully stop
    	} catch (InterruptedException  e) {
    		Thread.currentThread().interrupt();
    	}
    }

	@Override
	public void run() {
		long lastFrameTime = System.nanoTime();
		
		while(running) {
			long now = System.nanoTime();
			long elapsed = now - lastFrameTime;
			
			if (elapsed >= NANOSECONDS_PER_FRAME) {
				lastFrameTime = now;
				double deltaTime = elapsed / 1_000_000_000.0;
				gameController.update(deltaTime);
				repaint();
				}
			else {
				long sleepTime = (NANOSECONDS_PER_FRAME - elapsed) / 1_000_000;
				if (sleepTime > 1) {
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gameController.render(g2d);
    
	}
}