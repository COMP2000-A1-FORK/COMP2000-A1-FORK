import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Sidebar: live stats, the day/night cycle indicator, the cure tool
 * toggle, and the play/pause/speed controls.
 */
public class ControlPanel extends JPanel {

    private final Simulation sim;
    private final SimulationPanel canvas;

    private final JLabel humansLabel = new JLabel();
    private final JLabel zombiesLabel = new JLabel();
    private final JLabel safeZonesLabel = new JLabel();
    private final JLabel buildingsLabel = new JLabel();
    private final JLabel curesLabel = new JLabel();
    private final JLabel cycleLabel = new JLabel();
    private final JButton cureButton = new JButton("Arm cure tool");
    private final JButton playButton = new JButton("Play");
    private final JButton speedButton = new JButton("Speed: 1x");

    public ControlPanel(Simulation sim, SimulationPanel canvas) {
        this.sim = sim;
        this.canvas = canvas;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(250, 248, 243));
        setPreferredSize(new Dimension(260, Simulation.HEIGHT));

        add(sectionTitle("Simulation stats"));
        add(statRow(new Color(66, 133, 244), humansLabel));
        add(statRow(new Color(124, 179, 66), zombiesLabel));
        add(statRow(new Color(129, 199, 156), safeZonesLabel));
        add(statRow(new Color(120, 120, 120), buildingsLabel));
        add(Box.createVerticalStrut(6));
        add(curesLabel);

        add(Box.createVerticalStrut(18));
        add(sectionTitle("Cycle"));
        cycleLabel.setOpaque(true);
        cycleLabel.setBackground(new Color(20, 30, 24));
        cycleLabel.setForeground(Color.WHITE);
        cycleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cycleLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        cycleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cycleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        add(cycleLabel);

        add(Box.createVerticalStrut(18));
        add(sectionTitle("Cure tool"));
        JLabel cureHint = new JLabel("<html>Click an adjacent zombie<br>to attempt a cure</html>");
        cureHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        cureHint.setForeground(new Color(90, 90, 90));
        add(cureHint);
        add(Box.createVerticalStrut(8));
        cureButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        cureButton.addActionListener(e -> {
            boolean armed = cureButton.getText().startsWith("Arm");
            cureButton.setText(armed ? "Disarm cure tool" : "Arm cure tool");
            canvas.setCureToolArmed(armed);
        });
        add(cureButton);

        add(Box.createVerticalGlue());

        speedButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        speedButton.addActionListener(e -> {
            sim.cycleSpeed();
            speedButton.setText("Speed: " + (int) sim.getSpeedMultiplier() + "x");
        });
        add(speedButton);

        add(Box.createVerticalStrut(8));
        playButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        playButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        playButton.setBackground(new Color(122, 168, 66));
        playButton.setForeground(Color.WHITE);
        playButton.setFocusPainted(false);
        playButton.setFont(playButton.getFont().deriveFont(Font.BOLD, 14f));
        playButton.addActionListener(e -> {
            sim.togglePlay();
            playButton.setText(sim.isRunning() ? "Pause" : "Play");
        });
        add(playButton);

        Timer refreshTimer = new Timer(200, e -> refreshStats());
        refreshTimer.start();
        refreshStats();
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 218, 210)));
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        return label;
    }

    private JPanel statRow(Color dotColor, JLabel label) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setBackground(dotColor);

        row.add(dot);
        row.add(label);
        return row;
    }

    private void refreshStats() {
        humansLabel.setText("Humans alive: " + sim.getHumans().size());
        zombiesLabel.setText("Zombies: " + sim.getZombies().size());
        safeZonesLabel.setText("Safe zones: " + sim.getSafeZones().size());
        buildingsLabel.setText("Buildings: " + sim.getBuildings().size());
        curesLabel.setText("Cures: " + sim.getCuresSucceeded() + " / " + sim.getCuresAttempted() + " attempted");
        cycleLabel.setText(sim.getPhaseLabel() + " \u00b7 " + sim.getClockLabel());
    }
}
