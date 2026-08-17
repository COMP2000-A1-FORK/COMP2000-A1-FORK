import java.awt.Color;

/**
 * Common superclass for anything that lives on the simulation grid.
 * Holds position + radius, and defines the contract every living
 * entity must fulfil: update itself each tick, and report its draw color.
 */
public abstract class Entity {

    protected double x, y;
    protected final int radius = 7;

    protected Entity(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getRadius() { return radius; }

    /** Advance this entity's state by one simulation tick. */
    public abstract void update(Simulation sim);

    /** Color used to draw this entity on the canvas. */
    public abstract Color getColor();
}
