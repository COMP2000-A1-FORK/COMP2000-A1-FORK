package com.zombiesim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controls the main logic of the zombie simulation.
 *
 * SimulationEngine acts as the central controller for the simulation. It
 * manages the grid, humans, zombies, cures and simulation clock.
 *
 * Each time tick() is called, the engine:
 *
 * 1. Advances the simulation clock.
 * 2. Updates the zombies.
 * 3. Updates the humans.
 * 4. Spawns any new cures.
 * 5. Lets humans pick up cures.
 * 6. Resolves human/zombie contacts.
 * 7. Removes dead agents.
 * 8. Checks whether either side has won.
 */
public class SimulationEngine {

    /**
     * Represents the possible outcomes of the simulation.
     *
     * NONE    = simulation is still running.
     * HUMANS  = all zombies have been eliminated.
     * ZOMBIES = all humans have been eliminated.
     */
    public enum Winner {
        NONE,
        HUMANS,
        ZOMBIES
    }

    /** The grid containing the simulation map and terrain. */
    private final Grid grid;

    /** List containing all human agents currently in the simulation. */
    private final List<Human> humans = new ArrayList<>();

    /** List containing all zombie agents currently in the simulation. */
    private final List<Zombie> zombies = new ArrayList<>();

    /** Controls spawning and consumption of cures. */
    private final CureManager cureManager;

    /** Controls simulation time, day/night and simulation speed. */
    private final SimClock clock;

    /** Random number generator used throughout the simulation. */
    private final Random rng;

    /** Stores the current winner. NONE means the simulation is still running. */
    private Winner winner = Winner.NONE;

    /*
     * ---------------------------------------------------------
     * HUMAN BEHAVIOUR SETTINGS
     * ---------------------------------------------------------
     *
     * These values control how humans behave.
     *
     * Keeping them as constants makes the simulation easier to tune without
     * having to change the Human class itself.
     */

    /** Sleep meter lost while a human is idle. */
    private static final double DRAIN_IDLE = 0.4;

    /** Sleep meter lost while a human is walking normally. */
    private static final double DRAIN_WALK = 1.2;

    /** Sleep meter lost while a human is fleeing from a zombie. */
    private static final double DRAIN_FLEE = 2.5;

    /** Sleep level below which a human will try to find a shelter. */
    private static final double LOW_SLEEP_THRESHOLD = 30.0;

    /** Normal human movement speed in tiles per second. */
    private static final double HUMAN_WANDER_SPEED = 1.6;

    /** Human movement speed while fleeing or chasing. */
    private static final double HUMAN_FLEE_SPEED = 2.6;

    /*
     * ---------------------------------------------------------
     * ZOMBIE BEHAVIOUR SETTINGS
     * ---------------------------------------------------------
     */

    /** Number of tiles a zombie can see when searching for humans. */
    private static final int ZOMBIE_VISION_RADIUS = 5;

    /** Zombie movement speed while wandering. */
    private static final double ZOMBIE_WANDER_SPEED = 1.0;

    /** Zombie movement speed while chasing a human. */
    private static final double ZOMBIE_CHASE_SPEED = 2.0;

    /**
     * Creates and initialises the simulation.
     *
     * The starting humans and zombies are placed at random LAND positions.
     * The clock and cure manager are also created here.
     *
     * @param grid the grid used by the simulation
     * @param startingHumans number of humans to create initially
     * @param startingZombies number of zombies to create initially
     * @param totalCures total number of cures available during the simulation
     * @param rng random number generator used for initial placement and behaviour
     */
    public SimulationEngine(
            Grid grid,
            int startingHumans,
            int startingZombies,
            int totalCures,
            Random rng) {

        // Store references to the grid and random number generator.
        this.grid = grid;
        this.rng = rng;

        /*
         * Create the simulation clock.
         *
         * Each cycle contains:
         * 60 simulation seconds of daytime.
         * 45 simulation seconds of nighttime.
         */
        this.clock = new SimClock(60, 45);

        /*
         * Create the cure manager.
         *
         * A new cure can spawn every 4 simulation seconds until the
         * total cure supply has been reached.
         */
        this.cureManager = new CureManager(
                totalCures,
                4.0,
                rng
        );

        /*
         * Create the starting human population.
         *
         * Each human receives a random LAND position on the grid.
         */
        for (int i = 0; i < startingHumans; i++) {
            humans.add(
                    newHuman(
                            grid.randomLandPosition(rng)
                    )
            );
        }

        /*
         * Create the starting zombie population.
         *
         * Each zombie receives a random LAND position and the configured
         * vision and movement settings.
         */
        for (int i = 0; i < startingZombies; i++) {
            zombies.add(
                    new Zombie(
                            grid.randomLandPosition(rng),
                            ZOMBIE_VISION_RADIUS,
                            ZOMBIE_WANDER_SPEED,
                            ZOMBIE_CHASE_SPEED,
                            rng
                    )
            );
        }
    }

