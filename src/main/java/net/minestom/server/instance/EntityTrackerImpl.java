package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.minestom.server.ServerFlag;
import net.minestom.server.Viewable;
import net.minestom.server.coordinate.PointIndex;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.vectrix.flare.fastutil.Int2ObjectSyncMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static net.minestom.server.instance.Chunk.CHUNK_SIZE_X;
import static net.minestom.server.instance.Chunk.CHUNK_SIZE_Z;

final class EntityTrackerImpl implements EntityTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityTrackerImpl.class);

    static final AtomicInteger TARGET_COUNTER = new AtomicInteger();

    private final PointIndex[] indices;

    private final Int2ObjectSyncMap<@Nullable Entity> entitiesById = Int2ObjectSyncMap.hashmap();
    private final Map<UUID, @Nullable Entity> entitiesByUuid = new ConcurrentHashMap<>();
    private final Map<ChunkViewKey, @Nullable ChunkView> viewers = new ConcurrentHashMap<>();

    EntityTrackerImpl() {
        final int targetCount = Target.TARGETS.size();
        this.indices = new PointIndex[targetCount];
        for (int i = 0; i < targetCount; i++) this.indices[i] = PointIndex.createConcurrent();
    }

    @Override
    public <T extends Entity> void register(Entity entity, Point point,
                                            Target<T> target, @Nullable Update<T> update) {
        final int id = entity.getEntityId();
        final UUID uuid = entity.getUuid();
        final Entity previousById = entitiesById.putIfAbsent(id, entity);
        if (previousById != null) {
            throw new IllegalStateException("There is already an entity registered with id " + id);
        }
        final Entity previousByUuid = entitiesByUuid.putIfAbsent(uuid, entity);
        if (previousByUuid != null) {
            entitiesById.remove(id);
            throw new IllegalStateException("There is already an entity registered with uuid " + uuid);
        }
        forEachTargetIndex(entity, index -> index.add(id, point));
        if (update != null) {
            update.referenceUpdate(point, this);
            indices[target.ordinal()].forEachInChunkRange(point, ServerFlag.ENTITY_VIEW_DISTANCE, neighborId -> {
                if (neighborId == id) return;
                acceptEntity(neighborId, update::add);
            });
        }
    }

    @Override
    public <T extends Entity> void unregister(Entity entity, Target<T> target, @Nullable Update<T> update) {
        final int id = entity.getEntityId();
        final Entity removed = entitiesById.remove(id);
        if (removed == null) return;
        entitiesByUuid.remove(entity.getUuid());
        @Nullable Point lastPoint = null;
        for (Target<? extends Entity> t : Target.TARGETS) {
            if (!t.type().isInstance(entity)) continue;
            final Point p = indices[t.ordinal()].remove(id);
            if (lastPoint == null) lastPoint = p;
        }
        if (lastPoint == null) return;
        if (update != null) {
            update.referenceUpdate(lastPoint, null);
            indices[target.ordinal()].forEachInChunkRange(lastPoint, ServerFlag.ENTITY_VIEW_DISTANCE, neighborId -> {
                if (neighborId == id) return;
                acceptEntity(neighborId, update::remove);
            });
        }
    }

    @Override
    public @Nullable Entity getEntityById(int id) {
        return entitiesById.get(id);
    }

    @Override
    public @Nullable Entity getEntityByUuid(UUID uuid) {
        return entitiesByUuid.get(uuid);
    }

    @Override
    public <T extends Entity> void move(Entity entity, Point newPoint,
                                        Target<T> target, @Nullable Update<T> update) {
        final int id = entity.getEntityId();
        final PointIndex entitiesIndex = indices[Target.ENTITIES.ordinal()];
        final Point oldPoint = entitiesIndex.get(id);
        if (oldPoint == null) {
            LOGGER.warn("Attempted to move unregistered entity {} in the entity tracker", id);
            return;
        }
        forEachTargetIndex(entity, index -> index.move(id, newPoint));
        if (oldPoint.sameChunk(newPoint)) return;
        if (update != null) {
            indices[target.ordinal()].forEachInChunkRangeDiffering(oldPoint, newPoint, ServerFlag.ENTITY_VIEW_DISTANCE,
                    addedId -> {
                        if (addedId == id) return;
                        acceptEntity(addedId, update::add);
                    },
                    removedId -> {
                        if (removedId == id) return;
                        acceptEntity(removedId, update::remove);
                    });
            update.referenceUpdate(newPoint, this);
        }
    }

    @Override
    public @Unmodifiable <T extends Entity> Collection<T> chunkEntities(int chunkX, int chunkZ, Target<T> target) {
        return new MappedCollection<>(indices[target.ordinal()].inChunk(chunkX, chunkZ), entitiesById);
    }

    @Override
    public <T extends Entity> void nearbyEntitiesByChunkRange(Point point, int chunkRange,
                                                              Target<T> target, Consumer<T> query) {
        indices[target.ordinal()].forEachInChunkRange(point, chunkRange, id -> {
            acceptEntity(id, query);
        });
    }

    @Override
    public <T extends Entity> void nearbyEntities(Point point, double range,
                                                  Target<T> target, Consumer<T> query) {
        indices[target.ordinal()].forEachWithin(point, range, id -> {
            acceptEntity(id, query);
        });
    }

    @Override
    public @UnmodifiableView <T extends Entity> Set<T> entities(Target<T> target) {
        return new MappedSet<>(indices[target.ordinal()].all(), entitiesById);
    }

    @Override
    public Viewable viewable(List<SharedInstance> sharedInstances, int chunkX, int chunkZ) {
        return viewers.computeIfAbsent(new ChunkViewKey(sharedInstances, chunkX, chunkZ), ChunkView::new);
    }

    private void forEachTargetIndex(Entity entity, Consumer<PointIndex> consumer) {
        for (Target<? extends Entity> target : Target.TARGETS) {
            if (target.type().isInstance(entity)) consumer.accept(indices[target.ordinal()]);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Entity> void acceptEntity(int id, Consumer<T> consumer) {
        final Entity entity = entitiesById.get(id);
        if (entity != null) consumer.accept((T) entity);
    }

    private static final class MappedCollection<T extends Entity> extends AbstractCollection<T> {
        private final IntCollection ids;
        private final Int2ObjectSyncMap<@Nullable Entity> resolver;

        MappedCollection(IntCollection ids, Int2ObjectSyncMap<@Nullable Entity> resolver) {
            this.ids = ids;
            this.resolver = resolver;
        }

        @Override
        public Iterator<T> iterator() {
            return mappingIterator(ids.intIterator(), resolver);
        }

        @Override
        public int size() {
            return ids.size();
        }

        @Override
        public boolean isEmpty() {
            return ids.isEmpty();
        }
    }

    private static final class MappedSet<T extends Entity> extends AbstractSet<T> {
        private final IntCollection ids;
        private final Int2ObjectSyncMap<@Nullable Entity> resolver;

        MappedSet(IntCollection ids, Int2ObjectSyncMap<@Nullable Entity> resolver) {
            this.ids = ids;
            this.resolver = resolver;
        }

        @Override
        public Iterator<T> iterator() {
            return mappingIterator(ids.intIterator(), resolver);
        }

        @Override
        public int size() {
            return ids.size();
        }

        @Override
        public boolean isEmpty() {
            return ids.isEmpty();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> Iterator<T> mappingIterator(IntIterator ids,
                                                                  Int2ObjectSyncMap<@Nullable Entity> resolver) {
        return new Iterator<>() {
            private @Nullable T next;

            @Override
            public boolean hasNext() {
                if (next != null) return true;
                while (ids.hasNext()) {
                    final Entity entity = resolver.get(ids.nextInt());
                    if (entity != null) {
                        next = (T) entity;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                final T value = Objects.requireNonNull(next);
                next = null;
                return value;
            }
        };
    }

    record ChunkViewKey(List<SharedInstance> sharedInstances, int chunkX, int chunkZ) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ChunkViewKey(List<SharedInstance> instances, int x, int z))) return false;
            return sharedInstances == instances &&
                    chunkX == x &&
                    chunkZ == z;
        }
    }

    private final class ChunkView implements Viewable {
        private final ChunkViewKey key;
        private final Point point;
        final Set<Player> set = new SetImpl();
        private int lastReferenceCount;

        private ChunkView(ChunkViewKey key) {
            this.key = key;
            this.point = new Vec(CHUNK_SIZE_X * key.chunkX, 0, CHUNK_SIZE_Z * key.chunkZ);
        }

        @Override
        public boolean addViewer(Player player) {
            throw new UnsupportedOperationException("Chunk does not support manual viewers");
        }

        @Override
        public boolean removeViewer(Player player) {
            throw new UnsupportedOperationException("Chunk does not support manual viewers");
        }

        @Override
        public Set<? extends Player> getViewers() {
            return set;
        }

        private Collection<Player> references() {
            final Int2ObjectOpenHashMap<@Nullable Player> entityMap = new Int2ObjectOpenHashMap<>(lastReferenceCount);
            collectPlayers(EntityTrackerImpl.this, entityMap);
            if (!key.sharedInstances.isEmpty()) {
                for (SharedInstance instance : key.sharedInstances) {
                    collectPlayers(instance.getEntityTracker(), entityMap);
                }
            }
            this.lastReferenceCount = entityMap.size();
            return entityMap.values();
        }

        private void collectPlayers(EntityTracker tracker, Int2ObjectOpenHashMap<@Nullable Player> map) {
            tracker.nearbyEntitiesByChunkRange(point, ServerFlag.CHUNK_VIEW_DISTANCE, Target.PLAYERS,
                    (Player player) -> map.putIfAbsent(player.getEntityId(), player));
        }

        final class SetImpl extends AbstractSet<Player> {
            @Override
            public Iterator<Player> iterator() {
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
