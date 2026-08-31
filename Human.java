import java.util.Random;

public class Human extends Entity implements Infectable {

    private boolean infected = false;
    private static final Random rand = new Random();

    public Human(int x, int y) {
        super(x, y, "\uD83E\uDDCD"); // 🧍
    }

    // Overloaded constructor: allow creating a human with a custom emoji
    public Human(int x, int y, String customEmoji) {
        super(x, y, customEmoji);
    }

    @Override
    public void move(int gridWidth, int gridHeight, Entity[] allEntities, boolean[][] blocked) {
        // Humans wander randomly, avoiding buildings
        int dx = rand.nextInt(3) - 1; // -1, 0, or 1
        int dy = rand.nextInt(3) - 1;

        int newX = clamp(gridX + dx, 0, gridWidth - 1);
        int newY = clamp(gridY + dy, 0, gridHeight - 1);

        if (!blocked[newY][newX]) {
            setPosition(newX, newY);
        }
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