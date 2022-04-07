package net.minestom.server.entity.pathfinding.engine.astar;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.entity.pathfinding.engine.PathfindingResult;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public record AStarEngine(Block.Getter getter, double entityPadding) implements PathfindingEngine {

    @Override
    public @NotNull PathfindingResult findP2P(@NotNull PathfindOptions options, @NotNull BoundingBox box, @NotNull Point start, @NotNull Point end) {
        double step = Math.min(box.width(), box.depth()) / 2;
        return PathfindingResult.futureCompleted(CompletableFuture.supplyAsync(() -> AStarPathfinder.findPath(
                start,
                end,
                step,
                options.costProvider(),
                // If any corners that the entity is in is solid, it can't go through it
                options.blockedPredicate()
        )));
    }

    @Override
    public @NotNull PathfindingResult findE2E(@NotNull PathfindOptions options, @NotNull Entity start, @NotNull Entity end) {
        // TODO: Implement dynamic movement recalculation
        return findP2P(options, start.getBoundingBox(), start.getPosition(), end.getPosition());
    }

    @Override
    public @NotNull PathfindingResult findP2E(@NotNull PathfindOptions options, @NotNull BoundingBox box, @NotNull Point start, @NotNull Entity end) {
        // TODO: Implement dynamic movement recalculation
        return findP2P(options, box, start, end.getPosition());
    }

    @Override
    public @NotNull PathfindingResult findE2P(@NotNull PathfindOptions options, @NotNull Entity start, @NotNull Point end) {
        // TODO: Implement dynamic movement recalculation
        return findP2P(options, start.getBoundingBox(), start.getPosition(), end);
    }

    @Override
    public boolean async() {
        return false;
    }
}
