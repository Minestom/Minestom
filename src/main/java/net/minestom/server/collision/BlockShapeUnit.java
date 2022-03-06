package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;

public record BlockShapeUnit(Point size, Point position) implements Collidable {
    public boolean relativeCollision(Collidable blockShape, Point point) {
        return false;
    }
}
