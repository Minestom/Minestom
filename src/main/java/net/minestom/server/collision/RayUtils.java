package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

final class RayUtils {

    /**
     * Swept AABB-vs-AABB intersection via the slab method.
     * <p>
     * The moving box is treated as a point by expanding the static box by the
     * moving box's half-extents (Minkowski sum) and intersecting the ray with
     * the expanded box. If a hit is found that improves on the current
     * {@code finalResult.res}, the result is updated with the new percentage,
     * the axis-aligned entry-face normal, the world-space hit position and
     * a reference to the colliding shape.
     *
     * @param shape the shape that owns {@code collidableStatic} (recorded on hit)
     * @return true iff {@code finalResult} was updated
     */
    static boolean BoundingBoxIntersectionCheck(Shape shape,
                                                BoundingBox moving, Point rayStart, Point rayDirection,
                                                BoundingBox collidableStatic,
                                                double sx, double sy, double sz,
                                                SweepResult finalResult) {
        // Moving box origin (in its own frame) and half-extents
        final double mMinX = moving.minX(), mMinY = moving.minY(), mMinZ = moving.minZ();
        final double mw2 = (moving.maxX() - mMinX) * 0.5;
        final double mh2 = (moving.maxY() - mMinY) * 0.5;
        final double md2 = (moving.maxZ() - mMinZ) * 0.5;

        // Expanded static box bounds (Minkowski sum) in world space
        final double minX = sx + collidableStatic.minX() - mw2;
        final double maxX = sx + collidableStatic.maxX() + mw2;
        final double minY = sy + collidableStatic.minY() - mh2;
        final double maxY = sy + collidableStatic.maxY() + mh2;
        final double minZ = sz + collidableStatic.minZ() - md2;
        final double maxZ = sz + collidableStatic.maxZ() + md2;

        // Ray origin = moving box centre in world space
        final double rsx = rayStart.x(), rsy = rayStart.y(), rsz = rayStart.z();
        final double rx = rsx + mMinX + mw2;
        final double ry = rsy + mMinY + mh2;
        final double rz = rsz + mMinZ + md2;

        final double dx = rayDirection.x();
        final double dy = rayDirection.y();
        final double dz = rayDirection.z();

        // Per-axis slab intervals.
        // For zero-velocity axes, the ray is parallel to the slab so an
        // overlap requires the origin to lie within the slab.
        final double tMinX, tMaxX;
        if (dx == 0) {
            if (rx < minX || rx > maxX) return false;
            tMinX = Double.NEGATIVE_INFINITY;
            tMaxX = Double.POSITIVE_INFINITY;
        } else {
            double t1 = (minX - rx) / dx;
            double t2 = (maxX - rx) / dx;
            // Snap near-zero values: forgives the "just past the face" case
            // that legacy behavior accepted via the same epsilon clamp.
            if (Math.abs(t1) < Vec.EPSILON) t1 = 0;
            if (Math.abs(t2) < Vec.EPSILON) t2 = 0;
            if (t1 > t2) {
                tMinX = t2;
                tMaxX = t1;
            } else {
                tMinX = t1;
                tMaxX = t2;
            }
        }
        final double tMinY, tMaxY;
        if (dy == 0) {
            if (ry < minY || ry > maxY) return false;
            tMinY = Double.NEGATIVE_INFINITY;
            tMaxY = Double.POSITIVE_INFINITY;
        } else {
            double t1 = (minY - ry) / dy;
            double t2 = (maxY - ry) / dy;
            if (Math.abs(t1) < Vec.EPSILON) t1 = 0;
            if (Math.abs(t2) < Vec.EPSILON) t2 = 0;
            if (t1 > t2) {
                tMinY = t2;
                tMaxY = t1;
            } else {
                tMinY = t1;
                tMaxY = t2;
            }
        }
        final double tMinZ, tMaxZ;
        if (dz == 0) {
            if (rz < minZ || rz > maxZ) return false;
            tMinZ = Double.NEGATIVE_INFINITY;
            tMaxZ = Double.POSITIVE_INFINITY;
        } else {
            double t1 = (minZ - rz) / dz;
            double t2 = (maxZ - rz) / dz;
            if (Math.abs(t1) < Vec.EPSILON) t1 = 0;
            if (Math.abs(t2) < Vec.EPSILON) t2 = 0;
            if (t1 > t2) {
                tMinZ = t2;
                tMaxZ = t1;
            } else {
                tMinZ = t1;
                tMaxZ = t2;
            }
        }

        // Entry t and axis. Tie-break order X > Z > Y matches legacy face precedence.
        double tNear = tMinX;
        int face = 0;
        if (tMinZ > tNear) {
            tNear = tMinZ;
            face = 1;
        }
        if (tMinY > tNear) {
            tNear = tMinY;
            face = 2;
        }
        final double tFar = Math.min(Math.min(tMaxX, tMaxY), tMaxZ);
        // NaN-safe: any NaN makes both comparisons false, returning here.
        if (!(tNear <= tFar)) return false;

        final double percentage = tNear * 0.99999;
        if (!(percentage >= 0 && percentage <= finalResult.res)) return false;

        finalResult.res = percentage;
        finalResult.normalX = (face == 0) ? 1 : 0;
        finalResult.normalY = (face == 2) ? 1 : 0;
        finalResult.normalZ = (face == 1) ? 1 : 0;
        finalResult.collidedPositionX = rsx + dx * percentage;
        finalResult.collidedPositionY = rsy + dy * percentage;
        finalResult.collidedPositionZ = rsz + dz * percentage;
        finalResult.collidedShapeX = sx;
        finalResult.collidedShapeY = sy;
        finalResult.collidedShapeZ = sz;
        finalResult.collidedShape = shape;
        return true;
    }

    static boolean BoundingBoxRayIntersectionCheck(Vec start, Vec direction, BoundingBox boundingBox, Pos position) {
        return BoundingBoxIntersectionCheck(boundingBox, BoundingBox.ZERO, start, direction, boundingBox,
                position.x(), position.y(), position.z(),
                new SweepResult(Double.MAX_VALUE, 0, 0, 0, null, 0, 0, 0, 0, 0, 0));
    }
}
