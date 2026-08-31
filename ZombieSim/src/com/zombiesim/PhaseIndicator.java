package com.zombiesim;

import javax.swing.*;
import java.awt.*;

/** Small badge rendering a sun or moon glyph for whichever phase is active. */
public class PhaseIndicator extends JComponent {

    private boolean night = false;

    public PhaseIndicator() {
        setPreferredSize(new Dimension(24, 24));
        setOpaque(false);
    }

    public void setNight(boolean night) {
        if (this.night != night) {
            this.night = night;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int size = Math.min(getWidth(), getHeight());
        int pad = 2;

        if (night) {
            g2.setColor(new Color(225, 225, 240));
            g2.fillOval(pad, pad, size - 2 * pad, size - 2 * pad);
            Color bg = getParent() != null ? getParent().getBackground() : getBackground();
            g2.setColor(bg != null ? bg : Color.WHITE);
            g2.fillOval(pad + size / 4, pad - size / 6, size - 2 * pad, size - 2 * pad);
        } else {
            g2.setColor(new Color(250, 200, 40));
            int core = size - 2 * pad - 6;
            g2.fillOval(pad + 3, pad + 3, core, core);
            g2.setStroke(new BasicStroke(2f));
            int cx = size / 2, cy = size / 2;
            for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians(i * 45);
                int x1 = (int) (cx + Math.cos(angle) * (size / 2 - 2));
                int y1 = (int) (cy + Math.sin(angle) * (size / 2 - 2));
                int x2 = (int) (cx + Math.cos(angle) * (size / 2 + 4));
                int y2 = (int) (cy + Math.sin(angle) * (size / 2 + 4));
                g2.drawLine(x1, y1, x2, y2);
            }
        }
        g2.dispose();
    }
}
