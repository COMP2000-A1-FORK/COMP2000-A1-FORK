package com.zombiesim;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main visual panel for the zombie simulation.
 *
 * SimulationPanel has two main responsibilities:
 *
 * 1. Running the simulation loop using a Swing Timer.
 * 2. Drawing the current state of the simulation onto the screen.
 *
 * The panel does not contain the actual simulation rules. Those are handled
 * by SimulationEngine. Instead, this class asks the engine for the current
 * state and displays it.
 */
public class SimulationPanel extends JPanel implements ActionListener {

    /** The simulation logic and current state. */
    private final SimulationEngine engine;

    /** Handles loading and displaying the simulation icons. */
    private final IconManager icons;

    /** Swing timer used to repeatedly update and redraw the simulation. */
    private final Timer timer;

    /**
     * Stores the time of the previous frame.
     *
     * System.nanoTime() is used so that movement is based on elapsed time
     * rather than assuming every frame takes exactly the same amount of time.
     */
    private long lastNanoTime;

    /** Current size of one grid tile in pixels. */
    private int tileSize = 28;

    /*
     * ---------------------------------------------------------
     * DISPLAY COLOURS
     * ---------------------------------------------------------
     *
     * These colours are used when drawing the map or when an icon is
     * unavailable and a simple fallback shape needs to be drawn.
     */

    /** Colour used for normal walkable land. */
    private static final Color LAND_COLOR =
            new Color(210, 230, 200);

    /** Colour used for blocked tiles. */
    private static final Color BLOCKED_COLOR =
            new Color(90, 90, 90);

    /** Colour used for shelter tiles. */
    private static final Color SHELTER_FLOOR_COLOR =
            new Color(235, 220, 170);

    /** Transparent dark overlay applied during nighttime. */
    private static final Color NIGHT_TINT =
            new Color(15, 20, 60, 110);

    /** Light grid line drawn around each tile. */
    private static final Color GRID_LINE =
            new Color(0, 0, 0, 25);

    /**
     * Creates the simulation display panel.
     *
     * @param engine simulation engine containing the current game state
     * @param icons manager responsible for loading and displaying icons
     */
    public SimulationPanel(
            SimulationEngine engine,
            IconManager icons) {

        this.engine = engine;
        this.icons = icons;

        /*
         * Set the background colour that will be visible around the grid
         * if the grid does not fill the entire panel.
         */
        setBackground(new Color(245, 245, 245));

        /*
         * Record the current time before the first simulation update.
         *
         * This allows the first frame to calculate how much real time
         * has passed since the panel was created.
         */
        lastNanoTime = System.nanoTime();

        /*
         * Create a Swing Timer that fires approximately every 16 ms.
         *
         * 1000 / 16 ≈ 62.5 updates per second, which is roughly 60 FPS.
         *
         * The timer calls actionPerformed() each time it fires.
         */
        timer = new Timer(16, this);

        /*
         * Start the timer immediately when the panel is created.
         *
         * The simulation itself may still be paused through the clock.
         */
        timer.start();
    }

    /**
     * Called automatically by the Swing Timer approximately every 16 ms.
     *
     * This method calculates how much real time has passed, passes that
     * time to the SimulationEngine, and then requests the panel to repaint.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        /*
         * Get the current high-resolution time.
         */
        long now = System.nanoTime();

        /*
         * Calculate the real-world time that has passed since the previous
         * update.
         *
         * System.nanoTime() returns nanoseconds, so divide by 1,000,000,000
         * to convert the result into seconds.
         */
        double deltaSeconds =
                (now - lastNanoTime) / 1_000_000_000.0;

        /*
         * Store the current time so it can be used as the starting point
         * for the next update.
         */
        lastNanoTime = now;

        /*
         * Prevent an unusually large time jump.
         *
         * For example, if the application freezes for several seconds,
         * allowing the full delay through could cause the simulation to
         * suddenly move a huge distance when it resumes.
         */
        deltaSeconds = Math.min(deltaSeconds, 0.1);

        /*
         * Give the elapsed time to the simulation engine.
         *
         * SimulationEngine then converts real time into simulation time
         * using the current speed multiplier.
         */
        engine.tick(deltaSeconds);

