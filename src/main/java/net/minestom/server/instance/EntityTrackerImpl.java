package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
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
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;
import static net.minestom.server.utils.chunk.ChunkUtils.*;

final class EntityTrackerImpl implements EntityTracker {
    static final AtomicInteger TARGET_COUNTER = new AtomicInteger();

    // Store all data associated to a Target
    // The array index is the Target enum ordinal
    final TargetEntry<Entity>[] entries = EntityTracker.Target.TARGETS.stream().map((Function<Target<?>, TargetEntry>) TargetEntry::new).toArray(TargetEntry[]::new);
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
            nearbyEntitiesByChunkRange(point, MinecraftServer.getEntityViewDistance(), target, newEntity -> {
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
            nearbyEntitiesByChunkRange(point, MinecraftServer.getEntityViewDistance(), target, newEntity -> {
                if (newEntity == entity) return;
                update.remove(newEntity);
            });
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
    public @Unmodifiable <T extends Entity> Collection<T> chunkEntities(int chunkX, int chunkZ, @NotNull Target<T> target) {
        final TargetEntry<Entity> entry = entries[target.ordinal()];
        //noinspection unchecked
        var chunkEntities = (List<T>) entry.chunkEntities(getChunkIndex(chunkX, chunkZ));
        return Collections.unmodifiableList(chunkEntities);
    }

    @Override
    public <T extends Entity> void nearbyEntitiesByChunkRange(@NotNull Point point, int chunkRange, @NotNull Target<T> target, @NotNull Consumer<T> query) {
        final Long2ObjectSyncMap<List<Entity>> entities = entries[target.ordinal()].chunkEntities;
        if (chunkRange == 0) {
            // Single chunk
            final var chunkEntities = (List<T>) entities.get(getChunkIndex(point));
            if (chunkEntities != null && !chunkEntities.isEmpty()) {
                chunkEntities.forEach(query);
            }
        } else {
            // Multiple chunks
            forChunksInRange(point, chunkRange, (chunkX, chunkZ) -> {
                final var chunkEntities = (List<T>) entities.get(getChunkIndex(chunkX, chunkZ));
                if (chunkEntities == null || chunkEntities.isEmpty()) return;
                chunkEntities.forEach(query);
            });
        }
    }

    @Override
    public <T extends Entity> void nearbyEntities(@NotNull Point point, double range, @NotNull Target<T> target, @NotNull Consumer<T> query) {
        final Long2ObjectSyncMap<List<Entity>> entities = entries[target.ordinal()].chunkEntities;
        final int minChunkX = ChunkUtils.getChunkCoordinate(point.x() - range);
        final int minChunkZ = ChunkUtils.getChunkCoordinate(point.z() - range);
        final int maxChunkX = ChunkUtils.getChunkCoordinate(point.x() + range);
        final int maxChunkZ = ChunkUtils.getChunkCoordinate(point.z() + range);
        final double squaredRange = range * range;
        if (minChunkX == maxChunkX && minChunkZ == maxChunkZ) {
            // Single chunk
            final var chunkEntities = (List<T>) entities.get(getChunkIndex(point));
            if (chunkEntities != null && !chunkEntities.isEmpty()) {
                chunkEntities.forEach(entity -> {
                    final Point position = entityPositions.get(entity.getEntityId());
                    if (point.distanceSquared(position) <= squaredRange) query.accept(entity);
                });
            }
        } else {
            // Multiple chunks
            final int chunkRange = (int) (range / Chunk.CHUNK_SECTION_SIZE) + 1;
            forChunksInRange(point, chunkRange, (chunkX, chunkZ) -> {
                final var chunkEntities = (List<T>) entities.get(getChunkIndex(chunkX, chunkZ));
                if (chunkEntities == null || chunkEntities.isEmpty()) return;
                chunkEntities.forEach(entity -> {
                    final Point position = entityPositions.get(entity.getEntityId());
                    if (point.distanceSquared(position) <= squaredRange) {
                        query.accept(entity);
                    }
                });
            });
        }
    }

    @Override
    public @UnmodifiableView @NotNull <T extends Entity> Set<@NotNull T> entities(@NotNull Target<T> target) {
        //noinspection unchecked
        return (Set<T>) entries[target.ordinal()].entitiesView;
    }

    @Override
    public @NotNull Viewable viewable(@NotNull List<@NotNull SharedInstance> sharedInstances, int chunkX, int chunkZ) {
        var entry = entries[Target.PLAYERS.ordinal()];
        return entry.viewers.computeIfAbsent(new ChunkViewKey(sharedInstances, chunkX, chunkZ), ChunkView::new);
    }

    private <T extends Entity> void difference(Point oldPoint, Point newPoint,
                                               @NotNull Target<T> target, @NotNull Update<T> update) {
        final TargetEntry<Entity> entry = entries[target.ordinal()];
        forDifferingChunksInRange(newPoint.chunkX(), newPoint.chunkZ(), oldPoint.chunkX(), oldPoint.chunkZ(),
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

    record ChunkViewKey(List<SharedInstance> sharedInstances, int chunkX, int chunkZ) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ChunkViewKey key)) return false;
            return sharedInstances == key.sharedInstances &&
                    chunkX == key.chunkX &&
                    chunkZ == key.chunkZ;
        }
    }

