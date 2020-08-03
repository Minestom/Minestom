package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;

import java.util.HashSet;
import java.util.Set;

public class SingleThreadProvider extends ThreadProvider {

    private Set<Instance> instances = new HashSet<>();
    private long time;

    @Override
    public void start() {
        this.instances.clear();
        this.time = System.currentTimeMillis();
    }

    @Override
    public void linkThread(Instance instance, Chunk chunk) {

        if (instances.add(instance)) {
            updateInstance(instance, time);
        }

        updateChunk(instance, chunk, time);

        updateEntities(instance, chunk, time);
    }

    @Override
    public void end() {

    }

    @Override
    public void update() {

    }
}
