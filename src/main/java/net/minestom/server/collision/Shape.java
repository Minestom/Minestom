package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;

public interface Shape {
    boolean intersectEntity(Point position, BoundingBox boundingBox, Point placementPosition);
    boolean intersectEntitySwept(Point rayStart, Point rayDirection, Point blockPos, BoundingBox moving, Point entityPosition, SweepResult tempResult, SweepResult finalResult);
}