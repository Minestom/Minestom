package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;

public interface Shape {
    static ShapeImpl parseBlockFromRegistry(String str, Block block) {
        return null;
    }

    boolean intersectEntity(Point position, BoundingBox boundingBox, Point placementPosition);
    boolean intersectEntitySwept(Point rayStart, Point rayDirection, Point blockPos, BoundingBox moving, Pos entityPosition, RayUtils.SweepResult tempResult, RayUtils.SweepResult finalResult);
}