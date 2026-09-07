import java.awt.*;

public class Cure {
    private final int gridX;
    private final int gridY;
    private final String emoji = "\uD83D\uDC89"; 

    public Cure(int x, int y) {
        this.gridX = x;
        this.gridY = y;
    }

    public int getX() { return gridX; }
    public int getY() { return gridY; }

    public void draw(Graphics2D g2, int cellSize) {
        Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, (int) (cellSize * 0.65));
        g2.setFont(emojiFont);
        FontMetrics fm = g2.getFontMetrics();

        int px = gridX * cellSize;
        int py = gridY * cellSize;

        int textWidth = fm.stringWidth(emoji);
        int textX = (int) (px + (cellSize - textWidth) / 2.0);
        int textY = (int) (py + (cellSize + fm.getAscent()) / 2.0 - 4);

        g2.drawString(emoji, textX, textY);
    }
}
