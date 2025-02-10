package net.minestom.server.instance.chunksystem;

public record TaskTrackingSnapshot(int runningWorkerTaskCount, int runningSaveTaskCount, int runningTickScheduledCount) {
}
