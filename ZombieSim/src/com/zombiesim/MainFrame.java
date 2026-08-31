package com.zombiesim;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Creates and configures the main window for the Zombie Apocalypse Simulation.
 *
 * MainFrame is responsible for setting up the initial simulation environment,
 * creating the simulation engine and graphical components, and arranging those
 * components inside the application window.
 *
 * It does not control the simulation behaviour itself. That responsibility is
 * handled by SimulationEngine and the other simulation classes.
 */
public class MainFrame extends JFrame {

    /*
     * These constants control the initial configuration of the simulation.
     *
     * Keeping these values together makes it easy to change the map size,
     * starting populations, cure supply, and terrain generation settings.
     */
    private static final int ROWS = 20;
    private static final int COLS = 30;
    private static final int STARTING_HUMANS = 25;
    private static final int STARTING_ZOMBIES = 6;
    private static final int TOTAL_CURES = 20;
    private static final int BLOCKED_TILE_COUNT = 25;
    private static final int SHELTER_CLUSTER_COUNT = 6;
    private static final String ICON_DIRECTORY = "resources/icons";

    /**
     * Creates the main application window and sets up the initial simulation.
     */
    public MainFrame() {

        // Set the title displayed at the top of the application window.
        super("Zombie Apocalypse Simulation");

        /*
         * Create a random number generator.
         *
         * The same Random object is passed to the different parts of the
         * simulation so that random terrain and agent positions can be created.
         */
        Random rng = new Random();

        // Create the grid that represents the simulation map.
        Grid grid = new Grid(ROWS, COLS);

        // Add randomly positioned blocked tiles to create obstacles.
        placeRandomTerrain(
                grid,
                Terrain.BLOCKED,
                BLOCKED_TILE_COUNT,
                rng
        );

        // Add shelter tiles after the obstacles have been placed.
        placeShelterClusters(
                grid,
                SHELTER_CLUSTER_COUNT,
                rng
        );

        /*
         * Create the SimulationEngine.
         *
         * The engine is responsible for controlling the actual simulation,
         * including humans, zombies, cures, movement and the simulation clock.
         */
        SimulationEngine engine = new SimulationEngine(
                grid,
                STARTING_HUMANS,
                STARTING_ZOMBIES,
                TOTAL_CURES,
                rng
        );

        /*
         * Load the image assets used by the simulation.
         *
         * These include icons for humans, zombies, cures, shelters,
         * blocked tiles and dead agents.
         */
        IconManager icons = new IconManager(ICON_DIRECTORY);

        /*
         * Create the three main GUI components.
         *
         * SimulationPanel:
         *     Displays the actual simulation map.
         *
         * HUDPanel:
         *     Displays information such as time, population and cures.
         *
         * ControlPanel:
         *     Provides controls such as play/pause and simulation speed.
         */
        SimulationPanel simPanel = new SimulationPanel(engine, icons);
        HUDPanel hud = new HUDPanel(engine);
        ControlPanel controls = new ControlPanel(engine);

        /*
         * Arrange the GUI components using BorderLayout.
         *
         * NORTH  -> control buttons
         * CENTER -> simulation map
         * SOUTH  -> information HUD
         *
         * The CENTER area automatically receives most of the available space.
         */
        setLayout(new BorderLayout());

        add(controls, BorderLayout.NORTH);
        add(simPanel, BorderLayout.CENTER);
        add(hud, BorderLayout.SOUTH);

        // Close the entire application when the window is closed.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the initial size of the application window.
        setSize(1100, 820);

        // Position the window in the centre of the screen.
        setLocationRelativeTo(null);
    }

