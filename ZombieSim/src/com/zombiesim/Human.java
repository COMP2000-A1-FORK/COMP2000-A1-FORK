package com.zombiesim;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Represents a human agent in the simulation.
 *
 * Humans can move around the map, flee from nearby zombies, hunt zombies
 * when carrying a cure, seek shelters when their sleep level becomes low,
 * and sleep inside shelters to restore their sleep meter.
 *
 * Human movement is controlled using A* pathfinding.
 */
public class Human extends Agent {

    /**
     * The different behaviours a human can currently be performing.
     *
     * WANDER:
     *     Move to random locations when there is no immediate threat.
     *
     * FLEE:
     *     Move away from a nearby zombie.
     *
     * SEEK_SHELTER:
     *     Search for an available shelter when the sleep meter is low.
     *
     * SLEEPING:
     *     Stay inside a shelter and restore the sleep meter.
     *
     * HUNTING_ZOMBIE:
     *     Move towards a nearby zombie when carrying a cure.
     */
    public enum State {
        WANDER,
        FLEE,
        SEEK_SHELTER,
        SLEEPING,
        HUNTING_ZOMBIE
    }

    /**
     * Amount of simulation time required for a sleeping human
     * to completely restore their sleep meter.
     */
    private static final double SLEEP_DURATION_SIM_SECONDS = 5.0;

    /** Current sleep level, ranging from 0 to 100. */
    private double sleepMeter = 100.0;

    /** Rate at which sleep decreases while idle. */
    private final double drainIdle;

    /** Rate at which sleep decreases while normally walking. */
    private final double drainWalk;

    /** Rate at which sleep decreases while fleeing. */
    private final double drainFlee;

    /**
     * Sleep level below which the human will attempt to find a shelter.
     */
    private final double lowSleepThreshold;

    /** Normal movement speed of the human. */
    private final double wanderSpeed;

    /** Movement speed used while fleeing or hunting. */
    private final double fleeSpeed;

    /** Random number generator used when choosing destinations. */
    private final Random rng;

    /** The human's current behaviour/state. */
    private State state = State.WANDER;

    /** Whether the human currently has a cure. */
    private boolean carryingCure = false;

    /** The shelter tile where the human is currently sleeping. */
    private Position sleepingAt = null;

    /** Amount of time the human has spent sleeping. */
    private double sleepTimer = 0;

    /**
     * Prevents the human from calculating a new A* path every single
     * simulation update.
     */
    private double repathCooldown = 0;

    /**
     * Creates a human at the specified starting position.
     *
     * @param start starting position of the human
     * @param drainIdle sleep drain while idle
     * @param drainWalk sleep drain while walking
     * @param drainFlee sleep drain while fleeing
     * @param lowSleepThreshold sleep level that causes the human to seek shelter
     * @param wanderSpeed normal movement speed
     * @param fleeSpeed movement speed while fleeing/hunting
     * @param rng random number generator
     */
    public Human(Position start, double drainIdle, double drainWalk, double drainFlee,
                 double lowSleepThreshold, double wanderSpeed, double fleeSpeed, Random rng) {
        super(start);

        this.drainIdle = drainIdle;
        this.drainWalk = drainWalk;
        this.drainFlee = drainFlee;
        this.lowSleepThreshold = lowSleepThreshold;
        this.wanderSpeed = wanderSpeed;
        this.fleeSpeed = fleeSpeed;
        this.rng = rng;
    }

    /** @return the human's current sleep level */
    public double getSleepMeter() {
        return sleepMeter;
    }

    /** @return the human's current behaviour/state */
    public State getState() {
        return state;
    }

    /** @return true if the human is currently carrying a cure */
    public boolean isCarryingCure() {
        return carryingCure;
    }

    /**
     * Gives the human a cure.
     */
    public void giveCure() {
        carryingCure = true;
    }

    /**
     * Removes the cure from the human after it has been used.
     */
    public void useCure() {
        carryingCure = false;
    }

