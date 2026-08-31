package com.zombiesim;

import java.util.*;

/**
 * The 2D array backing the entire map, plus a separate visual-only dead-marker
 * overlay and shelter occupancy tracking. Rows/cols are adjustable; validateDimensions()
 * is the backend check that guarantees the terrain array always matches the configured size.
 */
public class Grid {

    private int rows;
    private int cols;
    private Terrain[][] terrain;
    private boolean[][] deadMarkers;
    private final Map<Position, Human> shelterOccupants = new HashMap<>();

    public Grid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.terrain = new Terrain[rows][cols];
        this.deadMarkers = new boolean[rows][cols];
        for (Terrain[] r : terrain) Arrays.fill(r, Terrain.LAND);
        validateDimensions();
    }

    /** Backend check: ensures the terrain array always matches the configured rows/cols. */
    public final void validateDimensions() {
        if (terrain.length != rows || (rows > 0 && terrain[0].length != cols)) {
            resize(rows, cols);
        }
    }

    /** Resizes the backing array, preserving any overlapping terrain/dead-marker data. */
    public void resize(int newRows, int newCols) {
        Terrain[][] newTerrain = new Terrain[newRows][newCols];
        boolean[][] newDead = new boolean[newRows][newCols];
        for (Terrain[] r : newTerrain) Arrays.fill(r, Terrain.LAND);

        int copyRows = Math.min(newRows, terrain.length);
        int copyCols = terrain.length > 0 ? Math.min(newCols, terrain[0].length) : 0;
        for (int r = 0; r < copyRows; r++) {
            for (int c = 0; c < copyCols; c++) {
                newTerrain[r][c] = terrain[r][c];
                newDead[r][c] = deadMarkers[r][c];
            }
        }

        this.rows = newRows;
        this.cols = newCols;
        this.terrain = newTerrain;
        this.deadMarkers = newDead;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }

    public boolean inBounds(Position p) {
        return p.row >= 0 && p.row < rows && p.col >= 0 && p.col < cols;
    }

    public Terrain getTerrain(Position p) { return terrain[p.row][p.col]; }
    public void setTerrain(Position p, Terrain t) { terrain[p.row][p.col] = t; }

    /**
     * Pure movement passability, per the species rule:
     * zombies treat shelter AND blocked as walls; humans treat only blocked as a wall.
     */
    public boolean isWalkable(Position p, boolean isZombie) {
        if (!inBounds(p)) return false;
        Terrain t = terrain[p.row][p.col];
        if (t == Terrain.BLOCKED) return false;
        if (isZombie && t == Terrain.SHELTER) return false;
        return true;
    }

    /** Shelter is enterable (as a destination) only by humans, never zombies. */
    public boolean isShelterEnterable(Position p, boolean isZombie) {
        return !isZombie && inBounds(p) && terrain[p.row][p.col] == Terrain.SHELTER;
    }

    /** Places a permanent visual-only dead marker; does not affect pathing. */
    public void markDead(Position p) { deadMarkers[p.row][p.col] = true; }
    public boolean hasDeadMarker(Position p) { return deadMarkers[p.row][p.col]; }

    public boolean isShelterOccupied(Position p) { return shelterOccupants.containsKey(p); }
    public void occupyShelter(Position p, Human h) { shelterOccupants.put(p, h); }
    public void vacateShelter(Position p) { shelterOccupants.remove(p); }
    public Human getShelterOccupant(Position p) { return shelterOccupants.get(p); }

    public List<Position> getAllShelterTiles() {
        List<Position> list = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (terrain[r][c] == Terrain.SHELTER) list.add(new Position(r, c));
            }
        }
        return list;
    }

    public List<Position> neighbors4(Position p) {
        List<Position> list = new ArrayList<>(4);
        list.add(new Position(p.row - 1, p.col));
        list.add(new Position(p.row + 1, p.col));
        list.add(new Position(p.row, p.col - 1));
        list.add(new Position(p.row, p.col + 1));
        return list;
    }

    public Position randomLandPosition(Random rng) {
        Position p;
        int guard = 0;
        do {
            p = new Position(rng.nextInt(rows), rng.nextInt(cols));
            guard++;
        } while (terrain[p.row][p.col] != Terrain.LAND && guard < 10000);
        return p;
    }
}
