package com.zombiesim;

import javax.swing.*;
import java.awt.*;

public class HUDPanel extends JPanel {

    private final SimulationEngine engine;
    private final PhaseIndicator phaseIndicator = new PhaseIndicator();
    private final JLabel phaseLabel = new JLabel();
    private final JLabel clockLabel = new JLabel();
    private final JLabel humanCountLabel = new JLabel();
    private final JLabel zombieCountLabel = new JLabel();
    private final JLabel cureCountLabel = new JLabel();
    private final JLabel statusBar = new JLabel();
    private final Timer refreshTimer;

    public HUDPanel(SimulationEngine engine) {
        this.engine = engine;
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        top.add(phaseIndicator);
        top.add(phaseLabel);
        top.add(separatorLabel());
        top.add(clockLabel);
        top.add(separatorLabel());
        top.add(humanCountLabel);
        top.add(zombieCountLabel);
        top.add(cureCountLabel);
        add(top, BorderLayout.NORTH);

        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));
        statusBar.setFont(statusBar.getFont().deriveFont(Font.ITALIC));
        add(statusBar, BorderLayout.SOUTH);

        refreshTimer = new Timer(150, e -> refresh());
        refreshTimer.start();
        refresh();
    }

    private JLabel separatorLabel() {
        JLabel l = new JLabel("|");
        l.setForeground(Color.GRAY);
        return l;
    }

    private void refresh() {
        SimClock clock = engine.getClock();
        boolean night = clock.isNight();

        phaseIndicator.setNight(night);
        phaseLabel.setText(night ? "Night" : "Day");
        clockLabel.setText("Time " + clock.formattedTime() + "   Speed " + clock.getSpeedMultiplier() + "x");

        long aliveHumans = engine.getHumans().stream().filter(Human::isAlive).count();
        long aliveZombies = engine.getZombies().stream().filter(Zombie::isAlive).count();
        humanCountLabel.setText("Humans: " + aliveHumans + "   ");
        zombieCountLabel.setText("Zombies: " + aliveZombies + "   ");
        cureCountLabel.setText("Cures: " + engine.getCureManager().getRemaining());

        if (engine.getWinner() != SimulationEngine.Winner.NONE) {
            statusBar.setText(engine.getWinner() == SimulationEngine.Winner.HUMANS
                    ? "Simulation over — humans win"
                    : "Simulation over — zombies win");
        } else if (night) {
            statusBar.setText("Night — visibility reduced, zombies active");
        } else {
            statusBar.setText("Day — zombies frozen");
        }
    }
}
