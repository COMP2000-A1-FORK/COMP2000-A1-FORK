import javax.swing.*;
import java.awt.*;

// Small circular widget in the corner of the game view showing the current
// time of day - a sun during the day, a crescent moon at night.
public class ClockIndicator extends JComponent {

    private final GamePanel gamePanel;

    public ClockIndicator(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth(), getHeight());
        int pad = 4;

        g2.setColor(Color.WHITE);
        g2.fillOval(pad, pad, size - pad * 2, size - pad * 2);
        g2.setColor(Color.BLACK);
        g2.drawOval(pad, pad, size - pad * 2, size - pad * 2);

        double t = gamePanel.getDayProgress();
        boolean isDaytime = t < 0.5;

        int iconSize = size - pad * 2 - 14;
        int iconX = (getWidth() - iconSize) / 2;
        int iconY = (getHeight() - iconSize) / 2;

        if (isDaytime) {
            g2.setColor(new Color(255, 221, 89));
            g2.fillOval(iconX, iconY, iconSize, iconSize);
        } else {
            g2.setColor(new Color(210, 210, 225));
            g2.fillOval(iconX, iconY, iconSize, iconSize);
            g2.setColor(Color.WHITE);
            g2.fillOval(iconX + iconSize / 3, iconY - 2, iconSize, iconSize);
        }
    }
}