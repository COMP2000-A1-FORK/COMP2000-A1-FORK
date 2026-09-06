public class Zombie extends Entity {

    public Zombie(int x, int y) {
        super(x, y, "\uD83E\uDDDF"); // 🧟
    }

    // Overloaded constructor: build a Zombie directly from a Human that just got infected
    public Zombie(Human infectedHuman) {
        super(infectedHuman.getX(), infectedHuman.getY(), "\uD83E\uDDDF");
    }

    @Override
    public void move(int gridWidth, int gridHeight, Entity[] allEntities, boolean[][] blocked) {
        Human target = findNearestHuman(allEntities);

        if (target == null) {
            return; // no humans left to chase
        }

        int dx = Integer.compare(target.getX(), this.gridX); // -1, 0, or 1 towards target
        int dy = Integer.compare(target.getY(), this.gridY);

        int newX = Math.max(0, Math.min(gridWidth - 1, gridX + dx));
        int newY = Math.max(0, Math.min(gridHeight - 1, gridY + dy));

        if (!blocked[newY][newX]) {
            setPosition(newX, newY);
        }
    }

    private Human findNearestHuman(Entity[] allEntities) {
        Human nearest = null;
        int bestDist = Integer.MAX_VALUE;

        for (Entity e : allEntities) {
            if (e instanceof Human) {
                Human h = (Human) e;
                int dist = Math.abs(h.getX() - gridX) + Math.abs(h.getY() - gridY);
                if (dist < bestDist) {
                    bestDist = dist;
                    nearest = h;
                }
            }
        }
        return nearest;
    }
}