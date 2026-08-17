/**
 * A rectangular obstacle that blocks movement and pathfinding.
 */
public class Building {

    final int x, y, width, height;

    public Building(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
