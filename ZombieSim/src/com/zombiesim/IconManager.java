package com.zombiesim;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the six 1000x1000 transparent PNG icons and pre-scales each one once
 * to the actual tile size (rather than resizing every frame). If an icon file
 * isn't present at iconDirectory, get() returns null and the renderer draws a
 * simple fallback shape instead, so the simulation still runs without assets.
 */
public class IconManager {

    public enum IconType { HUMAN, ZOMBIE, CURE, SHELTER, BLOCKED, DEAD }

    private static final Map<IconType, String> FILE_NAMES = new HashMap<>();
    static {
        FILE_NAMES.put(IconType.HUMAN, "Human_Icon.png");
        FILE_NAMES.put(IconType.ZOMBIE, "Zombie_Icon.png");
        FILE_NAMES.put(IconType.CURE, "Syringe_Icon.png");
        FILE_NAMES.put(IconType.SHELTER, "House_Icon.png");
        FILE_NAMES.put(IconType.BLOCKED, "Blocked_Icon.png");
        FILE_NAMES.put(IconType.DEAD, "Dead_Icon.png");
    }

    private final Map<IconType, BufferedImage> rawImages = new HashMap<>();
    private final Map<IconType, Image> scaledCache = new HashMap<>();
    private int cachedTileSize = -1;

    public IconManager(String iconDirectory) {
        for (Map.Entry<IconType, String> e : FILE_NAMES.entrySet()) {
            File f = new File(iconDirectory, e.getValue());
            if (f.exists()) {
                try {
                    rawImages.put(e.getKey(), ImageIO.read(f));
                } catch (IOException ex) {
                    System.err.println("Failed to load icon " + f + ": " + ex.getMessage());
                }
            }
        }
    }

    /** Pre-scales every loaded icon once to tileSize; cheap no-op if the size hasn't changed. */
    public void prescale(int tileSize) {
        if (tileSize == cachedTileSize) return;
        scaledCache.clear();
        for (Map.Entry<IconType, BufferedImage> e : rawImages.entrySet()) {
            Image scaled = e.getValue().getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH);
            BufferedImage buffered = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = buffered.createGraphics();
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();
            scaledCache.put(e.getKey(), buffered);
        }
        cachedTileSize = tileSize;
    }

    /** Returns the pre-scaled icon, or null if it wasn't loaded (caller draws a fallback shape). */
    public Image get(IconType type) {
        return scaledCache.get(type);
    }
}
