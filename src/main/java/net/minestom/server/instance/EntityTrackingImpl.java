package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minestom.server.utils.chunk.ChunkUtils.forDifferingChunksInRange;
import static net.minestom.server.utils.chunk.ChunkUtils.getChunkIndex;

final class EntityTrackingImpl {

    static final AtomicInteger TARGET_COUNTER = new AtomicInteger();

    static <T extends Entity> EntityTracking.Target<T> create(Class<T> type) {
        final int ordinal = TARGET_COUNTER.getAndIncrement();
        return new EntityTracking.Target<>() {
            @Override
            public Class<T> type() {
                return type;
            }

            @Override
            public int ordinal() {
                return ordinal;
            }
        };
    }

    /**
     * Default tracking implementation storing entities per-chunk.
     */
    static final class PerChunk implements EntityTracking {
        private static final int ENTITIES_INDEX = Target.ENTITIES.ordinal();
        private static final int PLAYERS_INDEX = Target.PLAYERS.ordinal();

        private final Set<Entity> entitiesView;
        private final Set<Player> playersView;

        // Store all data associated to a Target
        // The array index is the Target enum ordinal
        private final TargetEntry<? extends Entity>[] entries = new TargetEntry[Target.count()];

        {
            Arrays.setAll(entries, value -> new TargetEntry<>());
            this.entitiesView = Collections.unmodifiableSet(entries[ENTITIES_INDEX].entities);
            this.playersView = (Set<Player>) Collections.unmodifiableSet(entries[PLAYERS_INDEX].entities);
        }

        @Override
        public void register(@NotNull Entity entity, @NotNull Point point, @Nullable Update<Entity> update) {
            for (var target : Target.values()) {
                if (target.type().isInstance(entity)) {
                    Set<Entity> entities = (Set<Entity>) entries[target.ordinal()].entities;
                    entities.add(entity);
                }
            }
            addTo(point, entity);
            if (update != null) visibleEntities(point, findViewingTarget(entity), update::add);
        }

        @Override
        public void unregister(@NotNull Entity entity, @NotNull Point point, @Nullable Update<Entity> update) {
            for (var target : Target.values()) {
                if (target.type().isInstance(entity)) {
                    entries[target.ordinal()].entities.remove(entity);
                }
            }
            removeFrom(point, entity);
            if (update != null) visibleEntities(point, findViewingTarget(entity), update::remove);
        }

        @Override
        public void move(@NotNull Entity entity, @NotNull Point oldPoint, @NotNull Point newPoint, @Nullable Update<Entity> update) {
            if (!oldPoint.sameChunk(newPoint)) {
                removeFrom(oldPoint, entity);
                addTo(newPoint, entity);
                if (update != null) difference(oldPoint, newPoint, (Target<Entity>) findViewingTarget(entity), update);
            }
        }

        @Override
        public <T extends Entity> void difference(@NotNull Point from, @NotNull Point to, @NotNull Target<T> target, @NotNull Update<T> update) {
            Long2ObjectMap<List<Entity>> map = Long2ObjectMap.class.cast(entries[target.ordinal()].chunkEntities);
            forDifferingChunksInRange(to.chunkX(), to.chunkZ(), from.chunkX(), from.chunkZ(),
                    MinecraftServer.getEntityViewDistance(), (chunkX, chunkZ) -> {
                        // Add
                        final List<? extends Entity> entities = map.get(getChunkIndex(chunkX, chunkZ));
                        if (entities == null || entities.isEmpty()) return;
                        for (Entity entity : entities) {
                            update.add((T) entity);
                        }
                    }, (chunkX, chunkZ) -> {
                        // Remove
                        final List<? extends Entity> entities = map.get(getChunkIndex(chunkX, chunkZ));
                        if (entities == null || entities.isEmpty()) return;
                        for (Entity entity : entities) {
                            update.remove((T) entity);
                        }
                    });
        }

        @Override
        public <T extends Entity> void chunkEntities(int chunkX, int chunkZ, @NotNull Target<T> target, @NotNull Query<T> query) {
            final Long2ObjectMap<List<Entity>> map = Long2ObjectMap.class.cast(entries[target.ordinal()].chunkEntities);
            final var entities = map.get(getChunkIndex(chunkX, chunkZ));
            if (entities == null || entities.isEmpty()) return;
            for (Entity entity : entities) {
                query.consume((T) entity);
            }
        }

