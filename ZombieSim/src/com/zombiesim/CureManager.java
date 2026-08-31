package com.zombiesim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Owns the single fixed-total cure supply for the run. Cures spawn at random
 * locations over time; once totalSupply has been spawned and used, no more appear.
 */
public class CureManager {

    private final int totalSupply;
    private int spawnedCount = 0;
    private int usedCount = 0;
    private final List<Cure> activeCures = new ArrayList<>();
    private final Random rng;
    private final double spawnInterval;
    private double spawnCooldown;

    public CureManager(int totalSupply, double spawnIntervalSeconds, Random rng) {
        this.totalSupply = totalSupply;
        this.spawnInterval = spawnIntervalSeconds;
        this.spawnCooldown = spawnIntervalSeconds;
        this.rng = rng;
    }

    /** Cures remaining = never-spawned + currently on the map (i.e. total minus used). */
    public int getRemaining() {
        return totalSupply - usedCount;
    }

    public List<Cure> getActiveCures() { return activeCures; }

    public void update(Grid grid, double deltaSeconds) {
        if (spawnedCount >= totalSupply) return;
        spawnCooldown -= deltaSeconds;
        if (spawnCooldown <= 0) {
            spawnCooldown = spawnInterval;
            activeCures.add(new Cure(grid.randomLandPosition(rng)));
            spawnedCount++;
        }
    }

    /** Called when a human occupies a tile; picks up a cure there if one exists and the human has capacity. */
    public void tryPickup(Human human) {
        if (human.isCarryingCure()) return; // capacity is exactly one cure at a time
        Iterator<Cure> it = activeCures.iterator();
        while (it.hasNext()) {
            Cure c = it.next();
            if (!c.pickedUp && c.position.equals(human.getPosition())) {
                c.pickedUp = true;
                it.remove();
                human.giveCure();
                return;
            }
        }
    }

    /** Called when a carried cure is consumed (self-cure, or curing a zombie). */
    public void consumeCure() {
        usedCount++;
    }
}
