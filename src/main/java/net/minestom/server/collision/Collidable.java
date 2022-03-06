package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;

public interface Collidable {
    boolean relativeCollision(Collidable blockShape, Point point);
}
