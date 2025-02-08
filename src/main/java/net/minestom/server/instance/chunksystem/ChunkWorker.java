package net.minestom.server.instance.chunksystem;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

@ApiStatus.Internal
public class ChunkWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkWorker.class);
    /**
     * Use a common worker pool for all managers. A manager may only submit a task if he holds
     * a permit in {@link #AVAILABLE_TASKS}
     */
    private static final ExecutorService WORKER_EXECUTOR;
    /**
     * We allow twice the number of available processors to be submitted before waiting.
     * This is so we don't waste time.
     * As soon as a task finishes, the thread will be able to take the next task,
     * which has already been submitted.
     */
    private static final Semaphore AVAILABLE_TASKS = new Semaphore(Runtime.getRuntime().availableProcessors() * 2);
    
    static {
        WORKER_EXECUTOR = new ForkJoinPool(Runtime
                .getRuntime()
                .availableProcessors(), pool -> new ForkJoinWorkerThread(pool) {
            {
                // Set the priority very low.
                // Generation takes time and resources; we don't want generation to slow down any
                // other logic, such as ticking.
                // Low priority does not mean less total CPU gets used, it just tells the scheduler
                // to prefer more important tasks,
                // and if there is nothing important to do, then do generation.
                setPriority(Thread.MIN_PRIORITY);
            }
        }, null, true);
    }

    @ApiStatus.Internal
    public static CompletableFuture<Void> globalShutdown() {
        WORKER_EXECUTOR.shutdownNow();
        var fut = new CompletableFuture<Void>();
        // Check this late, in the shutdownFuture. Maybe we don't even have to wait for termination
        if (WORKER_EXECUTOR.isTerminated()) {
            fut.complete(null);
        } else {
            Thread.startVirtualThread(() -> {
                try {
                    if (!WORKER_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                        LOGGER.error("Generator pool termination took more than 5 seconds. This indicates the generation for a single chunk took" +
                                " more than 5 seconds. No bueno, fix your generation logic. Server may take a while to shut down...");
                    }
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted generation pool termination waiting. This should not happen", e);
                } finally {
                    fut.complete(null);
                }
            });
        }
        return fut;
    }

    /**
     * Wrapper for worker execution.
     * The worker logic could be improved to dynamically change its behavior based on detected uses.
     * If the average chunk loads are very fast (void gen/simple gen) we could use virtual threads with a lower
     * permit count (as not to use all carrier threads).
     * Even the carrier threads used by chunk generation will not be used very long, and the virtual thread FIFO task queue
     * will not fill up because of the way tasks/loads are lazy.
     */
    private static void runOnWorker(Runnable runnable) {
        WORKER_EXECUTOR.execute(runnable);
    }

    static boolean tryRunOnWorker(Runnable runnable) {
        // try to reserve a worker
        if (!AVAILABLE_TASKS.tryAcquire()) {
            // no worker available
            return false;
        }
        runOnWorker(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                LOGGER.error("Exception during task on worker", t);
            } finally {
                AVAILABLE_TASKS.release();
            }
        });
        return true;
    }
}
