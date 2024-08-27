package net.minestom.server.instance.chunksystem;

import java.util.concurrent.atomic.AtomicInteger;

class TaskTracking {
    final AtomicInteger runningWorkerTaskCount = new AtomicInteger();
    final AtomicInteger runningSaveTaskCount = new AtomicInteger();
    final AtomicInteger runningTickScheduledCount = new AtomicInteger();
}
