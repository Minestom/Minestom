package net.minestom.server.instance.chunksystem;

import net.minestom.server.ServerFlag;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@ApiStatus.Internal
public class ChunkWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkWorker.class);
    private static final WeakHashMap<ChunkLoader, Boolean> WARNED_LOADERS = new WeakHashMap<>();
    /**
     * Use a common worker pool for all managers. A manager may only submit a task if he holds
     * a permit in {@link #AVAILABLE_TASKS}
     */
    private static ExecutorService WORKER_EXECUTOR;
    private static ExecutorService SAVE_EXECUTOR;
    /**
     * We allow twice the number of available processors to be submitted before waiting.
     * This is so we don't waste time.
     * As soon as a task finishes, the thread will be able to take the next task,
     * which has already been submitted.
     */
    private static final Semaphore AVAILABLE_TASKS = new Semaphore(Runtime.getRuntime().availableProcessors() * 2);
    private static final ReentrantLock WAITING_LOCK = new ReentrantLock();
    private static final Set<ManagerSignaling> WAITING = new HashSet<>();

    private final TaskSchedulerThread taskSchedulerThread;
    private final ChunkGenerationHandler chunkGenerationHandler;
    private final Instance instance;
    private final ChunkAccess chunkAccess;

    ChunkWorker(TaskSchedulerThread taskSchedulerThread, ChunkAccess chunkAccess) {
        this.taskSchedulerThread = taskSchedulerThread;
        this.instance = taskSchedulerThread.getInstance();
        this.chunkGenerationHandler = new ChunkGenerationHandler(this.instance);
        this.chunkAccess = chunkAccess;
    }

    void workerCopyFromMemory(Chunk unloading, int x, int z) {
        try {
            var copy = unloading.copy(unloading.getInstance(), x, z);
            // TODO copy entities
            this.workerFinishedGeneration(copy);
        } catch (Throwable throwable) {
            LOGGER.error("Exception while re-loading chunk", throwable);
        }
    }

    void workerGenerateChunk(int x, int z, @NotNull ChunkLoader loader, @NotNull ChunkSupplier supplier, @Nullable Generator generator) {
        if (!loader.supportsParallelLoading()) {
            // TODO maybe revisit and add locking to allow for non-parallel loaders, but not right now
            synchronized (WARNED_LOADERS) {
                if (!WARNED_LOADERS.containsKey(loader)) {
                    LOGGER.error("ChunkLoaders must support parallel loading. Please migrate your system. Violating loader: {}", loader, new AssertionError());
                    WARNED_LOADERS.put(loader, true);
                }
            }
        }

        var chunk = loader.loadChunk(instance, x, z);
        if (chunk == null) {
            // Loader couldn't load the chunk, generate it
            chunk = this.chunkGenerationHandler.createChunk(supplier, generator, x, z);
            chunk.onGenerate();
        }

        this.workerFinishedGeneration(chunk);
    }

    void workerFinishedGeneration(Chunk chunk) {
        this.taskSchedulerThread.addTask(new TaskSchedulerThread.Task.ChunkGenerationFinished(chunk));
        // TODO
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
        // We want to execute everything sync when running tests.
        // If we don't, we don't know when the async logic finishes and
        // potential new tasks get submitted to the task queue,
        // which may cause partition updates to be submitted, which
        // in turn only gets updated when ticking.
        // But because it is async, we don't know when we need to tick,
        // so we are in a pickle.
        if (!ServerFlag.ASYNC_CHUNK_SYSTEM) runnable.run();
        else WORKER_EXECUTOR.execute(runnable);
    }

    static boolean tryReserve() {
        return AVAILABLE_TASKS.tryAcquire();
    }

    static void release() {
        AVAILABLE_TASKS.release();
    }

    /**
     * We need this unconventional logic to make sure the manager thread can wake up from other sources, too
     */
    static void signalWhenReady(ManagerSignaling signaling) {
        if (AVAILABLE_TASKS.availablePermits() > 0) {
            signaling.signal();
        } else {
            WAITING_LOCK.lock();
            try {
                if (AVAILABLE_TASKS.availablePermits() > 0) {
                    signaling.signal();
                    return;
                }
                WAITING.add(signaling);
            } finally {
                WAITING_LOCK.unlock();
            }
        }
    }

    private static void signalAll() {
        WAITING_LOCK.lock();
        try {
            for (var managerSignaling : WAITING) {
                managerSignaling.signal();
            }
            WAITING.clear();
        } finally {
            WAITING_LOCK.unlock();
        }
    }

    static void submitReserved(Runnable runnable) {
        runOnWorker(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                LOGGER.error("Exception during task on worker", t);
            } finally {
                release();
                signalAll();
            }
        });
    }

    static void runOnSaveExecutor(Runnable runnable) {
        SAVE_EXECUTOR.execute(runnable);
    }

    static {
        globalInit();
    }

    @ApiStatus.Internal
    public static synchronized CompletableFuture<Void> globalShutdown() {
        // TODO make sure these workers are shut down correctly later
        if (true) return CompletableFuture.completedFuture(null);
        if (WORKER_EXECUTOR == null) throw new IllegalStateException();
        // We give the workers a little time (5 seconds) to shut down.
        // We give chunk saving more time (60 seconds).
        // Considering it's probably IO bound, and we don't want corrupt chunks,
        // a minute should be appropriate to ensure it's more than enough time.
        var fut = CompletableFuture.allOf(shutdown(WORKER_EXECUTOR, 5), shutdown(SAVE_EXECUTOR, 60));
        WORKER_EXECUTOR = null;
        SAVE_EXECUTOR = null;
        return fut;
    }

    @ApiStatus.Internal
    public static synchronized void globalInit() {
//        if (WORKER_EXECUTOR != null) throw new IllegalStateException();
        if (WORKER_EXECUTOR != null) return;
        WORKER_EXECUTOR = createLowPriority(1);

        // We should be able to get away with using fewer threads here.
        // Saving chunks should be rare enough.
        // TODO benchmark this
        SAVE_EXECUTOR = createLowPriority(0.5);
    }

    private static CompletableFuture<Void> shutdown(ExecutorService service, int timeoutSeconds) {
        var fut = new CompletableFuture<Void>();
        service.shutdown();
        if (service.isTerminated()) {
            fut.complete(null);
            LOGGER.info("Already shutdown {}", timeoutSeconds);
        } else {
            Thread.ofPlatform().daemon(true).start(() -> {
                try {
                    LOGGER.info("Start shutdown {}", timeoutSeconds);
                    if (!service.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                        LOGGER.error("Pool termination took more than {} seconds. This is not good", timeoutSeconds);
                    }
                    LOGGER.info("Stop complete");
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted generation pool termination waiting. This should not happen", e);
                } finally {
                    fut.complete(null);
                }
            });
        }
        return fut;
    }

    static ForkJoinPool createLowPriority(double multiplier) {
        var parallelism = Math.max(1, (int) (Runtime.getRuntime().availableProcessors() * multiplier));
        return new ForkJoinPool(parallelism, pool -> new ForkJoinWorkerThread(pool) {
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
}
