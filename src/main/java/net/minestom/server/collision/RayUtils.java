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
     * @param boundingBox entity bounding box
     * @param entityCentre position of entity
     * @param tempResult place to store temporary result of collision
     * @param finalResult place to store final result of collision
     */
    public static void Raycast(Vec rayDirection, Point rayStart, Instance instance, Chunk originChunk, BoundingBox boundingBox, Pos entityCentre, SweepResult tempResult, SweepResult finalResult) {
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
                if (CollisionUtils.checkBoundingBox(xi, yi, zi, rayDirection, entityCentre, boundingBox, instance, originChunk, tempResult, finalResult)) break;
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

                if (CollisionUtils.checkBoundingBox(xi, yi, zi, rayDirection, entityCentre, boundingBox, instance, originChunk, tempResult, finalResult)) break;
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

                if (CollisionUtils.checkBoundingBox(xi, yi, zi, rayDirection, entityCentre, boundingBox, instance, originChunk, tempResult, finalResult)) break;
            }
        }
    }

    /**
     * Check if a bounding box intersects a ray
     * @param rayDirection Ray to check
     * @param bbStatic Bounding box
     * @param rayStart Ray start position
     * @return true if an intersection between the ray and the bounding box was found
     */
    public static boolean RayBoundingBoxIntersectCheck(Point rayDirection, BoundingBox bbStatic, Point rayStart, Point bbStaticOffset, BoundingBox bbMoving) {
        // Translate bounding box
        Vec bbOffMin = new Vec(bbStatic.minX() - rayStart.x() + bbStaticOffset.x() - bbMoving.width() / 2, bbStatic.minY() - rayStart.y() + bbStaticOffset.y() - bbMoving.height() / 2, bbStatic.minZ() - rayStart.z() + bbStaticOffset.z() - bbMoving.depth() / 2);
        Vec bbOffMax = new Vec(bbStatic.maxX() - rayStart.x() + bbStaticOffset.x() + bbMoving.width() / 2, bbStatic.maxY() - rayStart.y() + bbStaticOffset.y() + bbMoving.height() / 2, bbStatic.maxZ() - rayStart.z() + bbStaticOffset.z() + bbMoving.depth() / 2);

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
                if (yix >= bbStatic.minY() + bbStaticOffset.y() - bbMoving.height() / 2
                        && yix <= bbStatic.maxY() + bbStaticOffset.y() + bbMoving.height() / 2
                        && zix >= bbStatic.minZ() + bbStaticOffset.z() - bbMoving.depth() / 2
                        && zix <= bbStatic.maxZ() + bbStaticOffset.z() + bbMoving.depth() / 2) {
                    return true;
                }
            }
            // Right side of bounding box
            {
                double xFac = bbOffMax.x() / rayDirection.x();
                double yix = rayDirection.y() * xFac + rayStart.y();
                double zix = rayDirection.z() * xFac + rayStart.z();

                if (yix >= bbStatic.minY() + bbStaticOffset.y() - bbMoving.height() / 2
                        && yix <= bbStatic.maxY() + bbStaticOffset.y() + bbMoving.height() / 2
                        && zix >= bbStatic.minZ() + bbStaticOffset.z() - bbMoving.depth() / 2
                        && zix <= bbStatic.maxZ() + bbStaticOffset.z() + bbMoving.depth() / 2) {
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

                if (xiz >= bbStatic.minX() + bbStaticOffset.x() - bbMoving.width() / 2
                        && xiz <= bbStatic.maxX() + bbStaticOffset.x() + bbMoving.width() / 2
                        && yiz >= bbStatic.minY() + bbStaticOffset.y() - bbMoving.height() / 2
                        && yiz <= bbStatic.maxY() + bbStaticOffset.y() + bbMoving.height() / 2) {
                    return true;
                }
            }
            {
                double zFac = bbOffMax.z() / rayDirection.z();
                double xiz = rayDirection.x() * zFac + rayStart.x();
                double yiz = rayDirection.y() * zFac + rayStart.y();

                if (xiz >= bbStatic.minX() + bbStaticOffset.x() - bbMoving.width() / 2
                        && xiz <= bbStatic.maxX() + bbStaticOffset.x() + bbMoving.width() / 2
                        && yiz >= bbStatic.minY() + bbStaticOffset.y() - bbMoving.height() / 2
                        && yiz <= bbStatic.maxY() + bbStaticOffset.y() + bbMoving.height() / 2) {
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

                if (xiy >= bbStatic.minX() + bbStaticOffset.x() - bbMoving.width() / 2
                        && xiy <= bbStatic.maxX() + bbStaticOffset.x() + bbMoving.width() / 2
                        && ziy >= bbStatic.minZ() + bbStaticOffset.z() - bbMoving.depth() / 2
                        && ziy <= bbStatic.maxZ() + bbStaticOffset.z() + bbMoving.depth() / 2) {
                    return true;
                }
            }
            {
                double yFac = bbOffMax.y() / rayDirection.y();
                double xiy = rayDirection.x() * yFac + rayStart.x();
                double ziy = rayDirection.z() * yFac + rayStart.z();

                if (xiy >= bbStatic.minX() + bbStaticOffset.x() - bbMoving.width() / 2
                        && xiy <= bbStatic.maxX() + bbStaticOffset.x() + bbMoving.width() / 2
                        && ziy >= bbStatic.minZ() + bbStaticOffset.z() - bbMoving.depth() / 2
                        && ziy <= bbStatic.maxZ() + bbStaticOffset.z() + bbMoving.depth() / 2) {
                    return true;
                }
            }
        }

        return false;
    }

    // Extended from 2d implementation found here https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-collision-detection-and-response-r3084/
    public static void SweptAABB(BoundingBox b1, BoundingBox b2, Point b1Pos, int b2PosX, int b2PosY, int b2PosZ, double vx, double vy, double vz, SweepResult writeTo) {
        double normalx, normaly, normalz;

        double xInvEntry, yInvEntry, zInvEntry;
        double xInvExit, yInvExit, zInvExit;

        // find the distance between the objects on the near and far sides for x, y, z
        if (vx > 0.0f) {
            xInvEntry = (b2PosX + b2.minX()) - (b1Pos.x() + b1.maxX());
            xInvExit = (b2PosX + b2.maxX()) - (b1Pos.x() + b1.minX());
        } else {
            xInvEntry = (b2PosX + b2.maxX()) - (b1Pos.x() + b1.minX());
            xInvExit = (b2PosX + b2.minX()) - (b1Pos.x() + b1.maxX());
        }

        if (vy > 0.0f) {
            yInvEntry = (b2PosY + b2.minY()) - (b1Pos.y() + b1.maxY());
            yInvExit = (b2PosY + b2.maxY()) - (b1Pos.y() + b1.minY());
        } else {
            yInvEntry = (b2PosY + b2.maxY()) - (b1Pos.y() + b1.minY());
            yInvExit = (b2PosY + b2.minY()) - (b1Pos.y() + b1.maxY());
        }

        if (vz > 0.0f) {
            zInvEntry = (b2PosZ + b2.minZ()) - (b1Pos.z() + b1.maxZ());
            zInvExit = (b2PosZ + b2.maxZ()) - (b1Pos.z() + b1.minZ());
        } else {
            zInvEntry = (b2PosZ + b2.maxZ()) - (b1Pos.z() + b1.minZ());
            zInvExit = (b2PosZ + b2.minZ()) - (b1Pos.z() + b1.maxZ());
        }

        // find time of collision and time of leaving for each axis (if statement is to prevent divide by zero)
        double xEntry, yEntry, zEntry;
        double xExit, yExit, zExit;

        if (vx == 0.0f) {
            xEntry = -Double.MAX_VALUE;
            xExit = Double.MAX_VALUE;
        } else {
            xEntry = xInvEntry / vx;
            xExit = xInvExit / vx;
        }

        if (vy == 0.0f) {
            yEntry = -Double.MAX_VALUE;
            yExit = Double.MAX_VALUE;
        } else {
            yEntry = yInvEntry / vy;
            yExit = yInvExit / vy;
        }

        if (vz == 0.0f) {
            zEntry = -Double.MAX_VALUE;
            zExit = Double.MAX_VALUE;
        } else {
            zEntry = zInvEntry / vz;
            zExit = zInvExit / vz;
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
