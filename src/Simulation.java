import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Owns every entity, building and safe zone, and advances the whole
 * world by one tick at a time. The Swing panel only reads from this
 * class to paint, and forwards user actions (play/pause/cure) into it.
 */
public class Simulation {

    public static final int WIDTH = 900;
    public static final int HEIGHT = 700;
    private static final int CELL_SIZE = 20;

    private static final int TICKS_PER_PHASE = 500;
    private static final double VISIBILITY_RADIUS = 110;
    private static final double CURE_RADIUS = 26;
    private static final double NIGHT_SPEED_FACTOR = 1.35;

    private final List<Human> humans = new ArrayList<>();
    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Building> buildings = new ArrayList<>();
    private final List<SafeZone> safeZones = new ArrayList<>();
    private final Pathfinder pathfinder;
    private final Random rand = new Random();

    private boolean running = false;
    private double speedMultiplier = 1.0;
    private int tick = 0;
    private boolean day = false;
    private int cycleNumber = 1;

    private int curesAttempted = 0;
    private int curesSucceeded = 0;

    public Simulation() {
        buildings.add(new Building(120, 90, 110, 90));
        buildings.add(new Building(340, 60, 150, 80));
        buildings.add(new Building(620, 140, 100, 130));
        buildings.add(new Building(180, 300, 120, 90));
        buildings.add(new Building(430, 380, 110, 100));
        buildings.add(new Building(60, 430, 90, 130));

        safeZones.add(new SafeZone(700, 480, 160, 140));
        safeZones.add(new SafeZone(40, 40, 100, 90));

        pathfinder = new Pathfinder(WIDTH, HEIGHT, CELL_SIZE, buildings);

        for (int i = 0; i < 42; i++) {
            Point2D.Double p = randomOpenSpot();
            humans.add(new Human(p.x, p.y));
        }
        for (int i = 0; i < 13; i++) {
            Point2D.Double p = randomOpenSpot();
            zombies.add(new Zombie(p.x, p.y));
        }
    }

    private Point2D.Double randomOpenSpot() {
        double x, y;
        int guard = 0;
        do {
            x = 15 + rand.nextDouble() * (WIDTH - 30);
            y = 15 + rand.nextDouble() * (HEIGHT - 30);
            guard++;
        } while (!isWalkable(x, y) && guard < 500);
        return new Point2D.Double(x, y);
    }

    /** Advances the world by one tick. Does nothing while paused. */
    public void step() {
        if (!running) {
            return;
        }

        tick++;
        if (tick >= TICKS_PER_PHASE) {
            tick = 0;
            day = !day;
            if (day) {
                cycleNumber++;
            }
        }

        for (Human h : humans) {
            h.update(this);
        }
        for (Zombie z : zombies) {
            z.update(this);
        }
    }

    // ---- Movement / world queries used by Human and Zombie ----

    public boolean isWalkable(double x, double y) {
        if (x < 0 || y < 0 || x > WIDTH || y > HEIGHT) {
            return false;
        }
        for (Building b : buildings) {
            if (b.contains(x, y)) {
                return false;
            }
        }
        return true;
    }

    public void clampToBounds(Entity e) {
        e.x = Math.max(0, Math.min(WIDTH, e.x));
        e.y = Math.max(0, Math.min(HEIGHT, e.y));
    }

    public Zombie findNearestZombie(double x, double y, double maxDist) {
        Zombie nearest = null;
        double best = maxDist;
        for (Zombie z : zombies) {
            double d = Math.hypot(z.getX() - x, z.getY() - y);
            if (d < best) {
                best = d;
                nearest = z;
            }
        }
        return nearest;
    }

    public Human findNearestHuman(double x, double y, double maxDist) {
        Human nearest = null;
        double best = maxDist;
        for (Human h : humans) {
            double d = Math.hypot(h.getX() - x, h.getY() - y);
            if (d < best) {
                best = d;
                nearest = h;
            }
        }
        return nearest;
    }

    public SafeZone findNearestSafeZone(double x, double y) {
        SafeZone nearest = null;
        double best = Double.MAX_VALUE;
        for (SafeZone s : safeZones) {
            double d = Math.hypot(s.centerX() - x, s.centerY() - y);
            if (d < best) {
                best = d;
                nearest = s;
            }
        }
        return nearest;
    }

    // ---- Cure tool ----

    /** Attempts to cure a zombie near (clickX, clickY) if a human is adjacent to it. */
    public boolean attemptCureAt(double clickX, double clickY) {
        Zombie clicked = null;
        for (Zombie z : zombies) {
            if (Math.hypot(z.getX() - clickX, z.getY() - clickY) <= z.getRadius() + 4) {
                clicked = z;
                break;
            }
        }
        if (clicked == null) {
            return false;
        }

        curesAttempted++;
        Human adjacentHuman = findNearestHuman(clicked.getX(), clicked.getY(), CURE_RADIUS);
        if (adjacentHuman == null) {
            return false;
        }

        zombies.remove(clicked);
        humans.add(new Human(clicked.getX(), clicked.getY()));
        curesSucceeded++;
        return true;
    }

    // ---- Controls ----

    public void togglePlay() {
        running = !running;
    }

    public boolean isRunning() {
        return running;
    }

    public void cycleSpeed() {
        if (speedMultiplier == 1.0) {
            speedMultiplier = 2.0;
        } else if (speedMultiplier == 2.0) {
            speedMultiplier = 4.0;
        } else {
            speedMultiplier = 1.0;
        }
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    // ---- Day / night ----

    public boolean isDay() {
        return day;
    }

    public double getNightSpeedFactor() {
        return day ? 0.0 : NIGHT_SPEED_FACTOR;
    }

    public int getCycleNumber() {
        return cycleNumber;
    }

    public String getClockLabel() {
        double fraction = tick / (double) TICKS_PER_PHASE;
        int hour = day
                ? 6 + (int) (fraction * 12)
                : (18 + (int) (fraction * 12)) % 24;
        return String.format("%02d:00", hour);
    }

    public String getPhaseLabel() {
        return (day ? "Day" : "Night") + " " + cycleNumber
                + (day ? " \u2014 zombies frozen" : " \u2014 zombies active");
    }

    // ---- Accessors for the panel to draw / read stats ----

    public List<Human> getHumans() { return humans; }
    public List<Zombie> getZombies() { return zombies; }
    public List<Building> getBuildings() { return buildings; }
    public List<SafeZone> getSafeZones() { return safeZones; }
    public double getVisibilityRadius() { return VISIBILITY_RADIUS; }
    public Pathfinder getPathfinder() { return pathfinder; }
    public int getCuresAttempted() { return curesAttempted; }
    public int getCuresSucceeded() { return curesSucceeded; }
}
