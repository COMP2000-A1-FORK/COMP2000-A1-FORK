package com.zombiesim;

import java.util.List;
import java.util.Random;

public class Zombie extends Agent {

    public enum State { FROZEN, WANDER, CHASE }

    private State state = State.FROZEN;
    private final int visionRadius;
    private final double wanderSpeed;   // tiles per second
    private final double chaseSpeed;    // tiles per second
    private final Random rng;
    private double repathCooldown = 0;

    public Zombie(Position start, int visionRadius, double wanderSpeed, double chaseSpeed, Random rng) {
        super(start);
        this.visionRadius = visionRadius;
        this.wanderSpeed = wanderSpeed;
        this.chaseSpeed = chaseSpeed;
        this.rng = rng;
    }

    public State getState() { return state; }

    public void update(Grid grid, boolean isNight, List<Human> humans, double deltaSeconds) {
        if (!alive) return;

        if (!isNight) {
            // Completely frozen during the day: no movement, no hunting.
            state = State.FROZEN;
            path = null;
            return;
        }

        Human target = findNearestVisibleHuman(humans);
        state = (target != null) ? State.CHASE : State.WANDER;
        repathCooldown -= deltaSeconds;

        if (state == State.CHASE) {
            if (!hasPath() || repathCooldown <= 0) {
                setPath(AStar.findPath(grid, position, target.getPosition(), true));
                repathCooldown = 0.4;
            }
            advanceAlongPath(chaseSpeed, deltaSeconds);
        } else {
            if (!hasPath()) {
                Position dest = randomWanderTarget(grid);
                if (dest != null) setPath(AStar.findPath(grid, position, dest, true));
            }
            advanceAlongPath(wanderSpeed, deltaSeconds);
        }
    }

    private Human findNearestVisibleHuman(List<Human> humans) {
        Human closest = null;
        int closestDist = Integer.MAX_VALUE;
        for (Human h : humans) {
            if (!h.isAlive()) continue;
            int dist = Math.abs(h.getPosition().row - position.row) + Math.abs(h.getPosition().col - position.col);
            if (dist <= visionRadius && dist < closestDist) {
                closestDist = dist;
                closest = h;
            }
        }
        return closest;
    }

    private Position randomWanderTarget(Grid grid) {
        for (int i = 0; i < 10; i++) {
            Position p = grid.randomLandPosition(rng);
            if (grid.isWalkable(p, true)) return p;
        }
        return null;
    }
}
