package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.chunk.ChunkUtils;

import java.util.*;

final class EntityTrackingImpl {

    /**
     * Default tracking implementation storing entities per-chunk.
     */
    static final class PerChunk implements EntityTracking {
        private final Map<Long, List<Entity>> chunkEntities = new HashMap<>();

        @Override
        public void register(Entity entity, Point spawnPoint) {
            this.chunkEntities.computeIfAbsent(getIndex(spawnPoint), c -> new ArrayList<>())
                    .add(entity);
        }

        @Override
        public List<Entity> registerAndView(Entity entity, Point spawnPoint, int range) {
            register(entity, spawnPoint);
            return null; // TODO
        }

        @Override
        public void unregister(Entity entity, Point point) {
            this.chunkEntities.computeIfAbsent(getIndex(point), c -> new ArrayList<>())
                    .remove(entity);
        }

        @Override
        public void move(Entity entity, Point oldPoint, Point newPoint) {
            // TODO
            if (!oldPoint.sameChunk(newPoint)) {

            }
        }

        @Override
        public List<Result> moveAndView(Entity entity, Point oldPoint, Point newPoint) {
            return null; // TODO
        }

        @Override
        public List<Entity> nearbyEntities(Point point, double range) {
            final int minX = ChunkUtils.getChunkCoordinate(point.x() - range);
            final int maxX = ChunkUtils.getChunkCoordinate(point.x() + range);
            final int minZ = ChunkUtils.getChunkCoordinate(point.z() - range);
            final int maxZ = ChunkUtils.getChunkCoordinate(point.z() + range);
            // Cache squared range to prevent sqrt operations
            final double squaredRange = range * range;

            List<Entity> result = new ArrayList<>();
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    final var chunkEntities = this.chunkEntities.get(getIndex(x, z));
                    if (chunkEntities == null || chunkEntities.isEmpty()) continue;
                    // Filter all entities out of range
                    for (Entity chunkEntity : chunkEntities) {
                        if (point.distanceSquared(chunkEntity.getPosition()) < squaredRange) {
                            result.add(chunkEntity);
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public List<Entity> chunkEntities(Point chunkPoint) {
            return chunkEntities.getOrDefault(getIndex(chunkPoint), Collections.emptyList());
        }

        @Override
        public List<Entity> chunkRangeEntities(Point chunkPoint, int range) {
            List<Entity> entities = new ArrayList<>();
            final long[] chunksInRange = ChunkUtils.getChunksInRange(chunkPoint, range);
            for (long chunkIndex : chunksInRange) {
                final List<Entity> ent = chunkEntities.get(chunkIndex);
                if (ent == null || ent.isEmpty()) continue;
                entities.addAll(ent);
            }
            return entities;
        }

        @Override
        public List<Result> difference(Point p1, Point p2) {
            return null; // TODO
        }

        private static long getIndex(int chunkX, int chunkZ) {
            return ChunkUtils.getChunkIndex(chunkX, chunkZ);
        }

        private static long getIndex(Point point) {
            return ChunkUtils.getChunkIndex(point.chunkX(), point.chunkZ());
        }
    }

    /**
     * Synchronize all tracking methods.
     */
    static final class Synchronized implements EntityTracking {
        private final EntityTracking t;
        private final Object mutex;

        public Synchronized(EntityTracking entityTracking) {
            this.t = entityTracking;
            this.mutex = this;
        }

        @Override
        public void register(Entity entity, Point spawnPoint) {
            synchronized (mutex) {
                t.register(entity, spawnPoint);
            }
        }

        @Override
        public List<Entity> registerAndView(Entity entity, Point spawnPoint, int range) {
            synchronized (mutex) {
                return t.registerAndView(entity, spawnPoint, range);
            }
        }

        @Override
        public void unregister(Entity entity, Point point) {
            synchronized (mutex) {
                t.unregister(entity, point);
            }
        }

        @Override
        public void move(Entity entity, Point oldPoint, Point newPoint) {
            synchronized (mutex) {
                t.move(entity, oldPoint, newPoint);
            }
        }

        @Override
        public List<Result> moveAndView(Entity entity, Point oldPoint, Point newPoint) {
            synchronized (mutex) {
                return t.moveAndView(entity, oldPoint, newPoint);
            }
        }

        @Override
        public List<Entity> nearbyEntities(Point point, double range) {
            synchronized (mutex) {
                return t.nearbyEntities(point, range);
            }
        }

        @Override
        public List<Entity> chunkEntities(Point chunkPoint) {
            synchronized (mutex) {
                return t.chunkEntities(chunkPoint);
            }
        }

        @Override
        public List<Entity> chunkRangeEntities(Point chunkPoint, int range) {
            synchronized (mutex) {
                return t.chunkRangeEntities(chunkPoint, range);
            }
        }

        @Override
        public List<Result> difference(Point p1, Point p2) {
            synchronized (mutex) {
                return t.difference(p1, p2);
            }
        }
    }

}
