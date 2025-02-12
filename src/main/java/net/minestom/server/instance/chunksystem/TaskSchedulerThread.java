package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.Function;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.event.instance.InstanceRegisterEvent;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * This class is responsible for managing the chunk claims.
 * <p>
 * Because the tree is not thread safe, this class distributes chunk tasks and takes in orders
 * in a thread safe manner.
 * <p>
 * Some of the key concepts of this system are:
 * <ul>
 *     <li>
 *         <b>Lazy updates</b>
 *         <p>
 *             All updates are lazy. This means if a player joins the world with 32 chunk view distance
 *             (a claim is added with 32 radius), the system won't immediately schedule load tasks for all
 *             32 chunks and instead load/unload chunks lazily / on-demand based on priority.
 *         </p>
 *     </li>
 *     <li>
 *         <b>Explicit before implicit updates</b>
 *         <p>
 *             Explicit updates are always handled before implicit updates. This makes chunk claims
 *             faster, because a high priority large radius claim won't stall a lower priority claim.
 *             First the chunks of those claims will be loaded/unloaded, then all implicit chunks originating
 *             from those claims.
 *         </p>
 *     </li>
 *     <li>
 *         <b>Unloads before loads</b><br>
 *         <p>
 *             It is important for all unloads to happen before any new loads. If that was not the case,
 *             when a player moves away from a fully loaded area, the far chunks are unloaded with low priority.
 *             If the player keeps moving and issues new loads with higher priority, because they are closer to
 *             the player, then the far chunks will never be unloaded.
 *         </p>
 *         <p>
 *             A side effect of this is that unloads will be able to stall new loads. Consider a player leaving
 *             the instance. All chunks in the view distance of that player will have to be unloaded before any
 *             new chunks can be loaded. This can cause "lag" during loading of those new chunks.
 *         </p>
 *         <p>
 *             <b>NOTE</b>: In the future some more complex logic could be implemented, like unloads first, but after
 *             two (or X amount of) unloads we see if a load update exists, if so then execute the load update,
 *             continue with unloads after, repeat.
 *             With that approach loads would still happen, but unloads remain (somewhat) prioritized.
 *             This would be more complex though and as of now is not deemed necessary.
 *         </p>
 *     </li>
 *     <li>
 *         <b>Read-Only chunk saving</b>
 *         <p>
 *             Chunk saving is expected to be read-only. When unloading a chunk it has to be saved. But when, during
 *             saving, the chunk should be loaded again we run into issues. If the saving has not yet finished, we'd
 *             load incorrect/incomplete/old data. For this reason chunks in the process of being unloaded are cached,
 *             and if a chunk should be loaded and is in the cache, a copy of the cached chunk will be created and
 *             used instead of the ChunkLoader.
 *         </p>
 *         <p>
 *             The alternative to this is waiting for saving to be complete, which is actually not that simple to
 *             implement without wasting work, and it is guaranteed to be slower than a simple memory copy.
 *         </p>
 *     </li>
 * </ul>
 */
class TaskSchedulerThread implements Runnable {
    /**
     * TODO we could also make this per-instance configurable
     */
    static final PriorityDrop PRIORITY_DROP = switch (ServerFlag.CHUNK_SYSTEM_PRIORITY_DROP) {
        case "simple" -> new PriorityDrop.Simple();
        case "hypotenuse" -> new PriorityDrop.Hypotenuse();
        case null, default -> new PriorityDrop.HypotenuseSquared();
    };

    // Needed to make this reusable.
    private final ReentrantLock mainLock = new ReentrantLock();
    private CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();

    private final ReentrantLock singleThreadedManagerLock = new ReentrantLock();
    private final SingleThreadedManager singleThreadedManager;
    private final int id = ThreadLocalRandom.current().nextInt();

    private final Instance instance;
    private final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();
    private final ManagerSignaling signaling = new ManagerSignaling();
    private final AtomicBoolean testRunning = new AtomicBoolean(false);
    private volatile boolean exit = false;

    public TaskSchedulerThread(@NotNull Instance instance, @Nullable ChunkSupplier chunkSupplier, @Nullable IChunkLoader chunkLoader, ChunkAccess chunkAccess) {
        this.instance = instance;
        this.singleThreadedManager = new SingleThreadedManager(this, chunkAccess);
        this.setChunkLoader(chunkLoader);
        this.setChunkSupplier(Objects.requireNonNullElse(chunkSupplier, DynamicChunk::new));
        this.singleThreadedManager.chunkLoader.loadInstance(instance);

        this.registerEvents();
    }

