package com.r3m.spaceshooter.system;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** * Manages the loading and caching of image assets used by the game. * * <p>The {@code AssetManager} follows the Singleton pattern, ensuring that * only one instance of the manager is used throughout the application. * Loaded images are stored in an internal cache so that an image does not * need to be loaded from the resource files multiple times.</p> * * <p>Images are loaded from the application's classpath using the resource * path supplied to {@link #loadImage(String)}.</p> * * @author R3M * @version 1.0 */public class AssetManager {
    private static AssetManager instance;
    private final Map<String, BufferedImage> images = new HashMap<>();

    /** Creates an independent image cache. Prefer {@link #getInstance()}. */
    public AssetManager() {}

    /** * Creates a new {@code AssetManager}. * * <p>This constructor is public, although the class is intended to be * accessed through the {@link #getInstance()} method.</p> *//** * Creates a new {@code AssetManager}. * * <p>This constructor is public, although the class is intended to be * accessed through the {@link #getInstance()} method.</p> */
    /**
     * Returns the process-wide asset manager, creating it on first use.
     *
     * @return shared asset manager
     */
    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    /** * Loads an image from the specified resource path. * * <p>If the image has already been loaded, the cached image is returned * instead of loading it again. This reduces unnecessary resource access * and improves performance when the same image is requested multiple * times.</p> * * <p>If the image cannot be loaded because an I/O error occurs, an error * message is printed to the standard error stream and {@code null} is * returned.</p> * * @param path the classpath resource path of the image to load * @return the loaded {@link BufferedImage}, or {@code null} if the image * could not be loaded */
    /**
     * Loads and caches an image from a classpath resource.
     *
     * @param path absolute classpath resource path
     * @return loaded image, or {@code null} when loading fails
     */
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
