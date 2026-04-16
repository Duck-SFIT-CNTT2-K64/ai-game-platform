package com.nhom_01.robot_pathfinding.ui;

/** Helper to track items being magnetically pulled by Sonar Radar. */
public final class FlyingItem {
    private final double fromX, fromY;
    private final long startMs;
    private final int rwd;
    private double currX, currY;
    private boolean arrived = false;

    public FlyingItem(double gx, double gy, int rwd) {
        this.fromX = gx; this.fromY = gy;
        this.startMs = System.currentTimeMillis();
        this.rwd = rwd;
        this.currX = gx; this.currY = gy;
    }
    public void update(double targetX, double targetY) {
        long elapsed = System.currentTimeMillis() - startMs;
        double duration = 400.0;
        double p = Math.min(1.0, elapsed / duration);
        currX = fromX + (targetX - fromX) * p;
        currY = fromY + (targetY - fromY) * p;
        if (p >= 1.0) arrived = true;
    }
    public double currentX() { return currX; }
    public double currentY() { return currY; }
    public boolean isArrived() { return arrived; }
    public int getReward() { return rwd; }
}
