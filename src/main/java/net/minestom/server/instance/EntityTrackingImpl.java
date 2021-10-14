package net.minestom.server.instance;

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

import static net.minestom.server.utils.chunk.ChunkUtils.forDifferingChunksInRange;
import static net.minestom.server.utils.chunk.ChunkUtils.getChunkIndex;

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

        private final Long2ObjectMap<List<Entity>[]> chunkRangeEntities = new Long2ObjectOpenHashMap<>();

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
                    MinecraftServer.getEntityViewDistance(), (chunkX, chunkZ) -> {
                        // Add
                        final List<Entity> entities = chunkEntities.get(getChunkIndex(chunkX, chunkZ));
                        if (entities == null) return;
                        for (Entity entity : entities) {
                            update.add(entity);
                        }
                    }, (chunkX, chunkZ) -> {
                        // Remove
                        final List<Entity> entities = chunkEntities.get(getChunkIndex(chunkX, chunkZ));
                        if (entities == null) return;
                        for (Entity entity : entities) {
                            update.remove(entity);
                        }
                    });
        }

        @Override
        public void chunkEntities(int chunkX, int chunkZ, @NotNull Query query) {
            final List<Entity> entities = chunkEntities.get(getChunkIndex(chunkX, chunkZ));
            if (entities == null) return;
            for (Entity entity : entities) {
                query.consume(entity);
            }
        }

        @Override
        public void chunkRangeEntities(@NotNull Point chunkPoint, int range, @NotNull Query query) {
            // Gets reference to all chunk entities lists within the range
            // This is used to avoid a map lookup per chunk
            final List<Entity>[] rangeEntities = chunkRangeEntities.computeIfAbsent(ChunkUtils.getChunkIndex(chunkPoint),
                    chunkIndex -> {
                        final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
                        final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
                        List<List<Entity>> entities = new ArrayList<>();
                        ChunkUtils.forChunksInRange(chunkX, chunkZ, MinecraftServer.getChunkViewDistance(),
                                (x, z) -> entities.add(computeChunkEntities(x, z)));
                        return entities.toArray(List[]::new);
                    });
            for (List<Entity> entities : rangeEntities) {
                for (Entity entity : entities) {
                    query.consume(entity);
                }
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

        private void addTo(Point chunkPoint, Entity entity) {
            computeChunkEntities(chunkPoint.chunkX(), chunkPoint.chunkZ()).add(entity);
        }

        private void removeFrom(Point chunkPoint, Entity entity) {
            List<Entity> entities = this.chunkEntities.get(getChunkIndex(chunkPoint));
            if (entities != null) entities.remove(entity);
        }

        private List<Entity> computeChunkEntities(int chunkX, int chunkZ) {
            return chunkEntities.computeIfAbsent(getChunkIndex(chunkX, chunkZ), l -> new CopyOnWriteArrayList<>());
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
        public void chunkEntities(int chunkX, int chunkZ, @NotNull Query query) {
            synchronized (mutex) {
                t.chunkEntities(chunkX, chunkZ, query);
            }
        }

        @Override
        public void chunkRangeEntities(@NotNull Point chunkPoint, int range, @NotNull Query query) {
            synchronized (mutex) {
                t.chunkRangeEntities(chunkPoint, range, query);
            }
        }

        @Override
        public void nearbyEntities(@NotNull Point point, double range, @NotNull Query query) {
            synchronized (mutex) {
                t.nearbyEntities(point, range, query);
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
