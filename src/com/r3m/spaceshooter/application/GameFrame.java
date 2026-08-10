package com.r3m.spaceshooter.application;

import com.r3m.spaceshooter.core.GamePanel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

/**
 * Top-level Swing window for Space Shooter HD.
 *
 * <p>The frame owns the {@link GamePanel} and forwards application-level
 * navigation requests to it.</p>
 */
public class GameFrame extends JFrame {
    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 640;

    /** Game surface hosted by this serializable frame. */
    private final GamePanel gamePanel;

    /** Creates and centers a fixed-size game window. */
    public GameFrame() {
        setTitle("Space Shooter HD");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(false);
        setResizable(false);

        gamePanel = new GamePanel(WINDOW_WIDTH, WINDOW_HEIGHT);
        add(gamePanel);
        getRootPane().registerKeyboardAction(
            event -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        pack();
        setLocationRelativeTo(null);
    }

    /** Starts the panel's game loop. */
    public void startGame() {
        gamePanel.startGame();
    }

    /** Starts a new gameplay session and brings the window to the front. */
    public void beginGameplay() {
        gamePanel.beginGameplay();
        toFront();
    }

    /** Displays the main menu. */
    public void showMainMenu() {
        gamePanel.showMainMenu();
    }

    /** Displays the instructions page. */
    public void showInstructions() {
        gamePanel.showInstructions();
    }

    /** Displays the controls page. */
    public void showControls() {
        gamePanel.showControls();
    }

    /** Stops the game loop and disposes of the window. */
    public void closeGame() {
        gamePanel.stopGame();
        dispose();
    }
}
