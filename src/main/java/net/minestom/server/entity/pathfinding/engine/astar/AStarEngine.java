package net.minestom.server.entity.pathfinding.engine.astar;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.pathfinding.Navigator;
import net.minestom.server.entity.pathfinding.engine.PathfindingEngine;
import net.minestom.server.entity.pathfinding.engine.PathfindingResult;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public record AStarEngine(Block.Getter getter, double entityPadding) implements PathfindingEngine<Navigator> {

    @Override
    public @NotNull PathfindingResult findP2P(@NotNull BoundingBox box, @NotNull Point start, @NotNull Point end) {
        Point relStart = box.relativeStart();
        Point relEnd = box.relativeEnd();
        double step = Math.min(box.width(), Math.min(box.height(), box.depth())) / 2.0;
        return PathfindingResult.futureCompleted(CompletableFuture.supplyAsync(() -> new AStarPathfinder(step).findPath(
                start,
                end,
                p -> getter.getBlock((Point) p).registry().speedFactor(),
                // If any corners that the entity is in is solid, it can't go through it
                p -> {
                    return getter.getBlock(p.add(relStart.x(), relStart.y(), relStart.z())).isSolid() ||
                            getter.getBlock(p.add(relStart.x(), relStart.y(), relEnd.z())).isSolid() ||
                            getter.getBlock(p.add(relStart.x(), relEnd.y(), relStart.z())).isSolid() ||
                            getter.getBlock(p.add(relStart.x(), relEnd.y(), relEnd.z())).isSolid() ||
                            getter.getBlock(p.add(relEnd.x(), relStart.y(), relStart.z())).isSolid() ||
                            getter.getBlock(p.add(relEnd.x(), relStart.y(), relEnd.z())).isSolid() ||
                            getter.getBlock(p.add(relEnd.x(), relEnd.y(), relStart.z())).isSolid() ||
                            getter.getBlock(p.add(relEnd.x(), relEnd.y(), relEnd.z())).isSolid();
                }
        )));
    }

    @Override
    public @NotNull PathfindingResult findN2N(@NotNull Navigator start, @NotNull Navigator end) {
        // TODO: Implement dynamic movement recalculation
        return findP2P(start.getBoundingBox(), start.getPosition(), end.getPosition());
    }

    @Override
    public @NotNull PathfindingResult findP2N(@NotNull BoundingBox box, @NotNull Point start, @NotNull Navigator end) {
        // TODO: Implement dynamic movement recalculation
        return findP2P(box, start, end.getPosition());
    }

    @Override
    public @NotNull PathfindingResult findN2P(@NotNull Navigator start, @NotNull Point end) {
        // TODO: Implement dynamic movement recalculation
        return findP2P(start.getBoundingBox(), start.getPosition(), end);
    }

    @Override
    public boolean async() {
        return false;
    }
}
