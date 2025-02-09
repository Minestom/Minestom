package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.instance.InstanceChunkLoadEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

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
class ChunkClaimManager implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChunkClaimManager.class);
    /**
     * TODO we could also make this per-instance configurable
     */
    static final PriorityDrop PRIORITY_DROP = switch (ServerFlag.CHUNK_SYSTEM_PRIORITY_DROP) {
        case "simple" -> new PriorityDrop.Simple();
        case "hypotenuse" -> new PriorityDrop.Hypotenuse();
        case null, default -> new PriorityDrop.HypotenuseSquared();
    };

    private final CompletableFuture<Void> shutdownFuture = new CompletableFuture<>();
    private final SingleThreadedManager singleThreadedManager;


    private final Instance instance;
    private final ConcurrentLinkedQueue<Task> tasks = new ConcurrentLinkedQueue<>();
    private final ManagerSignaling signaling = new ManagerSignaling();
    private volatile boolean exit = false;

    public ChunkClaimManager(@NotNull Instance instance, @Nullable ChunkSupplier chunkSupplier, @Nullable IChunkLoader chunkLoader, ChunkAccess chunkAccess) {
        this.instance = instance;
        this.singleThreadedManager = new SingleThreadedManager(this, chunkAccess);
        this.setChunkLoader(chunkLoader);
        this.setChunkSupplier(Objects.requireNonNullElse(chunkSupplier, DynamicChunk::new));
        this.singleThreadedManager.chunkLoader.loadInstance(instance);
        // We finally start ourselves.
        // Now we start accepting tasks.
        // Virtual thread is appropriate here, we
        // will spend most time waiting for signals/workers.
        // Also, 1 platform thread per instance can become quite expensive
        Thread.startVirtualThread(this);
    }

    @Override
    public void run() {
        while (!this.exit) {
            this.signaling.startIteration();
            while (true) {
                var task = this.tasks.poll();
                if (task == null) break;
                this.workTask(task);
            }
            this.workIteration();
            if (this.hasUpdated || this.exit) continue;

            if (this.signaling.waitForSignal()) {
                // A problem occurred. Probably an InterruptedException
                break;
            }
        }
        // after we shut down normally, we shouldn't have to process the remaining tasks.
        // TODO check in the future if this assumption holds
    }

    public @NotNull IChunkLoader getChunkLoader() {
        return this.singleThreadedManager.chunkLoader;
    }

    public void setChunkLoader(@Nullable IChunkLoader chunkLoader) {
        this.singleThreadedManager.chunkLoader = Objects.requireNonNullElse(chunkLoader, IChunkLoader.noop());
    }

    public @NotNull ChunkSupplier getChunkSupplier() {
        return this.singleThreadedManager.chunkSupplier;
    }

    public @Nullable Generator getGenerator() {
        return this.singleThreadedManager.generator;
    }

    public void setGenerator(@Nullable Generator generator) {
        this.singleThreadedManager.generator = generator;
    }

    public Instance getInstance() {
        return this.instance;
    }

    public void setChunkSupplier(@NotNull ChunkSupplier chunkSupplier) {
        this.singleThreadedManager.chunkSupplier = chunkSupplier;
    }

    public boolean isAutosaveEnabled() {
        return this.singleThreadedManager.autosaveEnabled;
    }

    public void setAutosaveEnabled(boolean autosaveEnabled) {
        this.singleThreadedManager.autosaveEnabled = autosaveEnabled;
    }

    public @Nullable Chunk getLoadedChunk(int chunkX, int chunkZ) {
        return this.singleThreadedManager.chunks.get(CoordConversion.chunkIndex(chunkX, chunkZ));
    }

    private void workTask(Task task) {
        switch (task) {
            case Task.AddClaim addClaim ->
                    this.singleThreadedManager.workAddClaim(addClaim.x, addClaim.z, addClaim.chunkAndClaim);
            case Task.RemoveClaim removeClaim ->
                    this.singleThreadedManager.workRemoveClaim(removeClaim.claim, removeClaim.future);
            case Task.ChunkGenerationFinished chunkGenerationFinished ->
                    this.singleThreadedManager.workChunkGenerationFinished(chunkGenerationFinished.chunk);
            case Task.SaveChunk saveChunk ->
                    this.singleThreadedManager.workSaveChunk(saveChunk.chunk, saveChunk.future);
            case Task.SaveChunks saveChunks -> this.singleThreadedManager.workSaveChunks(saveChunks.future);
            case Task.SaveInstanceData saveInstanceData ->
                    this.singleThreadedManager.workSaveInstanceData(saveInstanceData.future);
            case Task.SaveInstanceDataAndChunks saveInstanceDataAndChunks ->
                    this.singleThreadedManager.workSaveInstanceDataAndChunks(saveInstanceDataAndChunks.future);
            case Task.SaveInstanceDataCompleted saveInstanceDataCompleted ->
                    this.singleThreadedManager.workSaveInstanceCompleted();
            case Task.SaveChunkCompleted saveChunkCompleted ->
                    this.singleThreadedManager.workSaveChunkCompleted(saveChunkCompleted.x(), saveChunkCompleted.z());
            case Task.RetryUnload retryUnload -> this.singleThreadedManager.unloadChunk(retryUnload.chunk);
            case Task.FinishUnloadAfterSave finishUnloadAfterSave ->
                    this.singleThreadedManager.finishUnloadChunk(finishUnloadAfterSave.chunk());
        }
    }

    void addTask(Task task) {
        this.tasks.add(task);
        this.signaling.signal();
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

    public void addClaim(int x, int z, @NotNull ChunkAndClaim chunkAndClaim) {
        this.addTask(new Task.AddClaim(x, z, chunkAndClaim));
    }

    public void removeClaim(@NotNull ChunkClaim claim, @NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.RemoveClaim(claim, future));
    }

    public void saveChunk(@NotNull Chunk chunk, CompletableFuture<Void> future) {
        this.addTask(new Task.SaveChunk(chunk, future));
    }

    public void saveChunks(@NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.SaveChunks(future));
    }

    public void saveInstanceData(@NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.SaveInstanceData(future));
    }

    public void saveInstanceDataAndChunks(@NotNull CompletableFuture<Void> future) {
        this.addTask(new Task.SaveInstanceDataAndChunks(future));
    }

    public @UnmodifiableView @NotNull Collection<@NotNull Chunk> getLoadedChunks() {
        return Collections.unmodifiableCollection(this.singleThreadedManager.chunks.values());
    }

    /**
     * friendly shutdown without using interrupts or similar.
     *
     * @return the future for when shutdown has completed
     */
    public CompletableFuture<?> shutdown() {
        this.exit = true;
        this.signaling.signal();
        return this.shutdownFuture;
    }

    static final class LoadTask {
        final int x;
        final int z;
        double lastUpdatePriority;

        LoadTask(int x, int z, double lastUpdatePriority) {
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

        record FinishUnloadAfterSave(@NotNull Chunk chunk) implements Task {
        }

        /**
         * This may seem weird, so I'm gonna explain it.
         * A chunk may be unloaded. Unloading is not instant, so it may take a while.
         * If the same chunk gets loaded during that time, it won't wait for unloading to finish.
         * It sees there is still a stable version of the chunk in memory, so it just copies that version.
         * If that second chunk then gets unloaded, then we can have two unloads at the same time.
         * This is, obviously, not valid behavior. To mitigate this, the newer chunk unload will be delayed,
         * until the first chunk finishes unloading. When that happens, then this task will be submitted
         * (from the future) to unload the second chunk
         */
        record RetryUnload(@NotNull Chunk chunk) implements Task {
        }
    }

    record ClaimedChunk(int x, int z, CompletableFuture<Chunk> future) {
    }
}