    /**
     * Creates a new Human using the standard human behaviour settings.
     *
     * This method is also used when a cured zombie is converted back
     * into a human, ensuring that all humans use the same behaviour settings.
     *
     * @param pos starting position for the new human
     * @return newly created Human
     */
    private Human newHuman(Position pos) {
        return new Human(
                pos,
                DRAIN_IDLE,
                DRAIN_WALK,
                DRAIN_FLEE,
                LOW_SLEEP_THRESHOLD,
                HUMAN_WANDER_SPEED,
                HUMAN_FLEE_SPEED,
                rng
        );
    }

    /*
     * ---------------------------------------------------------
     * GETTERS
     * ---------------------------------------------------------
     *
     * These methods allow other parts of the program, such as the GUI,
     * to access the current simulation data without directly modifying
     * the engine's internal fields.
     */

    /** @return the simulation grid */
    public Grid getGrid() {
        return grid;
    }

    /** @return list of humans currently in the simulation */
    public List<Human> getHumans() {
        return humans;
    }

    /** @return list of zombies currently in the simulation */
    public List<Zombie> getZombies() {
        return zombies;
    }

    /** @return the cure manager */
    public CureManager getCureManager() {
        return cureManager;
    }

    /** @return the simulation clock */
    public SimClock getClock() {
        return clock;
    }

    /** @return the current winner */
    public Winner getWinner() {
        return winner;
    }

    /**
     * Pauses or resumes the simulation.
     */
    public void togglePaused() {
        clock.togglePaused();
    }

    /**
     * Changes the simulation speed to the next available speed.
     */
    public void cycleSpeed() {
        clock.cycleSpeed();
    }

    /**
     * Performs one update of the simulation.
     *
     * realDeltaSeconds represents how much real-world time has passed since
     * the previous update. The clock converts this into simulation time based
     * on the current speed multiplier.
     *
     * @param realDeltaSeconds real-world time since the previous update
     */
    public void tick(double realDeltaSeconds) {

        /*
         * Once a winner has been determined, stop updating the simulation.
         */
        if (winner != Winner.NONE) return;

        /*
         * Advance the simulation clock.
         *
         * If the simulation is paused, tick() returns 0.
         */
        double simDelta = clock.tick(realDeltaSeconds);

        // Nothing else should update if no simulation time has passed.
        if (simDelta <= 0) return;

        // Check whether the simulation is currently in the night phase.
        boolean isNight = clock.isNight();

        /*
         * ---------------------------------------------------------
         * UPDATE ZOMBIES
         * ---------------------------------------------------------
         *
         * Each living zombie receives the current grid, day/night state,
         * human list and elapsed simulation time.
         */
        for (Zombie z : zombies) {
            if (z.isAlive()) {
                z.update(
                        grid,
                        isNight,
                        humans,
                        simDelta
                );
            }
        }

        /*
         * ---------------------------------------------------------
         * UPDATE HUMANS
         * ---------------------------------------------------------
         *
         * Each living human receives the current grid, day/night state,
         * zombie list, cure manager and elapsed simulation time.
         */
        for (Human h : humans) {
            if (h.isAlive()) {
                h.update(
                        grid,
                        isNight,
                        zombies,
                        cureManager,
                        simDelta
                );
            }
        }

        /*
         * ---------------------------------------------------------
         * CURE SYSTEM
         * ---------------------------------------------------------
         *
         * First allow the CureManager to spawn new cures.
         */
        cureManager.update(grid, simDelta);

        /*
         * Then check whether any humans are standing on a cure.
         *
         * Humans can only carry one cure at a time.
         */
        for (Human h : humans) {
            if (h.isAlive()) {
                cureManager.tryPickup(h);
            }
        }

        /*
         * Check whether humans and zombies have reached the same tile.
         *
         * This handles infection, curing and zombie/human interactions.
         */
        resolveContacts();

        /*
         * Remove dead agents from their lists.
         *
         * removeIf() checks every object and removes it when isAlive()
         * returns false.
         */
        zombies.removeIf(z -> !z.isAlive());
        humans.removeIf(h -> !h.isAlive());

        /*
         * Finally, check whether the removal of dead agents has caused
         * either side to completely disappear.
         */
        checkWinCondition();
    }

