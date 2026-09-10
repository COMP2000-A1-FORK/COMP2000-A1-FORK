import java.util.Random;
public class WanderStrategy implements MovementStrategy {
    private static final Random rand = new Random();
    @Override public void move(Human self, int gridWidth, int gridHeight, Entity[] allEntitys, boolean [][] blocked) {
        int dx = rand.nextInt(3) - 1;
        int dy = rand.nextInt(3) - 1;
        int newX = Math.max(0, Math.min(gridWidth - 1, self.getX() + dx));
        int newY = Math.max(0, Math.min(gridHeight - 1, self.getY() + dy));
        if (!blocked[newY][newX] && !Entity.isTileOccupiedBy(Zombie.class, newX, newY, allEntitys, self) && !Entity.isTileOccupiedBy(Human.class, newX, newY, allEntitys, self)) {
            self.setPosition(newX, newY);
        }
    }
}