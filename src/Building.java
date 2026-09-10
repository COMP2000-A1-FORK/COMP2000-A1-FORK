import java.awt.*;

// A small rectangular building drawn with basic shapes (shadow, shaded walls,
// roof, chimney, door, windows). Implements Obstacle so humans/zombies path
// around it instead of walking through it.
public class Building implements Obstacle {

    private final int startCol;
    private final int startRow;
    private final int widthCells;
    private final int heightCells;

    public Building(int startCol, int startRow, int widthCells, int heightCells) {
        this.startCol = startCol;
        this.startRow = startRow;
        this.widthCells = widthCells;
        this.heightCells = heightCells;
    }

    @Override
    public boolean occupiesCell(int col, int row) {
        return col >= startCol && col < startCol + widthCells
                && row >= startRow && row < startRow + heightCells;
    }

    public void draw(Graphics2D g2, int cellSize) {
        int px = startCol * cellSize;
        int py = startRow * cellSize;
        int pw = widthCells * cellSize;
        int ph = heightCells * cellSize;

        // Soft shadow beneath the building
        g2.setColor(new Color(0, 0, 0, 55));
        g2.fillOval(px + 4, py + ph - 6, Math.max(pw - 8, 4), 12);

        // Walls with a subtle top-to-bottom shade
        GradientPaint wallGradient = new GradientPaint(
                px, py, new Color(214, 186, 140),
                px, py + ph, new Color(182, 150, 106));
        g2.setPaint(wallGradient);
        g2.fillRect(px, py, pw, ph);
        g2.setPaint(null);

        // Roof
        int roofH = Math.max(cellSize / 2, ph / 3);
        g2.setColor(new Color(150, 65, 55));
        g2.fillRect(px, py, pw, roofH);
        g2.setColor(new Color(120, 45, 40));
        g2.fillRect(px, py + roofH - 3, pw, 3);

        // Small chimney
        g2.setColor(new Color(120, 100, 90));
        g2.fillRect(px + pw - cellSize / 3, py - cellSize / 4, cellSize / 5, cellSize / 3);

        // Door, centered on the bottom wall
        int doorW = Math.max(cellSize / 2, pw / 3);
        int doorH = Math.min(cellSize, ph - roofH);
        int doorX = px + pw / 2 - doorW / 2;
        int doorY = py + ph - doorH;
        g2.setColor(new Color(96, 60, 32));
        g2.fillRect(doorX, doorY, doorW, doorH);
        g2.setColor(new Color(70, 45, 25));
        g2.drawRect(doorX, doorY, doorW, doorH);

        // Windows either side of the door, if there's room
        int winSize = cellSize / 3;
        if (pw > doorW + winSize * 2 + 8) {
            int wy = py + roofH + 4;

            g2.setColor(new Color(235, 224, 160));
            g2.fillRect(px + 4, wy, winSize, winSize);
            g2.setColor(new Color(120, 90, 50));
            g2.drawRect(px + 4, wy, winSize, winSize);

            g2.setColor(new Color(235, 224, 160));
            g2.fillRect(px + pw - winSize - 4, wy, winSize, winSize);
            g2.setColor(new Color(120, 90, 50));
            g2.drawRect(px + pw - winSize - 4, wy, winSize, winSize);
        }

        // Outline
        g2.setColor(new Color(90, 70, 50));
        g2.drawRect(px, py, pw, ph);
    }
}