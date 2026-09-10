public class FleeStrategy implements MovementStrategy { 
    @Override public void move(Human self, int gridWidth, int gridHeight, Entity[] allEntities, boolean[][] blocked) {
        Zombie nearestZombie = Entity.findNearest(allEntities, Zombie.class, self.getX(), self.getY());
        if (nearestZombie == null) return; 
        int dx = -Integer.compare(nearestZombie.getX(), self.getX()); 
        int dy = -Integer.compare(nearestZombie.getY(), self.getY()); 
        int newX = Math.max(0, Math.min(gridWidth - 1, self.getX() + dx)); 
        int newY = Math.max(0, Math.min(gridHeight - 1, self.getY() + dy)); 
        if (!blocked[newY][newX] && !Entity.isTileOccupiedBy(Human.class, newX, newY, allEntities, self)) {
            self.setPosition(newX, newY); 
        }
    }
}