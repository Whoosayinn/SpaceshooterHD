package com.r3m.spaceshooter.system;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputManager implements KeyListener {
    public static final int NO_MENU_CHOICE = -2;
    public static final int INVALID_MENU_CHOICE = -1;

    private boolean APressed, DPressed, spacePressed;
    private boolean WPressed, SPressed;
    private volatile int pendingMenuChoice = NO_MENU_CHOICE;

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
            case KeyEvent.VK_ESCAPE -> NO_MENU_CHOICE;
            default -> INVALID_MENU_CHOICE;
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

    public synchronized int consumeMenuChoice() {
        int choice = pendingMenuChoice;
        pendingMenuChoice = NO_MENU_CHOICE;
        return choice;
    }

    public boolean isAPressed()     { return APressed; }
    public boolean isDPressed()     { return DPressed; }
    public boolean isWPressed()     { return WPressed; }   
    public boolean isSPressed()     { return SPressed; }  
    public boolean isSpacePressed() { return spacePressed; }
}
