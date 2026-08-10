package com.r3m.spaceshooter.core;

import com.r3m.spaceshooter.entity.PlayerSpaceship;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.image.BufferedImage;

/**
 * MenuRenderer handles all non-gameplay screen rendering:
 * main menu, instructions, controls, and game over screen.
 *
 * Extracted from GameController to keep GameController focused
 * on game logic only — following the Single Responsibility Principle.
 *
 * GameController owns WHAT state to render.
 * MenuRenderer owns HOW to render it.
 */
public class MenuRenderer {

    private static final int PLAYER_WIDTH  = 64;
    private static final int PLAYER_HEIGHT = 56;

    // injected dependencies — MenuRenderer reads these but never modifies them
    private final PlayerSpaceship player;
    private final BufferedImage   asteroidImage;

    // panel dimensions — updated via setViewportSize()
    private int panelWidth;
    private int panelHeight;

    /**
     * Creates a renderer for the supplied preview assets and viewport.
     *
     * @param player player ship used in animated menu previews
     * @param asteroidImage asteroid decoration image; may be {@code null}
     * @param panelWidth viewport width in pixels
     * @param panelHeight viewport height in pixels
     */
    public MenuRenderer(PlayerSpaceship player, BufferedImage asteroidImage,
                        int panelWidth, int panelHeight) {
        this.player       = player;
        this.asteroidImage = asteroidImage;
        this.panelWidth   = panelWidth;
        this.panelHeight  = panelHeight;
    }

    /**
     * Updates the dimensions used to position menu elements.
     *
     * @param width viewport width in pixels
     * @param height viewport height in pixels
     */
    public void setViewportSize(int width, int height) {
        this.panelWidth  = width;
        this.panelHeight = height;
    }

    /**
     * Fills the viewport with the game's space gradient.
     *
     * @param g destination graphics context
     */
    public void renderSpaceBackground(Graphics2D g) {
        Paint oldPaint = g.getPaint();
        g.setPaint(new GradientPaint(
            0, 0, new Color(5, 16, 42),
            0, panelHeight, new Color(0, 2, 12)
        ));
        g.fillRect(0, 0, panelWidth, panelHeight);
        g.setPaint(oldPaint);
    }

    /**
     * Renders the animated main menu and its options.
     *
     * @param g destination graphics context
     * @param animationTime elapsed menu-animation time in seconds
     */
    public void renderMainMenu(Graphics2D g, double animationTime) {
        renderMenuDecorations(g, animationTime);

        String[] asciiTitle = {
            " ____  ____   _    ____ _____     ____  _   _  ___   ___ _____ _____ ____    _   _ ____  ",
            "/ ___||  _ \\ / \\  / ___| ____|   / ___|| | | |/ _ \\ / _ \\_   _| ____|  _ \\  | | | |  _ \\ ",
            "\\___ \\| |_) / _ \\| |   |  _|     \\___ \\| |_| | | | | | | || | |  _| | |_) | | |_| | | | |",
            " ___) |  __/ ___ \\ |___| |___     ___) |  _  | |_| | |_| || | | |___|  _ <  |  _  | |_| |",
            "|____/|_| /_/   \\_\\____|_____|   |____/|_| |_|\\___/ \\___/ |_| |_____|_| \\_\\ |_| |_|____/ "
        };

        int asciiSize = Math.max(8, Math.min(16, panelWidth / 70));
        g.setFont(new Font("Monospaced", Font.BOLD, asciiSize));
        int y = 92;
        for (String line : asciiTitle) {
            drawCenteredGlowString(g, line, y);
            y += asciiSize + 4;
        }

        renderAnimatedMenuShip(g, y + 10, animationTime);

        String[] options = {
            "[1]  START GAME",
            "[2]  INSTRUCTIONS",
            "[3]  VIEW CONTROLS",
            "[4]  EXIT"
        };

        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        int optionY = Math.max(y + 105, panelHeight / 2 + 30);
        for (String option : options) {
            g.setColor(new Color(230, 248, 255));
            drawCenteredString(g, option, optionY);
            optionY += 40;
        }

        int pulse = 175 + (int)(Math.sin(animationTime * 3.0) * 45);
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.setColor(new Color(75, pulse, 240));
        drawCenteredString(g, "PRESS 1 - 4 HERE  OR  TYPE IT IN THE TERMINAL", optionY + 28);
    }

