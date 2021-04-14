package net.minestom.server.thread;

import com.google.common.collect.Queues;
import net.minestom.server.lock.Acquisition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;

/**
 * Represents the data of a {@link BatchThread} involved in acquisition.
 * <p>
 * Used as a lock until an acquirable element is available.
 */
public class BatchQueue {

    private final Queue<Acquisition.AcquisitionData> acquisitionDataQueue = Queues.newConcurrentLinkedQueue();

    private volatile Thread waitingThread;

    @NotNull
    public Queue<Acquisition.AcquisitionData> getQueue() {
        return acquisitionDataQueue;
    }

    @Nullable
    public Thread getWaitingThread() {
        return waitingThread;
    }

    public void setWaitingThread(@Nullable Thread waitingThread) {
        this.waitingThread = waitingThread;
    }
}