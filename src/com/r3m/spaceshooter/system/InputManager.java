package com.r3m.spaceshooter.system;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class InputManager implements KeyListener, MouseListener, MouseMotionListener {
    private boolean APressed, DPressed, spacePressed;
    private boolean WPressed, SPressed;
    private volatile int mouseX = -1;
    private volatile int mouseY = -1;
    private boolean primaryClickPending;

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

    @Override
    public synchronized void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        primaryClickPending = true;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { mouseMoved(e); }
    @Override public void mouseExited(MouseEvent e) { mouseX = -1; mouseY = -1; }

    public synchronized boolean consumePrimaryClick() {
        boolean wasClicked = primaryClickPending;
        primaryClickPending = false;
        return wasClicked;
    }

    public boolean isAPressed()     { return APressed; }
    public boolean isDPressed()     { return DPressed; }
    public boolean isWPressed()     { return WPressed; }   
    public boolean isSPressed()     { return SPressed; }  
    public boolean isSpacePressed() { return spacePressed; }
    public int getMouseX()          { return mouseX; }
    public int getMouseY()          { return mouseY; }
}