    /**
     * Renders the gameplay instructions page.
     *
     * @param g destination graphics context
     */
    public void renderInstructionsPage(Graphics2D g) {
        renderInformationPage(g, "INSTRUCTIONS", new String[] {
            "Pilot your spaceship through the asteroid field.",
            "Avoid enemies and incoming asteroids.",
            "Destroy asteroid: +10   Destroy enemy: +25",
            "Collect power-up: +20   Take damage: -15"
        });
    }

    /**
     * Renders the keyboard-controls page.
     *
     * @param g destination graphics context
     */
    public void renderControlsPage(Graphics2D g) {
        renderInformationPage(g, "VIEW CONTROLS", new String[] {
            "W  -  MOVE UP",
            "A  -  MOVE LEFT",
            "S  -  MOVE DOWN",
            "D  -  MOVE RIGHT",
            "SPACE  -  SHOOT",
            "ESC  -  EXIT GAME"
        });
    }

    /**
     * Renders the fading game-over overlay and replay options.
     *
     * @param g destination graphics context
     * @param gameOverFade normalized overlay fade from {@code 0.0} to {@code 1.0}
     * @param animationTime elapsed animation time in seconds
     * @param score final score to display
     */
    public void renderGameOverScreen(Graphics2D g, double gameOverFade,
                                     double animationTime, int score) {
        // dark overlay fades in
        Graphics2D overlay = (Graphics2D) g.create();
        float darkness = (float)(0.82 * gameOverFade);
        overlay.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, darkness));
        overlay.setColor(new Color(0, 2, 12));
        overlay.fillRect(0, 0, panelWidth, panelHeight);
        overlay.dispose();

        if (gameOverFade < 0.28) return;

        float contentAlpha = (float) Math.min(1.0, (gameOverFade - 0.28) / 0.45);
        Graphics2D content = (Graphics2D) g.create();
        content.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, contentAlpha
        ));

        int centerX = panelWidth / 2;
        int titleY  = panelHeight / 2 - 95;

        content.setFont(new Font("Monospaced", Font.BOLD, 54));
        String title  = "GAME OVER";
        int    titleX = centerX - content.getFontMetrics().stringWidth(title) / 2;
        content.setColor(new Color(255, 55, 85, 65));
        content.drawString(title, titleX - 3, titleY);
        content.drawString(title, titleX + 3, titleY);
        content.setColor(new Color(255, 225, 230));
        content.drawString(title, titleX, titleY);

        int dividerY = titleY + 28;
        content.setColor(new Color(80, 205, 235, 150));
        content.drawLine(centerX - 190, dividerY, centerX - 24, dividerY);
        content.drawLine(centerX + 24,  dividerY, centerX + 190, dividerY);
        int[] diamondX = {centerX, centerX + 6, centerX, centerX - 6};
        int[] diamondY = {dividerY - 6, dividerY, dividerY + 6, dividerY};
        content.fillPolygon(diamondX, diamondY, 4);

        content.setFont(new Font("Monospaced", Font.BOLD, 17));
        content.setColor(new Color(130, 220, 245));
        drawCenteredString(content, "FINAL SCORE  //  " + score, dividerY + 42);

        content.setFont(new Font("Monospaced", Font.BOLD, 21));
        content.setColor(new Color(235, 248, 255));
        drawCenteredString(content, "[1]  BACK TO MAIN MENU", dividerY + 95);
        drawCenteredString(content, "[2]  REPLAY",            dividerY + 137);

        int pulse = 175 + (int)(Math.sin(animationTime * 3.0) * 45);
        content.setFont(new Font("Monospaced", Font.BOLD, 13));
        content.setColor(new Color(70, pulse, 240));
        drawCenteredString(content, "PRESS 1 OR 2", dividerY + 184);

        content.dispose();
    }

    // --- private helpers ---

    private void renderMenuDecorations(Graphics2D g, double animationTime) {
        renderShootingStars(g, animationTime);

        int margin       = 28;
        int cornerLength = 80;
        g.setColor(new Color(55, 185, 220, 90));
        g.drawLine(margin, margin, margin + cornerLength, margin);
        g.drawLine(margin, margin, margin, margin + 42);
        g.drawLine(panelWidth - margin, margin,
                   panelWidth - margin - cornerLength, margin);
        g.drawLine(panelWidth - margin, margin,
                   panelWidth - margin, margin + 42);
        g.drawLine(margin, panelHeight - margin,
                   margin + cornerLength, panelHeight - margin);
        g.drawLine(margin, panelHeight - margin,
                   margin, panelHeight - margin - 42);
        g.drawLine(panelWidth - margin, panelHeight - margin,
                   panelWidth - margin - cornerLength, panelHeight - margin);
        g.drawLine(panelWidth - margin, panelHeight - margin,
                   panelWidth - margin, panelHeight - margin - 42);

        if (asteroidImage == null) return;

        int size = 88;
        renderRotatingAsteroid(g, 55, panelHeight / 2 - size / 2,
                               size, animationTime * 0.14);
        renderRotatingAsteroid(g, panelWidth - 55 - size, panelHeight / 2 - size / 2,
                               size, -animationTime * 0.16);
    }

    private void renderRotatingAsteroid(Graphics2D g, int x, int y,
                                        int size, double angle) {
        Graphics2D decoration = (Graphics2D) g.create();
        decoration.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 0.42f
        ));
        decoration.rotate(angle, x + size / 2.0, y + size / 2.0);
        decoration.drawImage(asteroidImage, x, y, size, size, null);
        decoration.dispose();
    }

    private void renderShootingStars(Graphics2D g, double animationTime) {
        int travelWidth = panelWidth + 260;
        for (int i = 0; i < 3; i++) {
            double travel = animationTime * (135 + i * 35) + i * panelWidth * 0.38;
            int x         = (int)(travel % travelWidth) - 130;
            int y         = 65 + i * Math.max(80, panelHeight / 5);
            int tailLength = 48 + i * 15;

            g.setColor(new Color(55, 190, 245, 35));
            g.drawLine(x - tailLength - 22, y + 11, x, y);
            g.setColor(new Color(115, 230, 255, 115));
            g.drawLine(x - tailLength, y + 8, x, y);
            g.setColor(new Color(235, 255, 255, 210));
            g.fillRect(x - 1, y - 1, 3, 3);
        }
    }

    private void renderAnimatedMenuShip(Graphics2D g, int y, double animationTime) {
        int width  = 82;
        int height = width * PLAYER_HEIGHT / PLAYER_WIDTH;
        int drift  = (int)(Math.sin(animationTime * 1.4) * 24);
        int x      = (panelWidth - width) / 2 + drift;
        int shipY  = y + (int)(Math.sin(animationTime * 2.2) * 5);
        int engineY    = shipY + height / 2;
        int trailLength = 34 + (int)((Math.sin(animationTime * 7.0) + 1.0) * 8);

        g.setColor(new Color(40, 150, 255, 55));
        g.drawLine(x - trailLength - 24, engineY, x - 5, engineY);
        g.setColor(new Color(70, 220, 255, 155));
        g.drawLine(x - trailLength, engineY, x - 5, engineY);
        g.setColor(new Color(175, 245, 255, 120));
        g.drawLine(x - trailLength / 2, engineY - 4, x - 5, engineY - 4);
        g.drawLine(x - trailLength / 2, engineY + 4, x - 5, engineY + 4);

        int chevronPulse = 120 + (int)((Math.sin(animationTime * 3.0) + 1.0) * 45);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(new Color(60, chevronPulse, 235, 150));
        g.drawString("<<", x - 76,          engineY + 6);
        g.drawString(">>", x + width + 42,  engineY + 6);

        player.renderAt(g, x, shipY, width, height);
    }

    private void renderInformationPage(Graphics2D g, String heading, String[] lines) {
        g.setFont(new Font("Monospaced", Font.BOLD, 34));
        g.setColor(new Color(115, 225, 255));
        drawCenteredString(g, "+=== " + heading + " ===+", panelHeight / 3);

        g.setFont(new Font("Monospaced", Font.PLAIN, 17));
        int y = panelHeight / 3 + 70;
        for (String line : lines) {
            g.setColor(new Color(225, 245, 255));
            drawCenteredString(g, line, y);
            y += 34;
        }

        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.setColor(new Color(90, 205, 235));
        drawCenteredString(
            g, "PRESS 0 HERE  OR  ENTER IN THE TERMINAL TO GO BACK", y + 45
        );
    }

    // --- utility methods ---
    // kept here instead of a separate RenderUtils because they
    // depend on panelWidth which is already a field here

    private void drawCenteredString(Graphics2D g, String text, int baseline) {
        int x = (panelWidth - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, baseline);
    }

    private void drawCenteredGlowString(Graphics2D g, String text, int baseline) {
        int x = (panelWidth - g.getFontMetrics().stringWidth(text)) / 2;
        g.setColor(new Color(20, 175, 255, 42));
        g.drawString(text, x - 2, baseline);
        g.drawString(text, x + 2, baseline);
        g.setColor(new Color(115, 225, 255));
        g.drawString(text, x, baseline);
    }
}
