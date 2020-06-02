package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.storage.StorageFolder;
import net.minestom.server.utils.thread.MinestomThread;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.Dimension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public final class InstanceManager {

    private ExecutorService blocksPool = new MinestomThread(MinecraftServer.THREAD_COUNT_BLOCK_UPDATE, MinecraftServer.THREAD_NAME_BLOCK_UPDATE);

    private Set<Instance> instances = Collections.synchronizedSet(new HashSet<>());
    private UpdateType updateType = UpdateType.PER_INSTANCE;

    public InstanceContainer createInstanceContainer(InstanceContainer instanceContainer) {
        this.instances.add(instanceContainer);
        return instanceContainer;
    }

    public InstanceContainer createInstanceContainer(Dimension dimension, StorageFolder storageFolder) {
        InstanceContainer instance = new InstanceContainer(UUID.randomUUID(), dimension, storageFolder);
        return createInstanceContainer(instance);
    }

    public InstanceContainer createInstanceContainer(StorageFolder storageFolder) {
        return createInstanceContainer(Dimension.OVERWORLD, storageFolder);
    }

    public InstanceContainer createInstanceContainer(Dimension dimension) {
        return createInstanceContainer(dimension, null);
    }

    public InstanceContainer createInstanceContainer() {
        return createInstanceContainer(Dimension.OVERWORLD);
    }

    public SharedInstance createSharedInstance(SharedInstance sharedInstance) {
        InstanceContainer instanceContainer = sharedInstance.getInstanceContainer();
        Check.notNull(instanceContainer, "SharedInstance needs to have an InstanceContainer to be created!");

        instanceContainer.addSharedInstance(sharedInstance);
        this.instances.add(sharedInstance);
        return sharedInstance;
    }

    public SharedInstance createSharedInstance(InstanceContainer instanceContainer) {
        Check.notNull(instanceContainer, "Instance container cannot be null when creating a SharedInstance!");

        SharedInstance sharedInstance = new SharedInstance(UUID.randomUUID(), instanceContainer);
        return createSharedInstance(sharedInstance);
    }

    /**
     * Execute a whole block tick update for all instances
     */
    public void updateBlocks() {
        if (instances.isEmpty())
            return;

        long time = System.currentTimeMillis();

        switch (updateType) {
            case PER_INSTANCE:
                perInstanceUpdate(time);
                break;
            case PER_CHUNK:
                perChunkUpdate(time);
                break;
            case SINGLE_THREADED:
                singleThreadedUpdate(time);
                break;
        }
    }

    public Set<Instance> getInstances() {
        return Collections.unmodifiableSet(instances);
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public void setUpdateType(UpdateType updateType) {
        this.updateType = updateType;
    }

    /**
     * A thread per instance
     *
     * @param time the update time
     */
    private void perInstanceUpdate(long time) {
        for (Instance instance : instances) {
            if (instance instanceof InstanceContainer) { // SharedInstance should be updated at the same time (verify?)

                blocksPool.execute(() -> {
                    instance.tick(time);
                    for (Chunk chunk : instance.getChunks()) {
                        chunk.updateBlocks(time, instance);
                    }
                });

            }
        }
    }

    /**
     * A thread per chunk + a different one for the instance tick
     *
     * @param time the update time
     */
    private void perChunkUpdate(long time) {
        for (Instance instance : instances) {
            if (instance instanceof InstanceContainer) { // SharedInstance should be updated at the same time (verify?)

                blocksPool.execute(() -> instance.tick(time));

                for (Chunk chunk : instance.getChunks()) {
                    blocksPool.execute(() -> chunk.updateBlocks(time, instance));
                }

            }
        }
    }

    /**
     * Update everything on the current thread
     *
     * @param time the update time
     */
    private void singleThreadedUpdate(long time) {
        for (Instance instance : instances) {
            if (instance instanceof InstanceContainer) { // SharedInstance should be updated at the same time (verify?)

                instance.tick(time);

                for (Chunk chunk : instance.getChunks()) {
                    chunk.updateBlocks(time, instance);
                }

            }
        }
    }

    public enum UpdateType {
        PER_INSTANCE,
        PER_CHUNK,
        SINGLE_THREADED
    }

}
