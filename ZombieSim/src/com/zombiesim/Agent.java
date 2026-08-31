package com.zombiesim;
import java.util.List;


// Base algorithm that defines the algorithm thinking for all characters; Human and Zombie.
public abstract class Agent {
    protected Position position;    // Obtian current position.
    protected List<Position> path;  // Unexplored tiles

    protected double moveProgress;  // Tracks how far the agent has moved along its path.

    protected boolean alive = true; // Tracks whether the agent is alive or dead.

    /** Creates an agent at its starting position. */
    protected Agent(Position start) {
        this.position = start;
    }

    /** @return the current position of the agent */
    public Position getPosition() {
        return position;
    }

    /** @return whether the agent is alive or dead */
    public boolean isAlive() {
        return alive;
    }
    
    /** Updates the alive state of the agent. */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    /**
     * Sets the path that the agent will follow.
     * Resets the movement progress so the agent starts
     * moving from the beginning of the new path.
     * @param newPath the list of grid positions the agent will move through
     */
    protected void setPath(List<Position> newPath) {
        this.path = newPath;
        this.moveProgress = 0;
    }

     /**
     * Checks if the agent has a path with tiles remaining to move to.
     * @return true if the agent has a path to follow, otherwise false
     */
    protected boolean hasPath() {
        return path != null && !path.isEmpty();
    }

    /**
     * Moves the agent along its path according to its movement speed
     * and the amount of time that has passed.
     * Movement progress is built up over multiple updates. Once enough
     * progress has been made to move one full tile, the agent moves to
     * the next position in the path.
     * @param tilesPerSecond the agent's movement speed
     * @param deltaSeconds the time passed since the last update
     * @return true if the agent moved to a new tile, otherwise false
     */
    protected boolean advanceAlongPath(double tilesPerSecond, double deltaSeconds) {

        // Cannot move if there is no path to follow.
        if (!hasPath()) {
            return false;
        }

        moveProgress += tilesPerSecond * deltaSeconds;  // Add movement progress based on the agent's speed and elapsed time.

        // Move to the next tile once enough progress has been accumulated.
        if (moveProgress >= 1.0) {
            moveProgress = 0;
            position = path.remove(0);  // Take the next position from the path and move the agent there.

            return true;
        }

        // The agent has not accumulated enough progress to move yet.
        return false;
    }
}