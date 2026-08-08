package com.r3m.spaceshooter.application;

import com.r3m.spaceshooter.core.GamePanel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;


public class GameFrame extends JFrame {
    private static final int WINDOW_WIDTH = 960;
    private static final int WINDOW_HEIGHT = 640;

    private final GamePanel gamePanel;

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

    public void startGame() {
        gamePanel.startGame();
    }

    public void beginGameplay() {
        gamePanel.beginGameplay();
        toFront();
    }

    public void showMainMenu() {
        gamePanel.showMainMenu();
    }

    public void showInstructions() {
        gamePanel.showInstructions();
    }

    public void showControls() {
        gamePanel.showControls();
    }

    public void closeGame() {
        gamePanel.stopGame();
        dispose();
    }
}
