package net.minestom.server.thread;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;

import java.util.*;

/**
 * Separate work between instance (1 instance = 1 thread execution)
 */
public class PerInstanceThreadProvider extends ThreadProvider {

    private Map<Instance, GroupedInstanceChunk> groupMap = new HashMap<>();

    @Override
    public void start() {
        this.groupMap.clear();
    }

    @Override
    public void linkThread(Instance instance, Chunk chunk) {
        InstanceChunk instanceChunk = new InstanceChunk(instance, chunk);

        GroupedInstanceChunk groupedInstanceChunk = groupMap.computeIfAbsent(instance, inst -> new GroupedInstanceChunk());
        groupedInstanceChunk.instanceChunks.add(instanceChunk);
    }

    @Override
    public void end() {

    }

    @Override
    public void update() {
        final long time = System.currentTimeMillis();

        for (Map.Entry<Instance, GroupedInstanceChunk> entry : groupMap.entrySet()) {
            final Instance instance = entry.getKey();
            final GroupedInstanceChunk groupedInstanceChunk = entry.getValue();

            pool.execute(() -> {
                updateInstance(instance, time);

                for (InstanceChunk instanceChunk : groupedInstanceChunk.instanceChunks) {
                    Chunk chunk = instanceChunk.getChunk();

                    updateChunk(instance, chunk, time);

                    updateEntities(instance, chunk, time);
                }
            });
        }
    }

    /**
     * Contains a list of {@link InstanceChunk}
     */
    private static class GroupedInstanceChunk {
        private List<InstanceChunk> instanceChunks = new ArrayList<>();
    }

    /**
     * Contains both a chunk and its instance
     */
    private static class InstanceChunk {

        private Instance instance;
        private Chunk chunk;

        protected InstanceChunk(Instance instance, Chunk chunk) {
            this.instance = instance;
            this.chunk = chunk;
        }

        public Instance getInstance() {
            return instance;
        }

        public Chunk getChunk() {
            return chunk;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InstanceChunk that = (InstanceChunk) o;
            return Objects.equals(instance, that.instance) &&
                    Objects.equals(chunk, that.chunk);
        }

        @Override
        public int hashCode() {
            return Objects.hash(instance, chunk);
        }
    }

}
