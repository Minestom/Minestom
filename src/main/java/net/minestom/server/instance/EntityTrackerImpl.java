package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.LongFunction;

import static net.minestom.server.utils.chunk.ChunkUtils.forDifferingChunksInRange;
import static net.minestom.server.utils.chunk.ChunkUtils.getChunkIndex;

final class EntityTrackerImpl implements EntityTracker {
    static final AtomicInteger TARGET_COUNTER = new AtomicInteger();
    private static final LongFunction<List<Entity>> LIST_SUPPLIER = l -> new CopyOnWriteArrayList<>();

    // Store all data associated to a Target
    // The array index is the Target enum ordinal
    private final TargetEntry<Entity>[] entries = EntityTracker.Target.TARGETS.stream().map((Function<Target<?>, TargetEntry>) TargetEntry::new).toArray(TargetEntry[]::new);

    @Override
    public synchronized <T extends Entity> void register(@NotNull Entity entity, @NotNull Point point,
                                                         @NotNull Target<T> target, @Nullable Update<T> update) {
        final long index = getChunkIndex(point);
        for (TargetEntry<Entity> entry : entries) {
            if (entry.target.type().isInstance(entity)) {
                entry.entities.add(entity);
                entry.addToChunk(index, entity);
            }
        }
        if (update != null) {
            visibleEntities(point, target, update::add);
            update.updateTracker(point, this);
        }
    }

    @Override
    public synchronized <T extends Entity> void unregister(@NotNull Entity entity, @NotNull Point point,
                                                           @NotNull Target<T> target, @Nullable Update<T> update) {
        final long index = getChunkIndex(point);
        for (TargetEntry<Entity> entry : entries) {
            if (entry.target.type().isInstance(entity)) {
                entry.entities.remove(entity);
                entry.removeFromChunk(index, entity);
            }
        }
        if (update != null) {
            visibleEntities(point, target, update::remove);
            update.updateTracker(point, null);
        }
    }

    @Override
    public <T extends Entity> void move(@NotNull Entity entity,
                                        @NotNull Point oldPoint, @NotNull Point newPoint,
                                        @NotNull Target<T> target, @Nullable Update<T> update) {
        if (oldPoint.sameChunk(newPoint)) return;
        synchronized (this) {
            final long oldIndex = getChunkIndex(oldPoint);
            final long newIndex = getChunkIndex(newPoint);
            for (TargetEntry<Entity> entry : entries) {
                if (entry.target.type().isInstance(entity)) {
                    entry.addToChunk(newIndex, entity);
                    entry.removeFromChunk(oldIndex, entity);
                }
            }
            if (update != null) {
                difference(oldPoint, newPoint, target, update);
                update.updateTracker(newPoint, this);
            }
        }
    }

    @Override
    public synchronized <T extends Entity> void difference(int oldChunkX, int oldChunkZ,
                                                           int newChunkX, int newChunkZ,
                                                           @NotNull Target<T> target, @NotNull Update<T> update) {
        final TargetEntry<Entity> entry = entries[target.ordinal()];
        forDifferingChunksInRange(newChunkX, newChunkZ, oldChunkX, oldChunkZ,
                MinecraftServer.getEntityViewDistance(), (chunkX, chunkZ) -> {
                    // Add
                    final List<Entity> entities = entry.chunkEntities.get(getChunkIndex(chunkX, chunkZ));
                    if (entities == null || entities.isEmpty()) return;
                    for (Entity entity : entities) update.add((T) entity);
                }, (chunkX, chunkZ) -> {
                    // Remove
                    final List<Entity> entities = entry.chunkEntities.get(getChunkIndex(chunkX, chunkZ));
                    if (entities == null || entities.isEmpty()) return;
                    for (Entity entity : entities) update.remove((T) entity);
                });
    }

    @Override
    public synchronized <T extends Entity> void chunkEntities(int chunkX, int chunkZ, @NotNull Target<T> target, @NotNull Query<T> query) {
        final TargetEntry<Entity> entry = entries[target.ordinal()];
        final List<Entity> entities = entry.chunkEntities.get(getChunkIndex(chunkX, chunkZ));
        if (entities == null || entities.isEmpty()) return;
        for (Entity entity : entities) query.consume((T) entity);
    }

    @Override
    public <T extends Entity> void visibleEntities(int chunkX, int chunkZ, @NotNull Target<T> target, @NotNull Query<T> query) {
        for (List<T> entities : references(chunkX, chunkZ, target)) {
            if (entities.isEmpty()) continue;
            for (Entity entity : entities) query.consume((T) entity);
        }
    }

    @Override
    public synchronized @NotNull <T extends Entity> List<List<T>> references(int chunkX, int chunkZ, @NotNull Target<T> target) {
        // Gets reference to all chunk entities lists within the range
        // This is used to avoid a map lookup per chunk
        final TargetEntry<T> entry = (TargetEntry<T>) entries[target.ordinal()];
        return entry.chunkRangeEntities.computeIfAbsent(ChunkUtils.getChunkIndex(chunkX, chunkZ),
                chunkIndex -> {
                    List<List<T>> entities = new ArrayList<>();
                    ChunkUtils.forChunksInRange(ChunkUtils.getChunkCoordX(chunkIndex), ChunkUtils.getChunkCoordZ(chunkIndex),
                            MinecraftServer.getEntityViewDistance(),
                            (x, z) -> entities.add(entry.chunkEntities.computeIfAbsent(getChunkIndex(x, z), i -> (List<T>) LIST_SUPPLIER.apply(i))));
                    return List.copyOf(entities);
                });
    }

    @Override
    public synchronized <T extends Entity> void nearbyEntities(@NotNull Point point, double range, @NotNull Target<T> target, @NotNull Query<T> query) {
        final int chunkRange = Math.abs((int) (range / Chunk.CHUNK_SECTION_SIZE)) + 1;
        final double squaredRange = range * range;
        ChunkUtils.forChunksInRange(point, chunkRange, (chunkX, chunkZ) ->
                chunkEntities(chunkX, chunkZ, target, entity -> {
                    if (point.distanceSquared(entity.getPosition()) < squaredRange) {
                        query.consume(entity);
                    }
                }));
    }

    @Override
    public @UnmodifiableView @NotNull <T extends Entity> Set<@NotNull T> entities(@NotNull Target<T> target) {
        return (Set<T>) entries[target.ordinal()].entitiesView;
    }

    @Override
    public synchronized void synchronize(@NotNull Point point, @NotNull Runnable runnable) {
        runnable.run();
    }

    private static final class TargetEntry<T extends Entity> {
        private final EntityTracker.Target<T> target;
        private final Set<T> entities = ConcurrentHashMap.newKeySet(); // Thread-safe since exposed
        private final Set<T> entitiesView = Collections.unmodifiableSet(entities);
        // Chunk index -> entities inside it
        private final Long2ObjectMap<List<T>> chunkEntities = new Long2ObjectOpenHashMap<>(0);
        // Chunk index -> lists of visible entities (references to chunkEntities entries)
        private final Long2ObjectMap<List<List<T>>> chunkRangeEntities = new Long2ObjectOpenHashMap<>(0);

        TargetEntry(Target<T> target) {
            this.target = target;
        }

        void addToChunk(long index, T entity) {
            this.chunkEntities.computeIfAbsent(index, i -> (List<T>) LIST_SUPPLIER.apply(i)).add(entity);
        }

        void removeFromChunk(long index, T entity) {
            List<T> entities = chunkEntities.get(index);
            if (entities != null) entities.remove(entity);
        }
    }
}