    @Override
    public void run() {
        this.run(true);

        shutdownFuture.complete(null);
        // after we shut down normally, we shouldn't have to process the remaining tasks.
        // TODO check in the future if this assumption holds
    }

    /**
     * Custom run method in tests.
     * We want less stuff happening async.
     * Otherwise, deadlocks in tests are going to be a real issue to deal with.
     * <p>
     * One example of this:
     * If we add a ticket with radius 10 from inside a test,
     * this method will only return once all tasks for that ticket have been submitted.
     * This means all chunks in the radius have at least started generating.
     */
    private void testRun() {
        if (!ServerFlag.INSIDE_TEST) return;
        while (!this.exit) {
            if (!this.testRunning.compareAndSet(false, true)) {
                // Someone else is already running this logic.
                // One thread should only run this at a time.
                return;
            }
            try {
                this.run(false);
            } finally {
                this.testRunning.set(false);
            }
            // We may have to retry if another task has been submitted
            if (this.tasks.isEmpty()) {
                break;
            }
        }
    }

    private void run(boolean waitForSignal) {
        while (!this.exit) {
            this.signaling.startIteration();
            this.singleThreadedManagerLock.lock();
            SingleThreadedManager.IterationResult res;
            try {
                res = this.syncWork(this.singleThreadedManager::workIteration);
            } finally {
                this.singleThreadedManagerLock.unlock();
            }

            if (this.exit) continue;
            if (res == SingleThreadedManager.IterationResult.WAIT_FOR_SIGNAL_OR_WORKER) {
                ChunkWorker.signalWhenReady(this.signaling);

                if (this.signaling.waitForSignal()) {
                    // A problem occurred. Probably an InterruptedException
                    break;
                }
            } else if (res == SingleThreadedManager.IterationResult.WAIT_FOR_SIGNAL) {
                if (!waitForSignal) break;
                if (this.signaling.waitForSignal()) {
                    // A problem occurred. Probably an InterruptedException
                    break;
                }
            }
        }
    }

    /**
     * This method must be used for any "logic" that the singleThreadedManager should do.
     * <p>
     * This is required to ensure tasks get submitted in FIFO order.
     * Otherwise, a queued addClaim task can be removed by a sync removeClaim call,
     * before the addClaim has been handled.
     * <p>
     * To mitigate this, anytime we execute logic other than a simple getter/setter we
     * must handle all queued tasks first.
     */
    private <T> T syncWork(Supplier<T> supplier) {
        this.singleThreadedManagerLock.lock();
        while (true) {
            var task = this.tasks.poll();
            if (task == null) break;
            this.handleTask(task);
        }
        try {
            return supplier.get();
        } finally {
            this.singleThreadedManagerLock.unlock();
        }
    }

    public @NotNull IChunkLoader getChunkLoader() {
        this.singleThreadedManagerLock.lock();
        try {
            return this.singleThreadedManager.chunkLoader;
        } finally {
            this.singleThreadedManagerLock.unlock();
        }
    }

    public void setChunkLoader(@Nullable IChunkLoader chunkLoader) {
        this.singleThreadedManagerLock.lock();
        try {
            this.singleThreadedManager.chunkLoader = Objects.requireNonNullElse(chunkLoader, IChunkLoader.noop());
        } finally {
            this.singleThreadedManagerLock.unlock();
        }
    }

    public @NotNull ChunkSupplier getChunkSupplier() {
        this.singleThreadedManagerLock.lock();
        try {
            return this.singleThreadedManager.chunkSupplier;
        } finally {
            this.singleThreadedManagerLock.unlock();
        }
    }

    public @Nullable Generator getGenerator() {
        this.singleThreadedManagerLock.lock();
        try {
            return this.singleThreadedManager.generator;
        } finally {
            this.singleThreadedManagerLock.unlock();
        }
    }

    public void setGenerator(@Nullable Generator generator) {
        this.singleThreadedManagerLock.lock();
        try {
            this.singleThreadedManager.generator = generator;
        } finally {
            this.singleThreadedManagerLock.unlock();
        }
    }

    public Instance getInstance() {
        return this.instance;
    }

