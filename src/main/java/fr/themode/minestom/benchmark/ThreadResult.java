package fr.themode.minestom.benchmark;

public class ThreadResult {

    private double cpuPercentage, userPercentage, blockedPercentage;

    protected ThreadResult(double cpuPercentage, double userPercentage, double blockedPercentage) {
        this.cpuPercentage = cpuPercentage;
        this.userPercentage = userPercentage;
        this.blockedPercentage = blockedPercentage;
    }

    public double getCpuPercentage() {
        return cpuPercentage;
    }

    public double getUserPercentage() {
        return userPercentage;
    }

    public double getBlockedPercentage() {
        return blockedPercentage;
    }
}