    /**
     * Handles interactions when a human and zombie occupy the same tile.
     *
     * The outcome depends on whether the human is carrying a cure and
     * whether the zombie was actively chasing the human.
     */
    private void resolveContacts() {

        /*
         * Create temporary copies of the lists while processing contacts.
         *
         * This allows humans/zombies to be added or marked as dead without
         * causing problems with the loops currently processing the lists.
         */
        for (Zombie z : new ArrayList<>(zombies)) {

            // Ignore zombies that have already died during this update.
            if (!z.isAlive()) continue;

            for (Human h : new ArrayList<>(humans)) {

                /*
                 * A contact only occurs if:
                 *
                 * 1. The human is alive.
                 * 2. Both agents occupy exactly the same grid position.
                 */
                if (!h.isAlive()
                        || !h.getPosition().equals(z.getPosition())) {
                    continue;
                }

                /*
                 * ---------------------------------------------------------
                 * HUMAN HAS A CURE
                 * ---------------------------------------------------------
                 */
                if (h.isCarryingCure()) {

                    /*
                     * If the zombie was actively chasing the human and
                     * reached the human first, the human uses the cure
                     * defensively.
                     */
                    if (z.getState() == Zombie.State.CHASE) {

                        // Remove the cure from the human's inventory.
                        h.useCure();

                        // Count the cure as used from the total supply.
                        cureManager.consumeCure();

                    } else {

                        /*
                         * Otherwise the human was actively hunting the zombie,
                         * so the zombie is cured and becomes a new human.
                         */
                        cureZombie(z, h);
                    }

                } else {

                    /*
                     * ---------------------------------------------------------
                     * HUMAN HAS NO CURE
                     * ---------------------------------------------------------
                     *
                     * Without a cure, the human is immediately killed
                     * when a zombie reaches the same tile.
                     */
                    h.setAlive(false);

                    // Leave a visual marker showing where the human died.
                    grid.markDead(h.getPosition());

                    /*
                     * If the human died inside a shelter, free that shelter
                     * so another human can use it.
                     */
                    if (h.getState() == Human.State.SLEEPING) {
                        grid.vacateShelter(h.getPosition());
                    }
                }
            }
        }
    }

    /**
     * Converts a zombie back into a human using a cure.
     *
     * The zombie is removed from the simulation and a new human is created
     * at the same grid position.
     *
     * @param z zombie being cured
     * @param curer human using the cure
     */
    private void cureZombie(Zombie z, Human curer) {

        // Mark the zombie as dead so it will be removed from the zombie list.
        z.setAlive(false);

        // Remove one cure from the total available supply.
        cureManager.consumeCure();

        // Remove the cure from the human's inventory.
        curer.useCure();

        /*
         * Create a new human at the exact position where the zombie
         * was cured.
         */
        humans.add(
                newHuman(z.getPosition())
        );
    }

    /**
     * Determines whether the simulation has reached an end condition.
     *
     * Humans win when no living zombies remain.
     * Zombies win when no living humans remain.
     */
    private void checkWinCondition() {

        /*
         * Count how many humans are still alive.
         */
        long aliveHumans = humans.stream()
                .filter(Human::isAlive)
                .count();

        /*
         * Count how many zombies are still alive.
         */
        long aliveZombies = zombies.stream()
                .filter(Zombie::isAlive)
                .count();

        /*
         * If there are no humans remaining, the zombies win.
         */
        if (aliveHumans == 0) {
            winner = Winner.ZOMBIES;

        /*
         * Otherwise, if there are no zombies remaining, the humans win.
         */
        } else if (aliveZombies == 0) {
            winner = Winner.HUMANS;
        }
    }
}