package com.zombiesim;

/** The three terrain values backing every tile in the grid. */
public enum Terrain {
    /** Walkable by everyone. */
    LAND,
    /** Single-tile footprint, 1 occupant max, humans-only. Zombies treat it as a wall. */
    SHELTER,
    /** Impassable for humans and zombies alike; agents must route around it. */
    BLOCKED
}
