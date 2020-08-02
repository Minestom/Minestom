package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.Set;
import java.util.concurrent.ExecutorService;
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
     * Called to prepare the thread provider, to provide threads for the next server tick
     */
    public abstract void start();

    /**
     * Assign a thread to a chunk, create one if none is defined
     *
     * @param instance the instance where the chunk is
     * @param chunk    the chunk which should get an assigned thread
     */
    public abstract void linkThread(Instance instance, Chunk chunk);

    /**
     * Inform the server that all chunks have been assigned to a thread
     */
    public abstract void end();

    /**
     * Perform a server tick for all chunks based on their linked thread
     */
    public abstract void update();

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
     * Execute an instance tick
     *
     * @param instance the instance
     * @param time     the current time in ms
     */
    protected void updateInstance(Instance instance, long time) {
        instance.tick(time);
    }

    /**
     * Execute a chunk tick (blocks update)
     *
     * @param instance the chunk's instance
     * @param chunk    the chunk
     * @param time     the current time in ms
     */
    protected void updateChunk(Instance instance, Chunk chunk, long time) {
        chunk.updateBlocks(time, instance);
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
        Set<Entity> entities = instance.getChunkEntities(chunk);

        if (!entities.isEmpty()) {
            for (Entity entity : entities) {
                if (condition != null && !condition.apply(entity))
                    continue;
                entity.tick(time);
            }
        }
    }

}
