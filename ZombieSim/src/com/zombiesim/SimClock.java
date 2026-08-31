package com.zombiesim;

/**
 * Controls the simulation's internal clock.
 *
 * SimClock keeps track of:
 * - How much simulation time has passed.
 * - Whether it is currently day or night.
 * - How quickly simulation time runs compared with real time.
 * - Whether the simulation is paused.
 *
 * The rest of the simulation uses the simulation-time delta returned by
 * tick(), rather than directly using real-world time.
 */
public class SimClock {

    /**
     * The two possible phases of the simulation.
     */
    public enum Phase {
        DAY,
        NIGHT
    }

    /**
     * Available simulation speed settings.
     *
     * 0.5x = half speed
     * 1.0x = normal speed
     * 2.0x = double speed
     * 4.0x = four times normal speed
     */
    private static final double[] SPEED_STEPS = {
        0.5,
        1.0,
        2.0,
        4.0
    };

    /** Total amount of simulation time that has passed, measured in seconds. */
    private double simSeconds = 0;

    /** How long the daytime phase lasts in simulation seconds. */
    private final double dayLengthSeconds;

    /** How long the nighttime phase lasts in simulation seconds. */
    private final double nightLengthSeconds;

    /**
     * Current day/night phase.
     *
     * The simulation always starts during the day.
     */
    private Phase phase = Phase.DAY;

    /**
     * Amount of time that has passed within the current day/night phase.
     *
     * This is reset when the simulation switches between day and night.
     */
    private double phaseElapsed = 0;

    /**
     * Index of the current speed inside SPEED_STEPS.
     *
     * Index 1 corresponds to 1.0x, so the simulation starts at normal speed.
     */
    private int speedIndex = 1;

    /** True when the simulation is paused. */
    private boolean paused = false;

    /**
     * Creates a simulation clock.
     *
     * @param dayLengthSeconds length of daytime in simulation seconds
     * @param nightLengthSeconds length of nighttime in simulation seconds
     */
    public SimClock(double dayLengthSeconds, double nightLengthSeconds) {
        this.dayLengthSeconds = dayLengthSeconds;
        this.nightLengthSeconds = nightLengthSeconds;
    }

    /**
     * Checks whether the simulation is currently paused.
     *
     * @return true if paused, otherwise false
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Switches between the paused and running states.
     *
     * If the simulation is running, it becomes paused.
     * If it is paused, it starts running again.
     */
    public void togglePaused() {
        paused = !paused;
    }

    /**
     * Changes the simulation speed to the next available setting.
     *
     * The speed sequence is:
     *
     * 0.5x -> 1x -> 2x -> 4x -> 0.5x
     *
     * The modulo operator (%) makes the index return to 0 after
     * reaching the final speed.
     */
    public void cycleSpeed() {
        speedIndex = (speedIndex + 1) % SPEED_STEPS.length;
    }

    /**
     * Returns the current simulation speed multiplier.
     *
     * @return current speed such as 0.5, 1.0, 2.0 or 4.0
     */
    public double getSpeedMultiplier() {
        return SPEED_STEPS[speedIndex];
    }

    /**
     * Returns the current day/night phase.
     *
     * @return DAY or NIGHT
     */
    public Phase getPhase() {
        return phase;
    }

    /**
     * Checks whether the simulation is currently at night.
     *
     * @return true during night, false during day
     */
    public boolean isNight() {
        return phase == Phase.NIGHT;
    }

    /**
     * Returns the total amount of simulation time that has passed.
     *
     * @return simulation time in seconds
     */
    public double getSimSeconds() {
        return simSeconds;
    }

    /**
     * Advances the simulation clock.
     *
     * The method receives the amount of real-world time that has passed
     * since the previous update. This value is multiplied by the current
     * simulation speed to determine how much simulation time should pass.
     *
     * For example, if 1 real second passes:
     *
     * At 0.5x -> 0.5 simulation seconds pass.
     * At 1.0x -> 1.0 simulation second passes.
     * At 2.0x -> 2.0 simulation seconds pass.
     * At 4.0x -> 4.0 simulation seconds pass.
     *
     * If the simulation is paused, no simulation time passes and 0 is returned.
     *
     * @param realDeltaSeconds real-world time since the previous update
     * @return amount of simulation time that passed during this tick
     */
    public double tick(double realDeltaSeconds) {

        // Do not advance the clock while the simulation is paused.
        if (paused) return 0;

        /*
         * Convert real-world elapsed time into simulation time using
         * the current speed multiplier.
         */
        double simDelta = realDeltaSeconds * getSpeedMultiplier();

        // Add the elapsed simulation time to the total simulation clock.
        simSeconds += simDelta;

        // Also add it to the timer for the current day/night phase.
        phaseElapsed += simDelta;

        /*
         * Determine how long the current phase should last.
         *
         * Day uses dayLengthSeconds.
         * Night uses nightLengthSeconds.
         */
        double phaseLength =
                (phase == Phase.DAY)
                        ? dayLengthSeconds
                        : nightLengthSeconds;

        /*
         * Check whether the current phase has reached its time limit.
         */
        if (phaseElapsed >= phaseLength) {

            /*
             * Remove the completed phase from the elapsed time.
             *
             * For example, if the phase should last 10 seconds but
             * 10.2 seconds have passed, 0.2 seconds carries over
             * into the next phase.
             */
            phaseElapsed -= phaseLength;

            /*
             * Switch between day and night.
             *
             * DAY -> NIGHT
             * NIGHT -> DAY
             */
            phase =
                    (phase == Phase.DAY)
                            ? Phase.NIGHT
                            : Phase.DAY;
        }

        /*
         * Return the simulation-time change.
         *
         * The SimulationEngine uses this value to update humans,
         * zombies, cures and other simulation systems.
         */
        return simDelta;
    }

    /**
     * Converts the total simulation time into a readable clock format.
     *
     * The result is displayed as:
     *
     * HH:MM:SS
     *
     * For example:
     * 00:05:32
     * 01:20:15
     */
    public String formattedTime() {

        // Convert the total simulation time from double to whole seconds.
        int totalSeconds = (int) simSeconds;

        // Calculate the current hour, wrapping after 24 hours.
        int hours = (totalSeconds / 3600) % 24;

        // Calculate the number of minutes within the current hour.
        int minutes = (totalSeconds / 60) % 60;

        // Calculate the remaining seconds within the current minute.
        int seconds = totalSeconds % 60;

        /*
         * Format the values with two digits each.
         *
         * Example:
         * 5 hours, 3 minutes and 7 seconds
         * becomes "05:03:07".
         */
        return String.format(
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
        );
    }
}