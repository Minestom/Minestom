package fr.themode.minestom.instance;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.utils.thread.MinestomThread;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class InstanceManager {

    private ExecutorService blocksPool = new MinestomThread(MinecraftServer.THREAD_COUNT_BLOCK_UPDATE, MinecraftServer.THREAD_NAME_BLOCK_UPDATE);

    private Set<Instance> instances = Collections.synchronizedSet(new HashSet<>());

    public InstanceContainer createInstanceContainer(File folder) {
        if (folder != null && !folder.exists())
            folder.mkdir();

        InstanceContainer instance = new InstanceContainer(UUID.randomUUID(), folder);
        this.instances.add(instance);
        return instance;
    }

    public InstanceContainer createInstanceContainer() {
        return createInstanceContainer(null);
    }

    public SharedInstance createSharedInstance(InstanceContainer instanceContainer) {
        if (instanceContainer == null)
            throw new IllegalArgumentException("Instance container cannot be null when creating a Shared instance!");

        SharedInstance sharedInstance = new SharedInstance(UUID.randomUUID(), instanceContainer);
        instanceContainer.addSharedInstance(sharedInstance);
        this.instances.add(sharedInstance);
        return sharedInstance;
    }

    public void updateBlocks() {
        if (instances.isEmpty())
            return;

        long time = System.currentTimeMillis();
        for (Instance instance : instances) {
            if (instance instanceof InstanceContainer) { // SharedInstance should be updated at the same time (verify?)

                blocksPool.execute(() -> {
                    for (Chunk chunk : instance.getChunks()) {
                        chunk.updateBlocks(time, instance);
                    }
                });

            }
        }
    }

    public Set<Instance> getInstances() {
        return Collections.unmodifiableSet(instances);
    }

}
