import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Draws the world: grid background, buildings, safe zones, humans,
 * zombies, and (when the cure tool is armed) a highlight ring around
 * the last cure attempt. Also owns the animation Timer that drives
 * Simulation.step() roughly 30 times a second.
 */
public class SimulationPanel extends JPanel {

    private final Simulation sim;
    private final Timer timer;
    private boolean cureToolArmed = false;
    private String lastCureMessage = "";

    public SimulationPanel(Simulation sim) {
        this.sim = sim;
        setPreferredSize(new Dimension(Simulation.WIDTH, Simulation.HEIGHT));
        setBackground(new Color(15, 30, 22));

        timer = new Timer(1000 / 30, (ActionEvent e) -> {
            sim.step();
            repaint();
        });
        timer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!cureToolArmed) {
                    return;
                }
                boolean cured = sim.attemptCureAt(e.getX(), e.getY());
                lastCureMessage = cured
                        ? "Cure successful \u2014 zombie converted back to a human."
                        : "Cure failed \u2014 no zombie there, or no human close enough.";
                repaint();
            }
        });
    }

    public void setCureToolArmed(boolean armed) {
        this.cureToolArmed = armed;
        lastCureMessage = armed ? "Cure tool armed \u2014 click an adjacent zombie." : "";
        repaint();
    }

    public String getStatusMessage() {
        if (!lastCureMessage.isEmpty()) {
            return lastCureMessage;
        }
        return sim.getPhaseLabel();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawSafeZones(g2);
        drawBuildings(g2);
        drawHumans(g2);
        drawZombies(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(30, 50, 38));
        for (int x = 0; x < Simulation.WIDTH; x += 30) {
            g2.drawLine(x, 0, x, Simulation.HEIGHT);
        }
        for (int y = 0; y < Simulation.HEIGHT; y += 30) {
            g2.drawLine(0, y, Simulation.WIDTH, y);
        }
    }

    private void drawBuildings(Graphics2D g2) {
        g2.setColor(new Color(90, 90, 90));
        for (Building b : sim.getBuildings()) {
            g2.fillRoundRect(b.x, b.y, b.width, b.height, 6, 6);
        }
    }

    private void drawSafeZones(Graphics2D g2) {
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0, new float[] {6, 5}, 0));
        for (SafeZone s : sim.getSafeZones()) {
            g2.setColor(new Color(60, 130, 90, 90));
            g2.fillRoundRect(s.x, s.y, s.width, s.height, 10, 10);
            g2.setColor(new Color(120, 220, 160));
            g2.drawRoundRect(s.x, s.y, s.width, s.height, 10, 10);
        }
        g2.setStroke(new BasicStroke(1));
    }

    private void drawHumans(Graphics2D g2) {
        for (Human h : sim.getHumans()) {
            g2.setColor(h.getColor());
            int r = h.getRadius();
            g2.fillOval((int) h.getX() - r, (int) h.getY() - r, r * 2, r * 2);
        }
    }

    private void drawZombies(Graphics2D g2) {
        for (Zombie z : sim.getZombies()) {
            g2.setColor(z.getColor());
            int r = z.getRadius();
            g2.fillOval((int) z.getX() - r, (int) z.getY() - r, r * 2, r * 2);
        }
    }
}
