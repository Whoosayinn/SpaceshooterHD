package com.r3m.spaceshooter.application;

import com.r3m.spaceshooter.core.GamePanel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;


public class GameFrame extends JFrame {
    private final GamePanel gamePanel;

    public GameFrame() {
        GraphicsDevice screenDevice = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();
        DisplayMode displayMode = screenDevice.getDisplayMode();

        setTitle("2D Space Shooter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        gamePanel = new GamePanel(displayMode.getWidth(), displayMode.getHeight());
        add(gamePanel);
        getRootPane().registerKeyboardAction(
            event -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
        pack();

        if (screenDevice.isFullScreenSupported()) {
            screenDevice.setFullScreenWindow(this);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    public void startGame() {
        gamePanel.startGame();
    }
}

