package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class RayUtils {
    /**
     *
     * @param rayDirection Ray vector
     * @param rayStart Ray start point
     * @param instance entity instance
     * @param originChunk entity chunk
     * @param entityBoundingBox entity bounding box
     * @param entityCentre position of entity
     * @param tempResult place to store temporary result of collision
     * @param finalResult place to store final result of collision
     */
    public static void RaycastCollision(Vec rayDirection, Point rayStart, Instance instance, Chunk originChunk, EntityBoundingBox entityBoundingBox, Pos entityCentre, SweepResult tempResult, SweepResult finalResult) {
        // This works by finding all the x, y and z grid line intersections and calculating the value of the point at that intersection
        // Finding all the intersections will give us all the full blocks that are traversed by the ray

        if (rayDirection.x() != 0) {
            // Which direction we're stepping the block boundary in
            double xStep = rayDirection.x() < 0 ? -1 : 1;

            // If we are going in the positive direction, the block that we stepped over is the one we want
            int xFix = rayDirection.x() > 0 ? 1 : 0;

            // Total number of axis block boundaries that will be passed
            int xStepCount = (int)Math.ceil((rayDirection.x())/xStep) + xFix;

            int xStepsCompleted = xFix;

            while (xStepsCompleted <= xStepCount) {
                // Get the axis value
                int xi = (int) (xStepsCompleted*xStep + rayStart.blockX());
                double factor = (xi - rayStart.x()) / rayDirection.x();

                // Solve for y and z
                int yi = (int) Math.floor(rayDirection.y() * factor + rayStart.y());

                // If the y distance is much greater than the collision point that is currently being used, break
                if (Math.abs(rayDirection.y()*finalResult.res) - Math.abs(entityCentre.y() - (yi)) < -2) break;

                int zi = (int) Math.floor(rayDirection.z() * factor + rayStart.z());
                if (Math.abs(rayDirection.z()*finalResult.res) - Math.abs(entityCentre.z() - (zi)) < -2) break;

                xi -= xFix;
                xStepsCompleted++;

                // Check for collisions with the found block
                // If a collision was found, break
                if (CollisionUtils.checkBoundingBox(xi, yi, zi, rayDirection, entityCentre, entityBoundingBox, instance, originChunk, tempResult, finalResult)) break;
            }
        }

        if (rayDirection.z() != 0) {
            double zStep = rayDirection.z() < 0 ? -1 : 1;
            int zFix = rayDirection.z() > 0 ? 1 : 0;
            int zStepsCompleted = zFix;
            int zStepCount = (int)Math.ceil((rayDirection.z())/zStep) + zFix;

            while (zStepsCompleted <= zStepCount) {
                int zi = (int) (zStepsCompleted*zStep + rayStart.blockZ());
                double factor = (zi - rayStart.z()) / rayDirection.z();

                int xi = (int) Math.floor(rayDirection.x() * factor + rayStart.x());
                if (Math.abs(rayDirection.x()*finalResult.res) - Math.abs(entityCentre.x() - (xi)) < -2) break;

                int yi = (int) Math.floor(rayDirection.y() * factor + rayStart.y());
                if (Math.abs(rayDirection.y()*finalResult.res) - Math.abs(entityCentre.y() - (yi)) < -2) break;

                zi -= zFix;
                zStepsCompleted++;

                if (CollisionUtils.checkBoundingBox(xi, yi, zi, rayDirection, entityCentre, entityBoundingBox, instance, originChunk, tempResult, finalResult)) break;
            }
        }

        if (rayDirection.y() != 0) {
            int yFix = rayDirection.y() > 0 ? 1 : 0;
            double yStep = rayDirection.y() < 0 ? -1 : 1;
            int yStepsCompleted = yFix;
            int yStepCount = (int)Math.ceil((rayDirection.y())/yStep) + yFix;

            while (yStepsCompleted <= yStepCount) {
                int yi = (int) (yStepsCompleted*yStep + rayStart.blockY());
                double factor = (yi - rayStart.y()) / rayDirection.y();

                int xi = (int) Math.floor(rayDirection.x() * factor + rayStart.x());
                if (Math.abs(rayDirection.x()*finalResult.res) - Math.abs(entityCentre.x() - (xi)) < -2) break;

                int zi = (int) Math.floor(rayDirection.z() * factor + rayStart.z());
                if (Math.abs(rayDirection.z()*finalResult.res) - Math.abs(entityCentre.z() - (zi)) < -2) break;

                yi -= yFix;
                yStepsCompleted++;

                if (CollisionUtils.checkBoundingBox(xi, yi, zi, rayDirection, entityCentre, entityBoundingBox, instance, originChunk, tempResult, finalResult)) break;
            }
        }
    }

    /**
     * Check if a bounding box intersects a ray
     * @param rayStart Ray start position
     * @param rayDirection Ray to check
     * @param collidableStatic Bounding box
     * @return true if an intersection between the ray and the bounding box was found
     */
    public static boolean RayBoundingBoxIntersectCheck(Collidable collidableMoving, Point rayStart, Point rayDirection, Collidable collidableStatic, Point staticCollidableOffset) {
        // Translate bounding box
        Vec bbOffMin = new Vec(collidableStatic.minX() - rayStart.x() + staticCollidableOffset.x() - collidableMoving.width() / 2, collidableStatic.minY() - rayStart.y() + staticCollidableOffset.y() - collidableMoving.height() / 2, collidableStatic.minZ() - rayStart.z() + staticCollidableOffset.z() - collidableMoving.depth() / 2);
        Vec bbOffMax = new Vec(collidableStatic.maxX() - rayStart.x() + staticCollidableOffset.x() + collidableMoving.width() / 2, collidableStatic.maxY() - rayStart.y() + staticCollidableOffset.y() + collidableMoving.height() / 2, collidableStatic.maxZ() - rayStart.z() + staticCollidableOffset.z() + collidableMoving.depth() / 2);

        // This check is done in 2d. it can be visualised as a rectangle (the face we are checking), and a point.
        // If the point is within the rectangle, we know the vector intersects the face.

        // Intersect X
        if (rayDirection.x() != 0) {
            // Left side of bounding box
            {
                double xFac = bbOffMin.x() / rayDirection.x();
                double yix = rayDirection.y() * xFac + rayStart.y();
                double zix = rayDirection.z() * xFac + rayStart.z();

                // Check if ray passes through y/z plane
                if (yix >= collidableStatic.minY() + staticCollidableOffset.y() - collidableMoving.height() / 2
                        && yix <= collidableStatic.maxY() + staticCollidableOffset.y() + collidableMoving.height() / 2
                        && zix >= collidableStatic.minZ() + staticCollidableOffset.z() - collidableMoving.depth() / 2
                        && zix <= collidableStatic.maxZ() + staticCollidableOffset.z() + collidableMoving.depth() / 2) {
                    return true;
                }
            }
            // Right side of bounding box
            {
                double xFac = bbOffMax.x() / rayDirection.x();
                double yix = rayDirection.y() * xFac + rayStart.y();
                double zix = rayDirection.z() * xFac + rayStart.z();

                if (yix >= collidableStatic.minY() + staticCollidableOffset.y() - collidableMoving.height() / 2
                        && yix <= collidableStatic.maxY() + staticCollidableOffset.y() + collidableMoving.height() / 2
                        && zix >= collidableStatic.minZ() + staticCollidableOffset.z() - collidableMoving.depth() / 2
                        && zix <= collidableStatic.maxZ() + staticCollidableOffset.z() + collidableMoving.depth() / 2) {
                    return true;
                }
            }
        }

        // Intersect Z
        if (rayDirection.z() != 0) {
            {
                double zFac = bbOffMin.z() / rayDirection.z();
                double xiz = rayDirection.x() * zFac + rayStart.x();
                double yiz = rayDirection.y() * zFac + rayStart.y();

                if (xiz >= collidableStatic.minX() + staticCollidableOffset.x() - collidableMoving.width() / 2
                        && xiz <= collidableStatic.maxX() + staticCollidableOffset.x() + collidableMoving.width() / 2
                        && yiz >= collidableStatic.minY() + staticCollidableOffset.y() - collidableMoving.height() / 2
                        && yiz <= collidableStatic.maxY() + staticCollidableOffset.y() + collidableMoving.height() / 2) {
                    return true;
                }
            }
            {
                double zFac = bbOffMax.z() / rayDirection.z();
                double xiz = rayDirection.x() * zFac + rayStart.x();
                double yiz = rayDirection.y() * zFac + rayStart.y();

                if (xiz >= collidableStatic.minX() + staticCollidableOffset.x() - collidableMoving.width() / 2
                        && xiz <= collidableStatic.maxX() + staticCollidableOffset.x() + collidableMoving.width() / 2
                        && yiz >= collidableStatic.minY() + staticCollidableOffset.y() - collidableMoving.height() / 2
                        && yiz <= collidableStatic.maxY() + staticCollidableOffset.y() + collidableMoving.height() / 2) {
                    return true;
                }
            }
        }

        // Intersect Y
        if (rayDirection.y() != 0) {
            {
                double yFac = bbOffMin.y() / rayDirection.y();
                double xiy = rayDirection.x() * yFac + rayStart.x();
                double ziy = rayDirection.z() * yFac + rayStart.z();

                if (xiy >= collidableStatic.minX() + staticCollidableOffset.x() - collidableMoving.width() / 2
                        && xiy <= collidableStatic.maxX() + staticCollidableOffset.x() + collidableMoving.width() / 2
                        && ziy >= collidableStatic.minZ() + staticCollidableOffset.z() - collidableMoving.depth() / 2
                        && ziy <= collidableStatic.maxZ() + staticCollidableOffset.z() + collidableMoving.depth() / 2) {
                    return true;
                }
            }
            {
                double yFac = bbOffMax.y() / rayDirection.y();
                double xiy = rayDirection.x() * yFac + rayStart.x();
                double ziy = rayDirection.z() * yFac + rayStart.z();

                if (xiy >= collidableStatic.minX() + staticCollidableOffset.x() - collidableMoving.width() / 2
                        && xiy <= collidableStatic.maxX() + staticCollidableOffset.x() + collidableMoving.width() / 2
                        && ziy >= collidableStatic.minZ() + staticCollidableOffset.z() - collidableMoving.depth() / 2
                        && ziy <= collidableStatic.maxZ() + staticCollidableOffset.z() + collidableMoving.depth() / 2) {
                    return true;
                }
            }
        }

        return false;
    }

    // Point rayDirection, Collidable bbStatic, Point rayStart, Point bbStaticOffset, Collidable bbMoving

    // Extended from 2d implementation found here https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-collision-detection-and-response-r3084/
    public static void SweptAABB(Collidable collidableMoving, Point rayStart, Point rayDirection, Collidable collidableStatic, Point staticCollidableOffset, SweepResult writeTo) {
        double normalx, normaly, normalz;

        double xInvEntry, yInvEntry, zInvEntry;
        double xInvExit, yInvExit, zInvExit;

        // find the distance between the objects on the near and far sides for x, y, z
        if (rayDirection.x() > 0.0f) {
            xInvEntry = (staticCollidableOffset.x() + collidableStatic.minX()) - (rayStart.x() + collidableMoving.maxX());
            xInvExit = (staticCollidableOffset.x() + collidableStatic.maxX()) - (rayStart.x() + collidableMoving.minX());
        } else {
            xInvEntry = (staticCollidableOffset.x() + collidableStatic.maxX()) - (rayStart.x() + collidableMoving.minX());
            xInvExit = (staticCollidableOffset.x() + collidableStatic.minX()) - (rayStart.x() + collidableMoving.maxX());
        }

        if (rayDirection.y() > 0.0f) {
            yInvEntry = (staticCollidableOffset.y() + collidableStatic.minY()) - (rayStart.y() + collidableMoving.maxY());
            yInvExit = (staticCollidableOffset.y() + collidableStatic.maxY()) - (rayStart.y() + collidableMoving.minY());
        } else {
            yInvEntry = (staticCollidableOffset.y() + collidableStatic.maxY()) - (rayStart.y() + collidableMoving.minY());
            yInvExit = (staticCollidableOffset.y() + collidableStatic.minY()) - (rayStart.y() + collidableMoving.maxY());
        }

        if (rayDirection.z() > 0.0f) {
            zInvEntry = (staticCollidableOffset.z() + collidableStatic.minZ()) - (rayStart.z() + collidableMoving.maxZ());
            zInvExit = (staticCollidableOffset.z() + collidableStatic.maxZ()) - (rayStart.z() + collidableMoving.minZ());
        } else {
            zInvEntry = (staticCollidableOffset.z() + collidableStatic.maxZ()) - (rayStart.z() + collidableMoving.minZ());
            zInvExit = (staticCollidableOffset.z() + collidableStatic.minZ()) - (rayStart.z() + collidableMoving.maxZ());
        }

        // find time of collision and time of leaving for each axis (if statement is to prevent divide by zero)
        double xEntry, yEntry, zEntry;
        double xExit, yExit, zExit;

        if (rayDirection.x() == 0.0f) {
            xEntry = -Double.MAX_VALUE;
            xExit = Double.MAX_VALUE;
        } else {
            xEntry = xInvEntry / rayDirection.x();
            xExit = xInvExit / rayDirection.x();
        }

        if (rayDirection.y() == 0.0f) {
            yEntry = -Double.MAX_VALUE;
            yExit = Double.MAX_VALUE;
        } else {
            yEntry = yInvEntry / rayDirection.y();
            yExit = yInvExit / rayDirection.y();
        }

        if (rayDirection.z() == 0.0f) {
            zEntry = -Double.MAX_VALUE;
            zExit = Double.MAX_VALUE;
        } else {
            zEntry = zInvEntry / rayDirection.z();
            zExit = zInvExit / rayDirection.z();
        }

        // find the earliest/latest times of collision
        double entryTime = Math.max(Math.max(xEntry, yEntry), zEntry);
        double exitTime = Math.min(Math.max(xExit, yExit), zExit);

        if (entryTime > exitTime || xEntry > 1.0f || yEntry > 1.0f || zEntry > 1.0f || (xEntry < 0.0f && yEntry < 0.0f && zEntry < 0.0f)) {
            writeTo.res = 1;
            writeTo.normalx = 0;
            writeTo.normaly = 0;
            writeTo.normalz = 0;
            return;
        }

        // calculate normal of collided surface
        if (xEntry > yEntry && xEntry > zEntry) {
            if (xInvEntry < 0.0f) {
                normalx = 1.0f;
                normaly = 0.0f;
                normalz = 0.0f;
            } else {
                normalx = -1.0f;
                normaly = 0.0f;
                normalz = 0.0f;
            }
        }
        else if (yEntry > zEntry) {
            if (yInvEntry < 0.0f) {
                normalx = 0.0f;
                normaly = 1.0f;
                normalz = 0.0f;
            } else {
                normalx = 0.0f;
                normaly = -1.0f;
                normalz = 0.0f;
            }
        }
        else {
            if (zInvEntry < 0.0f) {
                normalx = 0.0f;
                normaly = 0.0f;
                normalz = 1.0f;
            } else {
                normalx = 0.0f;
                normaly = 0.0f;
                normalz = -1.0f;
            }
        }

        writeTo.res = entryTime * 0.99999;
        writeTo.normalx = normalx;
        writeTo.normaly = normaly;
        writeTo.normalz = normalz;
    }

    public static class SweepResult {
        public double res;
        public double normalx, normaly, normalz;
        public Pos collisionBlock;
        public Block blockType;

        public SweepResult(double res, double normalx, double normaly, double normalz) {
            this.res = res;
            this.normalx = normalx;
            this.normaly = normaly;
            this.normalz = normalz;
        }
    }
}