        /*
         * Ask Swing to redraw the panel.
         *
         * Swing will call paintComponent() when it is ready.
         */
        repaint();
    }

    /**
     * Draws the complete simulation.
     *
     * The drawing order is important. Background terrain is drawn first,
     * followed by dead markers, cures, humans and zombies. Effects such as
     * the nighttime overlay and winner banner are drawn last so they appear
     * over the rest of the simulation.
     */
    @Override
    protected void paintComponent(Graphics g) {

        /*
         * Clears the previous frame before drawing the new one.
         */
        super.paintComponent(g);

        /*
         * Convert Graphics into Graphics2D so additional drawing features
         * such as antialiasing can be used.
         */
        Graphics2D g2 = (Graphics2D) g;

        /*
         * Enable antialiasing to make circles, icons and other shapes
         * appear smoother.
         */
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        /*
         * Get the current grid from the simulation engine.
         */
        Grid grid = engine.getGrid();

        /*
         * Recalculate the tile size based on the current panel dimensions.
         *
         * This allows the grid to resize automatically when the window
         * is resized.
         */
        recalcTileSize(grid);

        /*
         * Resize the loaded icons to match the current tile size.
         *
         * IconManager caches these resized versions so they do not have
         * to be resized every frame.
         */
        icons.prescale(tileSize);

        /*
         * Draw the simulation from back to front.
         */
        drawTerrain(g2, grid);
        drawDeadMarkers(g2, grid);
        drawCures(g2);
        drawHumans(g2);
        drawZombies(g2);

        /*
         * During nighttime, place a transparent dark overlay across the
         * entire simulation. This creates the visual impression that
         * visibility is reduced at night.
         */
        if (engine.getClock().isNight()) {
            g2.setColor(NIGHT_TINT);
            g2.fillRect(
                    0,
                    0,
                    getWidth(),
                    getHeight()
            );
        }

        /*
         * If the simulation has ended, display the winner banner on top
         * of everything else.
         */
        if (engine.getWinner() != SimulationEngine.Winner.NONE) {
            drawWinnerBanner(g2);
        }
    }

    /**
     * Calculates how large each grid tile should be.
     *
     * The tile size is based on whichever dimension of the panel is more
     * restrictive. This ensures that the entire grid can fit on screen.
     *
     * @param grid current simulation grid
     */
    private void recalcTileSize(Grid grid) {

        /*
         * Make sure the panel dimensions are at least 1 pixel so division
         * cannot produce an invalid value.
         */
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());

        /*
         * Calculate the largest square tile size that allows the entire
         * grid to fit inside the panel.
         *
         * The minimum of the width-based and height-based sizes is used.
         *
         * The minimum tile size of 6 prevents tiles from becoming
         * impossibly small.
         */
        tileSize = Math.max(
                6,
                Math.min(
                        w / grid.getCols(),
                        h / grid.getRows()
                )
        );
    }

    /**
     * Draws all terrain tiles in the grid.
     *
     * Each tile is converted from grid coordinates (row, column)
     * into screen coordinates (x, y).
     */
    private void drawTerrain(Graphics2D g2, Grid grid) {

        /*
         * Loop through every row of the grid.
         */
        for (int r = 0; r < grid.getRows(); r++) {

            /*
             * Loop through every column in the current row.
             */
            for (int c = 0; c < grid.getCols(); c++) {

                /*
                 * Create a Position representing this tile.
                 */
                Position p = new Position(r, c);

                /*
                 * Find out what type of terrain occupies the tile.
                 */
                Terrain t = grid.getTerrain(p);

                /*
                 * Convert grid coordinates into pixel coordinates.
                 *
                 * Column controls X.
                 * Row controls Y.
                 */
                int x = c * tileSize;
                int y = r * tileSize;

                /*
                 * Choose how to draw the tile based on its terrain type.
                 */
                switch (t) {

                    /*
                     * -----------------------------------------------------
                     * BLOCKED TILE
                     * -----------------------------------------------------
                     */
                    case BLOCKED:

                        // Draw the blocked tile background.
                        g2.setColor(BLOCKED_COLOR);
                        g2.fillRect(
                                x,
                                y,
                                tileSize,
                                tileSize
                        );

                        // Draw the blocked icon, or a fallback square.
                        drawIconOrFallbackSquare(
                                g2,
                                IconManager.IconType.BLOCKED,
                                x,
                                y,
                                BLOCKED_COLOR.darker()
                        );
                        break;

                    /*
                     * -----------------------------------------------------
                     * SHELTER TILE
                     * -----------------------------------------------------
                     */
                    case SHELTER:

                        // Draw the shelter floor.
                        g2.setColor(SHELTER_FLOOR_COLOR);
                        g2.fillRect(
                                x,
                                y,
                                tileSize,
                                tileSize
                        );

                        // Draw the shelter icon, or a fallback square.
                        drawIconOrFallbackSquare(
                                g2,
                                IconManager.IconType.SHELTER,
                                x,
                                y,
                                new Color(150, 100, 40)
                        );
                        break;

                    /*
                     * -----------------------------------------------------
                     * NORMAL LAND
                     * -----------------------------------------------------
                     */
                    case LAND:
                    default:

                        // Draw a normal walkable tile.
                        g2.setColor(LAND_COLOR);
                        g2.fillRect(
                                x,
                                y,
                                tileSize,
                                tileSize
                        );
                }

                /*
                 * Draw a subtle border around every tile so the grid
                 * structure remains visible.
                 */
                g2.setColor(GRID_LINE);
                g2.drawRect(
                        x,
                        y,
                        tileSize,
                        tileSize
                );
            }
        }
    }

    /**
     * Draws markers showing where agents have died.
     *
     * Dead markers are stored separately from terrain, meaning that they
     * are purely visual and do not affect A* pathfinding.
     */
    private void drawDeadMarkers(Graphics2D g2, Grid grid) {

        /*
         * Check every tile in the grid.
         */
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {

                Position p = new Position(r, c);

                /*
                 * Only draw something if this tile has previously been
                 * marked as a death location.
                 */
                if (grid.hasDeadMarker(p)) {

                    /*
                     * Draw the dead icon, or a fallback circle if the
                     * icon could not be loaded.
                     */
                    drawIconOrFallbackCircle(
                            g2,
                            IconManager.IconType.DEAD,
                            c * tileSize,
                            r * tileSize,
                            new Color(60, 60, 60)
                    );
                }
            }
        }
    }

    /**
     * Draws every cure that is currently available on the map.
     */
    private void drawCures(Graphics2D g2) {

        /*
         * Get all currently active cures from the CureManager.
         */
        for (Cure c : engine.getCureManager().getActiveCures()) {

            /*
             * Draw each cure at its grid position.
             */
            drawIconOrFallbackCircle(
                    g2,
                    IconManager.IconType.CURE,
                    c.position.col * tileSize,
                    c.position.row * tileSize,
                    new Color(30, 140, 220)
            );
        }
    }

    /**
     * Draws all living humans.
     *
     * Humans carrying a cure use a different fallback colour so that
     * they can still be visually distinguished if the human icon is missing.
     */
    private void drawHumans(Graphics2D g2) {

        for (Human h : engine.getHumans()) {

            // Dead humans should not be drawn as active agents.
            if (!h.isAlive()) continue;

            /*
             * Convert the human's grid position into screen coordinates.
             */
            int x = h.getPosition().col * tileSize;
            int y = h.getPosition().row * tileSize;

            /*
             * Humans carrying a cure get a green fallback colour.
             * Normal humans use blue.
             */
            Color fallback =
                    h.isCarryingCure()
                            ? new Color(30, 180, 90)
                            : new Color(40, 90, 200);

            /*
             * Draw the human icon or fallback circle.
             */
            drawIconOrFallbackCircle(
                    g2,
                    IconManager.IconType.HUMAN,
                    x,
                    y,
                    fallback
            );

            /*
             * Draw the human's sleep meter in the corner of its tile.
             */
            drawSleepMeter(
                    g2,
                    h,
                    x,
                    y
            );
        }
    }

    /**
     * Draws all living zombies.
     */
    private void drawZombies(Graphics2D g2) {

        for (Zombie z : engine.getZombies()) {

            // Do not draw zombies that are no longer alive.
            if (!z.isAlive()) continue;

            /*
             * Convert the zombie's grid position into screen coordinates.
             */
            int x = z.getPosition().col * tileSize;
            int y = z.getPosition().row * tileSize;

            /*
             * Draw the zombie icon, or a red fallback circle.
             */
            drawIconOrFallbackCircle(
                    g2,
                    IconManager.IconType.ZOMBIE,
                    x,
                    y,
                    new Color(120, 40, 40)
            );
        }
    }

    /**
     * Draws an icon if it was successfully loaded.
     *
     * If the icon is missing, a simple square is drawn instead so that
     * the simulation remains usable even without the image assets.
     */
    private void drawIconOrFallbackSquare(
            Graphics2D g2,
            IconManager.IconType type,
            int x,
            int y,
            Color fallback) {

        /*
         * Ask IconManager whether an image exists for this icon type.
         */
        Image img = icons.get(type);

        if (img != null) {

            // Draw the loaded icon.
            g2.drawImage(
                    img,
                    x,
                    y,
                    null
            );

        } else {

            /*
             * No image was found, so draw a simple square instead.
             */
            g2.setColor(fallback);

            /*
             * Add some padding so the fallback shape does not completely
             * cover the grid tile.
             */
            int pad = Math.max(
                    1,
                    tileSize / 6
            );

            g2.fillRect(
                    x + pad,
                    y + pad,
                    tileSize - 2 * pad,
                    tileSize - 2 * pad
            );
        }
    }

    /**
     * Draws an icon if it was successfully loaded.
     *
     * If the icon is missing, a simple circle is drawn instead.
     */
    private void drawIconOrFallbackCircle(
            Graphics2D g2,
            IconManager.IconType type,
            int x,
            int y,
            Color fallback) {

        /*
         * Try to retrieve the pre-scaled icon.
         */
        Image img = icons.get(type);

        if (img != null) {

            // Draw the loaded image.
            g2.drawImage(
                    img,
                    x,
                    y,
                    null
            );

        } else {

            /*
             * No icon is available, so use a simple coloured circle.
             */
            g2.setColor(fallback);

            /*
             * Leave a small amount of space around the circle.
             */
            int pad = Math.max(
                    1,
                    tileSize / 6
            );

            g2.fillOval(
                    x + pad,
                    y + pad,
                    tileSize - 2 * pad,
                    tileSize - 2 * pad
            );
        }
    }

    /**
     * Draws a circular sleep meter on the corner of a human's tile.
     *
     * The meter represents the human's current sleep level from 0 to 100.
     * A green meter indicates a healthy sleep level, while red indicates
     * that the human is becoming tired.
     */
    private void drawSleepMeter(
            Graphics2D g2,
            Human h,
            int x,
            int y) {

        /*
         * Make the meter roughly one third of the tile size.
         *
         * The minimum of 4 pixels prevents it becoming invisible on
         * very small tiles.
         */
        int barSize = Math.max(
                4,
                tileSize / 3
        );

        /*
         * Position the meter in the top-right corner of the tile.
         */
        int bx = x + tileSize - barSize;
        int by = y;

        /*
         * Convert the sleep meter from 0-100 into a 0.0-1.0 fraction.
         *
         * For example:
         *
         * 100 -> 1.0
         * 50  -> 0.5
         * 0   -> 0.0
         */
        double frac = h.getSleepMeter() / 100.0;

        /*
         * Draw a dark background circle behind the meter.
         */
        g2.setColor(
                new Color(0, 0, 0, 140)
        );

        g2.fillOval(
                bx,
                by,
                barSize,
                barSize
        );

        /*
         * Use green when the sleep meter is above 30%.
         * Use red when it falls below the threshold.
         */
        g2.setColor(
                frac > 0.3
                        ? new Color(60, 200, 90)
                        : new Color(220, 60, 60)
        );

        /*
         * Draw only the portion of the circle representing the remaining
         * sleep percentage.
         */
        g2.fillArc(
                bx,
                by,
                barSize,
                barSize,
                90,
                -(int) (360 * frac)
        );
    }

    /**
     * Draws the final winner message when the simulation ends.
     *
     * A dark transparent banner is placed across the centre of the panel
     * with either "HUMANS WIN" or "ZOMBIES WIN".
     */
    private void drawWinnerBanner(Graphics2D g2) {

        /*
         * Determine which team won.
         */
        String text =
                engine.getWinner() == SimulationEngine.Winner.HUMANS
                        ? "HUMANS WIN"
                        : "ZOMBIES WIN";

        /*
         * Set a large, bold font for the winner message.
         */
        g2.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        36
                )
        );

        /*
         * Get information about the font so the text can be centred.
         */
        FontMetrics fm = g2.getFontMetrics();

        int tw = fm.stringWidth(text);

        /*
         * Calculate the X coordinate required to centre the text.
         */
        int tx = (getWidth() - tw) / 2;

        /*
         * Position the banner roughly in the middle of the panel.
         */
        int ty = getHeight() / 2;

        /*
         * Draw a semi-transparent black background behind the message.
         */
        g2.setColor(
                new Color(0, 0, 0, 170)
        );

        g2.fillRect(
                0,
                ty - 40,
                getWidth(),
                60
        );

        /*
         * Finally, draw the winner text in white.
         */
        g2.setColor(Color.WHITE);

        g2.drawString(
                text,
                tx,
                ty
        );
    }
}