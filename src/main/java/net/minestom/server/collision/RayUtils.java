package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

final class RayUtils {
    /**
     * Check if a bounding box intersects a ray
     *
     * @param rayStart         Ray start position
     * @param rayDirection     Ray to check
     * @param collidableStatic Bounding box
     * @param finalResult
     * @return true if an intersection between the ray and the bounding box was found
     */
    public static boolean BoundingBoxIntersectionCheck(BoundingBox moving, Point rayStart, Point rayDirection, BoundingBox collidableStatic, Point staticCollidableOffset, SweepResult finalResult) {
        Point bbCentre = new Vec(moving.minX() + moving.width() / 2, moving.minY() + moving.height() / 2 + Vec.EPSILON, moving.minZ() + moving.depth() / 2);
        Point rayCentre = rayStart.add(bbCentre);

        // Translate bounding box
        Vec bbOffMin = new Vec(collidableStatic.minX() - rayCentre.x() + staticCollidableOffset.x() - moving.width() / 2, collidableStatic.minY() - rayCentre.y() + staticCollidableOffset.y() - moving.height() / 2, collidableStatic.minZ() - rayCentre.z() + staticCollidableOffset.z() - moving.depth() / 2);
        Vec bbOffMax = new Vec(collidableStatic.maxX() - rayCentre.x() + staticCollidableOffset.x() + moving.width() / 2, collidableStatic.maxY() - rayCentre.y() + staticCollidableOffset.y() + moving.height() / 2, collidableStatic.maxZ() - rayCentre.z() + staticCollidableOffset.z() + moving.depth() / 2);

        // This check is done in 2d. it can be visualised as a rectangle (the face we are checking), and a point.
        // If the point is within the rectangle, we know the vector intersects the face.

        double signumRayX = Math.signum(rayDirection.x());
        double signumRayY = Math.signum(rayDirection.y());
        double signumRayZ = Math.signum(rayDirection.z());

        boolean isHit = false;
        double percentage = Double.MAX_VALUE;
        int collisionFace = -1;

        // Intersect X
        // Left side of bounding box
        if (rayDirection.x() > 0) {
            double xFac = bbOffMin.x() / rayDirection.x();
            if (xFac < percentage) {
                double yix = rayDirection.y() * xFac + rayCentre.y();
                double zix = rayDirection.z() * xFac + rayCentre.z();

                // Check if ray passes through y/z plane
                if (((yix - rayCentre.y()) * signumRayY) >= 0
                        && ((zix - rayCentre.z()) * signumRayZ) >= 0
                        && yix >= collidableStatic.minY() + staticCollidableOffset.y() - moving.height() / 2
                        && yix <= collidableStatic.maxY() + staticCollidableOffset.y() + moving.height() / 2
                        && zix >= collidableStatic.minZ() + staticCollidableOffset.z() - moving.depth() / 2
                        && zix <= collidableStatic.maxZ() + staticCollidableOffset.z() + moving.depth() / 2) {
                    isHit = true;
                    percentage = xFac;
                    collisionFace = 0;
                }
            }
        }
        // Right side of bounding box
        if (rayDirection.x() < 0) {
            double xFac = bbOffMax.x() / rayDirection.x();
            if (xFac < percentage) {
                double yix = rayDirection.y() * xFac + rayCentre.y();
                double zix = rayDirection.z() * xFac + rayCentre.z();

                if (((yix - rayCentre.y()) * signumRayY) >= 0
                        && ((zix - rayCentre.z()) * signumRayZ) >= 0
                        && yix >= collidableStatic.minY() + staticCollidableOffset.y() - moving.height() / 2
                        && yix <= collidableStatic.maxY() + staticCollidableOffset.y() + moving.height() / 2
                        && zix >= collidableStatic.minZ() + staticCollidableOffset.z() - moving.depth() / 2
                        && zix <= collidableStatic.maxZ() + staticCollidableOffset.z() + moving.depth() / 2) {
                    isHit = true;
                    percentage = xFac;
                    collisionFace = 0;
                }
            }
        }

        // Intersect Z
        if (rayDirection.z() > 0) {
            double zFac = bbOffMin.z() / rayDirection.z();
            if (zFac < percentage) {
                double xiz = rayDirection.x() * zFac + rayCentre.x();
                double yiz = rayDirection.y() * zFac + rayCentre.y();

                if (((yiz - rayCentre.y()) * signumRayY) >= 0
                        && ((xiz - rayCentre.x()) * signumRayX) >= 0
                        && xiz >= collidableStatic.minX() + staticCollidableOffset.x() - moving.width() / 2
                        && xiz <= collidableStatic.maxX() + staticCollidableOffset.x() + moving.width() / 2
                        && yiz >= collidableStatic.minY() + staticCollidableOffset.y() - moving.height() / 2
                        && yiz <= collidableStatic.maxY() + staticCollidableOffset.y() + moving.height() / 2) {
                    isHit = true;
                    percentage = zFac;
                    collisionFace = 1;
                }
            }
        }
        if (rayDirection.z() < 0) {
            double zFac = bbOffMax.z() / rayDirection.z();
            if (zFac < percentage) {
                double xiz = rayDirection.x() * zFac + rayCentre.x();
                double yiz = rayDirection.y() * zFac + rayCentre.y();

                if (((yiz - rayCentre.y()) * signumRayY) >= 0
                        && ((xiz - rayCentre.x()) * signumRayX) >= 0
                        && xiz >= collidableStatic.minX() + staticCollidableOffset.x() - moving.width() / 2
                        && xiz <= collidableStatic.maxX() + staticCollidableOffset.x() + moving.width() / 2
                        && yiz >= collidableStatic.minY() + staticCollidableOffset.y() - moving.height() / 2
                        && yiz <= collidableStatic.maxY() + staticCollidableOffset.y() + moving.height() / 2) {
                    isHit = true;
                    percentage = zFac;
                    collisionFace = 1;
                }
            }
        }

        // Intersect Y
        if (rayDirection.y() > 0) {
            double yFac = bbOffMin.y() / rayDirection.y();
            if (yFac < percentage) {
                double xiy = rayDirection.x() * yFac + rayCentre.x();
                double ziy = rayDirection.z() * yFac + rayCentre.z();

                if (((ziy - rayCentre.z()) * signumRayZ) >= 0
                        && ((xiy - rayCentre.x()) * signumRayX) >= 0
                        && xiy >= collidableStatic.minX() + staticCollidableOffset.x() - moving.width() / 2
                        && xiy <= collidableStatic.maxX() + staticCollidableOffset.x() + moving.width() / 2
                        && ziy >= collidableStatic.minZ() + staticCollidableOffset.z() - moving.depth() / 2
                        && ziy <= collidableStatic.maxZ() + staticCollidableOffset.z() + moving.depth() / 2) {
                    isHit = true;
                    percentage = yFac;
                    collisionFace = 2;
                }
            }
        }

        if (rayDirection.y() < 0) {
            double yFac = bbOffMax.y() / rayDirection.y();
            if (yFac < percentage) {
                double xiy = rayDirection.x() * yFac + rayCentre.x();
                double ziy = rayDirection.z() * yFac + rayCentre.z();

                if (((ziy - rayCentre.z()) * signumRayZ) >= 0
                        && ((xiy - rayCentre.x()) * signumRayX) >= 0
                        && xiy >= collidableStatic.minX() + staticCollidableOffset.x() - moving.width() / 2
                        && xiy <= collidableStatic.maxX() + staticCollidableOffset.x() + moving.width() / 2
                        && ziy >= collidableStatic.minZ() + staticCollidableOffset.z() - moving.depth() / 2
                        && ziy <= collidableStatic.maxZ() + staticCollidableOffset.z() + moving.depth() / 2) {
                    isHit = true;
                    percentage = yFac;
                    collisionFace = 2;
                }
            }
        }

        percentage *= 0.99999;

        if (percentage >= 0 && percentage <= finalResult.res) {
            finalResult.res = percentage;
            finalResult.normalX = 0;
            finalResult.normalY = 0;
            finalResult.normalZ = 0;

            if (collisionFace == 0) finalResult.normalX = 1;
            if (collisionFace == 1) finalResult.normalZ = 1;
            if (collisionFace == 2) finalResult.normalY = 1;
        }

        return isHit;
    }

    public static boolean BoundingBoxRayIntersectionCheck(Vec start, Vec direction, BoundingBox boundingBox, Pos position) {
        return BoundingBoxIntersectionCheck(BoundingBox.ZERO, start, direction, boundingBox, position, new SweepResult(Double.MAX_VALUE, 0, 0, 0, null));
    }
}
