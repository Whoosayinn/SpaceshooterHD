package com.r3m.spaceshooter.system;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    private static AssetManager instance;
    private final Map<String, BufferedImage> images = new HashMap<>();

    public AssetManager() {}

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    public BufferedImage loadImage(String path) {
        if (images.containsKey(path)) return images.get(path);
        try {
            BufferedImage img = ImageIO.read(getClass().getResourceAsStream(path));
            images.put(path, img);
            return img;
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
            return null;
        }
    }
}

// NOTES: apply polymorphism to create player spaceship and enemy spaceship
// the difference would be user created spaceship is controlled by player
// enemy spaceship spawns randomly throughout the y axis and only move linearly
// or fly straight

// shooting mechanism of enemy, always check for y axis, if player flew on enemy
// y-axis enemy shoots, otherwise give enemy 1-4 out of 10 chance to shoot randomly
// always check for x axis aswell, if it passes 0 or - 10 remove the space ship