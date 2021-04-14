package net.minestom.server.thread.batch;

import net.minestom.server.instance.Chunk;
import net.minestom.server.lock.Acquirable;
import net.minestom.server.thread.BatchThread;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;

public class BatchHandler {

    private final BatchInfo batchInfo = new BatchInfo();

    private final ArrayList<Chunk> chunks = new ArrayList<>();
    private int estimatedCost;

    public void updateChunk(@NotNull Chunk chunk, long time) {
        // Set the BatchInfo field
        //Acquirable.Handler handler = acquirable.getHandler();
        //handler.refreshBatchInfo(batchInfo);

        this.chunks.add(chunk);
        this.estimatedCost++;
    }

    public void pushTask(@NotNull Set<BatchThread> threads, long time) {
        BatchThread fitThread = null;
        int minCost = Integer.MAX_VALUE;

        // Find the thread with the lowest number of tasks
        for (BatchThread thread : threads) {
            final boolean switchThread = fitThread == null || thread.getCost() < minCost;
            if (switchThread) {
                fitThread = thread;
                minCost = thread.getCost();
            }
        }

        Check.notNull(fitThread, "The task thread returned null, something went terribly wrong.");

        // The thread has been decided
        this.batchInfo.refreshThread(fitThread);

        // Create the runnable and send it to the thread for execution in the next tick
        final Runnable runnable = createRunnable(time);
        fitThread.addRunnable(runnable, estimatedCost);
    }

    @NotNull
    private Runnable createRunnable(long time) {
        return () -> {
            for (Chunk chunk : chunks) {
                chunk.tick(time);
                chunk.getInstance().getEntities().forEach(entity -> {
                    entity.tick(time);
                });
            }
        };
    }

}