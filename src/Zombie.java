import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Zombies are frozen during the day. At night they wander randomly until
 * a human enters their visibility radius, at which point they use the
 * Pathfinder to chase, navigating around buildings.
 */
public class Zombie extends Entity {

    static final double SPEED = 1.1;

    private final Random rand = new Random();
    private double dirX, dirY;
    private int wanderTimer = 0;
    private int pathRecalcTimer = 0;
    private Point2D.Double cachedStep = null;

    public Zombie(double x, double y) {
        super(x, y);
        pickNewDirection();
    }

    @Override
    public void update(Simulation sim) {
        if (sim.isDay()) {
            return; // day freezes zombies in place
        }

        Human target = sim.findNearestHuman(x, y, sim.getVisibilityRadius());
        double moveX, moveY;

        if (target != null) {
            pathRecalcTimer--;
            if (pathRecalcTimer <= 0 || cachedStep == null) {
                cachedStep = sim.getPathfinder().nextStep(x, y, target.getX(), target.getY());
                pathRecalcTimer = 6;
            }
            Point2D.Double step = (cachedStep != null) ? cachedStep
                    : new Point2D.Double(target.getX(), target.getY());

            moveX = step.x - x;
            moveY = step.y - y;
            double len = Math.hypot(moveX, moveY);
            if (len > 0.001) {
                moveX /= len;
                moveY /= len;
            }
        } else {
            cachedStep = null;
            wanderTimer--;
            if (wanderTimer <= 0) {
                pickNewDirection();
            }
            moveX = dirX;
            moveY = dirY;
        }

        double speed = SPEED * sim.getSpeedMultiplier() * sim.getNightSpeedFactor();
        double newX = x + moveX * speed;
        double newY = y + moveY * speed;

        if (sim.isWalkable(newX, newY)) {
            x = newX;
            y = newY;
        } else {
            pickNewDirection();
        }

        sim.clampToBounds(this);
    }

    private void pickNewDirection() {
        double angle = rand.nextDouble() * Math.PI * 2;
        dirX = Math.cos(angle);
        dirY = Math.sin(angle);
        wanderTimer = 40 + rand.nextInt(60);
    }

    @Override
    public Color getColor() {
        return new Color(124, 179, 66);
    }
}
