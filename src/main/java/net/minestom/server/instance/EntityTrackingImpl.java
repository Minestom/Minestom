package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static net.minestom.server.utils.chunk.ChunkUtils.*;

final class EntityTrackingImpl {

    /**
     * Default tracking implementation storing entities per-chunk.
     */
    static final class PerChunk implements EntityTracking {
        private final Set<Entity> entities = new HashSet<>();
        private final Set<Entity> entitiesView = Collections.unmodifiableSet(entities);
        private final Set<Player> players = new HashSet<>();
        private final Set<Player> playersView = Collections.unmodifiableSet(players);
        private final Long2ObjectMap<List<Entity>> chunkEntities = new Long2ObjectOpenHashMap<>();

        @Override
        public void register(@NotNull Entity entity, @NotNull Point spawnPoint, @Nullable Update update) {
            if (!entities.add(entity)) return;
            if (entity instanceof Player) players.add((Player) entity);
            addTo(spawnPoint, entity);
            if (update != null) chunkRangeEntities(spawnPoint, MinecraftServer.getEntityViewDistance(), update::add);
        }

        @Override
        public void unregister(@NotNull Entity entity, @NotNull Point point, @Nullable Update update) {
            if (!entities.remove(entity)) return;
            if (entity instanceof Player) players.remove(entity);
            removeFrom(point, entity);
            if (update != null) chunkRangeEntities(point, MinecraftServer.getEntityViewDistance(), update::remove);
        }

        @Override
        public void move(@NotNull Entity entity, @NotNull Point oldPoint, @NotNull Point newPoint, @Nullable Update update) {
            if (!oldPoint.sameChunk(newPoint)) {
                removeFrom(oldPoint, entity);
                addTo(newPoint, entity);
                if (update != null) difference(oldPoint, newPoint, update);
            }
        }

        @Override
        public void difference(@NotNull Point from, @NotNull Point to, @NotNull Update update) {
            forDifferingChunksInRange(to.chunkX(), to.chunkZ(), from.chunkX(), from.chunkZ(),
                    MinecraftServer.getEntityViewDistance(), chunkIndex -> {
                        // Add
                        final List<Entity> entities = chunkEntities.get(chunkIndex);
                        if (entities == null) return;
                        for (Entity entity : entities) {
                            update.add(entity);
                        }
                    }, chunkIndex -> {
                        // Remove
                        final List<Entity> entities = chunkEntities.get(chunkIndex);
                        if (entities == null) return;
                        for (Entity entity : entities) {
                            update.remove(entity);
                        }
                    });
        }

        @Override
        public void nearbyEntities(@NotNull Point point, double range, @NotNull Query query) {
            final int minX = getChunkCoordinate(point.x() - range);
            final int maxX = getChunkCoordinate(point.x() + range);
            final int minZ = getChunkCoordinate(point.z() - range);
            final int maxZ = getChunkCoordinate(point.z() + range);
            // Cache squared range to prevent sqrt operations
            final double squaredRange = range * range;

            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    final List<Entity> chunkEntities = this.chunkEntities.get(getChunkIndex(x, z));
                    if (chunkEntities == null) continue;
                    // Filter all entities out of range
                    for (Entity chunkEntity : chunkEntities) {
                        if (point.distanceSquared(chunkEntity.getPosition()) < squaredRange) {
                            query.consume(chunkEntity);
                        }
                    }
                }
            }
        }

        @Override
        public void chunkEntities(@NotNull Point chunkPoint, @NotNull Query query) {
            final List<Entity> entities = chunkEntities.get(getChunkIndex(chunkPoint.chunkX(), chunkPoint.chunkZ()));
            if (entities == null) return;
            for (Entity entity : entities) {
                query.consume(entity);
            }
        }

        @Override
        public void chunkRangeEntities(@NotNull Point chunkPoint, int range, @NotNull Query query) {
            forChunksInRange(chunkPoint, range, chunkIndex -> {
                final List<Entity> entities = chunkEntities.get(chunkIndex);
                if (entities == null) return;
                for (Entity entity : entities) {
                    query.consume(entity);
                }
            });
        }

        @Override
        public @NotNull Set<@NotNull Entity> entities() {
            return entitiesView;
        }

        @Override
        public @UnmodifiableView @NotNull Set<@NotNull Player> players() {
            return playersView;
        }

        private void addTo(Point chunkPoint, Entity entity) {
            this.chunkEntities.computeIfAbsent(getChunkIndex(chunkPoint.chunkX(), chunkPoint.chunkZ()),
                    l -> new CopyOnWriteArrayList<>()).add(entity);
        }

        private void removeFrom(Point chunkPoint, Entity entity) {
            final long index = getChunkIndex(chunkPoint.chunkX(), chunkPoint.chunkZ());
            List<Entity> entities = this.chunkEntities.get(index);
            if (entities == null) return;
            entities.remove(entity);
            if (entities.isEmpty()) this.chunkEntities.remove(index);
        }
    }

    static final class Synchronized implements EntityTracking {
        private final EntityTracking t;
        private final Object mutex;

        public Synchronized(EntityTracking entityTracking) {
            this.t = entityTracking;
            this.mutex = this;
        }

        @Override
        public void register(@NotNull Entity entity, @NotNull Point spawnPoint, @Nullable Update update) {
            synchronized (mutex) {
                t.register(entity, spawnPoint, update);
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
        public void difference(@NotNull Point from, @NotNull Point to, @NotNull Update update) {
            synchronized (mutex) {
                t.difference(from, to, update);
            }
        }

        @Override
        public void nearbyEntities(@NotNull Point point, double range, @NotNull Query query) {
            synchronized (mutex) {
                t.nearbyEntities(point, range, query);
            }
        }

        @Override
        public void chunkEntities(@NotNull Point chunkPoint, @NotNull Query query) {
            synchronized (mutex) {
                t.chunkEntities(chunkPoint, query);
            }
        }

        @Override
        public void chunkRangeEntities(@NotNull Point chunkPoint, int range, @NotNull Query query) {
            synchronized (mutex) {
                t.chunkRangeEntities(chunkPoint, range, query);
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
