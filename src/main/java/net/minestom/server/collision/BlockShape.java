package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;

import java.util.Arrays;
import java.util.List;

public interface BlockShape {
    static BlockShapeImpl parseBlockFromRegistry(String str) {
        return null;
    }

    boolean intersectEntity(Point position, EntityBoundingBox boundingBox, Point placementPosition);
    List<? extends Collidable> intersectEntitySwept(Point rayStart, Point rayDirection, Point blockPos, EntityBoundingBox moving);
}