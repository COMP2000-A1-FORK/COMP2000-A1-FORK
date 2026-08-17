import java.awt.Color;
import java.util.Random;

/**
 * Humans wander randomly by default. At night, if a zombie enters their
 * visibility radius, they flee toward the nearest safe zone (or directly
 * away from the threat if no safe zone is known).
 */
public class Human extends Entity {

    static final double SPEED = 1.6;

    private final Random rand = new Random();
    private double dirX, dirY;
    private int wanderTimer = 0;

    public Human(double x, double y) {
        super(x, y);
        pickNewDirection();
    }

    @Override
    public void update(Simulation sim) {
        Zombie threat = sim.findNearestZombie(x, y, sim.getVisibilityRadius());

        double moveX, moveY;

        if (threat != null && !sim.isDay()) {
            SafeZone target = sim.findNearestSafeZone(x, y);
            if (target != null) {
                moveX = target.centerX() - x;
                moveY = target.centerY() - y;
            } else {
                moveX = x - threat.getX();
                moveY = y - threat.getY();
            }
            double len = Math.hypot(moveX, moveY);
            if (len > 0.001) {
                moveX /= len;
                moveY /= len;
            }
        } else {
            wanderTimer--;
            if (wanderTimer <= 0) {
                pickNewDirection();
            }
            moveX = dirX;
            moveY = dirY;
        }

        double speed = SPEED * sim.getSpeedMultiplier();
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
        return new Color(66, 133, 244);
    }
}
