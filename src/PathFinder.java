import java.util.*;

// A* pathfinding implementation on a grid
// Done by identifying;
// Start cell, goal cell, blocked cells and map size
// Returns the next cells to return to
public class PathFinder {

    // Helper: a cell coordinate with a score, so it can sort by cost
    private static class Node implements Comparable<Node> {
        int x, y;
        int g;      // cost from start to here (steps taken so far)
        int f;      // g + h (estimated total cost)
        Node parent;

        Node(int x, int y, int g, int f, Node parent) {
            this.x = x; this.y = y;
            this.g = g; this.f = f;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f, other.f);
        }
    }

    // Manhattan distance heuristic for grid movement
    // |x1 - x2| + |y1 - y2|
    // Heuristic for 4-diectional grid movement (up, down, left, right)
    private static int heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    // Find the next step in a path from (startX, startY) to (goalX, goalY) on a grid of size gridSize,
    // where blocked[y][x] is true if the cell is blocked.
    public static int[] findNextStep(int startX, int startY, int goalX, int goalY,
                                     boolean[][] blocked, int gridSize) {

        if (startX == goalX && startY == goalY) return null;

        PriorityQueue<Node> open = new PriorityQueue<>();
        int[][] bestG = new int[gridSize][gridSize];
        for (int[] row : bestG) Arrays.fill(row, Integer.MAX_VALUE);

        Node start = new Node(startX, startY, 0, heuristic(startX, startY, goalX, goalY), null);
        open.add(start);
        bestG[startY][startX] = 0;

        int[] dx = { 0, 0, -1, 1 };
        int[] dy = { -1, 1, 0, 0 };

        while (!open.isEmpty()) {
            Node current = open.poll();

            // If the goal is reached, backtrack to find the next step
            if (current.x == goalX && current.y == goalY) {
                Node step = current;
                while (step.parent != null && step.parent.parent != null) {
                    step = step.parent;
                }
                return new int[]{ step.x, step.y };
            }

            for (int i = 0; i < 4; i++) {
                int nx = current.x + dx[i];
                int ny = current.y + dy[i];

                if (nx < 0 || ny < 0 || nx >= gridSize || ny >= gridSize) continue;
                if (blocked[ny][nx]) continue;

                int tentativeG = current.g + 1;
                if (tentativeG < bestG[ny][nx]) {
                    bestG[ny][nx] = tentativeG;
                    int f = tentativeG + heuristic(nx, ny, goalX, goalY);
                    open.add(new Node(nx, ny, tentativeG, f, current));
                }
            }
        }

        return null; // no path found
    }
}