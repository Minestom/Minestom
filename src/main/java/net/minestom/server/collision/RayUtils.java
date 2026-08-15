package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;

final class RayUtils {
    /**
     * Check if a bounding box intersects a ray
     *
     * @param rayStart         Ray start position
     * @param rayDirection     Ray to check
     * @param collidableStatic Bounding box
     * @param finalResult      the sweep result to write the intersection details to
     * @return true if an intersection between the ray and the bounding box was found
     */
    static boolean boundingBoxIntersectionCheck(BoundingBox moving, Point rayStart, Point rayDirection, BoundingBox collidableStatic, Point staticCollidableOffset, SweepResult finalResult) {
        return !Double.isNaN(boundingBoxIntersectionPercentage(moving, rayStart, rayDirection,
                collidableStatic, staticCollidableOffset, finalResult.res, finalResult));
    }

    static boolean boundingBoxRayIntersectionCheck(Vec start, Vec direction, BoundingBox boundingBox, Pos position) {
        return !Double.isNaN(boundingBoxIntersectionPercentage(
                BoundingBox.ZERO, start, direction, boundingBox, position, Double.MAX_VALUE));
    }

    /**
     * Returns the accepted movement percentage, or {@link Double#NaN} if the boxes do not intersect.
     */
    static double boundingBoxIntersectionPercentage(BoundingBox moving, Point rayStart, Point rayDirection,
                                                     BoundingBox collidableStatic, Point staticCollidableOffset,
                                                     double maxPercentage) {
        return boundingBoxIntersectionPercentage(moving, rayStart, rayDirection,
                collidableStatic, staticCollidableOffset, maxPercentage, null);
    }

    @SuppressWarnings("DuplicatedCode")
    private static double boundingBoxIntersectionPercentage(BoundingBox moving, Point rayStart, Point rayDirection,
                                                            BoundingBox collidableStatic, Point staticCollidableOffset,
                                                            double maxPercentage, @Nullable SweepResult finalResult) {
        final double halfWidth = moving.width() / 2;
        final double halfHeight = moving.height() / 2;
        final double halfDepth = moving.depth() / 2;

        final double rayCentreX = rayStart.x() + moving.minX() + halfWidth;
        final double rayCentreY = rayStart.y() + moving.minY() + halfHeight;
        final double rayCentreZ = rayStart.z() + moving.minZ() + halfDepth;

        final double rayDirX = rayDirection.x();
        final double rayDirY = rayDirection.y();
        final double rayDirZ = rayDirection.z();

        // Expand the static box by the moving box and intersect its three axis slabs.
        final double expandedMinX = collidableStatic.minX() + staticCollidableOffset.x() - halfWidth;
        final double expandedMinY = collidableStatic.minY() + staticCollidableOffset.y() - halfHeight;
        final double expandedMinZ = collidableStatic.minZ() + staticCollidableOffset.z() - halfDepth;
        final double expandedMaxX = collidableStatic.maxX() + staticCollidableOffset.x() + halfWidth;
        final double expandedMaxY = collidableStatic.maxY() + staticCollidableOffset.y() + halfHeight;
        final double expandedMaxZ = collidableStatic.maxZ() + staticCollidableOffset.z() + halfDepth;

        final double entryX, exitX;
        if (rayDirX == 0) {
            if (rayCentreX < expandedMinX || rayCentreX > expandedMaxX) return Double.NaN;
            entryX = Double.NEGATIVE_INFINITY;
            exitX = Double.POSITIVE_INFINITY;
        } else if (rayDirX > 0) {
            entryX = epsilon((expandedMinX - rayCentreX) / rayDirX);
            exitX = (expandedMaxX - rayCentreX) / rayDirX;
        } else {
            entryX = epsilon((expandedMaxX - rayCentreX) / rayDirX);
            exitX = (expandedMinX - rayCentreX) / rayDirX;
        }

        final double entryZ, exitZ;
        if (rayDirZ == 0) {
            if (rayCentreZ < expandedMinZ || rayCentreZ > expandedMaxZ) return Double.NaN;
            entryZ = Double.NEGATIVE_INFINITY;
            exitZ = Double.POSITIVE_INFINITY;
        } else if (rayDirZ > 0) {
            entryZ = epsilon((expandedMinZ - rayCentreZ) / rayDirZ);
            exitZ = (expandedMaxZ - rayCentreZ) / rayDirZ;
        } else {
            entryZ = epsilon((expandedMaxZ - rayCentreZ) / rayDirZ);
            exitZ = (expandedMinZ - rayCentreZ) / rayDirZ;
        }

        final double entryY, exitY;
        if (rayDirY == 0) {
            if (rayCentreY < expandedMinY || rayCentreY > expandedMaxY) return Double.NaN;
            entryY = Double.NEGATIVE_INFINITY;
            exitY = Double.POSITIVE_INFINITY;
        } else if (rayDirY > 0) {
            entryY = epsilon((expandedMinY - rayCentreY) / rayDirY);
            exitY = (expandedMaxY - rayCentreY) / rayDirY;
        } else {
            entryY = epsilon((expandedMaxY - rayCentreY) / rayDirY);
            exitY = (expandedMinY - rayCentreY) / rayDirY;
        }

        double percentage = entryX;
        int collisionFace = 0;
        if (entryZ > percentage) {
            percentage = entryZ;
            collisionFace = 1;
        }
        if (entryY > percentage) {
            percentage = entryY;
            collisionFace = 2;
        }

        if (percentage > Math.min(exitX, Math.min(exitY, exitZ)) || percentage < 0) return Double.NaN;
        percentage *= 0.99999;
        if (!(percentage <= maxPercentage)) return Double.NaN;

        if (finalResult != null) {
            finalResult.res = percentage;
            finalResult.normalX = collisionFace == 0 ? 1 : 0;
            finalResult.normalY = collisionFace == 2 ? 1 : 0;
            finalResult.normalZ = collisionFace == 1 ? 1 : 0;
        }
        return percentage;
    }

    private static double epsilon(double value) {
        return Math.abs(value) < Vec.EPSILON ? 0 : value;
    }
}