    /**
     * Updates the human's behaviour and movement for one simulation step.
     *
     * The update follows this general process:
     *
     * 1. Ignore the human if they are dead.
     * 2. Handle sleeping if currently inside a shelter.
     * 3. Find the nearest visible zombie.
     * 4. Choose the appropriate behaviour/state.
     * 5. Calculate sleep drain and movement speed.
     * 6. Check whether the human has become exhausted.
     * 7. Calculate/recalculate an A* path if necessary.
     * 8. Move along the current path.
     * 9. Enter a shelter if the human reaches one.
     *
     * @param grid simulation map
     * @param isNight whether it is currently night
     * @param zombies list of zombies in the simulation
     * @param cureManager manages the simulation's cures
     * @param deltaSeconds amount of simulation time since the last update
     */
    public void update(Grid grid, boolean isNight, List<Zombie> zombies,
                       CureManager cureManager, double deltaSeconds) {

        // Dead humans no longer perform any actions.
        if (!alive) return;

        /*
         * SLEEPING STATE
         *
         * A sleeping human does not move or lose sleep.
         * Instead, their sleep meter gradually increases until it reaches 100.
         */
        if (state == State.SLEEPING) {

            // Track how long the human has been sleeping.
            sleepTimer += deltaSeconds;

            /*
             * Restore the sleep meter gradually.
             *
             * 100 / 5 means the human restores 20 sleep points
             * per simulation second, reaching 100 after 5 seconds.
             */
            sleepMeter = Math.min(
                    100.0,
                    sleepMeter + (100.0 / SLEEP_DURATION_SIM_SECONDS) * deltaSeconds
            );

            /*
             * Once the full sleeping duration has passed, leave the shelter
             * and return to normal wandering behaviour.
             */
            if (sleepTimer >= SLEEP_DURATION_SIM_SECONDS) {

                sleepMeter = 100.0;

                // The shelter is now available for another human.
                grid.vacateShelter(sleepingAt);

                sleepingAt = null;
                sleepTimer = 0;

                // Return to normal behaviour.
                state = State.WANDER;

                // Remove the old movement path.
                path = null;
            }

            // Sleeping humans do not drain energy or move.
            return;
        }

        /*
         * DETECT ZOMBIES
         *
         * Find the closest living zombie that is within the human's
         * current vision range.
         */
        Zombie nearestZombie = findNearestVisibleZombie(zombies, isNight);

        /*
         * CHOOSE BEHAVIOUR
         *
         * The human's state is determined by priority:
         *
         * 1. If carrying a cure and a zombie is visible -> hunt it.
         * 2. Otherwise, if a zombie is visible -> flee.
         * 3. Otherwise, if sleep is low -> seek shelter.
         * 4. Otherwise -> wander normally.
         */
        if (carryingCure && nearestZombie != null) {
            state = State.HUNTING_ZOMBIE;

        } else if (nearestZombie != null) {
            state = State.FLEE;

        } else if (sleepMeter <= lowSleepThreshold) {
            state = State.SEEK_SHELTER;

        } else if (state != State.SEEK_SHELTER) {
            state = State.WANDER;
        }

        /*
         * Select the appropriate sleep drain rate and movement speed
         * based on the current behaviour.
         */
        double drainRate;
        double speed;

        switch (state) {

            // Fleeing uses the highest drain rate and faster movement.
            case FLEE:
                drainRate = drainFlee;
                speed = fleeSpeed;
                break;

            // Hunting uses normal walking drain but the faster movement speed.
            case HUNTING_ZOMBIE:
                drainRate = drainWalk;
                speed = fleeSpeed;
                break;

            // Normal wandering and shelter-seeking use normal movement.
            case SEEK_SHELTER:
            case WANDER:
                drainRate = drainWalk;
                speed = wanderSpeed;
                break;

            // Fallback for any state where the human is not moving.
            default:
                drainRate = drainIdle;
                speed = 0;
        }

        /*
         * APPLY SLEEP DRAIN
         *
         * Sleep decreases based on the selected drain rate and
         * the amount of simulation time that has passed.
         */
        sleepMeter -= drainRate * deltaSeconds;

        /*
         * If sleep reaches zero, the human becomes exhausted and dies.
         */
        if (sleepMeter <= 0) {

            sleepMeter = 0;
            alive = false;

            // Leave a permanent visual marker on the map.
            grid.markDead(position);

            return;
        }

        /*
         * Reduce the path recalculation cooldown.
         *
         * This prevents A* from being recalculated unnecessarily often.
         */
        repathCooldown -= deltaSeconds;

        /*
         * SELECT MOVEMENT BEHAVIOUR
         *
         * Each state uses a different method to decide where the human
         * should move next.
         */
        switch (state) {

            // Find a position away from the nearby zombie.
            case FLEE:
                repathFlee(grid, nearestZombie);
                break;

            // Use A* to move towards the visible zombie.
            case HUNTING_ZOMBIE:
                repathToward(grid, nearestZombie.getPosition());
                break;

            // Find the closest reachable unoccupied shelter.
            case SEEK_SHELTER:
                pursueShelter(grid);
                break;

            // Choose a random land destination.
            case WANDER:
            default:
                wander(grid);
                break;
        }

        /*
         * MOVE ALONG PATH
         *
         * Agent.advanceAlongPath() handles the actual tile-by-tile movement.
         * The speed determines how quickly progress towards the next tile
         * accumulates.
         */
        boolean movedTile = advanceAlongPath(speed, deltaSeconds);

        /*
         * If the human reaches a shelter while seeking one, check whether
         * the shelter is still available.
         */
        if (movedTile && state == State.SEEK_SHELTER) {

            if (grid.getTerrain(position) == Terrain.SHELTER
                    && !grid.isShelterOccupied(position)) {

                // Reserve the shelter for this human.
                grid.occupyShelter(position, this);

                // Change behaviour to sleeping.
                state = State.SLEEPING;

                // Remember which shelter the human is using.
                sleepingAt = position;

                // Start the sleep timer from zero.
                sleepTimer = 0;

                // The human no longer needs a movement path.
                path = null;
            }
        }
    }

