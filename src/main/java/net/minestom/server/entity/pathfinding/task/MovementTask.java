package net.minestom.server.entity.pathfinding.task;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.entity.pathfinding.engine.PathfindingResult;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.ServerPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.PathfindUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class MovementTask<P extends MovementTask.Path> extends PathfindTask<P> {

    private final Point initialTarget;

    MovementTask(@NotNull Point initialTarget) {
        this.initialTarget = initialTarget;
    }

    public Point initialTarget() {
        return initialTarget;
    }

    /**
     * This path represents a dynamic path that changes as the navigator moves.
     */
    public abstract static class Path extends PathfindTask.Path {

        private @NotNull PathfindingResult result;
        private @Nullable Queue<Point> calculatedPath = new ConcurrentLinkedQueue<>();

        protected Path(@NotNull PathfindingEngine engine, @NotNull Entity entity, @NotNull MovementTask<?> task) {
            super(engine, entity);
            this.result = startPathfind(task.initialTarget());
            handlePath(result.createPathIterator());
        }

        /////////
        // Api //
        /////////

        @Override
        public @Nullable Point nextPoint() {
            if (calculatedPath == null) {
                return null;
            }
            return calculatedPath.peek();
        }

        @Override
        public void updateTarget(Point target) {
            this.result = startPathfind(target);
            handlePath(result.createPathIterator());
        }

        @Override
        public @Nullable Queue<Point> fullPath() {
            return null;
        }

        ////////////////////
        // Implementation //
        ////////////////////

        /**
         * Returns the movement cost required to travel between the two points.
         * @param from the starting point
         * @param to the ending point
         * @return the movement cost
         */
        protected double getCost(Point from, Point to) {
            Entity entity = entity();
            // TODO: Implement line intersection algorithm to determine the cost
            // The current algorithm is flawed and may tell the navigator to move through very
            // specific corners that are not actually possible
            Instance instance = entity.getInstance();
            Objects.requireNonNull(instance, "The navigator must be in an instance while pathfinding.");
            Block block = instance.getBlock(to);
            if (block.isSolid()) {
                return Double.POSITIVE_INFINITY;
            }
            return block.registry().speedFactor();
        }

        /**
         * Determines whether the entity can exist in the given position.
         * @param point the position
         * @return true if the entity can exist in the given position, false otherwise
         */
        protected boolean isBlocked(Point point) {
            Entity entity = entity();
            Instance instance = entity.getInstance();
            Objects.requireNonNull(instance, "The navigator must be in an instance while pathfinding.");
            return PathfindUtils.isBlocked(point, entity.getBoundingBox(), instance, 0.1);
        }

        /**
         * Moves the entity towards the target position.
         * @param point the target position
         * @return false if the navigator is not able to move further, true otherwise
         */
        protected abstract boolean moveTowards(Point point);

        private @NotNull PathfindingResult startPathfind(@NotNull Point target) {
            Entity entity = entity();
            // TODO: Make the max distance configurable
            PathfindingEngine.PathfindOptions options = new PathfindingEngine.PathfindOptions(this::getCost,
                    this::isBlocked, 100);
            return engine().findE2P(options, entity, target);
        }

        private void handlePath(@NotNull Iterator<@NotNull CompletableFuture<@Nullable Queue<Point>>> iterator) {
            Entity entity = entity();
            // Use the iterator to get the future points
            if (iterator.hasNext()) {
                CompletableFuture<Queue<Point>> future = iterator.next();

                // Once the point queue is calculated
                future.thenAccept(points -> {
                    if (points == null) {
                        // If the point queue is null, the pathfinding failed
                        calculatedPath = null;
                        return;
                    }
                    // Move the navigator to each point
                    entity.scheduleNextTick((ignored) -> handleMovementThen(points, () -> {
                        // Once we have moved to each point, we can handle the next point queue
                        handlePath(iterator);
                    }));
                });
            } else {
                // If there is no more future points, there are no more points to walk to
                // This means we are done
                calculatedPath = null;
            }
        }

        private void handleMovementThen(Queue<Point> points, Runnable then) {
            Entity entity = entity();
            if (points.isEmpty()) {
                then.run();
                return;
            }

            Point target = points.peek();
            assert target != null;
            while (PathfindUtils.isTouching(entity.getBoundingBox(), entity.getPosition(), target, 0.1)) {
                // If the target is close enough, remove it from the queue
                target = points.poll();
                if (target == null) {
                    break;
                }
            }
            if (target != null) {
                Point finalTarget = target;
                entity.scheduleNextTick((ignored) -> {
                    // TODO: Remove this debug
                    ServerPacket packet = ParticleCreator.createParticlePacket(
                            Particle.SMOKE,
                            finalTarget.x(), finalTarget.y(), finalTarget.z(),
                            0, 0, 0,
                            1
                    );
                    PacketUtils.sendGroupedPacket(MinecraftServer.getConnectionManager().getOnlinePlayers(), packet);

                    boolean isStuck = moveTowards(finalTarget);
                    if (isStuck) {
                        // If we are stuck, we can't move further, so we should give up
                        calculatedPath = null;
                        return;
                    }
                    handleMovementThen(points, then);
                });
            } else {
                then.run();
            }
        }
    }
}
