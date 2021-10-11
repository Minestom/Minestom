package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class EntityTrackingImpl {

    /**
     * Default tracking implementation storing entities per-chunk.
     */
    static final class PerChunk implements EntityTracking {
        private final Long2ObjectMap<List<Entity>> chunkEntities = new Long2ObjectOpenHashMap<>();

        @Override
        public void register(@NotNull Entity entity, @NotNull Point spawnPoint) {
            addTo(spawnPoint, entity);
        }

        @Override
        public void unregister(@NotNull Entity entity, @NotNull Point point) {
            removeFrom(point, entity);
        }

        @Override
        public void move(@NotNull Entity entity, @NotNull Point oldPoint, @NotNull Point newPoint, Update update) {
            if (!oldPoint.sameChunk(newPoint)) {
                removeFrom(oldPoint, entity);
                addTo(newPoint, entity);
                difference(oldPoint, newPoint, update);
            }
        }

        @Override
        public void difference(@NotNull Point from, @NotNull Point to, @NotNull Update update) {
            final int range = MinecraftServer.getEntityViewDistance();
            ChunkUtils.forDifferingChunksInRange(to.chunkX(), to.chunkZ(), range, from.chunkX(), from.chunkZ(), range, chunkIndex -> {
                // Add
                final List<Entity> entities = getOptional(chunkIndex);
                if (entities == null) return;
                for (Entity entity : entities) {
                    update.add(entity);
                }
            }, chunkIndex -> {
                // Remove
                final List<Entity> entities = getOptional(chunkIndex);
                if (entities == null) return;
                for (Entity entity : entities) {
                    update.remove(entity);
                }
            });
        }

        @Override
        public void nearbyEntities(@NotNull Point point, double range, @NotNull Query query) {
            final int minX = ChunkUtils.getChunkCoordinate(point.x() - range);
            final int maxX = ChunkUtils.getChunkCoordinate(point.x() + range);
            final int minZ = ChunkUtils.getChunkCoordinate(point.z() - range);
            final int maxZ = ChunkUtils.getChunkCoordinate(point.z() + range);
            // Cache squared range to prevent sqrt operations
            final double squaredRange = range * range;

            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    final List<Entity> chunkEntities = getOptional(x, z);
                    if (chunkEntities == null || chunkEntities.isEmpty()) continue;
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
            final List<Entity> entities = getOptional(chunkPoint);
            if (entities != null && !entities.isEmpty()) {
                for (Entity entity : entities) {
                    query.consume(entity);
                }
            }
        }

        @Override
        public void chunkRangeEntities(@NotNull Point chunkPoint, int range, @NotNull Query query) {
            ChunkUtils.forChunksInRange(chunkPoint, range, chunkIndex -> {
                final List<Entity> entities = getOptional(chunkIndex);
                if (entities == null || entities.isEmpty()) return;
                for (Entity entity : entities) {
                    query.consume(entity);
                }
            });
        }

        private void addTo(Point chunkPoint, Entity entity) {
            this.chunkEntities.computeIfAbsent(ChunkUtils.getChunkIndex(chunkPoint.chunkX(), chunkPoint.chunkZ()),
                    l -> new CopyOnWriteArrayList<>()).add(entity);
        }

        private void removeFrom(Point chunkPoint, Entity entity) {
            final long index = ChunkUtils.getChunkIndex(chunkPoint.chunkX(), chunkPoint.chunkZ());
            List<Entity> entities = this.chunkEntities.get(index);
            if (entities == null) return;
            entities.remove(entity);
            if (entities.isEmpty()) this.chunkEntities.remove(index);
        }

        private List<Entity> getOptional(long index) {
            return chunkEntities.get(index);
        }

        private List<Entity> getOptional(int chunkX, int chunkZ) {
            return getOptional(ChunkUtils.getChunkIndex(chunkX, chunkZ));
        }

        private List<Entity> getOptional(Point point) {
            return getOptional(point.chunkX(), point.chunkZ());
        }
    }

}
