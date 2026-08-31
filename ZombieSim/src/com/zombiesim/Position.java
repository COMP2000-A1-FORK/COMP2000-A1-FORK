package com.zombiesim;

import java.util.Objects;

/**
 * Represents a single coordinate on the simulation grid.
 *
 * A Position stores a row and column rather than a pixel location. This makes
 * it possible for the simulation to work with discrete grid tiles when
 * calculating movement and A* paths.
 *
 * Position is immutable, meaning that once a row and column are created they
 * cannot be changed. If an agent moves, a new Position object can represent
 * its new tile.
 */
public final class Position {

    /** The row of this position in the grid. */
    public final int row;

    /** The column of this position in the grid. */
    public final int col;

    /**
     * Creates a new grid position.
     *
     * @param row row number of the tile
     * @param col column number of the tile
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Checks whether another object represents the same grid coordinate.
     *
     * Two Position objects are considered equal when both their row and
     * column values are the same. This is important because the simulation
     * frequently creates new Position objects for the same grid tile.
     *
     * @param o object to compare with this Position
     * @return true if both positions have the same row and column
     */
    @Override
    public boolean equals(Object o) {

        // If both references point to the exact same object, they are equal.
        if (this == o) return true;

        // An object of another type cannot represent the same Position.
        if (!(o instanceof Position)) return false;

        // Convert the object into a Position so its coordinates can be compared.
        Position p = (Position) o;

        // Positions are equal only when both row and column match.
        return row == p.row && col == p.col;
    }

    /**
     * Generates a hash value based on the row and column.
     *
     * This is required when Position is used in collections such as HashMap
     * and HashSet. It ensures that two equal Position objects produce the
     * same hash value.
     *
     * @return hash value calculated from row and column
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    /**
     * Converts the position into a simple readable format.
     *
     * For example, a position with row 3 and column 7 becomes "(3,7)".
     * This is useful when printing positions for debugging.
     *
     * @return position formatted as (row,col)
     */
    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}