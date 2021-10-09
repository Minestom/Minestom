package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;

import java.util.stream.Stream;

final class EntityTrackingImpl {
    static final class Synchronized implements EntityTracking {
        private final EntityTracking t;
        private final Object mutex;

        public Synchronized(EntityTracking entityTracking) {
            this.t = entityTracking;
            this.mutex = this;
        }

        @Override
        public void register(Entity entity, Chunk spawnChunk) {
            synchronized (mutex) {
                t.register(entity, spawnChunk);
            }
        }

        @Override
        public Stream<Entity> registerAndView(Entity entity, Chunk spawnChunk, int range) {
            synchronized (mutex) {
                return t.registerAndView(entity, spawnChunk, range);
            }
        }

        @Override
        public void unregister(Entity entity, Chunk chunk) {
            synchronized (mutex) {
                t.unregister(entity, chunk);
            }
        }

        @Override
        public void move(Entity entity, Point oldPoint, Point newPoint) {
            synchronized (mutex) {
                t.move(entity, oldPoint, newPoint);
            }
        }

        @Override
        public Stream<Result> moveAndView(Entity entity, Point oldPoint, Point newPoint) {
            synchronized (mutex) {
                return t.moveAndView(entity, oldPoint, newPoint);
            }
        }

        @Override
        public Stream<Entity> nearbyEntities(Point point, double range) {
            synchronized (mutex) {
                return t.nearbyEntities(point, range);
            }
        }

        @Override
        public Stream<Entity> chunkEntities(Point chunkPoint) {
            synchronized (mutex) {
                return t.chunkEntities(chunkPoint);
            }
        }

        @Override
        public Stream<Entity> chunkRangeEntities(Point chunkPoint, int range) {
            synchronized (mutex) {
                return t.chunkRangeEntities(chunkPoint, range);
            }
        }

        @Override
        public Stream<Result> difference(Point p1, Point p2) {
            synchronized (mutex) {
                return t.difference(p1, p2);
            }
        }
    }

}
