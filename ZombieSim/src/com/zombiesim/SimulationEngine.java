package com.zombiesim;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationEngine {

    public enum Winner { NONE, HUMANS, ZOMBIES }

    private final Grid grid;
    private final List<Human> humans = new ArrayList<>();
    private final List<Zombie> zombies = new ArrayList<>();
    private final CureManager cureManager;
    private final SimClock clock;
    private final Random rng;
    private Winner winner = Winner.NONE;

    // Human behavioral tuning, shared by initial spawns and zombies cured back into humans.
    private static final double DRAIN_IDLE = 0.4;
    private static final double DRAIN_WALK = 1.2;
    private static final double DRAIN_FLEE = 2.5;
    private static final double LOW_SLEEP_THRESHOLD = 30.0;
    private static final double HUMAN_WANDER_SPEED = 1.6;
    private static final double HUMAN_FLEE_SPEED = 2.6;
    private static final int ZOMBIE_VISION_RADIUS = 5;
    private static final double ZOMBIE_WANDER_SPEED = 1.0;
    private static final double ZOMBIE_CHASE_SPEED = 2.0;

    public SimulationEngine(Grid grid, int startingHumans, int startingZombies, int totalCures, Random rng) {
        this.grid = grid;
        this.rng = rng;
        this.clock = new SimClock(60, 45); // sim-seconds of day / night per cycle
        this.cureManager = new CureManager(totalCures, 4.0, rng);

        for (int i = 0; i < startingHumans; i++) {
            humans.add(newHuman(grid.randomLandPosition(rng)));
        }
        for (int i = 0; i < startingZombies; i++) {
            zombies.add(new Zombie(grid.randomLandPosition(rng), ZOMBIE_VISION_RADIUS, ZOMBIE_WANDER_SPEED, ZOMBIE_CHASE_SPEED, rng));
        }
    }

    private Human newHuman(Position pos) {
        return new Human(pos, DRAIN_IDLE, DRAIN_WALK, DRAIN_FLEE, LOW_SLEEP_THRESHOLD, HUMAN_WANDER_SPEED, HUMAN_FLEE_SPEED, rng);
    }

    public Grid getGrid() { return grid; }
    public List<Human> getHumans() { return humans; }
    public List<Zombie> getZombies() { return zombies; }
    public CureManager getCureManager() { return cureManager; }
    public SimClock getClock() { return clock; }
    public Winner getWinner() { return winner; }

    public void togglePaused() { clock.togglePaused(); }
    public void cycleSpeed() { clock.cycleSpeed(); }

    public void tick(double realDeltaSeconds) {
        if (winner != Winner.NONE) return;

        double simDelta = clock.tick(realDeltaSeconds);
        if (simDelta <= 0) return;

        boolean isNight = clock.isNight();

        for (Zombie z : zombies) {
            if (z.isAlive()) z.update(grid, isNight, humans, simDelta);
        }
        for (Human h : humans) {
            if (h.isAlive()) h.update(grid, isNight, zombies, cureManager, simDelta);
        }

        cureManager.update(grid, simDelta);
        for (Human h : humans) {
            if (h.isAlive()) cureManager.tryPickup(h);
        }

        resolveContacts();

        zombies.removeIf(z -> !z.isAlive());
        humans.removeIf(h -> !h.isAlive());

        checkWinCondition();
    }

    private void resolveContacts() {
        for (Zombie z : new ArrayList<>(zombies)) {
            if (!z.isAlive()) continue;
            for (Human h : new ArrayList<>(humans)) {
                if (!h.isAlive() || !h.getPosition().equals(z.getPosition())) continue;

                if (h.isCarryingCure()) {
                    // CHASE means the zombie was actively pursuing and reached the human first;
                    // otherwise the human (in HUNTING_ZOMBIE state) closed the distance itself.
                    if (z.getState() == Zombie.State.CHASE) {
                        h.useCure();
                        cureManager.consumeCure();
                    } else {
                        cureZombie(z, h);
                    }
                } else {
                    // No cure in play: the human always turns, no chance roll, no escape.
                    h.setAlive(false);
                    grid.markDead(h.getPosition());
                    if (h.getState() == Human.State.SLEEPING) {
                        grid.vacateShelter(h.getPosition());
                    }
                }
            }
        }
    }

    private void cureZombie(Zombie z, Human curer) {
        z.setAlive(false);
        cureManager.consumeCure();
        curer.useCure();
        humans.add(newHuman(z.getPosition()));
    }

    private void checkWinCondition() {
        long aliveHumans = humans.stream().filter(Human::isAlive).count();
        long aliveZombies = zombies.stream().filter(Zombie::isAlive).count();
        if (aliveHumans == 0) {
            winner = Winner.ZOMBIES;
        } else if (aliveZombies == 0) {
            winner = Winner.HUMANS;
        }
    }
}
