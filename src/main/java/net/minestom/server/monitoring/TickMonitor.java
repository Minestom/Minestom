package net.minestom.server.monitoring;

public class TickMonitor {

    private final double tickTime;

    public TickMonitor(double tickTime) {
        this.tickTime = tickTime;
    }

    public double getTickTime() {
        return tickTime;
    }
}