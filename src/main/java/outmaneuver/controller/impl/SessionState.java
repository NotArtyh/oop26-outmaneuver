package outmaneuver.controller.impl;

public final class SessionState {

    private int stars;
    private double speedMultiplier = 1.0;
    private boolean shieldActive;
    private long elapsedMs;

    public int getStars() {
        return stars;
    }

    public void setStars(final int stars) {
        this.stars = stars;
    }

    public double getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(final double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public void setShieldActive(final boolean shieldActive) {
        this.shieldActive = shieldActive;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public void addElapsed(final long ms) {
        this.elapsedMs += ms;
    }

    public void increaseStars() {
        this.stars += 1;
    }

    public void reset() {
        stars = 0;
        speedMultiplier = 1.0;
        shieldActive = false;
        elapsedMs = 0;
    }
}
