package net.minestom.server.collision;

import net.minestom.server.collision.impl.RayUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public interface BoundingBox {
    boolean intersectBlock(Point src, Block block, Point dest);
    boolean intersectBlockSwept(Point entityPosition, Point rayDirection, Block block, Point blockPos, SweepResult tempResult, SweepResult finalResult);
    boolean intersectCollidable(@NotNull Point src, @NotNull Collidable entityBoundingBox, @NotNull Point dest);
    boolean intersectEntity(@NotNull Point src, @NotNull Entity entity);
    @NotNull BoundingBox expand(double x, double y, double z);
    @NotNull BoundingBox contract(double x, double y, double z);
}
