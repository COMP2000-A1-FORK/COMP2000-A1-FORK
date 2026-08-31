package com.zombiesim;

/**
 * Tracks simulation time, the day/night phase, and the speed multiplier.
 * Speed cycles 0.5x -> 1x -> 2x -> 4x -> back to 0.5x. Because everything else
 * in the simulation is driven off simulation-time deltas (not real time), a
 * "5 sim-second" sleep always takes 5 sim-seconds regardless of speed.
 */
public class SimClock {

    public enum Phase { DAY, NIGHT }

    private static final double[] SPEED_STEPS = {0.5, 1.0, 2.0, 4.0};

    private double simSeconds = 0;
    private final double dayLengthSeconds;
    private final double nightLengthSeconds;
    private Phase phase = Phase.DAY; // simulation always begins during daytime
    private double phaseElapsed = 0;

    private int speedIndex = 1; // starts at 1x
    private boolean paused = false;

    public SimClock(double dayLengthSeconds, double nightLengthSeconds) {
        this.dayLengthSeconds = dayLengthSeconds;
        this.nightLengthSeconds = nightLengthSeconds;
    }

    public boolean isPaused() { return paused; }
    public void togglePaused() { paused = !paused; }

    public void cycleSpeed() {
        speedIndex = (speedIndex + 1) % SPEED_STEPS.length;
    }

    public double getSpeedMultiplier() { return SPEED_STEPS[speedIndex]; }
    public Phase getPhase() { return phase; }
    public boolean isNight() { return phase == Phase.NIGHT; }
    public double getSimSeconds() { return simSeconds; }

    /**
     * Advances the clock by realDeltaSeconds of wall-clock time, scaled by the
     * speed multiplier, and returns the resulting simulation-time delta for the
     * rest of the engine to apply this tick (0 if paused).
     */
    public double tick(double realDeltaSeconds) {
        if (paused) return 0;
        double simDelta = realDeltaSeconds * getSpeedMultiplier();
        simSeconds += simDelta;
        phaseElapsed += simDelta;

        double phaseLength = (phase == Phase.DAY) ? dayLengthSeconds : nightLengthSeconds;
        if (phaseElapsed >= phaseLength) {
            phaseElapsed -= phaseLength;
            phase = (phase == Phase.DAY) ? Phase.NIGHT : Phase.DAY;
        }
        return simDelta;
    }

    public String formattedTime() {
        int totalSeconds = (int) simSeconds;
        int hours = (totalSeconds / 3600) % 24;
        int minutes = (totalSeconds / 60) % 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
