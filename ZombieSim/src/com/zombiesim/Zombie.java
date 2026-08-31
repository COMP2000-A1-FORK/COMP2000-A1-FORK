package com.zombiesim;

import java.util.List;
import java.util.Random;

/**
 * Represents a zombie in the simulation.
 *
 * Zombies can be in one of three states:
 * - FROZEN: During the day, zombies do not move or hunt.
 * - WANDER: At night, if no human is nearby, the zombie moves to a random location.
 * - CHASE: At night, if a human is within its vision range, the zombie uses A*
 *          to find a path towards that human.
 *
 * Zombies use the Agent class for their position and movement along an A* path.
 */
public class Zombie extends Agent {

    /** The current behaviour/state of the zombie. */
    public enum State { FROZEN, WANDER, CHASE }

    /** Current state of this zombie. */
    private State state = State.FROZEN;

    /** Maximum Manhattan-distance range at which the zombie can detect a human. */
    private final int visionRadius;

    /** Zombie movement speed while randomly wandering, measured in tiles per second. */
    private final double wanderSpeed;

    /** Zombie movement speed while chasing a human, measured in tiles per second. */
    private final double chaseSpeed;

    /** Random number generator used to select wandering destinations. */
    private final Random rng;

    /**
     * Time remaining before the zombie is allowed to calculate a new path.
     *
     * This prevents A* from being recalculated every single frame while
     * chasing a moving human.
     */
    private double repathCooldown = 0;

    /**
     * Creates a zombie at the given starting position.
     *
     * @param start starting grid position
     * @param visionRadius maximum distance at which humans can be detected
     * @param wanderSpeed movement speed when wandering
     * @param chaseSpeed movement speed when chasing
     * @param rng random number generator used for wandering
     */
    public Zombie(Position start, int visionRadius, double wanderSpeed,
                  double chaseSpeed, Random rng) {
        super(start);
        this.visionRadius = visionRadius;
        this.wanderSpeed = wanderSpeed;
        this.chaseSpeed = chaseSpeed;
        this.rng = rng;
    }

    /** @return the zombie's current behaviour state. */
    public State getState() {
        return state;
    }

    /**
     * Updates the zombie's behaviour for one simulation tick.
     *
     * During the day, the zombie is completely frozen.
     * During the night, it searches for the nearest visible human.
     * If a human is found, it enters CHASE mode and uses A* to follow them.
     * Otherwise, it enters WANDER mode and moves towards a random location.
     *
     * @param grid the simulation grid
     * @param isNight whether the simulation is currently in the night phase
     * @param humans list of humans currently in the simulation
     * @param deltaSeconds amount of simulation time since the previous update
     */
    public void update(Grid grid, boolean isNight,
                       List<Human> humans, double deltaSeconds) {

        // Dead zombies should not perform any further actions.
        if (!alive) return;

        /*
         * Zombies are inactive during the daytime.
         *
         * Their current path is also cleared so that they do not continue
         * moving when the next night begins.
         */
        if (!isNight) {
            state = State.FROZEN;
            path = null;
            return;
        }

        /*
         * It is nighttime, so check whether there is a human close enough
         * for the zombie to see.
         */
        Human target = findNearestVisibleHuman(humans);

        /*
         * If a human is visible, chase them.
         * Otherwise, wander around the map.
         */
        state = (target != null) ? State.CHASE : State.WANDER;

        // Count down until another A* path calculation is allowed.
        repathCooldown -= deltaSeconds;

        if (state == State.CHASE) {

            /*
             * Recalculate the path when:
             * 1. The zombie has no current path, or
             * 2. Enough time has passed since the previous calculation.
             *
             * Recalculating periodically is important because the human
             * may have moved since the zombie calculated its previous path.
             */
            if (!hasPath() || repathCooldown <= 0) {
                setPath(AStar.findPath(
                        grid,
                        position,
                        target.getPosition(),
                        true
                ));

                // Wait 0.4 simulation seconds before recalculating again.
                repathCooldown = 0.4;
            }

            /*
             * Move along the A* path using the zombie's faster chase speed.
             * Agent.advanceAlongPath() handles the actual tile-by-tile movement.
             */
            advanceAlongPath(chaseSpeed, deltaSeconds);

        } else {

            /*
             * WANDER mode:
             * If the zombie has no destination, choose a random walkable
             * location and use A* to find a path there.
             */
            if (!hasPath()) {
                Position dest = randomWanderTarget(grid);

                if (dest != null) {
                    setPath(AStar.findPath(
                            grid,
                            position,
                            dest,
                            true
                    ));
                }
            }

            // Move along the wandering path at the normal zombie speed.
            advanceAlongPath(wanderSpeed, deltaSeconds);
        }
    }

    /**
     * Finds the closest living human within the zombie's vision radius.
     *
     * Distance is calculated using Manhattan distance:
     *
     *     |row difference| + |column difference|
     *
     * This matches the simulation's 4-directional grid movement, where
     * agents can only move up, down, left, or right.
     *
     * @param humans list of humans to search
     * @return the nearest visible living human, or null if none are visible
     */
    private Human findNearestVisibleHuman(List<Human> humans) {

        Human closest = null;
        int closestDist = Integer.MAX_VALUE;

        for (Human h : humans) {

            // Dead humans cannot be detected or chased.
            if (!h.isAlive()) continue;

            // Calculate the human's distance from this zombie.
            int dist = Math.abs(
                    h.getPosition().row - position.row
            ) + Math.abs(
                    h.getPosition().col - position.col
            );

            /*
             * Only humans within the zombie's vision radius can be detected.
             * Keep the closest one if multiple humans are visible.
             */
            if (dist <= visionRadius && dist < closestDist) {
                closestDist = dist;
                closest = h;
            }
        }

        return closest;
    }

    /**
     * Selects a random location on the map for the zombie to wander towards.
     *
     * Several attempts are made in case the randomly selected location
     * is not suitable for a zombie, such as a shelter tile or blocked tile.
     *
     * @param grid the simulation grid
     * @return a walkable destination, or null if none was found
     */
    private Position randomWanderTarget(Grid grid) {

        // Try up to 10 random locations before giving up.
        for (int i = 0; i < 10; i++) {

            Position p = grid.randomLandPosition(rng);

            /*
             * Zombies can only wander onto tiles that are walkable for them.
             * In particular, zombies cannot enter shelter tiles.
             */
            if (grid.isWalkable(p, true)) {
                return p;
            }
        }

        // No suitable random destination was found.
        return null;
    }
}