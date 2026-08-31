package com.zombiesim;

import java.util.*;

/**
 * Represents the main game map.
 *
 * The Grid stores the terrain for every tile in the simulation and provides
 * methods for checking movement, finding neighbouring tiles, managing shelters,
 * placing dead markers, and generating random land positions.
 *
 * The map is represented as a 2D array where each row and column corresponds
 * to a tile in the simulation.
 */
public class Grid {

    /** Number of rows in the map. */
    private int rows;

    /** Number of columns in the map. */
    private int cols;

    /**
     * Stores the type of terrain at every position on the map.
     * Each position contains a Terrain value such as LAND, BLOCKED, or SHELTER.
     */
    private Terrain[][] terrain;

    /**
     * Stores visual markers showing where humans have died.
     *
     * These markers are separate from the terrain and therefore do not
     * affect movement or A* pathfinding.
     */
    private boolean[][] deadMarkers;

    /**
     * Keeps track of which human is currently occupying each shelter tile.
     *
     * The Position is used as the key and the Human occupying that shelter
     * is stored as the value.
     */
    private final Map<Position, Human> shelterOccupants = new HashMap<>();

    /**
     * Creates a new grid with the specified number of rows and columns.
     *
     * All tiles initially start as LAND, meaning they are walkable.
     *
     * @param rows number of rows in the map
     * @param cols number of columns in the map
     */
    public Grid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;

        // Create the terrain and dead-marker arrays to match the map dimensions.
        this.terrain = new Terrain[rows][cols];
        this.deadMarkers = new boolean[rows][cols];

        // Fill every terrain tile with LAND by default.
        for (Terrain[] r : terrain) {
            Arrays.fill(r, Terrain.LAND);
        }

