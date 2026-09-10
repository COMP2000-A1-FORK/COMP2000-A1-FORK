
public interface MovementStrategy {
    void move(Human self, int gridWidth, int gridHeight, Entity[] allEntitys, boolean[][] blocked);
}