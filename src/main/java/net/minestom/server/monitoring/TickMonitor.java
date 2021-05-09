package net.minestom.server.monitoring;

public class TickMonitor {

    private final double tickTime;
    private final double acquisitionTime;

    public TickMonitor(double tickTime, double acquisitionTime) {
        this.tickTime = tickTime;
        this.acquisitionTime = acquisitionTime;
    }

    public double getTickTime() {
        return tickTime;
    }

    public double getAcquisitionTime() {
        return acquisitionTime;
    }
}
