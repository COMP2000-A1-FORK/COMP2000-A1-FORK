package com.zombiesim;

/** A single cure pickup sitting on the map, waiting for a human to reach its tile. */
public class Cure {
    public final Position position;
    public boolean pickedUp = false;

    public Cure(Position position) {
        this.position = position;
    }
}
