package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Used to link chunks into multiple groups
 * Then executed into a thread pool
 */
public abstract class ThreadProvider {

    /**
     * The thread pool of this thread provider
     */
    protected ExecutorService pool;
    /**
     * The amount of threads in the thread pool
     */
    private int threadCount;

    {
        // Default thread count in the pool
        setThreadCount(5);
    }

    /**
     * Called when a chunk is loaded
     *
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public abstract void onChunkLoad(Instance instance, int chunkX, int chunkZ);

    /**
     * Called when a chunk is unloaded
     *
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public abstract void onChunkUnload(Instance instance, int chunkX, int chunkZ);

    /**
     * Perform a server tick for all chunks based on their linked thread
     *
     * @param time the update time in milliseconds
     * @return the futures to execute to complete the tick
     */
    public abstract List<Future<?>> update(long time);

    /**
     * Get the current size of the thread pool
     *
     * @return the thread pool's size
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Change the amount of threads in the thread pool
     *
     * @param threadCount the new amount of threads
     */
    public synchronized void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        refreshPool();
    }

    private void refreshPool() {
        this.pool = new MinestomThread(threadCount, MinecraftServer.THREAD_NAME_TICK);
    }

    /**
     * INSTANCE UPDATE
     */

    /**
     * Process a whole tick for a chunk
     *
     * @param instance   the instance of the chunk
     * @param chunkIndex the index of the chunk {@link ChunkUtils#getChunkIndex(int, int)}
     * @param time       the time of the update in milliseconds
     */
    protected void processChunkTick(Instance instance, long chunkIndex, long time) {
        final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
        final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);

        final Chunk chunk = instance.getChunk(chunkX, chunkZ);
        if (!ChunkUtils.isLoaded(chunk))
            return;

        updateChunk(instance, chunk, time);

        updateEntities(instance, chunk, time);
    }

    /**
     * Execute an instance tick
     *
     * @param instance the instance
     * @param time     the current time in ms
     */
    protected void updateInstance(Instance instance, long time) {
        // The instance
        instance.tick(time);
        updateSharedInstances(instance, sharedInstance -> updateInstance(sharedInstance, time));
    }

    /**
     * Execute a chunk tick (blocks update)
     *
     * @param instance the chunk's instance
     * @param chunk    the chunk
     * @param time     the current time in ms
     */
    protected void updateChunk(Instance instance, Chunk chunk, long time) {
        chunk.tick(time, instance);
    }

    /**
     * ENTITY UPDATE
     */

    /**
     * Execute an entity tick (all entities type creatures/objects/players) in an instance's chunk
     *
     * @param instance the chunk's instance
     * @param chunk    the chunk
     * @param time     the current time in ms
     */
    protected void updateEntities(Instance instance, Chunk chunk, long time) {
        conditionalEntityUpdate(instance, chunk, time, null);
    }

    /**
     * Execute an entity tick for object entities in an instance's chunk
     *
     * @param instance the chunk's instance
     * @param chunk    the chunk
     * @param time     the current time in ms
     */
    protected void updateObjectEntities(Instance instance, Chunk chunk, long time) {
        conditionalEntityUpdate(instance, chunk, time, entity -> entity instanceof ObjectEntity);
    }

    /**
     * Execute an entity tick for living entities in an instance's chunk
     *
     * @param instance the chunk's instance
     * @param chunk    the chunk
     * @param time     the current time in ms
     */
    protected void updateLivingEntities(Instance instance, Chunk chunk, long time) {
        conditionalEntityUpdate(instance, chunk, time, entity -> entity instanceof LivingEntity);
    }

    /**
     * Execute an entity tick for creatures entities in an instance's chunk
     *
     * @param instance the chunk's instance
     * @param chunk    the chunk
     * @param time     the current time in ms
     */
    protected void updateCreatures(Instance instance, Chunk chunk, long time) {
        conditionalEntityUpdate(instance, chunk, time, entity -> entity instanceof EntityCreature);
    }

    /**
     * Execute an entity tick for players in an instance's chunk
     *
     * @param instance the chunk's instance
     * @param chunk    the chunk
     * @param time     the current time in ms
     */
    protected void updatePlayers(Instance instance, Chunk chunk, long time) {
        conditionalEntityUpdate(instance, chunk, time, entity -> entity instanceof Player);
    }

    /**
     * Execute an entity tick in an instance's chunk if condition is verified
     *
     * @param instance  the chunk's instance
     * @param chunk     the chunk
     * @param time      the current time in ms
     * @param condition the condition which confirm if the update happens or not
     */
    protected void conditionalEntityUpdate(Instance instance, Chunk chunk, long time, Function<Entity, Boolean> condition) {
        final Set<Entity> entities = instance.getChunkEntities(chunk);

        if (!entities.isEmpty()) {
            for (Entity entity : entities) {
                if (condition != null && !condition.apply(entity))
                    continue;
                entity.tick(time);
            }
        }

        updateSharedInstances(instance, sharedInstance -> conditionalEntityUpdate(sharedInstance, chunk, time, condition));
    }

    /**
     * If {@code instance} is an {@link InstanceContainer}, run a callback for all of its
     * {@link SharedInstance}
     *
     * @param instance the instance
     * @param callback the callback to run for all the {@link SharedInstance}
     */
    private void updateSharedInstances(Instance instance, Consumer<SharedInstance> callback) {
        if (instance instanceof InstanceContainer) {
            final InstanceContainer instanceContainer = (InstanceContainer) instance;
            for (SharedInstance sharedInstance : instanceContainer.getSharedInstances()) {
                callback.accept(sharedInstance);
            }
        }
    }

}