    public void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier) {
        this.singleThreadedManagerLock.lock();
        try {
            this.singleThreadedManager.chunkSupplier = chunkSupplier;
        } finally {
            this.singleThreadedManagerLock.unlock();
        }
    }

    public boolean isAutosaveEnabled() {
        this.singleThreadedManagerLock.lock();
        try {
            return this.singleThreadedManager.autosaveEnabled;
        } finally {
            this.singleThreadedManagerLock.unlock();
        }
    }

    public void setAutosaveEnabled(boolean autosaveEnabled) {
        this.singleThreadedManagerLock.lock();
        try {
            this.singleThreadedManager.autosaveEnabled = autosaveEnabled;
        } finally {
            this.singleThreadedManagerLock.unlock();
        }
    }

    public @Nullable Chunk getLoadedChunk(int chunkX, int chunkZ) {
        return this.singleThreadedManager.loadedChunk(chunkX, chunkZ);
    }

    private void handleTask(Task task) {
        switch (task) {
            case Task.AddClaim(var x, var z, var chunkAndClaim) ->
                    this.singleThreadedManager.addClaim(x, z, chunkAndClaim);
            case Task.RemoveClaim(var claim, var future) -> this.singleThreadedManager.removeClaim(claim, future);
            case Task.ChunkGenerationFinished(var chunk) -> this.singleThreadedManager.chunkGenerationFinished(chunk);
            case Task.SaveChunk(var chunk, var future) -> this.singleThreadedManager.saveChunk(chunk, future);
            case Task.SaveChunks(var future) -> this.singleThreadedManager.saveChunks(future);
            case Task.SaveInstanceData(var future) -> this.singleThreadedManager.saveInstanceData(future);
            case Task.SaveInstanceDataAndChunks(var future) ->
                    this.singleThreadedManager.saveInstanceDataAndChunks(future);
            case Task.SaveInstanceDataCompleted() -> this.singleThreadedManager.saveInstanceCompleted();
            case Task.SaveChunkCompleted(var x, var z) -> this.singleThreadedManager.saveChunkCompleted(x, z);
            case Task.FinishUnloadAfterSave(var chunk) -> this.singleThreadedManager.finishUnloadChunkAfterSave(chunk);
            case Task.EnqueueUpdate(var update) -> this.singleThreadedManager.enqueue(update);
            case Task.UnloadFuture(var x, var z, var future) -> this.singleThreadedManager.unloadFuture(x, z, future);
            case Task.FinishUnloadAfterSaveAndPartition(var chunk) ->
                    this.singleThreadedManager.finishUnloadChunkAfterSaveAndPartition(chunk);
        }
    }

    void addTask(Task task) {
        this.tasks.add(task);
        this.signaling.signal();
        // If we are inside tests, try to run this right now.
        // If not, this method does nothing
        this.testRun();
    }

    /**
     * TODO
     * Handle future completion on another thread, so we can be sure nobody messes with this thread.
     * One problem with this is order... Starting a virtual thread per future removes any guarantees about the order of updates...
     * One possible approach would be an update queue with a lock and start a virtual thread that does all required queue work.
     */
    <T> void complete(CompletableFuture<T> future, T value) {
        future.complete(value);
        //        Thread.startVirtualThread(() -> future.complete(value));
    }

    /**
     * TODO
     * Handle future completion on another thread, so we can be sure nobody messes with this thread.
     * One problem with this is order... Starting a virtual thread per future removes any guarantees about the order of updates...
     * One possible approach would be an update queue with a lock and start a virtual thread that does all required queue work.
     */
    void completeExceptionally(CompletableFuture<?> future, Throwable throwable) {
        future.completeExceptionally(throwable);
        //        Thread.startVirtualThread(() -> future.completeExceptionally(throwable));
    }

    @SuppressWarnings("unchecked")
    static <T> void link(CompletableFuture<T> future1, CompletableFuture<T> future2) {
        link(future1, future2, v -> (T) v);
    }

    static <T, V> void link(CompletableFuture<T> future1, CompletableFuture<V> future2, Function<T, V> function) {
        future1.whenComplete((val, throwable) -> {
            if (throwable != null) future2.completeExceptionally(throwable);
            else future2.complete(function.apply(val));
        });
    }

    public void addClaimAsync(int x, int z, @NotNull ChunkAndClaim chunkAndClaim) {
        // Used for tests to start async ticking
        this.addTask(new Task.AddClaim(x, z, chunkAndClaim));
    }

    public void removeClaimAsync(@NotNull ChunkClaim claim, @NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.RemoveClaim(claim, future));
    }

    public void saveChunkAsync(@NotNull Chunk chunk, CompletableFuture<Void> future) {
        this.addTask(new Task.SaveChunk(chunk, future));
    }

    public void saveChunksAsync(@NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.SaveChunks(future));
    }

    public void saveInstanceDataAsync(@NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.SaveInstanceData(future));
    }

    public void saveInstanceDataAndChunksAsync(@NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.SaveInstanceDataAndChunks(future));
    }

    public void unloadFutureAsync(int x, int z, @NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.UnloadFuture(x, z, future));
    }

    public @UnmodifiableView @NotNull Collection<@NotNull Chunk> getLoadedChunks() {
        return this.singleThreadedManager.loadedChunks();
    }

    private void registerEvents() {
        // Don't register this inside tests
        if (ServerFlag.INSIDE_TEST) return;
        this.instance.eventNode().addListener(InstanceRegisterEvent.class, event -> {
            mainLock.lock();
            try {
                this.shutdownFuture = new CompletableFuture<>();
                start();
            } finally {
                mainLock.unlock();
            }
        });
        this.instance.eventNode().addListener(InstanceUnregisterEvent.class, event -> {
            try {
                mainLock.lock();
                shutdown().join();
            } finally {
                mainLock.unlock();
            }
        });
    }

    private void start() {
        // We finally start ourselves.
        // Now we start accepting tasks.
        // Virtual thread is appropriate here, we
        // will spend most time waiting for signals/workers.
        // Also, 1 platform thread per instance can become quite expensive
        Thread.ofPlatform().name("ChunkTaskScheduler-" + id).start(this);
    }

    /**
     * friendly shutdown without using interrupts or similar.
     *
     * @return the future for when shutdown has completed
     */
    private CompletableFuture<?> shutdown() {
        this.exit = true;
        this.signaling.signal();
        return this.shutdownFuture;
    }

    static final class LoadTask {
        final IChunkLoader loader;
        final ChunkSupplier chunkSupplier;
        final @Nullable Generator generator;
        final int x;
        final int z;
        double lastUpdatePriority;

        LoadTask(IChunkLoader loader, ChunkSupplier chunkSupplier, @Nullable Generator generator, int x, int z, double lastUpdatePriority) {
            this.loader = loader;
            this.chunkSupplier = chunkSupplier;
            this.generator = generator;
            this.x = x;
            this.z = z;
            this.lastUpdatePriority = lastUpdatePriority;
        }
    }

    static final class UnloadTask {
        final @NotNull Chunk chunk;
        final CompletableFuture<Void> future;
        final CompletableFuture<Void> partitionDeleted = new CompletableFuture<>();

        UnloadTask(@NotNull Chunk chunk, CompletableFuture<Void> future) {
            this.chunk = chunk;
            this.future = future;
        }
    }

    record SaveTask(@NotNull Chunk chunk, @NotNull CompletableFuture<Void> future) {
    }

    sealed interface Task {
        record AddClaim(int x, int z, ChunkAndClaim chunkAndClaim) implements Task {
        }

        record RemoveClaim(@NotNull ChunkClaim claim, @NotNull CompletableFuture<Void> future) implements Task {
        }

        record SaveChunk(@NotNull Chunk chunk, @NotNull CompletableFuture<Void> future) implements Task {
        }

        record SaveChunks(@NotNull CompletableFuture<Void> future) implements Task {
        }

        record SaveInstanceData(@NotNull CompletableFuture<Void> future) implements Task {
        }

        record SaveInstanceDataAndChunks(@NotNull CompletableFuture<Void> future) implements Task {
        }

        record ChunkGenerationFinished(@NotNull Chunk chunk) implements Task {
        }

        record SaveInstanceDataCompleted() implements Task {
        }

        record SaveChunkCompleted(int x, int z) implements Task {
        }

        record EnqueueUpdate(@NotNull PrioritizedUpdate update) implements Task {
        }

        record FinishUnloadAfterSave(@NotNull Chunk chunk) implements Task {
        }

        record UnloadFuture(int x, int z, @NotNull CompletableFuture<Void> future) implements Task {
        }

        record FinishUnloadAfterSaveAndPartition(@NotNull Chunk chunk) implements Task {
        }
    }

    record ClaimedChunk(int x, int z, CompletableFuture<Chunk> future) {
    }
}
