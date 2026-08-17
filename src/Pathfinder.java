import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.List;

/**
 * Grid-based breadth-first-search pathfinder. Buildings are rasterized
 * onto a coarse grid as blocked cells; nextStep() returns the next
 * waypoint an entity should move toward to get from one point to another
 * while going around obstacles.
 */
public class Pathfinder {

    private final int cellSize;
    private final int cols, rows;
    private final boolean[][] blocked;

    public Pathfinder(int width, int height, int cellSize, List<Building> buildings) {
        this.cellSize = cellSize;
        this.cols = width / cellSize;
        this.rows = height / cellSize;
        this.blocked = new boolean[cols][rows];

        for (Building b : buildings) {
            int c0 = clampCol(b.x / cellSize);
            int c1 = clampCol((b.x + b.width) / cellSize);
            int r0 = clampRow(b.y / cellSize);
            int r1 = clampRow((b.y + b.height) / cellSize);
            for (int c = c0; c <= c1; c++) {
                for (int r = r0; r <= r1; r++) {
                    blocked[c][r] = true;
                }
            }
        }
    }

    /**
     * Returns the next waypoint (pixel coordinates) to move toward, using
     * BFS on the obstacle grid. Returns null if no path exists.
     */
    public Point2D.Double nextStep(double fromX, double fromY, double toX, double toY) {
        int startC = clampCol((int) (fromX / cellSize));
        int startR = clampRow((int) (fromY / cellSize));
        int goalC = clampCol((int) (toX / cellSize));
        int goalR = clampRow((int) (toY / cellSize));

        if (startC == goalC && startR == goalR) {
            return new Point2D.Double(toX, toY);
        }

        boolean[][] visited = new boolean[cols][rows];
        Point[][] cameFrom = new Point[cols][rows];
        ArrayDeque<Point> queue = new ArrayDeque<>();

        Point start = new Point(startC, startR);
        queue.add(start);
        visited[startC][startR] = true;

        int[] dc = {1, -1, 0, 0, 1, 1, -1, -1};
        int[] dr = {0, 0, 1, -1, 1, -1, 1, -1};

        boolean found = false;

        while (!queue.isEmpty()) {
            Point cur = queue.poll();
            if (cur.x == goalC && cur.y == goalR) {
                found = true;
                break;
            }
            for (int i = 0; i < dc.length; i++) {
                int nc = cur.x + dc[i];
                int nr = cur.y + dr[i];
                if (nc < 0 || nc >= cols || nr < 0 || nr >= rows) continue;
                if (blocked[nc][nr] || visited[nc][nr]) continue;
                visited[nc][nr] = true;
                cameFrom[nc][nr] = cur;
                queue.add(new Point(nc, nr));
            }
        }

        if (!found) {
            return null;
        }

        // Walk the path backward from goal to start, keeping the first
        // step away from start - that's the waypoint we actually move to.
        Point step = new Point(goalC, goalR);
        Point prev = cameFrom[step.x][step.y];
        while (prev != null && !(prev.x == startC && prev.y == startR)) {
            step = prev;
            prev = cameFrom[step.x][step.y];
        }

        double targetX = step.x * cellSize + cellSize / 2.0;
        double targetY = step.y * cellSize + cellSize / 2.0;
        return new Point2D.Double(targetX, targetY);
    }

    private int clampCol(int c) {
        return Math.max(0, Math.min(cols - 1, c));
    }

    private int clampRow(int r) {
        return Math.max(0, Math.min(rows - 1, r));
    }
}
