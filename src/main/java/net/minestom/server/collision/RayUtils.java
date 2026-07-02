package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

final class RayUtils {
    /**
     * Swept AABB test (slab method on the Minkowski sum of the two boxes).
     * <p>
     * Finds the earliest time in {@code [0, finalResult.res]} at which {@code moving}, translated
     * from {@code rayStart} along {@code rayDirection}, touches the static box. On hit, writes the
     * time into {@code finalResult.res} and sets the entry-face axis normal.
     *
     * @return true if a hit at or before {@code finalResult.res} was found
     */
    public static boolean BoundingBoxIntersectionCheck(BoundingBox moving, Point rayStart, Point rayDirection,
                                                       BoundingBox collidableStatic, Point staticCollidableOffset,
                                                       SweepResult finalResult) {
        return sweep(
                rayStart.x() + moving.minX(), rayStart.y() + moving.minY(), rayStart.z() + moving.minZ(),
                rayStart.x() + moving.maxX(), rayStart.y() + moving.maxY(), rayStart.z() + moving.maxZ(),
                rayDirection.x(), rayDirection.y(), rayDirection.z(),
                collidableStatic.minX() + staticCollidableOffset.x(),
                collidableStatic.minY() + staticCollidableOffset.y(),
                collidableStatic.minZ() + staticCollidableOffset.z(),
                collidableStatic.maxX() + staticCollidableOffset.x(),
                collidableStatic.maxY() + staticCollidableOffset.y(),
                collidableStatic.maxZ() + staticCollidableOffset.z(),
                finalResult);
    }

    /**
     * Primitive core of {@link #BoundingBoxIntersectionCheck}: moving box world bounds at the ray
     * start, ray direction, static box world bounds.
     */
    static boolean sweep(double moMinX, double moMinY, double moMinZ,
                         double moMaxX, double moMaxY, double moMaxZ,
                         double dirX, double dirY, double dirZ,
                         double stMinX, double stMinY, double stMinZ,
                         double stMaxX, double stMaxY, double stMaxZ,
                         SweepResult finalResult) {
        // Per-axis entry/exit times; zero-direction axes must already overlap
        double enterX = Double.NEGATIVE_INFINITY, exitX = Double.POSITIVE_INFINITY;
        if (dirX != 0) {
            final double inv = 1 / dirX;
            final double t1 = (stMinX - moMaxX) * inv, t2 = (stMaxX - moMinX) * inv;
            enterX = epsilon(Math.min(t1, t2));
            exitX = Math.max(t1, t2);
        } else if (moMaxX < stMinX || moMinX > stMaxX) return false;

        double enterY = Double.NEGATIVE_INFINITY, exitY = Double.POSITIVE_INFINITY;
        if (dirY != 0) {
            final double inv = 1 / dirY;
            final double t1 = (stMinY - moMaxY) * inv, t2 = (stMaxY - moMinY) * inv;
            enterY = epsilon(Math.min(t1, t2));
            exitY = Math.max(t1, t2);
        } else if (moMaxY < stMinY || moMinY > stMaxY) return false;

        double enterZ = Double.NEGATIVE_INFINITY, exitZ = Double.POSITIVE_INFINITY;
        if (dirZ != 0) {
            final double inv = 1 / dirZ;
            final double t1 = (stMinZ - moMaxZ) * inv, t2 = (stMaxZ - moMinZ) * inv;
            enterZ = epsilon(Math.min(t1, t2));
            exitZ = Math.max(t1, t2);
        } else if (moMaxZ < stMinZ || moMinZ > stMaxZ) return false;

        // Entry time is the last slab entered; ties keep X over Z over Y (legacy face priority)
        final int axis;
        final double tEnter;
        if (enterX >= enterZ && enterX >= enterY) {
            axis = 0;
            tEnter = enterX;
        } else if (enterZ >= enterY) {
            axis = 2;
            tEnter = enterZ;
        } else {
            axis = 1;
            tEnter = enterY;
        }
        // No moving axis -> no sweep hit (static overlaps are handled elsewhere)
        if (tEnter == Double.NEGATIVE_INFINITY) return false;
        if (tEnter > Math.min(exitX, Math.min(exitY, exitZ))) return false;

        final double percentage = tEnter * 0.99999;
        if (percentage < 0 || percentage > finalResult.res) return false;
        finalResult.res = percentage;
        finalResult.normalX = axis == 0 ? 1 : 0;
        finalResult.normalY = axis == 1 ? 1 : 0;
        finalResult.normalZ = axis == 2 ? 1 : 0;
        return true;
    }

    private static double epsilon(double value) {
        return Math.abs(value) < Vec.EPSILON ? 0 : value;
    }

    public static boolean BoundingBoxRayIntersectionCheck(Vec start, Vec direction, BoundingBox boundingBox, Pos position) {
        return BoundingBoxIntersectionCheck(BoundingBox.ZERO, start, direction, boundingBox, position,
                new SweepResult(Double.MAX_VALUE, 0, 0, 0, null, 0, 0, 0, 0, 0, 0));
    }
}
