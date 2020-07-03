package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.thread.MinestomThread;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class DefaultThreadProvider extends ThreadProvider {

    private ExecutorService pool;
    private int threadCount;

    private Map<Instance, GroupedInstanceChunk> groupMap = new HashMap<>();

    {
        setThreadCount(5);
    }

    @Override
    public void start() {
        this.groupMap.clear();
    }

    @Override
    public void linkThread(Instance instance, Chunk chunk) {
        InstanceChunk instanceChunk = new InstanceChunk(instance, chunk);

        GroupedInstanceChunk groupedInstanceChunk = groupMap.getOrDefault(instance, new GroupedInstanceChunk());
        groupedInstanceChunk.instanceChunks.add(instanceChunk);

        this.groupMap.put(instance, groupedInstanceChunk);
    }

    @Override
    public void end() {
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

    public int getThreadCount() {
        return threadCount;
    }

    public synchronized void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        refreshPool();
    }

    private void refreshPool() {
        this.pool = new MinestomThread(threadCount, MinecraftServer.THREAD_NAME_TICK);
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
