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
     * @param finalResult      the sweep result to write the intersection details to
     * @return true if an intersection between the ray and the bounding box was found
     */
    public static boolean BoundingBoxIntersectionCheck(BoundingBox moving, Point rayStart, Point rayDirection, BoundingBox collidableStatic, Point staticCollidableOffset, SweepResult finalResult) {
        final double halfWidth = moving.width() / 2;
        final double halfHeight = moving.height() / 2;
        final double halfDepth = moving.depth() / 2;

        final double rayCentreX = rayStart.x() + moving.minX() + halfWidth;
        final double rayCentreY = rayStart.y() + moving.minY() + halfHeight;
        final double rayCentreZ = rayStart.z() + moving.minZ() + halfDepth;

        final double rayDirX = rayDirection.x();
        final double rayDirY = rayDirection.y();
        final double rayDirZ = rayDirection.z();

        // Static box bounds in world space (offset by the shape position)
        final double staticMinX = collidableStatic.minX() + staticCollidableOffset.x();
        final double staticMinY = collidableStatic.minY() + staticCollidableOffset.y();
        final double staticMinZ = collidableStatic.minZ() + staticCollidableOffset.z();
        final double staticMaxX = collidableStatic.maxX() + staticCollidableOffset.x();
        final double staticMaxY = collidableStatic.maxY() + staticCollidableOffset.y();
        final double staticMaxZ = collidableStatic.maxZ() + staticCollidableOffset.z();

        // Expanded (Minkowski) bounds relative to the ray centre
        final double bbOffMinX = staticMinX - rayCentreX - halfWidth;
        final double bbOffMinY = staticMinY - rayCentreY - halfHeight;
        final double bbOffMinZ = staticMinZ - rayCentreZ - halfDepth;
        final double bbOffMaxX = staticMaxX - rayCentreX + halfWidth;
        final double bbOffMaxY = staticMaxY - rayCentreY + halfHeight;
        final double bbOffMaxZ = staticMaxZ - rayCentreZ + halfDepth;

        // This check is done in 2d. it can be visualised as a rectangle (the face we are checking), and a point.
        // If the point is within the rectangle, we know the vector intersects the face.

        double signumRayX = Math.signum(rayDirX);
        double signumRayY = Math.signum(rayDirY);
        double signumRayZ = Math.signum(rayDirZ);

        boolean isHit = false;
        double percentage = Double.MAX_VALUE;
        int collisionFace = -1;

        // Intersect X
        // Left side of bounding box
        if (rayDirX > 0) {
            double xFac = epsilon(bbOffMinX / rayDirX);
            if (xFac < percentage) {
                double yix = rayDirY * xFac + rayCentreY;
                double zix = rayDirZ * xFac + rayCentreZ;

                // Check if ray passes through y/z plane
                if (((yix - rayCentreY) * signumRayY) >= 0
                        && ((zix - rayCentreZ) * signumRayZ) >= 0
                        && yix >= staticMinY - halfHeight
                        && yix <= staticMaxY + halfHeight
                        && zix >= staticMinZ - halfDepth
                        && zix <= staticMaxZ + halfDepth) {
                    isHit = true;
                    percentage = xFac;
                    collisionFace = 0;
                }
            }
        }
        // Right side of bounding box
        if (rayDirX < 0) {
            double xFac = epsilon(bbOffMaxX / rayDirX);
            if (xFac < percentage) {
                double yix = rayDirY * xFac + rayCentreY;
                double zix = rayDirZ * xFac + rayCentreZ;

                if (((yix - rayCentreY) * signumRayY) >= 0
                        && ((zix - rayCentreZ) * signumRayZ) >= 0
                        && yix >= staticMinY - halfHeight
                        && yix <= staticMaxY + halfHeight
                        && zix >= staticMinZ - halfDepth
                        && zix <= staticMaxZ + halfDepth) {
                    isHit = true;
                    percentage = xFac;
                    collisionFace = 0;
                }
            }
        }

        // Intersect Z
        if (rayDirZ > 0) {
            double zFac = epsilon(bbOffMinZ / rayDirZ);
            if (zFac < percentage) {
                double xiz = rayDirX * zFac + rayCentreX;
                double yiz = rayDirY * zFac + rayCentreY;

                if (((yiz - rayCentreY) * signumRayY) >= 0
                        && ((xiz - rayCentreX) * signumRayX) >= 0
                        && xiz >= staticMinX - halfWidth
                        && xiz <= staticMaxX + halfWidth
                        && yiz >= staticMinY - halfHeight
                        && yiz <= staticMaxY + halfHeight) {
                    isHit = true;
                    percentage = zFac;
                    collisionFace = 1;
                }
            }
        }
        if (rayDirZ < 0) {
            double zFac = epsilon(bbOffMaxZ / rayDirZ);
            if (zFac < percentage) {
                double xiz = rayDirX * zFac + rayCentreX;
                double yiz = rayDirY * zFac + rayCentreY;

                if (((yiz - rayCentreY) * signumRayY) >= 0
                        && ((xiz - rayCentreX) * signumRayX) >= 0
                        && xiz >= staticMinX - halfWidth
                        && xiz <= staticMaxX + halfWidth
                        && yiz >= staticMinY - halfHeight
                        && yiz <= staticMaxY + halfHeight) {
                    isHit = true;
                    percentage = zFac;
                    collisionFace = 1;
                }
            }
        }

        // Intersect Y
        if (rayDirY > 0) {
            double yFac = epsilon(bbOffMinY / rayDirY);
            if (yFac < percentage) {
                double xiy = rayDirX * yFac + rayCentreX;
                double ziy = rayDirZ * yFac + rayCentreZ;

                if (((ziy - rayCentreZ) * signumRayZ) >= 0
                        && ((xiy - rayCentreX) * signumRayX) >= 0
                        && xiy >= staticMinX - halfWidth
                        && xiy <= staticMaxX + halfWidth
                        && ziy >= staticMinZ - halfDepth
                        && ziy <= staticMaxZ + halfDepth) {
                    isHit = true;
                    percentage = yFac;
                    collisionFace = 2;
                }
            }
        }

        if (rayDirY < 0) {
            double yFac = epsilon(bbOffMaxY / rayDirY);
            if (yFac < percentage) {
                double xiy = rayDirX * yFac + rayCentreX;
                double ziy = rayDirZ * yFac + rayCentreZ;

                if (((ziy - rayCentreZ) * signumRayZ) >= 0
                        && ((xiy - rayCentreX) * signumRayX) >= 0
                        && xiy >= staticMinX - halfWidth
                        && xiy <= staticMaxX + halfWidth
                        && ziy >= staticMinZ - halfDepth
                        && ziy <= staticMaxZ + halfDepth) {
                    isHit = true;
                    percentage = yFac;
                    collisionFace = 2;
                }
            }
        }

        percentage *= 0.99999;

        if (isHit && percentage >= 0 && percentage <= finalResult.res) {
            finalResult.res = percentage;
            finalResult.normalX = 0;
            finalResult.normalY = 0;
            finalResult.normalZ = 0;

            if (collisionFace == 0) finalResult.normalX = 1;
            if (collisionFace == 1) finalResult.normalZ = 1;
            if (collisionFace == 2) finalResult.normalY = 1;

            return true;
        }

        return false;
    }

    private static double epsilon(double value) {
        return Math.abs(value) < Vec.EPSILON ? 0 : value;
    }

    public static boolean BoundingBoxRayIntersectionCheck(Vec start, Vec direction, BoundingBox boundingBox, Pos position) {
        return BoundingBoxIntersectionCheck(BoundingBox.ZERO, start, direction, boundingBox, position, new SweepResult(Double.MAX_VALUE, 0, 0, 0, null, 0, 0, 0, 0, 0, 0));
    }
}
