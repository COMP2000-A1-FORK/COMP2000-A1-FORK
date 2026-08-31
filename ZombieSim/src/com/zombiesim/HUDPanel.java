package com.zombiesim;

import javax.swing.*;
import java.awt.*;

/**
 * Displays the simulation's heads-up display (HUD).
 *
 * The HUD provides the user with information about the current simulation
 * state, including:
 * - Whether it is currently day or night
 * - The simulation time and speed
 * - The number of living humans and zombies
 * - The number of remaining cures
 * - The current simulation status or winner
 *
 * The displayed information is refreshed regularly so that the HUD stays
 * synchronised with the SimulationEngine.
 */
public class HUDPanel extends JPanel {

    /** Provides access to the current state of the simulation. */
    private final SimulationEngine engine;

    /** Displays the visual indicator for the current day/night phase. */
    private final PhaseIndicator phaseIndicator = new PhaseIndicator();

    /** Displays the text label showing "Day" or "Night". */
    private final JLabel phaseLabel = new JLabel();

    /** Displays the current simulation time and speed multiplier. */
    private final JLabel clockLabel = new JLabel();

    /** Displays the number of humans that are still alive. */
    private final JLabel humanCountLabel = new JLabel();

    /** Displays the number of zombies that are still alive. */
    private final JLabel zombieCountLabel = new JLabel();

    /** Displays the number of cures remaining in the simulation. */
    private final JLabel cureCountLabel = new JLabel();

    /** Displays information about the current simulation status. */
    private final JLabel statusBar = new JLabel();

    /**
     * Swing timer used to periodically update the information displayed
     * by the HUD.
     */
    private final Timer refreshTimer;

    /**
     * Creates the HUD panel and sets up all of its information displays.
     *
     * @param engine the SimulationEngine containing the current simulation state
     */
    public HUDPanel(SimulationEngine engine) {
        this.engine = engine;

        // Use BorderLayout so the main information is placed at the top
        // and the status message is placed at the bottom.
        setLayout(new BorderLayout());

        /*
         * Create the top section of the HUD.
         *
         * FlowLayout places each piece of information from left to right,
         * making the HUD appear as a horizontal information bar.
         */
        JPanel top = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 14, 4)
        );

        // Add the day/night visual indicator.
        top.add(phaseIndicator);

        // Add the text version of the current phase.
        top.add(phaseLabel);

        // Add a visual separator between the phase and clock information.
        top.add(separatorLabel());

        // Add the simulation time and current speed.
        top.add(clockLabel);

        // Add another separator before the population information.
        top.add(separatorLabel());

        // Add the number of living humans, zombies, and remaining cures.
        top.add(humanCountLabel);
        top.add(zombieCountLabel);
        top.add(cureCountLabel);

        // Place the information bar at the top of the HUD.
        add(top, BorderLayout.NORTH);

        /*
         * Configure the status bar at the bottom of the HUD.
         *
         * Empty borders provide some spacing around the text so that
         * it does not sit directly against the edge of the panel.
         */
        statusBar.setBorder(
                BorderFactory.createEmptyBorder(4, 10, 6, 10)
        );

        // Make the status message visually different from the main information.
        statusBar.setFont(
                statusBar.getFont().deriveFont(Font.ITALIC)
        );

        // Place the status message at the bottom of the HUD.
        add(statusBar, BorderLayout.SOUTH);

        /*
         * Refresh the HUD every 150 milliseconds.
         *
         * The timer repeatedly calls refresh(), which reads the latest
         * information from the SimulationEngine and updates the labels.
         */
        refreshTimer = new Timer(150, e -> refresh());
        refreshTimer.start();

        // Perform an immediate refresh so the HUD is populated when created.
        refresh();
    }

    /**
     * Creates a simple vertical separator used to visually divide
     * different groups of information in the HUD.
     *
     * @return a JLabel containing a grey "|" separator
     */
    private JLabel separatorLabel() {
        JLabel l = new JLabel("|");

        // Grey makes the separator less visually prominent than the data.
        l.setForeground(Color.GRAY);

        return l;
    }

    /**
     * Updates all information displayed by the HUD.
     *
     * This method reads the current simulation state from the engine and
     * updates the corresponding labels and indicators.
     */
    private void refresh() {

        // Get the simulation clock so we can check the current time and phase.
        SimClock clock = engine.getClock();

        // Determine whether the simulation is currently in the night phase.
        boolean night = clock.isNight();

        /*
         * Update the day/night information.
         *
         * Both the visual indicator and text label are updated so the user
         * can clearly see which phase the simulation is currently in.
         */
        phaseIndicator.setNight(night);
        phaseLabel.setText(night ? "Night" : "Day");

        /*
         * Display the current simulation time and speed.
         *
         * formattedTime() provides the readable simulation time, while
         * getSpeedMultiplier() shows how quickly the simulation is running.
         */
        clockLabel.setText(
                "Time " + clock.formattedTime()
                        + "   Speed " + clock.getSpeedMultiplier() + "x"
        );

        /*
         * Count the number of living humans and zombies.
         *
         * The streams go through every agent and only count those whose
         * isAlive() method returns true. Dead agents therefore do not appear
         * in the displayed population count.
         */
        long aliveHumans = engine.getHumans()
                .stream()
                .filter(Human::isAlive)
                .count();

        long aliveZombies = engine.getZombies()
                .stream()
                .filter(Zombie::isAlive)
                .count();

        // Update the population labels with the calculated counts.
        humanCountLabel.setText("Humans: " + aliveHumans + "   ");
        zombieCountLabel.setText("Zombies: " + aliveZombies + "   ");

        // Display how many cures remain available in the simulation.
        cureCountLabel.setText(
                "Cures: " + engine.getCureManager().getRemaining()
        );

        /*
         * Update the status message.
         *
         * The winner is checked first because the simulation-over message
         * should take priority over the normal day/night status.
         */
        if (engine.getWinner() != SimulationEngine.Winner.NONE) {

            // Display which species won once the simulation has ended.
            statusBar.setText(
                    engine.getWinner() == SimulationEngine.Winner.HUMANS
                            ? "Simulation over — humans win"
                            : "Simulation over — zombies win"
            );

        } else if (night) {

            // During night, zombies are active and visibility is reduced.
            statusBar.setText(
                    "Night — visibility reduced, zombies active"
            );

        } else {

            // During the day, zombies are frozen according to the simulation rules.
            statusBar.setText(
                    "Day — zombies frozen"
            );
        }
    }
}