    static final class TargetEntry<T extends Entity> {
        private final EntityTracker.Target<T> target;
        private final Set<T> entities = ConcurrentHashMap.newKeySet(); // Thread-safe since exposed
        private final Set<T> entitiesView = Collections.unmodifiableSet(entities);
        // Chunk index -> entities inside it
        final Long2ObjectSyncMap<List<T>> chunkEntities = Long2ObjectSyncMap.hashmap();
        final Map<ChunkViewKey, ChunkView> viewers = new ConcurrentHashMap<>();

        TargetEntry(Target<T> target) {
            this.target = target;
        }

        List<T> chunkEntities(long index) {
            return chunkEntities.computeIfAbsent(index, i -> (List<T>) new CopyOnWriteArrayList());
        }

        void addToChunk(long index, T entity) {
            chunkEntities(index).add(entity);
        }

        void removeFromChunk(long index, T entity) {
            List<T> entities = chunkEntities.get(index);
            if (entities != null) entities.remove(entity);
        }
    }

    private final class ChunkView implements Viewable {
        private final ChunkViewKey key;
        private final int chunkX, chunkZ;
        private final Point point;
        final Set<Player> set = new SetImpl();
        private int lastReferenceCount;

        private ChunkView(ChunkViewKey key) {
            this.key = key;

            this.chunkX = key.chunkX;
            this.chunkZ = key.chunkZ;

            this.point = new Vec(CHUNK_SIZE_X * chunkX, 0, CHUNK_SIZE_Z * chunkZ);
        }

        @Override
        public boolean addViewer(@NotNull Player player) {
            throw new UnsupportedOperationException("Chunk does not support manual viewers");
        }

        @Override
        public boolean removeViewer(@NotNull Player player) {
            throw new UnsupportedOperationException("Chunk does not support manual viewers");
        }

        @Override
        public @NotNull Set<@NotNull Player> getViewers() {
            return set;
        }

        private Collection<Player> references() {
            Int2ObjectOpenHashMap<Player> entityMap = new Int2ObjectOpenHashMap<>(lastReferenceCount);
            collectPlayers(EntityTrackerImpl.this, entityMap);
            if (!key.sharedInstances.isEmpty()) {
                for (SharedInstance instance : key.sharedInstances) {
                    collectPlayers(instance.getEntityTracker(), entityMap);
                }
            }
            this.lastReferenceCount = entityMap.size();
            return entityMap.values();
        }

        private void collectPlayers(EntityTracker tracker, Int2ObjectOpenHashMap<Player> map) {
            tracker.nearbyEntitiesByChunkRange(point, MinecraftServer.getChunkViewDistance(),
                    EntityTracker.Target.PLAYERS, (player) -> map.putIfAbsent(player.getEntityId(), player));
        }

        final class SetImpl extends AbstractSet<Player> {
            @Override
            public @NotNull Iterator<Player> iterator() {
                return references().iterator();
            }

            @Override
            public int size() {
                return references().size();
            }

            @Override
            public void forEach(Consumer<? super Player> action) {
                references().forEach(action);
            }
        }
    }
}
