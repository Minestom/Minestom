package net.minestom.server.monitoring;

public class ThreadResult {

    private final double cpuPercentage;
    private final double userPercentage;
    private final double waitedPercentage;
    private final double blockedPercentage;

    protected ThreadResult(double cpuPercentage,
                           double userPercentage,
                           double waitedPercentage,
                           double blockedPercentage) {
        this.cpuPercentage = cpuPercentage;
        this.userPercentage = userPercentage;
        this.waitedPercentage = waitedPercentage;
        this.blockedPercentage = blockedPercentage;
    }

    public double getCpuPercentage() {
        return cpuPercentage;
    }

    public double getUserPercentage() {
        return userPercentage;
    }

    public double getWaitedPercentage() {
        return waitedPercentage;
    }

    public double getBlockedPercentage() {
        return blockedPercentage;
    }
}