    /**
     * Finds the closest living zombie that the human can currently see.
     *
     * Humans have a larger vision radius during the day and a smaller
     * vision radius at night.
     *
     * Day: 8 tiles
     * Night: 4 tiles
     *
     * Manhattan distance is used because movement is restricted to
     * four directions.
     *
     * @param zombies list of zombies to check
     * @param isNight whether it is currently night
     * @return closest visible living zombie, or null if none are visible
     */
    private Zombie findNearestVisibleZombie(List<Zombie> zombies, boolean isNight) {

        // Vision is reduced at night.
        int visionRadius = isNight ? 4 : 8;

        Zombie closest = null;
        int closestDist = Integer.MAX_VALUE;

        // Check every zombie in the simulation.
        for (Zombie z : zombies) {

            // Dead zombies are ignored.
            if (!z.isAlive()) continue;

            /*
             * Calculate Manhattan distance:
             *
             * |row difference| + |column difference|
             */
            int dist = Math.abs(
                    z.getPosition().row - position.row
            ) + Math.abs(
                    z.getPosition().col - position.col
            );

            // Keep this zombie if it is visible and closer than the previous one.
            if (dist <= visionRadius && dist < closestDist) {
                closestDist = dist;
                closest = z;
            }
        }

        return closest;
    }

    /**
     * Creates an A* path towards a target position.
     *
     * A new path is only calculated when the human does not currently
     * have a path or when the repath cooldown has expired.
     *
     * @param grid simulation map
     * @param target position the human wants to reach
     */
    private void repathToward(Grid grid, Position target) {

        if (!hasPath() || repathCooldown <= 0) {

            // Find the shortest available path to the target.
            setPath(
                    AStar.findPath(grid, position, target, false)
            );

            // Wait 0.3 seconds before recalculating the path again.
            repathCooldown = 0.3;
        }
    }

