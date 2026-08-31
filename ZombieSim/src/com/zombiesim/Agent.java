package com.zombiesim;

import java.util.List;

/** Shared plumbing for anything that occupies a tile and moves along an A* path. */
public abstract class Agent {

    protected Position position;
    protected List<Position> path;
    protected double moveProgress;
    protected boolean alive = true;

    protected Agent(Position start) {
        this.position = start;
    }

    public Position getPosition() { return position; }
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    protected void setPath(List<Position> newPath) {
        this.path = newPath;
        this.moveProgress = 0;
    }

    protected boolean hasPath() { return path != null && !path.isEmpty(); }

    /**
     * Advances along the current path by tilesPerSecond * deltaSeconds of a tile.
     * When a full tile's worth of progress accumulates, the agent snaps onto the
     * next grid cell (this is what keeps movement strictly 4-directional / tile-based).
     * Returns true if the agent moved onto a new tile this call.
     */
    protected boolean advanceAlongPath(double tilesPerSecond, double deltaSeconds) {
        if (!hasPath()) return false;
        moveProgress += tilesPerSecond * deltaSeconds;
        if (moveProgress >= 1.0) {
            moveProgress = 0;
            position = path.remove(0);
            return true;
        }
        return false;
    }
}
