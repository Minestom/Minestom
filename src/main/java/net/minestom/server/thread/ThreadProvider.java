package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.SharedInstance;
import net.minestom.server.lock.Acquirable;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Used to link chunks into multiple groups.
 * Then executed into a thread pool.
 */
public abstract class ThreadProvider {

    private final List<BatchThread> threads;

    private final Map<BatchThread, Set<ChunkEntry>> threadChunkMap = new HashMap<>();
    private final Map<Chunk, ChunkEntry> chunkEntryMap = new HashMap<>();
    // Iterated over to refresh the thread used to update entities & chunks
    private final ArrayDeque<Chunk> chunks = new ArrayDeque<>();
    private final Set<Entity> removedEntities = ConcurrentHashMap.newKeySet();

    // Represents the maximum percentage of tick time
    // that can be spent refreshing chunks thread
    protected double refreshPercentage = 0.3f;
    // Minimum refresh time
    private int min = 3;
    private int max = (int) (MinecraftServer.TICK_MS * 0.3);

    public ThreadProvider(int threadCount) {
        this.threads = new ArrayList<>(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final BatchThread.BatchRunnable batchRunnable = new BatchThread.BatchRunnable();
            final BatchThread batchThread = new BatchThread(batchRunnable, i);
            this.threads.add(batchThread);

            batchThread.start();
        }
    }

    public synchronized void onInstanceCreate(@NotNull Instance instance) {
        instance.getChunks().forEach(this::addChunk);
    }

    public synchronized void onInstanceDelete(@NotNull Instance instance) {
        instance.getChunks().forEach(this::removeChunk);
    }

    public synchronized void onChunkLoad(Chunk chunk) {
        addChunk(chunk);
    }

    public synchronized void onChunkUnload(Chunk chunk) {
        removeChunk(chunk);
    }

    /**
     * Performs a server tick for all chunks based on their linked thread.
     *
     * @param chunk the chunk
     */
    public abstract long findThread(@NotNull Chunk chunk);

    protected void addChunk(Chunk chunk) {
        ChunkEntry chunkEntry = setChunkThread(chunk, (thread) -> new ChunkEntry(thread, chunk));
        this.chunkEntryMap.put(chunk, chunkEntry);
        this.chunks.add(chunk);
    }

    protected void switchChunk(@NotNull Chunk chunk) {
        ChunkEntry chunkEntry = chunkEntryMap.get(chunk);
        if (chunkEntry == null)
            return;
        var chunks = threadChunkMap.get(chunkEntry.thread);
        if (chunks == null || chunks.isEmpty())
            return;
        chunks.remove(chunkEntry);

        setChunkThread(chunk, batchThread -> {
            chunkEntry.thread = batchThread;
            return chunkEntry;
        });
    }

    protected @NotNull ChunkEntry setChunkThread(@NotNull Chunk chunk,
                                                 @NotNull Function<@NotNull BatchThread, @NotNull ChunkEntry> chunkEntrySupplier) {
        final int threadId = getThreadId(chunk);
        BatchThread thread = threads.get(threadId);
        var chunks = threadChunkMap.computeIfAbsent(thread, batchThread -> ConcurrentHashMap.newKeySet());

        ChunkEntry chunkEntry = chunkEntrySupplier.apply(thread);
        chunks.add(chunkEntry);
        return chunkEntry;
    }

    protected void removeChunk(Chunk chunk) {
        ChunkEntry chunkEntry = chunkEntryMap.get(chunk);
        if (chunkEntry != null) {
            BatchThread thread = chunkEntry.thread;
            var chunks = threadChunkMap.get(thread);
            if (chunks != null) {
                chunks.remove(chunkEntry);
            }
            chunkEntryMap.remove(chunk);
        }
        this.chunks.remove(chunk);
    }

    protected int getThreadId(Chunk chunk) {
        return (int) (Math.abs(findThread(chunk)) % threads.size());
    }

