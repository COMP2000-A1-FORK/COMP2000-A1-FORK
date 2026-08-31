package com.zombiesim;

import javax.swing.*;
import java.awt.*;

/**
 * Displays a small visual indicator showing whether the simulation is
 * currently in the day or night phase.
 *
 * During the day, a sun is drawn.
 * During the night, a moon is drawn.
 */
public class PhaseIndicator extends JComponent {

    /**
     * Stores the current phase.
     *
     * false = day
     * true  = night
     */
    private boolean night = false;

    /**
     * Creates the phase indicator and sets its preferred size.
     *
     * The component is made transparent so that the background of the
     * surrounding panel can remain visible.
     */
    public PhaseIndicator() {

        // Set the preferred width and height of the indicator to 24 pixels.
        setPreferredSize(new Dimension(24, 24));

        // Make the component transparent.
        setOpaque(false);
    }

    /**
     * Changes the current day/night state.
     *
     * The component is only repainted when the state actually changes.
     *
     * @param night true for night, false for day
     */
    public void setNight(boolean night) {

        /*
         * Check whether the new value is different from the current value.
         * There is no need to redraw the icon if nothing has changed.
         */
        if (this.night != night) {

            // Store the new day/night state.
            this.night = night;

            /*
             * Tell Swing that the component needs to be drawn again.
             *
             * Swing will call paintComponent() when the component is
             * repainted.
             */
            repaint();
        }
    }

    /**
     * Draws the sun or moon depending on the current simulation phase.
     *
     * Swing calls this method automatically whenever the component needs
     * to be displayed or redrawn.
     *
     * @param g graphics object supplied by Swing
     */
    @Override
    protected void paintComponent(Graphics g) {

        /*
         * Create a copy of the Graphics object.
         *
         * This allows us to modify the graphics settings without
         * accidentally affecting other Swing components.
         */
        Graphics2D g2 = (Graphics2D) g.create();

        /*
         * Enable anti-aliasing.
         *
         * This makes the circles and lines appear smoother rather than
         * having jagged edges.
         */
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        /*
         * Determine the largest square that fits inside the component.
         *
         * getWidth() and getHeight() may not always be identical, so the
         * smaller value is used.
         */
        int size = Math.min(getWidth(), getHeight());

        // Small spacing between the icon and the edge of the component.
        int pad = 2;

        /*
         * Draw the night icon.
         */
        if (night) {

            /*
             * First draw a light-coloured circle.
             *
             * This forms the full moon.
             */
            g2.setColor(new Color(225, 225, 240));
            g2.fillOval(
                    pad,
                    pad,
                    size - 2 * pad,
                    size - 2 * pad
            );

            /*
             * Get the background colour of the parent panel.
             *
             * This colour will be drawn over part of the moon to create
             * the curved crescent shape.
             */
            Color bg = getParent() != null
                    ? getParent().getBackground()
                    : getBackground();

            // Use the parent background, or white if no background is available.
            g2.setColor(bg != null ? bg : Color.WHITE);

            /*
             * Draw a second circle over the first one.
             *
             * Because this circle is offset from the first circle, it
             * hides part of it and creates the crescent moon shape.
             */
            g2.fillOval(
                    pad + size / 4,
                    pad - size / 6,
                    size - 2 * pad,
                    size - 2 * pad
            );

        } else {

            /*
             * Draw the daytime sun.
             */

            // Set the colour used for the sun.
            g2.setColor(new Color(250, 200, 40));

            /*
             * Calculate the size of the central circle of the sun.
             *
             * The rays need space around the outside, so the centre
             * circle is slightly smaller than the whole component.
             */
            int core = size - 2 * pad - 6;

            // Draw the circular centre of the sun.
            g2.fillOval(
                    pad + 3,
                    pad + 3,
                    core,
                    core
            );

            /*
             * Set the thickness of the sun rays.
             */
            g2.setStroke(new BasicStroke(2f));

            // Find the centre point of the component.
            int cx = size / 2;
            int cy = size / 2;

            /*
             * Draw eight rays around the sun.
             *
             * 360 degrees is divided into eight equal sections:
             *
             * 0°, 45°, 90°, 135°, 180°, 225°, 270°, 315°
             */
            for (int i = 0; i < 8; i++) {

                /*
                 * Convert the current ray angle from degrees to radians.
                 *
                 * Java's Math.sin() and Math.cos() functions use radians.
                 */
                double angle = Math.toRadians(i * 45);

                /*
                 * Calculate the starting point of the ray.
                 *
                 * cos() controls the horizontal position.
                 * sin() controls the vertical position.
                 */
                int x1 = (int) (
                        cx + Math.cos(angle) * (size / 2 - 2)
                );

                int y1 = (int) (
                        cy + Math.sin(angle) * (size / 2 - 2)
                );

                /*
                 * Calculate the ending point of the ray.
                 *
                 * This point is further away from the centre, making
                 * the ray extend outside the central sun.
                 */
                int x2 = (int) (
                        cx + Math.cos(angle) * (size / 2 + 4)
                );

                int y2 = (int) (
                        cy + Math.sin(angle) * (size / 2 + 4)
                );

                // Draw the ray between the calculated points.
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        /*
         * Dispose of the Graphics2D object after drawing is complete.
         *
         * This releases the resources associated with the graphics copy.
         */
        g2.dispose();
    }
}