package com.r3m.spaceshooter.application;

import com.r3m.spaceshooter.core.GamePanel;
import javax.swing.JFrame;


public class GameFrame extends JFrame {

    public GameFrame() {
        this.setTitle("2D Space Shooter");
        // Sets Frame's Title
        GamePanel gamePanel = new GamePanel();
        // instantiation of gamePanel class
        add(gamePanel);
        // appends gamePanel to the gameFrame container
        pack();
        // sizes game window to the gameFrame pre-configured size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Allow user to close window by pressing exit
        setResizable(false);
        // Not allowing user to resize the window
        setLocationRelativeTo(null);
        // Centers the window perfectly on screen
        gamePanel.startGame();
        // starts game by executing codes in gamePanel
    }
}