        @Override
        public <T extends Entity> void visibleEntities(@NotNull Point point, @NotNull Target<T> target, @NotNull Query<T> query) {
            // Gets reference to all chunk entities lists within the range
            // This is used to avoid a map lookup per chunk
            TargetEntry<Entity> entry = (TargetEntry<Entity>) entries[target.ordinal()];
            Long2ObjectMap<List<Entity>[]> map = entry.chunkRangeEntities;
            Long2ObjectMap<List<Entity>> chunkEntities = entry.chunkEntities;
            var range = map.computeIfAbsent(ChunkUtils.getChunkIndex(point), chunkIndex -> {
                final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
                final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
                List<List<Entity>> entities = new ArrayList<>();
                ChunkUtils.forChunksInRange(chunkX, chunkZ, MinecraftServer.getEntityViewDistance(),
                        (x, z) -> entities.add(chunkEntities.computeIfAbsent(getChunkIndex(x, z), listSupplier())));
                return entities.toArray(List[]::new);
            });
            for (var entities : range) {
                for (Entity entity : entities) query.consume((T) entity);
            }
        }

        @Override
        public @NotNull Set<@NotNull Entity> entities() {
            return entitiesView;
        }

        @Override
        public @UnmodifiableView @NotNull Set<@NotNull Player> players() {
            return playersView;
        }

        private static Target<? extends Entity> findViewingTarget(Entity entity) {
            if (entity instanceof Player) {
                // Players must be aware of all surrounding entities
                return Target.ENTITIES;
            }
            // General entities should only be aware of surrounding players to update their viewing list
            return Target.PLAYERS;
        }

        private static Long2ObjectFunction<List<Entity>> listSupplier() {
            return l -> new CopyOnWriteArrayList<>();
        }

        private void addTo(Point chunkPoint, Entity entity) {
            final long index = getChunkIndex(chunkPoint);
            for (var target : Target.values()) {
                if (target.type().isInstance(entity)) {
                    chunkEntities(target).computeIfAbsent(index, listSupplier()).add(entity);
                }
            }
        }

        private void removeFrom(Point chunkPoint, Entity entity) {
            final long index = getChunkIndex(chunkPoint);
            for (var target : Target.values()) {
                if (target.type().isInstance(entity)) {
                    var entities = chunkEntities(target).get(index);
                    if (entities != null) entities.remove(entity);
                }
            }
        }

        private Long2ObjectMap<List<Entity>> chunkEntities(Target<?> target) {
            return ((TargetEntry<Entity>) entries[target.ordinal()]).chunkEntities;
        }

        private static final class TargetEntry<T extends Entity> {
            private final Set<T> entities = new HashSet<>();
            // Chunk index -> entities inside it
            private final Long2ObjectMap<List<T>> chunkEntities = new Long2ObjectOpenHashMap<>();
            // Chunk index -> lists of visible entities (references to chunkEntities entries)
            private final Long2ObjectMap<List<T>[]> chunkRangeEntities = new Long2ObjectOpenHashMap<>();
        }
    }

    /**
     * Synchronizes every method.
     */
    static final class Synchronized implements EntityTracking {
        private final EntityTracking t;
        private final Object mutex;

        public Synchronized(EntityTracking entityTracking) {
            this.t = entityTracking;
            this.mutex = this;
        }

        @Override
        public void register(@NotNull Entity entity, @NotNull Point point, @Nullable Update update) {
            synchronized (mutex) {
                t.register(entity, point, update);
            }
        }

        @Override
        public void unregister(@NotNull Entity entity, @NotNull Point point, @Nullable Update update) {
            synchronized (mutex) {
                t.unregister(entity, point, update);
            }
        }

        @Override
        public void move(@NotNull Entity entity, @NotNull Point oldPoint, @NotNull Point newPoint, @Nullable Update update) {
            synchronized (mutex) {
                t.move(entity, oldPoint, newPoint, update);
            }
        }

        @Override
        public void difference(@NotNull Point from, @NotNull Point to, @NotNull Target target, @NotNull Update update) {
            synchronized (mutex) {
                t.difference(from, to, target, update);
            }
        }

        @Override
        public void chunkEntities(int chunkX, int chunkZ, @NotNull Target target, @NotNull Query query) {
            synchronized (mutex) {
                t.chunkEntities(chunkX, chunkZ, target, query);
            }
        }

        @Override
        public void visibleEntities(@NotNull Point point, @NotNull Target target, @NotNull Query query) {
            synchronized (mutex) {
                t.visibleEntities(point, target, query);
            }
        }

        @Override
        public void nearbyEntities(@NotNull Point point, double range, @NotNull Target target, @NotNull Query query) {
            synchronized (mutex) {
                t.nearbyEntities(point, range, target, query);
            }
        }

        @Override
        public @UnmodifiableView @NotNull Set<@NotNull Entity> entities() {
            synchronized (mutex) {
                return t.entities();
            }
        }

        @Override
        public @UnmodifiableView @NotNull Set<@NotNull Player> players() {
            synchronized (mutex) {
                return t.players();
            }
        }
    }

}