    /**
     * Calculates a path away from a nearby zombie.
     *
     * Rather than targeting the zombie, the human creates a destination
     * approximately five tiles away in the opposite direction.
     */
    private void repathFlee(Grid grid, Zombie threat) {

        if (!hasPath() || repathCooldown <= 0) {

            // Calculate a position away from the threat.
            Position fleeTarget = computeFleeTarget(grid, threat);

            // Use A* to find a valid route to that position.
            setPath(
                    AStar.findPath(grid, position, fleeTarget, false)
            );

            // Recalculate the fleeing route every 0.2 seconds if necessary.
            repathCooldown = 0.2;
        }
    }

    /**
     * Calculates a target position away from a threatening zombie.
     *
     * The row and column differences determine which direction the zombie
     * is located relative to the human. Integer.signum() converts these
     * differences into -1, 0, or 1, allowing the human to move away from
     * the zombie.
     *
     * @param grid simulation map
     * @param threat nearby zombie
     * @return a suitable position away from the zombie
     */
    private Position computeFleeTarget(Grid grid, Zombie threat) {

        // Difference between the human and zombie positions.
        int dr = position.row - threat.getPosition().row;
        int dc = position.col - threat.getPosition().col;

        /*
         * Move approximately five tiles away from the zombie.
         *
         * Math.max() and Math.min() prevent the target from going
         * outside the grid boundaries.
         */
        int targetRow = Math.max(
                0,
                Math.min(
                        grid.getRows() - 1,
                        position.row + Integer.signum(dr) * 5
                )
        );

        int targetCol = Math.max(
                0,
                Math.min(
                        grid.getCols() - 1,
                        position.col + Integer.signum(dc) * 5
                )
        );

        Position candidate = new Position(targetRow, targetCol);

        /*
         * If the calculated target is walkable, use it.
         * Otherwise, choose another random land position.
         */
        if (grid.isWalkable(candidate, false)) {
            return candidate;
        }

        return grid.randomLandPosition(rng);
    }

    /**
     * Searches for a reachable and unoccupied shelter.
     *
     * Shelters are sorted by Manhattan distance so that closer shelters
     * are attempted first. A* is then used to determine whether each
     * shelter is actually reachable around obstacles.
     */
    private void pursueShelter(Grid grid) {

        // Keep following the existing shelter path if one already exists.
        if (hasPath()) return;

        // Get every shelter tile on the map.
        List<Position> shelters = grid.getAllShelterTiles();

        /*
         * Sort shelters from closest to furthest using Manhattan distance.
         * This means nearby shelters are considered first.
         */
        shelters.sort(
                Comparator.comparingInt(
                        p -> Math.abs(p.row - position.row)
                                + Math.abs(p.col - position.col)
                )
        );

        // Check each shelter starting with the closest.
        for (Position s : shelters) {

            // Skip shelters already occupied by another human.
            if (grid.isShelterOccupied(s)) continue;

            /*
             * Use A* to check whether there is a valid route to the shelter.
             */
            List<Position> p = AStar.findPath(
                    grid,
                    position,
                    s,
                    false
            );

            // If a route exists, use it and stop searching.
            if (p != null) {
                setPath(p);
                return;
            }
        }

        /*
         * If no reachable shelter is available, the human remains where
         * they are until another shelter becomes available.
         */
    }

    /**
     * Gives the human a random destination to wander towards.
     *
     * A* is still used to reach the random destination so the human
     * avoids blocked terrain rather than simply moving in a straight line.
     */
    private void wander(Grid grid) {

        // Only choose a new destination when the current path is finished.
        if (!hasPath()) {

            // Select a random land tile as the destination.
            Position dest = grid.randomLandPosition(rng);

            // Calculate an obstacle-aware path to that destination.
            List<Position> p = AStar.findPath(
                    grid,
                    position,
                    dest,
                    false
            );

            // Use the path if a valid route was found.
            if (p != null) {
                setPath(p);
            }
        }
    }
}