package com.r3m.spaceshooter.system;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/** Tracks movement keys and queues numeric menu choices from keyboard events. */
public class InputManager implements KeyListener {
    /** Sentinel returned when no menu choice is waiting. */
    public static final int NO_MENU_CHOICE = -1;

    private boolean APressed, DPressed, spacePressed;
    private boolean WPressed, SPressed;
    private volatile int pendingMenuChoice = NO_MENU_CHOICE;

    /** Creates an input manager with all keys released. */
    public InputManager() {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_A     -> APressed     = true;
        case KeyEvent.VK_D     -> DPressed     = true;
        case KeyEvent.VK_W     -> WPressed     = true;   
        case KeyEvent.VK_S     -> SPressed     = true;   
        case KeyEvent.VK_SPACE -> spacePressed = true;
        }

        pendingMenuChoice = switch (e.getKeyCode()) {
            case KeyEvent.VK_0, KeyEvent.VK_NUMPAD0 -> 0;
            case KeyEvent.VK_1, KeyEvent.VK_NUMPAD1 -> 1;
            case KeyEvent.VK_2, KeyEvent.VK_NUMPAD2 -> 2;
            case KeyEvent.VK_3, KeyEvent.VK_NUMPAD3 -> 3;
            case KeyEvent.VK_4, KeyEvent.VK_NUMPAD4 -> 4;
            default -> pendingMenuChoice;
        };

    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_A     -> APressed     = false;
        case KeyEvent.VK_D     -> DPressed     = false;
        case KeyEvent.VK_W     -> WPressed     = false;   
        case KeyEvent.VK_S     -> SPressed     = false;   
        case KeyEvent.VK_SPACE -> spacePressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // required by the interface, unused here
    }

    /**
     * Returns and clears the most recent numeric menu choice.
     *
     * @return a choice from 0 through 4, or {@link #NO_MENU_CHOICE}
     */
    public synchronized int consumeMenuChoice() {
        int choice = pendingMenuChoice;
        pendingMenuChoice = NO_MENU_CHOICE;
        return choice;
    }

    /** Reports A-key state.
     * @return whether the A key is currently pressed */
    public boolean isAPressed()     { return APressed; }
    /** Reports D-key state.
     * @return whether the D key is currently pressed */
    public boolean isDPressed()     { return DPressed; }
    /** Reports W-key state.
     * @return whether the W key is currently pressed */
    public boolean isWPressed()     { return WPressed; }
    /** Reports S-key state.
     * @return whether the S key is currently pressed */
    public boolean isSPressed()     { return SPressed; }
    /** Reports Space-key state.
     * @return whether the Space key is currently pressed */
    public boolean isSpacePressed() { return spacePressed; }
}
