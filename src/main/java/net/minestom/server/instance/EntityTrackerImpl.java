package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntitySelector;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

final class EntityTrackerImpl implements EntityTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityTrackerImpl.class);

    private static final EntitySelector<Entity> SELECTOR = EntitySelector.selector(builder -> builder.chunkRange(ServerFlag.ENTITY_VIEW_DISTANCE));

    // Indexes
    private final Int2ObjectMap<TrackedEntity> idIndex = new Int2ObjectOpenHashMap<>();
    private final Map<UUID, TrackedEntity> uuidIndex = new HashMap<>();
    private final Int2ObjectMap<TrackedEntity> playerIdIndex = new Int2ObjectOpenHashMap<>();

    // Spatial partitioning
    private final Long2ObjectMap<Set<Entity>> chunksEntities = new Long2ObjectOpenHashMap<>();

    @Override
    public synchronized void register(@NotNull Entity entity, @NotNull Point point, @Nullable Update update) {
        TrackedEntity newEntry = new TrackedEntity(entity, new AtomicReference<>(point));
        // Indexing
        TrackedEntity prevEntryWithId = idIndex.putIfAbsent(entity.getEntityId(), newEntry);
        Check.isTrue(prevEntryWithId == null, "There is already an entity registered with id {0}", entity.getEntityId());
        TrackedEntity prevEntryWithUuid = uuidIndex.putIfAbsent(entity.getUuid(), newEntry);
        Check.isTrue(prevEntryWithUuid == null, "There is already an entity registered with uuid {0}", entity.getUuid());
        if (entity instanceof Player) {
            TrackedEntity prevEntryWithPlayerId = playerIdIndex.putIfAbsent(entity.getEntityId(), newEntry);
            Check.isTrue(prevEntryWithPlayerId == null, "There is already an entity registered with player id {0}", entity.getEntityId());
        }
        // Spatial partitioning
        final long index = CoordConversion.chunkIndex(point);
        Set<Entity> chunkEntities = chunksEntities.computeIfAbsent(index, t -> new HashSet<>());
        chunkEntities.add(entity);
        // Update
        if (update != null) {
            update.referenceUpdate(point, this);
            selectEntityConsume(SELECTOR, point, newEntity -> {
                if (newEntity == entity) return;
                update.add(newEntity);
            });
        }
    }

    @Override
    public synchronized void unregister(@NotNull Entity entity, @Nullable Update update) {
        // Indexing
        TrackedEntity entry = idIndex.remove(entity.getEntityId());
        if (entry == null) return;
        uuidIndex.remove(entity.getUuid());
        if (entity instanceof Player) {
            playerIdIndex.remove(entity.getEntityId());
        }
        // Spatial partitioning
        final Point point = entry.lastPosition().getPlain();
        final long index = CoordConversion.chunkIndex(point);
        Set<Entity> chunkEntities = chunksEntities.computeIfAbsent(index, t -> new HashSet<>());
        chunkEntities.remove(entity);
        if (chunkEntities.isEmpty()) {
            chunksEntities.remove(index); // Empty chunk
        }
        // Update
        if (update != null) {
            update.referenceUpdate(point, null);
            selectEntityConsume(SELECTOR, point, newEntity -> {
                if (newEntity == entity) return;
                update.remove(newEntity);
            });
        }
    }

    @Override
    public synchronized void move(@NotNull Entity entity, @NotNull Point newPoint, @Nullable Update update) {
        TrackedEntity entry = idIndex.get(entity.getEntityId());
        if (entry == null) {
            LOGGER.warn("Attempted to move unregistered entity {} in the entity tracker", entity.getEntityId());
            return;
        }
        Point oldPoint = entry.lastPosition().getPlain();
        entry.lastPosition().setPlain(newPoint);
        if (oldPoint == null || oldPoint.sameChunk(newPoint)) return;
        // Chunk change, update partitions
        final long oldIndex = CoordConversion.chunkIndex(oldPoint);
        final long newIndex = CoordConversion.chunkIndex(newPoint);
        Set<Entity> oldPartition = chunksEntities.computeIfAbsent(oldIndex, t -> new HashSet<>());
        Set<Entity> newPartition = chunksEntities.computeIfAbsent(newIndex, t -> new HashSet<>());
        oldPartition.remove(entity);
        newPartition.add(entity);
        if (oldPartition.isEmpty()) {
            chunksEntities.remove(oldIndex); // Empty chunk
        }
        if (newPartition.isEmpty()) {
            chunksEntities.remove(newIndex); // Empty chunk
        }
        // Update
        if (update != null) {
            difference(oldPoint, newPoint, new Update() {
                @Override
                public void add(@NotNull Entity added) {
                    if (entity != added) update.add(added);
                }

                @Override
                public void remove(@NotNull Entity removed) {
                    if (entity != removed) update.remove(removed);
                }
            });
            update.referenceUpdate(newPoint, this);
        }
    }

    @Override
    public synchronized @NotNull Stream<@NotNull Entity> selectEntityStream(@NotNull EntitySelector<? extends Entity> query, @NotNull Point origin) {
        Stream<TrackedEntity> stream = switch (query.target()) {
            case ALL_ENTITIES -> idIndex.values().stream();
            case ALL_PLAYERS -> playerIdIndex.values().stream();
            case NEAREST_ENTITY -> {
                final TrackedEntity nearest = findNearest(origin, false);
                yield nearest != null ? Stream.of(nearest) : Stream.empty();
            }
            case NEAREST_PLAYER -> {
                final TrackedEntity nearest = findNearest(origin, true);
                yield nearest != null ? Stream.of(nearest) : Stream.empty();
            }
            case RANDOM_PLAYER -> {
                if (!playerIdIndex.isEmpty()) {
                    var players = playerIdIndex.values();
                    var randomEntry = players.stream().skip(new Random().nextInt(players.size())).findFirst().orElse(null);
                    yield Stream.of(randomEntry);
                } else {
                    yield Stream.empty();
                }
            }
        };
        {
            EntitySelector<Entity> selector = (EntitySelector<Entity>) query;
            stream = stream.filter(trackedEntity -> selector.test(origin, trackedEntity.entity()));
        }

        switch (query.sort()) {
            case ARBITRARY -> {
                // Do not sort
            }
            case FURTHEST -> stream = stream.sorted((a, b) -> {
                double distanceA = origin.distanceSquared(a.lastPosition().getPlain());
                double distanceB = origin.distanceSquared(b.lastPosition().getPlain());
                return Double.compare(distanceB, distanceA); // Sort descending by distance
            });
            case NEAREST -> stream = stream.sorted((a, b) -> {
                double distanceA = origin.distanceSquared(a.lastPosition().getPlain());
                double distanceB = origin.distanceSquared(b.lastPosition().getPlain());
                return Double.compare(distanceA, distanceB); // Sort ascending by distance
            });
            case RANDOM -> {
                var list = Arrays.asList(stream.toArray(TrackedEntity[]::new));
                Collections.shuffle(list);
                stream = list.stream();
            }
        }

        if (query.limit() != -1) {
            stream = stream.limit(query.limit());
        }

        return stream.map(TrackedEntity::entity);
    }

    private TrackedEntity findNearest(Point origin, boolean player) {
        Stream<TrackedEntity> stream = player ? playerIdIndex.values().stream() : idIndex.values().stream();
        return stream.min(Comparator.comparingDouble(
                trackedEntity -> origin.distanceSquared(trackedEntity.lastPosition().getPlain())
        )).orElse(null);
    }

    private void difference(Point oldPoint, Point newPoint, @NotNull Update update) {
        ChunkRange.chunksInRangeDiffering(newPoint.chunkX(), newPoint.chunkZ(), oldPoint.chunkX(), oldPoint.chunkZ(),
                ServerFlag.ENTITY_VIEW_DISTANCE, (chunkX, chunkZ) -> {
                    // Add
                    final Set<Entity> entities = chunksEntities.get(CoordConversion.chunkIndex(chunkX, chunkZ));
                    if (entities == null || entities.isEmpty()) return;
                    for (Entity entity : entities) update.add(entity);
                }, (chunkX, chunkZ) -> {
                    // Remove
                    final Set<Entity> entities = chunksEntities.get(CoordConversion.chunkIndex(chunkX, chunkZ));
                    if (entities == null || entities.isEmpty()) return;
                    for (Entity entity : entities) update.remove(entity);
                });
    }

    private record TrackedEntity(Entity entity, AtomicReference<Point> lastPosition) {
    }
}
