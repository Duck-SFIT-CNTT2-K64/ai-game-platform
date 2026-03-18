package com.nhom_01.robot_pathfinding.core;

public class CollectedPowerUp {
    private final PowerUp powerUp;
    private boolean isActive;
    private long activatedTime;
    private long duration; // milliseconds

    public CollectedPowerUp(PowerUp powerUp) {
        this.powerUp = powerUp;
        this.isActive = false;
        this.activatedTime = 0;
        this.duration = 10000; // Default 10 seconds
    }

    public CollectedPowerUp(PowerUp powerUp, long duration) {
        this.powerUp = powerUp;
        this.isActive = false;
        this.activatedTime = 0;
        this.duration = duration;
    }

    public void activate() {
        this.isActive = true;
        this.activatedTime = System.currentTimeMillis();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public boolean isExpired() {
        if (!isActive) return false;
        return System.currentTimeMillis() - activatedTime > duration;
    }

    public long getRemainingTime() {
        if (!isActive) return 0;
        long elapsed = System.currentTimeMillis() - activatedTime;
        return Math.max(0, duration - elapsed);
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public boolean isActive() {
        return isActive;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