    /**
     * Randomly places a specified number of terrain tiles on the grid.
     *
     * Only LAND tiles can be replaced. This prevents the method from
     * overwriting terrain that has already been placed.
     *
     * A guard counter is also used to prevent the loop from running forever
     * if there are not enough suitable tiles available.
     *
     * @param grid the map where the terrain should be placed
     * @param terrain the type of terrain to place
     * @param count the number of tiles to place
     * @param rng random number generator used to select positions
     */
    private void placeRandomTerrain(
            Grid grid,
            Terrain terrain,
            int count,
            Random rng) {

        // Number of terrain tiles successfully placed.
        int placed = 0;

        /*
         * Number of attempts made to find suitable positions.
         *
         * This prevents an infinite loop if the map becomes too full
         * to place the requested number of tiles.
         */
        int guard = 0;

        /*
         * Keep selecting random positions until either:
         *
         * 1. The requested number of tiles has been placed, or
         * 2. The maximum number of attempts has been reached.
         */
        while (placed < count && guard < count * 50) {

            // Record another attempt.
            guard++;

            /*
             * Select a random row and column within the grid.
             *
             * nextInt() generates a value from 0 up to, but not including,
             * the supplied maximum.
             */
            Position p = new Position(
                    rng.nextInt(grid.getRows()),
                    rng.nextInt(grid.getCols())
            );

            /*
             * Only place the terrain if the selected tile is currently LAND.
             *
             * This prevents existing terrain from being overwritten.
             */
            if (grid.getTerrain(p) == Terrain.LAND) {

                // Change the tile to the requested terrain type.
                grid.setTerrain(p, terrain);

                // Record that another tile has successfully been placed.
                placed++;
            }
        }
    }

    /**
     * Places shelters across the map.
     *
     * Each shelter cluster starts with one randomly selected shelter tile.
     * There is then a 50% chance of adding one neighbouring shelter tile.
     *
     * This creates small groups of shelters rather than having every shelter
     * appear as a completely isolated tile.
     *
     * @param grid the map where shelters should be placed
     * @param clusterCount number of shelter base positions to create
     * @param rng random number generator used to select positions
     */
    private void placeShelterClusters(
            Grid grid,
            int clusterCount,
            Random rng) {

        // Number of shelter base tiles successfully placed.
        int placed = 0;

        /*
         * Counts how many attempts have been made to find suitable
         * positions for shelter clusters.
         */
        int guard = 0;

        /*
         * Continue creating shelter bases until:
         *
         * - The requested number has been placed, or
         * - The maximum number of attempts has been reached.
         */
        while (placed < clusterCount && guard < clusterCount * 50) {

            // Record another attempt.
            guard++;

            // Select a random position for the shelter base.
            Position base = new Position(
                    rng.nextInt(grid.getRows()),
                    rng.nextInt(grid.getCols())
            );

            /*
             * The shelter can only be placed on an empty LAND tile.
             *
             * If the position already contains blocked terrain or another
             * shelter, skip this attempt and try another position.
             */
            if (grid.getTerrain(base) != Terrain.LAND) {
                continue;
            }

            // Convert the selected LAND tile into a shelter.
            grid.setTerrain(base, Terrain.SHELTER);

            // Record that another shelter base has been placed.
            placed++;

            /*
             * There is a 50% chance of adding a second shelter tile
             * next to the base.
             *
             * nextBoolean() randomly returns either true or false.
             */
            if (rng.nextBoolean()) {

                /*
                 * Get the four tiles directly next to the base:
                 *
                 *        UP
                 *         ↑
                 * LEFT ← BASE → RIGHT
                 *         ↓
                 *       DOWN
                 */
                for (Position n : grid.neighbors4(base)) {

                    /*
                     * The neighbouring position must:
                     *
                     * 1. Be inside the grid.
                     * 2. Still be LAND.
                     *
                     * This prevents the new shelter from being placed
                     * outside the map or over existing terrain.
                     */
                    if (grid.inBounds(n)
                            && grid.getTerrain(n) == Terrain.LAND) {

                        // Convert the neighbouring tile into a shelter.
                        grid.setTerrain(n, Terrain.SHELTER);

                        /*
                         * Stop after placing one neighbour.
                         *
                         * This means each cluster can contain the base
                         * shelter plus at most one additional shelter.
                         */
                        break;
                    }
                }
            }
        }
    }
}