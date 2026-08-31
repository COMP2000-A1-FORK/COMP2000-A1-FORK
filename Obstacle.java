// Anything that blocks movement on the grid (currently just Building) implements this
public interface Obstacle {
    boolean occupiesCell(int col, int row);
}