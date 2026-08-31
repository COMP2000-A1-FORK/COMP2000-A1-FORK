// Base class for anything that lives on the grid (humans and zombies)
public abstract class Entity {

    protected int gridX;
    protected int gridY;

    // The pixel-smooth position used for drawing, which glides toward gridX/gridY
    // instead of jumping instantly - this is what makes movement look like walking.
    protected double renderX;
    protected double renderY;

    // Used to animate a small vertical bounce while the entity is actively walking
    protected double animPhase = 0;

    protected String emoji;

    public Entity(int x, int y, String emoji) {
        this.gridX = x;
        this.gridY = y;
        this.renderX = x;
        this.renderY = y;
        this.emoji = emoji;
    }

    public int getX() { return gridX; }
    public int getY() { return gridY; }
    public double getRenderX() { return renderX; }
    public double getRenderY() { return renderY; }
    public String getEmoji() { return emoji; }

    public void setPosition(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    // Used when a Human turns into a Zombie, so the new zombie starts drawing
    // from exactly where the human currently was rendered, not from scratch.
    public void syncRenderPosition(double rx, double ry) {
        this.renderX = rx;
        this.renderY = ry;
    }

    // Glides the rendered pixel position toward the grid target each frame,
    // and advances a small walking-bounce animation while actually moving.
    public void updateRenderPosition(double lerpFactor) {
        double dx = gridX - renderX;
        double dy = gridY - renderY;
        boolean moving = Math.abs(dx) > 0.02 || Math.abs(dy) > 0.02;

        if (moving) {
            animPhase += 0.35;
        } else {
            animPhase = 0;
        }

        renderX += dx * lerpFactor;
        renderY += dy * lerpFactor;
    }

    // Small vertical bounce applied while walking, 0 while standing still
    public double getBobOffset() {
        return Math.sin(animPhase) * 3.0;
    }

    // Decides the next grid cell to move to. blocked[row][col] is true where
    // a building (or other obstacle) occupies that cell.
    public abstract void move(int gridWidth, int gridHeight, Entity[] allEntities, boolean[][] blocked);
}