package com.zombiesim;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {

    public ControlPanel(SimulationEngine engine) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JButton playPause = new JButton("Pause");
        playPause.addActionListener(e -> {
            engine.togglePaused();
            playPause.setText(engine.getClock().isPaused() ? "Play" : "Pause");
        });
        add(playPause);

        JButton speedBtn = new JButton("Speed: 1.0x");
        speedBtn.addActionListener(e -> {
            engine.cycleSpeed();
            speedBtn.setText("Speed: " + engine.getClock().getSpeedMultiplier() + "x");
        });
        add(speedBtn);
    }
}
