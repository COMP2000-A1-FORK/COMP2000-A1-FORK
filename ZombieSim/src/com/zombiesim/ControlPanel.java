package com.zombiesim;

import javax.swing.*;
import java.awt.*;

/**
 * Provides the user controls for the simulation.
 *
 * The panel contains buttons that allow the user to pause/resume the
 * simulation and change the simulation speed. The controls communicate
 * with the SimulationEngine rather than directly changing the simulation
 * state themselves.
 */
public class ControlPanel extends JPanel {

    /**
     * Creates the control panel and connects each button to the simulation
     * engine supplied by the application.
     *
     * @param engine the SimulationEngine that controls the simulation state
     */
    public ControlPanel(SimulationEngine engine) {

        // Arrange the controls from left to right with spacing between them.
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        // Button used to pause or resume the simulation.
        JButton playPause = new JButton("Pause");

        // When clicked, switch the simulation between running and paused states.
        playPause.addActionListener(e -> {
            engine.togglePaused();

            // Update the button text to show the action available to the user.
            // If the simulation is paused, display "Play".
            // If the simulation is running, display "Pause".
            playPause.setText(
                engine.getClock().isPaused() ? "Play" : "Pause"
            );
        });

        // Add the play/pause button to the control panel.
        add(playPause);

        // Button used to cycle through the available simulation speeds.
        JButton speedBtn = new JButton("Speed: 1.0x");

        // When clicked, ask the simulation engine to move to the next speed.
        speedBtn.addActionListener(e -> {
            engine.cycleSpeed();

            // Get the new speed from the simulation clock and display it
            // on the button so the user can see the current speed multiplier.
            speedBtn.setText(
                "Speed: " + engine.getClock().getSpeedMultiplier() + "x"
            );
        });

        // Add the speed button to the control panel.
        add(speedBtn);
    }
}