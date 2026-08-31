package com.zombiesim;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Human extends Agent {

    public enum State { WANDER, FLEE, SEEK_SHELTER, SLEEPING, HUNTING_ZOMBIE }

    private static final double SLEEP_DURATION_SIM_SECONDS = 5.0;

    private double sleepMeter = 100.0;
    private final double drainIdle;
    private final double drainWalk;
    private final double drainFlee;
    private final double lowSleepThreshold;
    private final double wanderSpeed;
    private final double fleeSpeed;
    private final Random rng;

    private State state = State.WANDER;
    private boolean carryingCure = false;
    private Position sleepingAt = null;
    private double sleepTimer = 0;
    private double repathCooldown = 0;

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

    public double getSleepMeter() { return sleepMeter; }
    public State getState() { return state; }
    public boolean isCarryingCure() { return carryingCure; }
    public void giveCure() { carryingCure = true; }
    public void useCure() { carryingCure = false; }

    public void update(Grid grid, boolean isNight, List<Zombie> zombies, CureManager cureManager, double deltaSeconds) {
        if (!alive) return;

        if (state == State.SLEEPING) {
            sleepTimer += deltaSeconds;
            // Full refill happens exactly at the 5 sim-second mark; interpolate visually until then.
            sleepMeter = Math.min(100.0, sleepMeter + (100.0 / SLEEP_DURATION_SIM_SECONDS) * deltaSeconds);
            if (sleepTimer >= SLEEP_DURATION_SIM_SECONDS) {
                sleepMeter = 100.0;
                grid.vacateShelter(sleepingAt);
                sleepingAt = null;
                sleepTimer = 0;
                state = State.WANDER;
                path = null;
            }
            return; // no drain, no movement while sleeping
        }

        Zombie nearestZombie = findNearestVisibleZombie(zombies, isNight);

        if (carryingCure && nearestZombie != null) {
            state = State.HUNTING_ZOMBIE;
        } else if (nearestZombie != null) {
            state = State.FLEE;
        } else if (sleepMeter <= lowSleepThreshold) {
            state = State.SEEK_SHELTER;
        } else if (state != State.SEEK_SHELTER) {
            state = State.WANDER;
        }

        double drainRate;
        double speed;
        switch (state) {
            case FLEE:
                drainRate = drainFlee;
                speed = fleeSpeed;
                break;
            case HUNTING_ZOMBIE:
                drainRate = drainWalk;
                speed = fleeSpeed;
                break;
            case SEEK_SHELTER:
            case WANDER:
                drainRate = drainWalk;
                speed = wanderSpeed;
                break;
            default:
                drainRate = drainIdle;
                speed = 0;
        }

        sleepMeter -= drainRate * deltaSeconds;
        if (sleepMeter <= 0) {
            sleepMeter = 0;
            alive = false;
            grid.markDead(position);
            return;
        }

        repathCooldown -= deltaSeconds;

        switch (state) {
            case FLEE:
                repathFlee(grid, nearestZombie);
                break;
            case HUNTING_ZOMBIE:
                repathToward(grid, nearestZombie.getPosition());
                break;
            case SEEK_SHELTER:
                pursueShelter(grid);
                break;
            case WANDER:
            default:
                wander(grid);
                break;
        }

        boolean movedTile = advanceAlongPath(speed, deltaSeconds);

        if (movedTile && state == State.SEEK_SHELTER) {
            if (grid.getTerrain(position) == Terrain.SHELTER && !grid.isShelterOccupied(position)) {
                grid.occupyShelter(position, this);
                state = State.SLEEPING;
                sleepingAt = position;
                sleepTimer = 0;
                path = null;
            }
        }
    }

    private Zombie findNearestVisibleZombie(List<Zombie> zombies, boolean isNight) {
        int visionRadius = isNight ? 4 : 8; // night visibility is reduced vs. day
        Zombie closest = null;
        int closestDist = Integer.MAX_VALUE;
        for (Zombie z : zombies) {
            if (!z.isAlive()) continue;
            int dist = Math.abs(z.getPosition().row - position.row) + Math.abs(z.getPosition().col - position.col);
            if (dist <= visionRadius && dist < closestDist) {
                closestDist = dist;
                closest = z;
            }
        }
        return closest;
    }

    private void repathToward(Grid grid, Position target) {
        if (!hasPath() || repathCooldown <= 0) {
            setPath(AStar.findPath(grid, position, target, false));
            repathCooldown = 0.3;
        }
    }

    private void repathFlee(Grid grid, Zombie threat) {
        if (!hasPath() || repathCooldown <= 0) {
            Position fleeTarget = computeFleeTarget(grid, threat);
            setPath(AStar.findPath(grid, position, fleeTarget, false));
            repathCooldown = 0.2;
        }
    }

    private Position computeFleeTarget(Grid grid, Zombie threat) {
        int dr = position.row - threat.getPosition().row;
        int dc = position.col - threat.getPosition().col;
        int targetRow = Math.max(0, Math.min(grid.getRows() - 1, position.row + Integer.signum(dr) * 5));
        int targetCol = Math.max(0, Math.min(grid.getCols() - 1, position.col + Integer.signum(dc) * 5));
        Position candidate = new Position(targetRow, targetCol);
        if (grid.isWalkable(candidate, false)) return candidate;
        return grid.randomLandPosition(rng);
    }

    private void pursueShelter(Grid grid) {
        if (hasPath()) return;
        List<Position> shelters = grid.getAllShelterTiles();
        shelters.sort(Comparator.comparingInt(p -> Math.abs(p.row - position.row) + Math.abs(p.col - position.col)));
        for (Position s : shelters) {
            if (grid.isShelterOccupied(s)) continue;
            List<Position> p = AStar.findPath(grid, position, s, false);
            if (p != null) {
                setPath(p);
                return;
            }
        }
        // No reachable, unoccupied shelter right now — wait nearby (no path set) until one opens.
    }

    private void wander(Grid grid) {
        if (!hasPath()) {
            Position dest = grid.randomLandPosition(rng);
            List<Position> p = AStar.findPath(grid, position, dest, false);
            if (p != null) setPath(p);
        }
    }
}
