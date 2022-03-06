package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

public class CollisionHelper {
    public static boolean intersect(Collidable bb1, Point bb1Pos, Collidable bb2, Point bb2Pos) {
        return bb1.relativeCollision(bb2, bb1Pos.sub(bb2Pos));
    }

    public static void sweptIntercept(Collidable collidableMoving, Pos colledableMovingPosition, Collidable collidableStatic, Pos collidableStaticPosition, Vec delta) {
    }
}
