package net.minestom.server.entity.pathfinding;

import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.pathfinding.generators.NodeGenerator;
import net.minestom.server.instance.block.Block;

import java.util.concurrent.CompletableFuture;

public final class AsyncPathGenerator {
    private AsyncPathGenerator() {}

    public static CompletableFuture<PPath> generateAsync(Block.Getter getter, Point start, Point target,
                                                         double closeDistance, double maxDistance, double pathVariance,
                                                         BoundingBox boundingBox, boolean isOnGround, NodeGenerator generator,
                                                         Runnable onComplete) {
        return CompletableFuture.supplyAsync(() ->
                PathGenerator.generate(getter, start.asPos(), target, closeDistance, maxDistance, pathVariance, boundingBox, isOnGround, generator, onComplete));
    }
}