    /**
     * Prepares the update.
     *
     * @param time the tick time in milliseconds
     */
    public synchronized @NotNull CountDownLatch update(long time) {
        CountDownLatch countDownLatch = new CountDownLatch(threads.size());
        for (BatchThread thread : threads) {
            final var chunkEntries = threadChunkMap.get(thread);
            if (chunkEntries == null || chunkEntries.isEmpty()) {
                // The thread never had any task
                countDownLatch.countDown();
                continue;
            }

            // Execute tick
            thread.getMainRunnable().startTick(countDownLatch, () -> {
                final var entitiesList = chunkEntries.stream().map(chunkEntry -> chunkEntry.entities).collect(Collectors.toList());
                final var entities = entitiesList.stream()
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                Acquirable.refreshEntities(Collections.unmodifiableList(entities));

                final ReentrantLock lock = thread.getLock();
                lock.lock();
                chunkEntries.forEach(chunkEntry -> {
                    Chunk chunk = chunkEntry.chunk;
                    if (!ChunkUtils.isLoaded(chunk))
                        return;
                    chunk.tick(time);
                    chunkEntry.entities.forEach(entity -> {
                        final boolean hasQueue = lock.hasQueuedThreads();
                        if (hasQueue) {
                            lock.unlock();
                            // #acquire callbacks should be called here
                            lock.lock();
                        }
                        entity.tick(time);
                    });
                });
                lock.unlock();
            });
        }
        return countDownLatch;
    }

    public synchronized void refreshThreads(long tickTime) {
        // Clear removed entities
        {
            for (Entity entity : removedEntities) {
                Acquirable<Entity> acquirable = entity.getAcquiredElement();
                ChunkEntry chunkEntry = acquirable.getHandler().getChunkEntry();
                // Remove from list
                if (chunkEntry != null) {
                    chunkEntry.entities.remove(entity);
                }
            }
            this.removedEntities.clear();
        }


        final int timeOffset = MathUtils.clamp((int) ((double) tickTime * refreshPercentage), min, max);
        final long endTime = System.currentTimeMillis() + timeOffset;
        final int size = chunks.size();
        int counter = 0;
        while (true) {
            Chunk chunk = chunks.pollFirst();
            if (!ChunkUtils.isLoaded(chunk)) {
                removeChunk(chunk);
                return;
            }

            // Update chunk threads
            {
                switchChunk(chunk);
            }

            // Update entities
            {
                Instance instance = chunk.getInstance();
                refreshEntitiesThread(instance, chunk);
                if (instance instanceof InstanceContainer) {
                    for (SharedInstance sharedInstance : ((InstanceContainer) instance).getSharedInstances()) {
                        refreshEntitiesThread(sharedInstance, chunk);
                    }
                }
            }

            // Add back to the deque
            chunks.addLast(chunk);

            if (++counter > size)
                break;

            if (System.currentTimeMillis() >= endTime)
                break;

        }
        System.out.println("update " + counter);
    }

    public void removeEntity(@NotNull Entity entity) {
        this.removedEntities.add(entity);
    }

    public void shutdown() {
        this.threads.forEach(BatchThread::shutdown);
    }

    public @NotNull List<@NotNull BatchThread> getThreads() {
        return threads;
    }

    private void refreshEntitiesThread(Instance instance, Chunk chunk) {
        var entities = instance.getChunkEntities(chunk);
        for (Entity entity : entities) {
            Acquirable<Entity> acquirable = entity.getAcquiredElement();
            ChunkEntry handlerChunkEntry = acquirable.getHandler().getChunkEntry();
            Chunk batchChunk = handlerChunkEntry != null ? handlerChunkEntry.getChunk() : null;

            Chunk entityChunk = entity.getChunk();
            if (!Objects.equals(batchChunk, entityChunk)) {
                // Entity is possibly not in the correct thread

                // Remove from previous list
                {
                    if (handlerChunkEntry != null) {
                        handlerChunkEntry.entities.remove(entity);
                    }
                }

                // Add to new list
                {
                    ChunkEntry chunkEntry = chunkEntryMap.get(entityChunk);
                    if (chunkEntry != null) {
                        chunkEntry.entities.add(entity);
                        acquirable.getHandler().refreshChunkEntry(chunkEntry);
                    }
                }
            }
        }
    }

    public static class ChunkEntry {
        private volatile BatchThread thread;
        private final Chunk chunk;
        private final List<Entity> entities = new ArrayList<>();

        private ChunkEntry(BatchThread thread, Chunk chunk) {
            this.thread = thread;
            this.chunk = chunk;
        }

        public @NotNull BatchThread getThread() {
            return thread;
        }

        public @NotNull Chunk getChunk() {
            return chunk;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkEntry that = (ChunkEntry) o;
            return chunk.equals(that.chunk);
        }

        @Override
        public int hashCode() {
            return Objects.hash(chunk);
        }
    }

}