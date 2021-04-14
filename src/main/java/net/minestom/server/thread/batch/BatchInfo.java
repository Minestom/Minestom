package net.minestom.server.thread.batch;

import net.minestom.server.thread.BatchThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Object shared between all elements of a batch.
 * <p>
 * Used to retrieve the element tick thread.
 */
public class BatchInfo {

    private volatile BatchThread batchThread;

    @Nullable
    public BatchThread getBatchThread() {
        return batchThread;
    }

    /**
     * Specifies in which thread this element will be updated.
     * Currently defined before every tick for all game elements in {@link BatchHandler#pushTask(Set, long)}.
     *
     * @param batchThread the thread where this element will be updated
     */
    public void refreshThread(@NotNull BatchThread batchThread) {
        this.batchThread = batchThread;
    }

}