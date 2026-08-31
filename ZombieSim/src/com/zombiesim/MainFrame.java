package com.zombiesim;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MainFrame extends JFrame {

    // --- Configurable setup variables ---
    private static final int ROWS = 20;
    private static final int COLS = 30;
    private static final int STARTING_HUMANS = 25;
    private static final int STARTING_ZOMBIES = 6;
    private static final int TOTAL_CURES = 20;
    private static final int BLOCKED_TILE_COUNT = 25;
    private static final int SHELTER_CLUSTER_COUNT = 6;
    private static final String ICON_DIRECTORY = "resources/icons";

    public MainFrame() {
        super("Zombie Apocalypse Simulation");

        Random rng = new Random();
        Grid grid = new Grid(ROWS, COLS);
        placeRandomTerrain(grid, Terrain.BLOCKED, BLOCKED_TILE_COUNT, rng);
        placeShelterClusters(grid, SHELTER_CLUSTER_COUNT, rng);

        SimulationEngine engine = new SimulationEngine(grid, STARTING_HUMANS, STARTING_ZOMBIES, TOTAL_CURES, rng);
        IconManager icons = new IconManager(ICON_DIRECTORY);

        SimulationPanel simPanel = new SimulationPanel(engine, icons);
        HUDPanel hud = new HUDPanel(engine);
        ControlPanel controls = new ControlPanel(engine);

        setLayout(new BorderLayout());
        add(controls, BorderLayout.NORTH);
        add(simPanel, BorderLayout.CENTER);
        add(hud, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 820);
        setLocationRelativeTo(null);
    }

    private void placeRandomTerrain(Grid grid, Terrain terrain, int count, Random rng) {
        int placed = 0, guard = 0;
        while (placed < count && guard < count * 50) {
            guard++;
            Position p = new Position(rng.nextInt(grid.getRows()), rng.nextInt(grid.getCols()));
            if (grid.getTerrain(p) == Terrain.LAND) {
                grid.setTerrain(p, terrain);
                placed++;
            }
        }
    }

    /** Places shelters individually, occasionally attaching a neighbor to form a small cluster. */
    private void placeShelterClusters(Grid grid, int clusterCount, Random rng) {
        int placed = 0, guard = 0;
        while (placed < clusterCount && guard < clusterCount * 50) {
            guard++;
            Position base = new Position(rng.nextInt(grid.getRows()), rng.nextInt(grid.getCols()));
            if (grid.getTerrain(base) != Terrain.LAND) continue;

            grid.setTerrain(base, Terrain.SHELTER);
            placed++;

            if (rng.nextBoolean()) {
                for (Position n : grid.neighbors4(base)) {
                    if (grid.inBounds(n) && grid.getTerrain(n) == Terrain.LAND) {
                        grid.setTerrain(n, Terrain.SHELTER);
                        break;
                    }
                }
            }
        }
    }
}
