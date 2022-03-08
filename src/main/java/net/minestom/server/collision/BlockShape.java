package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

public interface BlockShape {
    static BlockShapeImpl parseBlockFromRegistry(String str) {
        return null;
    }

    boolean intersectEntity(Point position, EntityBoundingBox boundingBox, Point placementPosition);
    boolean intersectEntitySwept(Point rayStart, Point rayDirection, Point blockPos, EntityBoundingBox moving, Pos entityPosition, RayUtils.SweepResult tempResult, RayUtils.SweepResult finalResult, Block block);
}