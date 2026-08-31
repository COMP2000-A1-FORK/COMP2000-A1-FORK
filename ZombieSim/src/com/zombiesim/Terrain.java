package com.zombiesim;

/**
 * Represents the type of terrain occupying a single tile on the simulation grid.
 *
 * The terrain type determines whether humans and zombies are allowed to
 * move through or occupy a particular tile.
 */
public enum Terrain {

    /**
     * Normal open ground.
     *
     * Both humans and zombies can walk through and occupy LAND tiles.
     */
    LAND,

    /**
     * A shelter tile where a human can rest and recover their sleep meter.
     *
     * Only humans can enter a shelter. Zombies treat shelter tiles as
     * impassable walls. Each shelter tile can hold a maximum of one human.
     */
    SHELTER,

    /**
     * An obstacle that cannot be entered by either humans or zombies.
     *
     * The A* pathfinding algorithm treats BLOCKED tiles as walls and
     * automatically finds a route around them when possible.
     */
    BLOCKED
}