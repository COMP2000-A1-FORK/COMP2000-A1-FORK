package com.zombiesim;

import javax.swing.*;

/**
 * Main entry point for the Zombie Simulation program.
 *
 * This class is responsible for starting the application and creating
 * the main graphical user interface window.
 */
public class Main {

    /**
     * Starts the Zombie Simulation.
     *
     * The Swing user interface is created using Swing's Event Dispatch
     * Thread (EDT). This is the thread Swing uses to safely create and
     * update graphical components.
     *
     * @param args command-line arguments (not used by the simulation)
     */
    public static void main(String[] args) {

        /*
         * Schedule the creation of the GUI on Swing's Event Dispatch Thread.
         *
         * SwingUtilities.invokeLater() ensures that Swing components are
         * created and modified on the correct thread, preventing potential
         * GUI threading problems.
         */
        SwingUtilities.invokeLater(() -> {

            // Create the main application window.
            MainFrame frame = new MainFrame();

            // Make the application window visible to the user.
            frame.setVisible(true);
        });
    }
}