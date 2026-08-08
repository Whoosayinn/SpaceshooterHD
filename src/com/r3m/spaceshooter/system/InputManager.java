package com.r3m.spaceshooter.system;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputManager implements KeyListener {
    private boolean APressed, DPressed, spacePressed;
    private boolean WPressed, SPressed;

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_A     -> APressed     = true;
        case KeyEvent.VK_D     -> DPressed     = true;
        case KeyEvent.VK_W     -> WPressed     = true;   
        case KeyEvent.VK_S     -> SPressed     = true;   
        case KeyEvent.VK_SPACE -> spacePressed = true;
        }
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

    public boolean isAPressed()     { return APressed; }
    public boolean isDPressed()     { return DPressed; }
    public boolean isWPressed()     { return WPressed; }   
    public boolean isSPressed()     { return SPressed; }  
    public boolean isSpacePressed() { return spacePressed; }
}
