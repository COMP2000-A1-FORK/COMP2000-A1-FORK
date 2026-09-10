public class SeekCureStrategy implements MovementStrategy { 
    @Override public void move(Human self, int gridWidth, int gridHeight, Entity[] allEntities, boolean[][] blocked) {
        Cure target = null; int bestDist = Integer.MAX_VALUE; 
        for (Cure c : self.getVisibleCures()) { 
            int dist = Math.abs(c.getX() - self.getX()) + Math.abs(c.getY() - self.getY()); 
            if (dist < bestDist) {
                 bestDist = dist; target = c; 
            } 
        }
        if (target == null) return; 
        int dx = Integer.compare(target.getX(), self.getX()); 
        int dy = Integer.compare(target.getY(), self.getY()); 
        int newX = Math.max(0, Math.min(gridWidth - 1, self.getX() + dx)); 
        int newY = Math.max(0, Math.min(gridHeight - 1, self.getY() + dy)); 
        if (!blocked[newY][newX] && !Entity.isTileOccupiedBy(Human.class, newX, newY, allEntities, self)) { 
            self.setPosition(newX, newY); 
        }
    }
}