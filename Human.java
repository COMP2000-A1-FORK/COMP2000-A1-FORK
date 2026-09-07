import java.util.Random;

public class Human extends Entity implements Infectable {

    private boolean infected = false;
    private boolean hasCure = false;
    private static final Random rand = new Random();

    public Human(int x, int y) {
        super(x, y, "\uD83E\uDDCD"); // 🧍
    }

    // Overloaded constructor: allow creating a human with a custom emoji
    public Human(int x, int y, String customEmoji) {
        super(x, y, customEmoji);
    }
    public boolean hasCure() {
        return hasCure;
    }

    public void giveCure() {
        this.hasCure = true;
        this.emoji = "\uD83E\uDDD1\u200D\u2695\uFE0F"; //🧑‍⚕️
    }

    public void useCure() {
        this.hasCure = false;
        this.emoji = "\uD83E\uDDCD"; //🧍
    }

    @Override
    public void move(int gridWidth, int gridHeight, Entity[] allEntities, boolean[][] blocked) {
        if (hasCure) {
            Zombie targetZombie = findNearestZombie(allEntities);
            if (targetZombie != null) {
                int dx = Integer.compare(targetZombie.getX(), this.gridX);
                int dy = Integer.compare(targetZombie.getY(), this.gridY);

                int newX = clamp(gridX + dx, 0, gridWidth - 1);
                int newY = clamp(gridY + dy, 0, gridHeight - 1);

                if (!blocked[newY][newX]) {
                    setPosition(newX, newY);
                    return;
                }
            }
        }
        // Humans wander randomly, avoiding buildings
        int dx = rand.nextInt(3) - 1; // -1, 0, or 1
        int dy = rand.nextInt(3) - 1;

        int newX = clamp(gridX + dx, 0, gridWidth - 1);
        int newY = clamp(gridY + dy, 0, gridHeight - 1);

        if (!blocked[newY][newX]) {
            setPosition(newX, newY);
        }
    }
    private Zombie findNearestZombie(Entity[] allEntities) {
        Zombie nearest = null;
        int bestDist = Integer.MAX_VALUE;

        for (Entity e : allEntities) {
            if (e instanceof Zombie) {
                Zombie z = (Zombie) e;
                int dist = Math.abs(z.getX() - gridX) + Math.abs(z.getY() - gridY);
                if (dist < bestDist) {
                    bestDist = dist;
                    nearest = z;
                }
            }
        }
        return nearest;
    }
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public void infect() {
        this.infected = true;
    }

    @Override
    public boolean isInfected() {
        return infected;
    }
}