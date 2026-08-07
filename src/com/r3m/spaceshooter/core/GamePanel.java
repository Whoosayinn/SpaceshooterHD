package com.r3m.spaceshooter.core;

import com.r3m.spaceshooter.system.InputManager;
import com.r3m.spaceshooter.core.GameClock;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * GamePanel is the main game surface — it extends JPanel so it can
 * be added to a JFrame window, and implements Runnable so it can
 * run the game loop on a dedicated Thread.
 *
 * Responsibilities:
 * - Setting up the game window surface (size, background, double buffer)
 * - Managing the game Thread (start, stop)
 * - Running the game loop via run()
 * - Delegating all rendering to GameController via paintComponent()
 *
 * This class does NOT handle game logic — that stays in GameController.
 * This follows the Single Responsibility OOP principle.
 */

public class GamePanel extends JPanel implements Runnable {
	
	// --- SCREEN SETTINGS ---
	
    private static final int SCREEN_WIDTH = 640;
    private static final int SCREEN_HEIGHT = 480;
    private static final int TARGET_FPS = 60;
    
    // --- THREAD CONTROL ---

    // the dedicated thread that runs the game loop
    // separate from Swing's Event Dispatch Thread for precise timing
    private static final long NANOSECONDS_PER_FRAME = 1_000_000_000L / TARGET_FPS;
    private Thread gameThread;
    
    // flag that controls whether the game loop keeps running
    // setting this to false cleanly stops the loop on its next cycle
    private boolean running = false;
    
    // --- CORE DEPENDENCIES ---

    // handles all game logic — updating positions, collision, score
    private final GameController gameController;
    
    // listens to keyboard events and tracks which keys are held
    private final InputManager inputManager;
    
    // handles all frame timing — waits between frames, returns deltaTime
    private final GameClock gameClock;
    

     /**
     * Constructor — sets up the game surface and initializes all dependencies.
     * Called once when GameFrame creates a new GamePanel.
     */
    public GamePanel() {
    	
        // without this, addKeyListener won't work reliably
        setFocusable(true);
        
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        
        // ALlows GUI receive keyboard focus
        setDoubleBuffered(true);

        //instantiation of inputManager class
        this.inputManager = new InputManager();

        // allow the specified key listener to 
        addKeyListener(inputManager);

        // pass the SAME object into gameController
        this.gameController = new GameController(inputManager, SCREEN_WIDTH, SCREEN_HEIGHT);
        
        // Instantiation of GameClock class
        this.gameClock = new GameClock(TARGET_FPS);
  

    }


    public void startGame() {
        /**
         * Starts the game — called from GameFrame after the window is visible.
         * Requests keyboard focus so key events register immediately,
         * then launches the game loop on a new Thread.
         */
        requestFocusInWindow();
        running = true;
        
        // create a new Thread with this GamePanel as the Runnable
        // this means Thread will call our run() method when started
        gameThread = new Thread(this);
        
        // actually launch the thread — this calls run() on the new thread
        // NEVER call run() directly — that would execute on the current thread
        gameThread.start();
    }

    public void stopGame() {
        /**
         * Stops the game loop cleanly.
         * Sets running to false so the loop exits on its next cycle,
         * then waits for the thread to fully finish before returning.
         */
    	running = false;
    	try {
            // wait for the game thread to fully finish
            // prevents the program from exiting while the thread is mid-frame
    		gameThread.join(); // wait for thread to fully stop
    	} catch (InterruptedException  e) {
            // if interrupted while waiting, re-set the interrupted flag
            // so the rest of the program knows this thread was interrupted
    		Thread.currentThread().interrupt();
    	}
    }

    @Override
    public void run() {
        /**
         * The game loop — runs continuously on the game thread.
         * Every frame: wait for the right time, update logic, redraw screen.
         *
         * This method is called automatically by Thread.start().
         * Do NOT call this directly.
         */
    	
        // reset the clock so the first frame measures from right now
        // not from when the GameClock object was constructed
        gameClock.reset();

        while (running) {
            try {
                // wait until the next frame is due, get deltaTime in seconds
                // e.g. at 60fps this returns ~0.01666 each frame
                // if a frame runs slow it returns a larger value automatically
                double deltaTime = gameClock.waitForNextFrame();

                
                // update all game logic for this frame
                // moves player/enemies/bullets, checks collisions, updates score
                // passes deltaTime so movement is frame-rate independent
                gameController.update(deltaTime);
                
                // tell Swing to redraw the panel as soon as possible
                // this triggers paintComponent() which calls gameController.render()
                repaint();

            } catch (InterruptedException e) {
            	
                // thread was interrupted — re-set the flag and stop the loop
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
    	
        /**
         * Called by Swing every time repaint() is triggered.
         * Casts Graphics to Graphics2D for better rendering quality,
         * enables antialiasing for smoother edges,
         * then delegates all actual drawing to GameController.
         *
         * This method should NEVER contain game logic — only drawing calls.
         *
         * @param g the Graphics object provided by Swing's painting system
         */
    	
        // always call super first — clears the previous frame
        // skipping this causes ghosting (old frames visible behind new ones)
        super.paintComponent(g);
        
        // cast Graphics to Graphics2D — gives access to advanced rendering features
        // safe to cast because Swing always passes a Graphics2D object here
        Graphics2D g2d = (Graphics2D) g;
        
        // enable antialiasing — smooths diagonal lines and curves
        // small visual improvement with essentially zero performance cost
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // delegate all drawing to GameController
        gameController.render(g2d);
    
	}
}