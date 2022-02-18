package net.minestom.server.instance;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import space.vectrix.flare.fastutil.Int2ObjectSyncMap;
import space.vectrix.flare.fastutil.Long2ObjectSyncMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.minestom.server.utils.chunk.ChunkUtils.forDifferingChunksInRange;
import static net.minestom.server.utils.chunk.ChunkUtils.getChunkIndex;

final class EntityTrackerImpl implements EntityTracker {
    static final AtomicInteger TARGET_COUNTER = new AtomicInteger();
    private static final Supplier<List<Entity>> LIST_SUPPLIER = CopyOnWriteArrayList::new;

    // Store all data associated to a Target
    // The array index is the Target enum ordinal
    private final TargetEntry<Entity>[] entries = EntityTracker.Target.TARGETS.stream().map((Function<Target<?>, TargetEntry>) TargetEntry::new).toArray(TargetEntry[]::new);

    private final Int2ObjectSyncMap<Point> entityPositions = Int2ObjectSyncMap.hashmap();

    @Override
    public <T extends Entity> void register(@NotNull Entity entity, @NotNull Point point,
                                            @NotNull Target<T> target, @Nullable Update<T> update) {
        var prevPoint = entityPositions.putIfAbsent(entity.getEntityId(), point);
        if (prevPoint != null) return;
        final long index = getChunkIndex(point);
        for (TargetEntry<Entity> entry : entries) {
            if (entry.target.type().isInstance(entity)) {
                entry.entities.add(entity);
                entry.addToChunk(index, entity);
            }
        }
        if (update != null) {
            update.referenceUpdate(point, this);
            visibleEntities(point, target, newEntity -> {
                if (newEntity == entity) return;
                update.add(newEntity);
            });
        }
    }

    @Override
    public <T extends Entity> void unregister(@NotNull Entity entity,
                                              @NotNull Target<T> target, @Nullable Update<T> update) {
        final Point point = entityPositions.remove(entity.getEntityId());
        if (point == null) return;
        final long index = getChunkIndex(point);
        for (TargetEntry<Entity> entry : entries) {
            if (entry.target.type().isInstance(entity)) {
                entry.entities.remove(entity);
                entry.removeFromChunk(index, entity);
            }
        }
        if (update != null) {
            update.referenceUpdate(point, null);
            visibleEntities(point, target, update::remove);
        }
    }

    @Override
    public <T extends Entity> void move(@NotNull Entity entity, @NotNull Point newPoint,
                                        @NotNull Target<T> target, @Nullable Update<T> update) {
        Point oldPoint = entityPositions.put(entity.getEntityId(), newPoint);
        if (oldPoint == null || oldPoint.sameChunk(newPoint)) return;
        final long oldIndex = getChunkIndex(oldPoint);
        final long newIndex = getChunkIndex(newPoint);
        for (TargetEntry<Entity> entry : entries) {
            if (entry.target.type().isInstance(entity)) {
                entry.addToChunk(newIndex, entity);
                entry.removeFromChunk(oldIndex, entity);
            }
        }
        if (update != null) {
            difference(oldPoint, newPoint, target, new Update<>() {
                @Override
                public void add(@NotNull T added) {
                    if (entity != added) update.add(added);
                }

                @Override
                public void remove(@NotNull T removed) {
                    if (entity != removed) update.remove(removed);
                }
            });
            update.referenceUpdate(newPoint, this);
        }
    }

    @Override
    public <T extends Entity> void difference(int oldChunkX, int oldChunkZ,
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
    public @Unmodifiable <T extends Entity> Collection<T> chunkEntities(int chunkX, int chunkZ, @NotNull Target<T> target) {
        final TargetEntry<Entity> entry = entries[target.ordinal()];
        //noinspection unchecked
        var chunkEntities = (List<T>) entry.chunkEntities.computeIfAbsent(getChunkIndex(chunkX, chunkZ), i -> LIST_SUPPLIER.get());
        return Collections.unmodifiableList(chunkEntities);
    }

    @Override
    public <T extends Entity> void visibleEntities(int chunkX, int chunkZ, @NotNull Target<T> target, @NotNull Query<T> query) {
        for (List<T> entities : references(chunkX, chunkZ, MinecraftServer.getEntityViewDistance(), target)) {
            if (entities.isEmpty()) continue;
            for (T entity : entities) query.consume(entity);
        }
    }

    @Override
    public @NotNull <T extends Entity> List<List<T>> references(int chunkX, int chunkZ, int range, @NotNull Target<T> target) {
        // Gets reference to all chunk entities lists within the range
        // This is used to avoid a map lookup per chunk
        final TargetEntry<T> entry = (TargetEntry<T>) entries[target.ordinal()];
        var rangeRef = entry.references.computeIfAbsent(range, integer -> Long2ObjectSyncMap.hashmap());
        return rangeRef.computeIfAbsent(ChunkUtils.getChunkIndex(chunkX, chunkZ),
                chunkIndex -> {
                    final int count = ChunkUtils.getChunkCount(range);
                    List<List<T>> entities = new ArrayList<>(count);
                    ChunkUtils.forChunksInRange(ChunkUtils.getChunkCoordX(chunkIndex), ChunkUtils.getChunkCoordZ(chunkIndex), range,
                            (x, z) -> entities.add(entry.chunkEntities.computeIfAbsent(getChunkIndex(x, z), i -> (List<T>) LIST_SUPPLIER.get())));
                    assert entities.size() == count : "Expected " + count + " chunks, got " + entities.size();
                    return List.copyOf(entities);
                });
    }

    @Override
    public <T extends Entity> void nearbyEntities(@NotNull Point point, double range, @NotNull Target<T> target, @NotNull Query<T> query) {
        final int chunkRange = Math.abs((int) (range / Chunk.CHUNK_SECTION_SIZE)) + 1;
        final double squaredRange = range * range;
        ChunkUtils.forChunksInRange(point, chunkRange, (chunkX, chunkZ) -> {
            var chunkEntities = chunkEntities(chunkX, chunkZ, target);
            chunkEntities.forEach(entity -> {
                final Point position = entityPositions.get(entity.getEntityId());
                if (point.distanceSquared(position) <= squaredRange) {
                    query.consume(entity);
                }
            });
        });
    }

    @Override
    public @UnmodifiableView @NotNull <T extends Entity> Set<@NotNull T> entities(@NotNull Target<T> target) {
        //noinspection unchecked
        return (Set<T>) entries[target.ordinal()].entitiesView;
    }

    private static final class TargetEntry<T extends Entity> {
        private final EntityTracker.Target<T> target;
        private final Set<T> entities = ConcurrentHashMap.newKeySet(); // Thread-safe since exposed
        private final Set<T> entitiesView = Collections.unmodifiableSet(entities);
        // Chunk index -> entities inside it
        private final Long2ObjectSyncMap<List<T>> chunkEntities = Long2ObjectSyncMap.hashmap();
        // Chunk index -> lists of visible entities (references to chunkEntities entries)
        private final Int2ObjectSyncMap<Long2ObjectSyncMap<List<List<T>>>> references = Int2ObjectSyncMap.hashmap();

        TargetEntry(Target<T> target) {
            this.target = target;
        }

        void addToChunk(long index, T entity) {
            this.chunkEntities.computeIfAbsent(index, i -> (List<T>) LIST_SUPPLIER.get()).add(entity);
        }

        void removeFromChunk(long index, T entity) {
            List<T> entities = chunkEntities.get(index);
            if (entities != null) entities.remove(entity);
        }
    }
}
