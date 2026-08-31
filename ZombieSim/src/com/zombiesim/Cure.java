package com.zombiesim;

/**
 * Represents a cure pickup placed on the simulation map.
 *
 * A cure stays at a fixed position until a human reaches its tile
 * and collects it. The pickedUp variable is used to track whether
 * the cure has already been collected.
 */
public class Cure {

    /**
     * The grid position where the cure is located.
     * This is final because the cure does not move after being created.
     */
    public final Position position;

    /**
     * Tracks whether the cure has been collected.
     * New cures start as not collected.
     */
    public boolean pickedUp = false;

    /**
     * Creates a cure at the specified position on the grid.
     *
     * @param position the grid tile where the cure is placed
     */
    public Cure(Position position) {
        this.position = position;
    }
}