        // Make sure the backing arrays match the configured dimensions.
        validateDimensions();
    }

    /**
     * Checks that the terrain array has the same dimensions as the configured
     * number of rows and columns.
     *
     * If the dimensions do not match, the grid is resized so that the arrays
     * remain consistent with rows and cols.
     */
    public final void validateDimensions() {
        if (terrain.length != rows || (rows > 0 && terrain[0].length != cols)) {
            resize(rows, cols);
        }
    }

    /**
     * Changes the size of the map.
     *
     * Existing terrain and dead-marker information is preserved wherever
     * the old and new grids overlap. Any newly created tiles are initialised
     * as LAND.
     *
     * @param newRows new number of rows
     * @param newCols new number of columns
     */
    public void resize(int newRows, int newCols) {

        // Create new arrays using the requested dimensions.
        Terrain[][] newTerrain = new Terrain[newRows][newCols];
        boolean[][] newDead = new boolean[newRows][newCols];

        // New terrain tiles are LAND by default.
        for (Terrain[] r : newTerrain) {
            Arrays.fill(r, Terrain.LAND);
        }

        // Work out how much of the old grid can be copied into the new grid.
        int copyRows = Math.min(newRows, terrain.length);
        int copyCols = terrain.length > 0
                ? Math.min(newCols, terrain[0].length)
                : 0;

        // Copy existing terrain and dead-marker information into the new arrays.
        for (int r = 0; r < copyRows; r++) {
            for (int c = 0; c < copyCols; c++) {
                newTerrain[r][c] = terrain[r][c];
                newDead[r][c] = deadMarkers[r][c];
            }
        }

        // Replace the old grid data with the newly sized arrays.
        this.rows = newRows;
        this.cols = newCols;
        this.terrain = newTerrain;
        this.deadMarkers = newDead;
    }

    /** @return the number of rows in the grid. */
    public int getRows() {
        return rows;
    }

    /** @return the number of columns in the grid. */
    public int getCols() {
        return cols;
    }

    /**
     * Checks whether a position exists inside the boundaries of the map.
     *
     * This prevents the program from attempting to access an array position
     * that does not exist.
     *
     * @param p position being checked
     * @return true if the position is inside the grid
     */
    public boolean inBounds(Position p) {
        return p.row >= 0 && p.row < rows
                && p.col >= 0 && p.col < cols;
    }

    /**
     * Gets the terrain type at a specific position.
     *
     * @param p position to check
     * @return terrain type at that position
     */
    public Terrain getTerrain(Position p) {
        return terrain[p.row][p.col];
    }

    /**
     * Changes the terrain type at a specific position.
     *
     * @param p position whose terrain should be changed
     * @param t new terrain type
     */
    public void setTerrain(Position p, Terrain t) {
        terrain[p.row][p.col] = t;
    }

    /**
     * Determines whether an agent is allowed to move onto a tile.
     *
     * Humans can walk on LAND and SHELTER tiles but cannot walk through
     * BLOCKED tiles.
     *
     * Zombies can walk on LAND but treat both BLOCKED and SHELTER tiles
     * as walls.
     *
     * This method is used by the A* algorithm when deciding which tiles
     * can be included in a path.
     *
     * @param p position being checked
     * @param isZombie true when checking movement for a zombie
     * @return true if the tile can normally be entered
     */
    public boolean isWalkable(Position p, boolean isZombie) {

        // Positions outside the map cannot be entered.
        if (!inBounds(p)) return false;

        Terrain t = terrain[p.row][p.col];

        // BLOCKED tiles cannot be entered by either species.
        if (t == Terrain.BLOCKED) return false;

        // Zombies cannot enter shelters.
        if (isZombie && t == Terrain.SHELTER) return false;

        return true;
    }

    /**
     * Checks whether a shelter can be entered as a destination.
     *
     * Shelters are special because humans are allowed to enter them,
     * while zombies are never allowed to enter them.
     *
     * This is mainly used by A* when the shelter is the human's goal.
     *
     * @param p position being checked
     * @param isZombie true if the agent is a zombie
     * @return true if the tile is a shelter that the agent is allowed to enter
     */
    public boolean isShelterEnterable(Position p, boolean isZombie) {
        return !isZombie
                && inBounds(p)
                && terrain[p.row][p.col] == Terrain.SHELTER;
    }

    /**
     * Places a visual marker on a tile where a human has died.
     *
     * The marker is only visual and does not turn the tile into an obstacle.
     */
    public void markDead(Position p) {
        deadMarkers[p.row][p.col] = true;
    }

    /**
     * Checks whether a tile has a dead marker.
     *
     * @param p position being checked
     * @return true if a dead marker exists at that position
     */
    public boolean hasDeadMarker(Position p) {
        return deadMarkers[p.row][p.col];
    }

    /**
     * Checks whether a shelter is currently occupied by a human.
     *
     * @param p shelter position being checked
     * @return true if a human is currently occupying the shelter
     */
    public boolean isShelterOccupied(Position p) {
        return shelterOccupants.containsKey(p);
    }

    /**
     * Records that a human has entered a shelter.
     *
     * @param p position of the shelter
     * @param h human occupying the shelter
     */
    public void occupyShelter(Position p, Human h) {
        shelterOccupants.put(p, h);
    }

    /**
     * Removes the human currently occupying a shelter.
     *
     * @param p position of the shelter being vacated
     */
    public void vacateShelter(Position p) {
        shelterOccupants.remove(p);
    }

    /**
     * Gets the human currently occupying a shelter.
     *
     * @param p position of the shelter
     * @return the occupying human, or null if the shelter is empty
     */
    public Human getShelterOccupant(Position p) {
        return shelterOccupants.get(p);
    }

    /**
     * Finds every shelter tile currently present on the map.
     *
     * This is useful when a human needs to choose a shelter as a destination.
     *
     * @return list containing the positions of all shelter tiles
     */
    public List<Position> getAllShelterTiles() {

        List<Position> list = new ArrayList<>();

        // Check every tile in the grid.
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                // Add the position if it contains a shelter.
                if (terrain[r][c] == Terrain.SHELTER) {
                    list.add(new Position(r, c));
                }
            }
        }

        return list;
    }

    /**
     * Returns the four tiles directly adjacent to a position.
     *
     * Only up, down, left, and right are returned. Diagonal positions
     * are deliberately excluded because the simulation uses 4-directional
     * movement.
     *
     * The positions returned are not automatically checked for being
     * inside the grid. A* performs that check using inBounds().
     *
     * @param p centre position
     * @return list containing the four neighbouring positions
     */
    public List<Position> neighbors4(Position p) {

        List<Position> list = new ArrayList<>(4);

        // Tile above.
        list.add(new Position(p.row - 1, p.col));

        // Tile below.
        list.add(new Position(p.row + 1, p.col));

        // Tile to the left.
        list.add(new Position(p.row, p.col - 1));

        // Tile to the right.
        list.add(new Position(p.row, p.col + 1));

        return list;
    }

    /**
     * Selects a random LAND tile on the map.
     *
     * This is used when spawning objects such as cures so that they
     * appear on valid land rather than inside blocked areas or shelters.
     *
     * The loop has a maximum of 10,000 attempts to prevent it from
     * running forever if there are very few or no LAND tiles available.
     *
     * @param rng random number generator used to select positions
     * @return a randomly selected land position
     */
    public Position randomLandPosition(Random rng) {

        Position p;
        int guard = 0;

        // Keep selecting random positions until a LAND tile is found
        // or the maximum number of attempts has been reached.
        do {
            p = new Position(
                rng.nextInt(rows),
                rng.nextInt(cols)
            );

            guard++;

        } while (
            terrain[p.row][p.col] != Terrain.LAND
            && guard < 10000
        );

        return p;
    }
}