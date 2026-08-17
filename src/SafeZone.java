/**
 * A rectangular area humans flee toward. Zombies cannot enter.
 */
public class SafeZone {

    final int x, y, width, height;

    public SafeZone(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    double centerX() {
        return x + width / 2.0;
    }

    double centerY() {
        return y + height / 2.0;
    }
}
