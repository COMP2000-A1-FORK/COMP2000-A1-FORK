public class DeliverCureStrategy implements MovementStrategy { 
    @Override public void move(Human self, int gridWidth, int gridHeight, Entity[] allEntities, boolean[][] blocked) {
        Zombie targetZombie = Entity.findNearest(allEntities, Zombie.class, self.getX(), self.getY()); 
        if (targetZombie == null) return; 
        int dx = Integer.compare(targetZombie.getX(), self.getX()); 
        int dy = Integer.compare(targetZombie.getY(), self.getY()); 
        int newX = Math.max(0, Math.min(gridWidth - 1, self.getX() + dx)); 
        int newY = Math.max(0, Math.min(gridHeight - 1, self.getY() + dy));
        
        if (!blocked[newY][newX]) { 
            self.setPosition(newX, newY);
        }
    }
}