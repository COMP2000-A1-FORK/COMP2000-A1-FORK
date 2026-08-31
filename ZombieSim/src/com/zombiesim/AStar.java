package com.zombiesim;

import java.util.*;

/**
 * 4-directional A* (Manhattan distance heuristic) shared by both species.
 * Each call applies the caller's own passability rule (isZombie flag) against
 * the same underlying terrain array.
 */
public class AStar {

    private static final class Node {
        final Position pos;
        final Node parent;
        final int g;
        final int f;

        Node(Position pos, Node parent, int g, int f) {
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }
    }

    /**
     * Finds a 4-directional path from start to goal.
     * Returns an empty list if start == goal, a list of steps (excluding start) if found,
     * or null if no path exists.
     */
    public static List<Position> findPath(Grid grid, Position start, Position goal, boolean isZombie) {
        if (start.equals(goal)) return Collections.emptyList();

        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<Position, Integer> bestG = new HashMap<>();
        Set<Position> closed = new HashSet<>();

        open.add(new Node(start, null, 0, manhattan(start, goal)));
        bestG.put(start, 0);

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (closed.contains(current.pos)) continue;
            if (current.pos.equals(goal)) return reconstruct(current);
            closed.add(current.pos);

            for (Position n : grid.neighbors4(current.pos)) {
                if (!grid.inBounds(n)) continue;
                // A tile is enterable if it's normally walkable for this species, OR it's the
                // goal itself and it's a shelter tile a human is allowed to enter.
                boolean enterable = grid.isWalkable(n, isZombie)
                        || (n.equals(goal) && grid.isShelterEnterable(n, isZombie));
                if (!enterable || closed.contains(n)) continue;

                int tentativeG = current.g + 1;
                if (tentativeG < bestG.getOrDefault(n, Integer.MAX_VALUE)) {
                    bestG.put(n, tentativeG);
                    open.add(new Node(n, current, tentativeG, tentativeG + manhattan(n, goal)));
                }
            }
        }
        return null;
    }

    private static int manhattan(Position a, Position b) {
        return Math.abs(a.row - b.row) + Math.abs(a.col - b.col);
    }

    private static List<Position> reconstruct(Node end) {
        LinkedList<Position> path = new LinkedList<>();
        Node cur = end;
        while (cur.parent != null) {
            path.addFirst(cur.pos);
            cur = cur.parent;
        }
        return path;
    }
}
