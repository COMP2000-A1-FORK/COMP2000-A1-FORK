package com.zombiesim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Manages all cures available during the simulation.
 *
 * The CureManager controls how many cures exist in total, when new cures
 * appear on the map, which cures are currently available to collect, and
 * how many cures have already been used.
 *
 * The total number of cures is fixed at the start of the simulation.
 * Once every cure has been spawned, no additional cures will appear.
 */
public class CureManager {

    /**
     * Maximum number of cures that can exist during the entire simulation.
     */
    private final int totalSupply;

    /**
     * Number of cures that have been created and placed on the map.
     */
    private int spawnedCount = 0;

    /**
     * Number of cures that have been collected and consumed.
     */
    private int usedCount = 0;

    /**
     * Stores cures that have been spawned but have not yet been collected.
     */
    private final List<Cure> activeCures = new ArrayList<>();

    /**
     * Random number generator used to choose where cures spawn.
     */
    private final Random rng;

    /**
     * Amount of time that must pass between cure spawns.
     */
    private final double spawnInterval;

    /**
     * Counts down until the next cure can spawn.
     */
    private double spawnCooldown;

    /**
     * Creates a CureManager with a fixed supply of cures.
     *
     * @param totalSupply total number of cures available for the simulation
     * @param spawnIntervalSeconds time between cure spawns, in seconds
     * @param rng random number generator used for selecting spawn locations
     */
    public CureManager(int totalSupply, double spawnIntervalSeconds, Random rng) {
        this.totalSupply = totalSupply;
        this.spawnInterval = spawnIntervalSeconds;

        // Start the cooldown at the spawn interval so the first cure
        // appears only after the specified amount of time has passed.
        this.spawnCooldown = spawnIntervalSeconds;

        this.rng = rng;
    }

    /**
     * Returns the number of cures that have not yet been used.
     *
     * This includes both:
     * - cures that have not spawned yet
     * - cures currently sitting on the map
     * - cures being carried by humans
     *
     * Once a cure is consumed, usedCount increases and the remaining
     * supply decreases.
     *
     * @return number of cures still available
     */
    public int getRemaining() {
        return totalSupply - usedCount;
    }

    /**
     * Returns the list of cures currently sitting on the map.
     *
     * Cures are removed from this list when a human picks them up.
     *
     * @return list of currently active cures
     */
    public List<Cure> getActiveCures() {
        return activeCures;
    }

    /**
     * Updates the cure spawning system.
     *
     * The cooldown decreases according to the amount of simulation time
     * that has passed. When the cooldown reaches zero, a new cure is
     * created at a random land position.
     *
     * No new cures are created once the total supply has been spawned.
     *
     * @param grid the simulation grid used to select a valid spawn location
     * @param deltaSeconds amount of simulation time that has passed
     */
    public void update(Grid grid, double deltaSeconds) {

        // Stop spawning once every cure in the total supply has been created.
        if (spawnedCount >= totalSupply) return;

        // Reduce the time remaining before the next cure can spawn.
        spawnCooldown -= deltaSeconds;

        // When the cooldown reaches zero, create a new cure.
        if (spawnCooldown <= 0) {

            // Reset the cooldown for the next cure.
            spawnCooldown = spawnInterval;

            // Place the new cure at a randomly selected land position.
            activeCures.add(
                new Cure(grid.randomLandPosition(rng))
            );

            // Keep track of how many cures have been spawned.
            spawnedCount++;
        }
    }

    /**
     * Attempts to give a cure to a human occupying the same tile.
     *
     * A human can only carry one cure at a time. The method searches through
     * all active cures and, if one is found on the human's current position,
     * removes it from the map and gives it to the human.
     *
     * @param human the human attempting to collect a cure
     */
    public void tryPickup(Human human) {

        // Humans can only carry one cure, so do nothing if they already have one.
        if (human.isCarryingCure()) return;

        // Use an Iterator so the cure can safely be removed from the list
        // while the list is being searched.
        Iterator<Cure> it = activeCures.iterator();

        while (it.hasNext()) {
            Cure c = it.next();

            // Check whether this cure is on the same tile as the human.
            if (!c.pickedUp && c.position.equals(human.getPosition())) {

                // Mark the cure as collected.
                c.pickedUp = true;

                // Remove the cure from the map's active cure list.
                it.remove();

                // Give the cure to the human so they can use it later.
                human.giveCure();

                // Stop searching because the human can only collect one cure.
                return;
            }
        }
    }

    /**
     * Records that a cure has been consumed.
     *
     * A cure is consumed when a human uses it, either to cure themselves
     * or to cure a zombie.
     */
    public void consumeCure() {
        usedCount++;
    }
}