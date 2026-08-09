package com.r3m.spaceshooter.system;

import java.awt.image.BufferedImage;

/**
 * SpriteSheet slices a single PNG containing multiple sprites
 * into an array of individual BufferedImage frames.
 * 
 * Instead of loading 25 separate PNG files, we load one sheet
 * and mathematically extract each sprite by its grid position.
 */
public class SpriteSheet {

    // the full spritesheet image loaded from disk
    private final BufferedImage sheet;

    // width and height of each individual sprite in pixels
    private final int spriteWidth;
    private final int spriteHeight;

    // total number of columns and rows in the grid
    private final int columns;
    private final int rows;

    // pre-extracted array of all individual sprite frames
    // index 0 = sprite 1, index 24 = sprite 25
    private final BufferedImage[] frames;

    /**
     * Constructor — loads the sheet and pre-slices all frames
     * @param sheet     the full spritesheet BufferedImage
     * @param columns   how many sprites wide the sheet is (5 in your case)
     * @param rows      how many sprites tall the sheet is (5 in your case)
     */
    public SpriteSheet(BufferedImage sheet, int columns, int rows) {
        this.sheet = sheet;
        this.columns = columns;
        this.rows = rows;

        // calculate individual sprite dimensions by dividing sheet size
        // assumes all sprites are equal size and evenly distributed
        this.spriteWidth  = sheet.getWidth()  / columns;
        this.spriteHeight = sheet.getHeight() / rows;

        // pre-slice all frames at startup — never slice during render()
        // doing it here means we only do this math once, not 60x per second
        this.frames = new BufferedImage[columns * rows];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                // calculate pixel position of this sprite in the sheet
                int x = col * spriteWidth;
                int y = row * spriteHeight;

                // extract the sub-image at that position
                // index formula: row * columns + col
                // e.g. row=1, col=2 → index 7 → sprite 8
                frames[row * columns + col] = sheet.getSubimage(
                    x, y, spriteWidth, spriteHeight
                );
            }
        }
    }

    /**
     * Returns a specific frame by index (0-based)
     * @param index 0 = first sprite, 24 = last sprite
     */
    public BufferedImage getFrame(int index) {
        // clamp index to valid range — prevents ArrayIndexOutOfBoundsException
        int clamped = Math.max(0, Math.min(index, frames.length - 1));
        return frames[clamped];
    }

    public int getTotalFrames() { return frames.length; }
    public int getSpriteWidth()  { return spriteWidth; }
    public int getSpriteHeight() { return spriteHeight; }
}