package com.zombiesim;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the images used to represent objects in the simulation.
 *
 * The manager loads each icon from the assets folder and keeps the original
 * image in memory. Before the map is rendered, the icons are resized to the
 * current tile size and stored in a cache. This avoids repeatedly resizing
 * large images every frame, which improves rendering performance.
 *
 * If an icon cannot be loaded, get() returns null. The renderer can then use
 * its own fallback drawing instead, allowing the simulation to continue
 * running even when an image asset is missing.
 */
public class IconManager {

    /**
     * Identifies the different types of icons used by the simulation.
     */
    public enum IconType {
        HUMAN,
        ZOMBIE,
        CURE,
        SHELTER,
        BLOCKED,
        DEAD
    }

    /**
     * Associates each icon type with the filename of its image asset.
     *
     * This provides one central place to define which file represents
     * each type of object in the simulation.
     */
    private static final Map<IconType, String> FILE_NAMES = new HashMap<>();

    static {
        FILE_NAMES.put(IconType.HUMAN, "Human_Icon.png");
        FILE_NAMES.put(IconType.ZOMBIE, "Zombie_Icon.png");
        FILE_NAMES.put(IconType.CURE, "Syringe_Icon.png");
        FILE_NAMES.put(IconType.SHELTER, "House_Icon.png");
        FILE_NAMES.put(IconType.BLOCKED, "Blocked_Icon.png");
        FILE_NAMES.put(IconType.DEAD, "Dead_Icon.png");
    }

    /**
     * Stores the original loaded images.
     *
     * Keeping the original images means they can be resized again if the
     * tile size changes without having to load the files from disk again.
     */
    private final Map<IconType, BufferedImage> rawImages = new HashMap<>();

    /**
     * Stores resized versions of the icons ready for drawing.
     *
     * This cache prevents the renderer from having to resize the same
     * image repeatedly.
     */
    private final Map<IconType, Image> scaledCache = new HashMap<>();

    /**
     * Stores the tile size used to create the current scaled images.
     *
     * A value of -1 means that the icons have not been scaled yet.
     */
    private int cachedTileSize = -1;

    /**
     * Loads the icon files from the supplied directory.
     *
     * Each icon type is matched with its filename from FILE_NAMES.
     * If a file exists, ImageIO reads it and stores the image in rawImages.
     *
     * Missing or unreadable files do not stop the simulation. The renderer
     * can instead draw a fallback shape when an icon is unavailable.
     *
     * @param iconDirectory directory containing the icon image files
     */
    public IconManager(String iconDirectory) {

        // Go through every icon type and its corresponding filename.
        for (Map.Entry<IconType, String> e : FILE_NAMES.entrySet()) {

            // Create the path to the image file.
            File f = new File(iconDirectory, e.getValue());

            // Only attempt to load the image if the file exists.
            if (f.exists()) {
                try {

                    // Read the image from the file and store the original.
                    rawImages.put(e.getKey(), ImageIO.read(f));

                } catch (IOException ex) {

                    // Report an error without stopping the simulation.
                    System.err.println(
                            "Failed to load icon " + f + ": " + ex.getMessage()
                    );
                }
            }
        }
    }

    /**
     * Resizes every loaded icon to the specified tile size.
     *
     * The resized images are stored in scaledCache so the renderer can
     * use them directly.
     *
     * If the icons have already been resized to this tile size, the method
     * immediately returns and reuses the existing cached images.
     *
     * @param tileSize width and height of each icon in pixels
     */
    public void prescale(int tileSize) {

        // If the icons are already this size, there is nothing to do.
        if (tileSize == cachedTileSize) return;

        // Remove old scaled images because the tile size has changed.
        scaledCache.clear();

        // Resize every image that was successfully loaded.
        for (Map.Entry<IconType, BufferedImage> e : rawImages.entrySet()) {

            /*
             * Create a smoothly resized version of the original image.
             *
             * SCALE_SMOOTH gives better visual quality than simply
             * stretching the image using a basic scaling method.
             */
            Image scaled = e.getValue().getScaledInstance(
                    tileSize,
                    tileSize,
                    Image.SCALE_SMOOTH
            );

            /*
             * Create a new BufferedImage to store the resized icon.
             *
             * TYPE_INT_ARGB supports transparency, which is important
             * because the PNG icons have transparent backgrounds.
             */
            BufferedImage buffered = new BufferedImage(
                    tileSize,
                    tileSize,
                    BufferedImage.TYPE_INT_ARGB
            );

            // Create a graphics object so the scaled image can be drawn.
            Graphics2D g2 = buffered.createGraphics();

            // Draw the scaled image into the new buffered image.
            g2.drawImage(scaled, 0, 0, null);

            // Release the graphics resources once drawing is finished.
            g2.dispose();

            // Store the resized image in the cache.
            scaledCache.put(e.getKey(), buffered);
        }

        // Remember which tile size the cached images use.
        cachedTileSize = tileSize;
    }

    /**
     * Returns the cached image for a particular icon type.
     *
     * The renderer uses this method when it wants to draw an icon.
     * If the icon was not loaded or has not been scaled yet, null is
     * returned so the renderer can use a fallback shape instead.
     *
     * @param type type of icon to retrieve
     * @return the pre-scaled image, or null if it is unavailable
     */
    public Image get(IconType type) {
        return scaledCache.get(type);
    }
}