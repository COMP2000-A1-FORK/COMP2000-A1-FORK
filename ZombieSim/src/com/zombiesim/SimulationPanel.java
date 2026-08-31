package com.zombiesim;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Renders the grid/agents and drives the real-time game loop via a Swing Timer. */
public class SimulationPanel extends JPanel implements ActionListener {

    private final SimulationEngine engine;
    private final IconManager icons;
    private final Timer timer;
    private long lastNanoTime;
    private int tileSize = 28;

    private static final Color LAND_COLOR = new Color(210, 230, 200);
    private static final Color BLOCKED_COLOR = new Color(90, 90, 90);
    private static final Color SHELTER_FLOOR_COLOR = new Color(235, 220, 170);
    private static final Color NIGHT_TINT = new Color(15, 20, 60, 110);
    private static final Color GRID_LINE = new Color(0, 0, 0, 25);

    public SimulationPanel(SimulationEngine engine, IconManager icons) {
        this.engine = engine;
        this.icons = icons;
        setBackground(new Color(245, 245, 245));
        lastNanoTime = System.nanoTime();
        timer = new Timer(16, this); // ~60 FPS real-time tick
        timer.start(); // runs by default on load, not paused
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double deltaSeconds = (now - lastNanoTime) / 1_000_000_000.0;
        lastNanoTime = now;
        deltaSeconds = Math.min(deltaSeconds, 0.1); // clamp to avoid a spiral of death after any stall
        engine.tick(deltaSeconds);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Grid grid = engine.getGrid();
        recalcTileSize(grid);
        icons.prescale(tileSize);

        drawTerrain(g2, grid);
        drawDeadMarkers(g2, grid);
        drawCures(g2);
        drawHumans(g2);
        drawZombies(g2);

        if (engine.getClock().isNight()) {
            g2.setColor(NIGHT_TINT);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        if (engine.getWinner() != SimulationEngine.Winner.NONE) {
            drawWinnerBanner(g2);
        }
    }

    private void recalcTileSize(Grid grid) {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        tileSize = Math.max(6, Math.min(w / grid.getCols(), h / grid.getRows()));
    }

    private void drawTerrain(Graphics2D g2, Grid grid) {
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                Position p = new Position(r, c);
                Terrain t = grid.getTerrain(p);
                int x = c * tileSize, y = r * tileSize;
                switch (t) {
                    case BLOCKED:
                        g2.setColor(BLOCKED_COLOR);
                        g2.fillRect(x, y, tileSize, tileSize);
                        drawIconOrFallbackSquare(g2, IconManager.IconType.BLOCKED, x, y, BLOCKED_COLOR.darker());
                        break;
                    case SHELTER:
                        g2.setColor(SHELTER_FLOOR_COLOR);
                        g2.fillRect(x, y, tileSize, tileSize);
                        drawIconOrFallbackSquare(g2, IconManager.IconType.SHELTER, x, y, new Color(150, 100, 40));
                        break;
                    case LAND:
                    default:
                        g2.setColor(LAND_COLOR);
                        g2.fillRect(x, y, tileSize, tileSize);
                }
                g2.setColor(GRID_LINE);
                g2.drawRect(x, y, tileSize, tileSize);
            }
        }
    }

    private void drawDeadMarkers(Graphics2D g2, Grid grid) {
        for (int r = 0; r < grid.getRows(); r++) {
            for (int c = 0; c < grid.getCols(); c++) {
                Position p = new Position(r, c);
                if (grid.hasDeadMarker(p)) {
                    drawIconOrFallbackCircle(g2, IconManager.IconType.DEAD, c * tileSize, r * tileSize, new Color(60, 60, 60));
                }
            }
        }
    }

    private void drawCures(Graphics2D g2) {
        for (Cure c : engine.getCureManager().getActiveCures()) {
            drawIconOrFallbackCircle(g2, IconManager.IconType.CURE, c.position.col * tileSize, c.position.row * tileSize, new Color(30, 140, 220));
        }
    }

    private void drawHumans(Graphics2D g2) {
        for (Human h : engine.getHumans()) {
            if (!h.isAlive()) continue;
            int x = h.getPosition().col * tileSize, y = h.getPosition().row * tileSize;
            Color fallback = h.isCarryingCure() ? new Color(30, 180, 90) : new Color(40, 90, 200);
            drawIconOrFallbackCircle(g2, IconManager.IconType.HUMAN, x, y, fallback);
            drawSleepMeter(g2, h, x, y);
        }
    }

    private void drawZombies(Graphics2D g2) {
        for (Zombie z : engine.getZombies()) {
            if (!z.isAlive()) continue;
            int x = z.getPosition().col * tileSize, y = z.getPosition().row * tileSize;
            drawIconOrFallbackCircle(g2, IconManager.IconType.ZOMBIE, x, y, new Color(120, 40, 40));
        }
    }

    private void drawIconOrFallbackSquare(Graphics2D g2, IconManager.IconType type, int x, int y, Color fallback) {
        Image img = icons.get(type);
        if (img != null) {
            g2.drawImage(img, x, y, null);
        } else {
            g2.setColor(fallback);
            int pad = Math.max(1, tileSize / 6);
            g2.fillRect(x + pad, y + pad, tileSize - 2 * pad, tileSize - 2 * pad);
        }
    }

    private void drawIconOrFallbackCircle(Graphics2D g2, IconManager.IconType type, int x, int y, Color fallback) {
        Image img = icons.get(type);
        if (img != null) {
            g2.drawImage(img, x, y, null);
        } else {
            g2.setColor(fallback);
            int pad = Math.max(1, tileSize / 6);
            g2.fillOval(x + pad, y + pad, tileSize - 2 * pad, tileSize - 2 * pad);
        }
    }

    /** Small radial/circular sleep-meter badge in the corner of a human's tile. */
    private void drawSleepMeter(Graphics2D g2, Human h, int x, int y) {
        int barSize = Math.max(4, tileSize / 3);
        int bx = x + tileSize - barSize;
        int by = y;
        double frac = h.getSleepMeter() / 100.0;
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillOval(bx, by, barSize, barSize);
        g2.setColor(frac > 0.3 ? new Color(60, 200, 90) : new Color(220, 60, 60));
        g2.fillArc(bx, by, barSize, barSize, 90, -(int) (360 * frac));
    }

    private void drawWinnerBanner(Graphics2D g2) {
        String text = engine.getWinner() == SimulationEngine.Winner.HUMANS ? "HUMANS WIN" : "ZOMBIES WIN";
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int tx = (getWidth() - tw) / 2;
        int ty = getHeight() / 2;
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, ty - 40, getWidth(), 60);
        g2.setColor(Color.WHITE);
        g2.drawString(text, tx, ty);
    }
}
