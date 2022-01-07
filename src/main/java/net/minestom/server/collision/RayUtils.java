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
     * @param vec Ray vector
     * @param rayOffset Ray start point
     * @param instance entity instance
     * @param originChunk entity chunk
     * @param correctedEntityPos entity centre
     * @param boundingBox entity bounding box
     * @param entityPosition position of entity
     * @param tempResult place to store temporary result of collision
     * @param finalResult place to store final result of collision
     */
    public static void Raycast(Vec vec, Point rayOffset, Instance instance, Chunk originChunk, Pos correctedEntityPos, BoundingBox boundingBox, Pos entityPosition, SweepResult tempResult, SweepResult finalResult) {
        // This works by finding all the x, y and z grid line intersections and calculating the value of the point at that intersection
        // Finding all the intersections will give us all the full blocks that are traversed by the ray

        if (vec.x() != 0) {
            // Which direction we're stepping the block boundary in
            double xStep = vec.x() < 0 ? -1 : 1;

            // If we are going in the positive direction, the block that we stepped over is the one we want
            int xFix = vec.x() > 0 ? 1 : 0;

            // Total number of axis block boundaries that will be passed
            int xStepCount = (int)Math.ceil((vec.x())/xStep) + xFix;

            int xStepsCompleted = xFix;

            while (xStepsCompleted <= xStepCount) {
                // Get the axis value
                int xi = (int) (xStepsCompleted*xStep + rayOffset.blockX());
                double factor = (xi - rayOffset.x()) / vec.x();

                // Solve for y and z
                int yi = (int) Math.floor(vec.y() * factor + rayOffset.y());

                // If the y distance is much greater than the collision point that is currently being used, break
                if (Math.abs(vec.y()*finalResult.res) - Math.abs(entityPosition.y() - (yi)) < -2) break;

                int zi = (int) Math.floor(vec.z() * factor + rayOffset.z());
                if (Math.abs(vec.z()*finalResult.res) - Math.abs(entityPosition.z() - (zi)) < -2) break;

                xi -= xFix;
                xStepsCompleted++;

                // Check for collisions with the found block
                // If a collision was found, break
                if (CollisionUtils.checkBoundingBox(xi, yi, zi, instance, originChunk, vec, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult)) break;
            }
        }

        if (vec.z() != 0) {
            double zStep = vec.z() < 0 ? -1 : 1;
            int zFix = vec.z() > 0 ? 1 : 0;
            int zStepsCompleted = zFix;
            int zStepCount = (int)Math.ceil((vec.z())/zStep) + zFix;

            while (zStepsCompleted <= zStepCount) {
                int zi = (int) (zStepsCompleted*zStep + rayOffset.blockZ());
                double factor = (zi - rayOffset.z()) / vec.z();

                int xi = (int) Math.floor(vec.x() * factor + rayOffset.x());
                if (Math.abs(vec.x()*finalResult.res) - Math.abs(entityPosition.x() - (xi)) < -2) break;

                int yi = (int) Math.floor(vec.y() * factor + rayOffset.y());
                if (Math.abs(vec.y()*finalResult.res) - Math.abs(entityPosition.y() - (yi)) < -2) break;

                zi -= zFix;
                zStepsCompleted++;

                if (CollisionUtils.checkBoundingBox(xi, yi, zi, instance, originChunk, vec, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult)) break;
            }
        }

        if (vec.y() != 0) {
            int yFix = vec.y() > 0 ? 1 : 0;
            double yStep = vec.y() < 0 ? -1 : 1;
            int yStepsCompleted = yFix;
            int yStepCount = (int)Math.ceil((vec.y())/yStep) + yFix;

            while (yStepsCompleted <= yStepCount) {
                int yi = (int) (yStepsCompleted*yStep + rayOffset.blockY());
                double factor = (yi - rayOffset.y()) / vec.y();

                int xi = (int) Math.floor(vec.x() * factor + rayOffset.x());
                if (Math.abs(vec.x()*finalResult.res) - Math.abs(entityPosition.x() - (xi)) < -2) break;

                int zi = (int) Math.floor(vec.z() * factor + rayOffset.z());
                if (Math.abs(vec.z()*finalResult.res) - Math.abs(entityPosition.z() - (zi)) < -2) break;

                yi -= yFix;
                yStepsCompleted++;

                if (CollisionUtils.checkBoundingBox(xi, yi, zi, instance, originChunk, vec, correctedEntityPos, boundingBox, entityPosition, tempResult, finalResult)) break;
            }
        }
    }

    /**
     * Check if a bounding box intersects a ray
     * @param ray Ray to check
     * @param bb Bounding box
     * @param rayStart Ray start position
     * @param bbOffsetX Bounding box x
     * @param bbOffsetY Bounding box y
     * @param bbOffsetZ Bounding box z
     * @param bbOffW Bounding box added width
     * @param bbOffH Bounding box added height
     * @param bbOffD Bounding box added depth
     * @return true if an intersection between the ray and the bounding box was found
     */
    public static boolean RayBoundingBoxIntersectCheck(Vec ray, BoundingBox bb, Point rayStart, int bbOffsetX, int bbOffsetY, int bbOffsetZ, double bbOffW, double bbOffH, double bbOffD) {
        // Translate bounding box
        Vec bbOffMin = new Vec(bb.minX() - rayStart.x() + bbOffsetX - bbOffW / 2, bb.minY() - rayStart.y() + bbOffsetY - bbOffH / 2, bb.minZ() - rayStart.z() + bbOffsetZ - bbOffD / 2);
        Vec bbOffMax = new Vec(bb.maxX() - rayStart.x() + bbOffsetX + bbOffW / 2, bb.maxY() - rayStart.y() + bbOffsetY + bbOffH / 2, bb.maxZ() - rayStart.z() + bbOffsetZ + bbOffD / 2);

        // Intersect X
        if (ray.x() != 0) {
            // Left side of bounding box
            {
                double xFac = bbOffMin.x() / ray.x();
                double yix = ray.y() * xFac + rayStart.y();
                double zix = ray.z() * xFac + rayStart.z();

                if (yix > bb.minY() + bbOffsetY - bbOffH / 2
                        && yix < bb.maxY() + bbOffsetY + bbOffH / 2
                        && zix > bb.minZ() + bbOffsetZ - bbOffD / 2
                        && zix < bb.maxZ() + bbOffsetZ + bbOffD / 2) {
                    return true;
                }
            }
            // Right side of bounding box
            {
                double xFac = bbOffMax.x() / ray.x();
                double yix = ray.y() * xFac + rayStart.y();
                double zix = ray.z() * xFac + rayStart.z();

                if (yix > bb.minY() + bbOffsetY - bbOffH / 2
                        && yix < bb.maxY() + bbOffsetY + bbOffH / 2
                        && zix > bb.minZ() + bbOffsetZ - bbOffD / 2
                        && zix < bb.maxZ() + bbOffsetZ + bbOffD / 2) {
                    return true;
                }
            }
        }

        // Intersect Z
        if (ray.z() != 0) {
            {
                double zFac = bbOffMin.z() / ray.z();
                double xiz = ray.x() * zFac + rayStart.x();
                double yiz = ray.y() * zFac + rayStart.y();

                if (xiz > bb.minX() + bbOffsetX - bbOffW / 2
                        && xiz < bb.maxX() + bbOffsetX + bbOffW / 2
                        && yiz > bb.minY() + bbOffsetY - bbOffH / 2
                        && yiz < bb.maxY() + bbOffsetY + bbOffH / 2) {
                    return true;
                }
            }
            {
                double zFac = bbOffMax.z() / ray.z();
                double xiz = ray.x() * zFac + rayStart.x();
                double yiz = ray.y() * zFac + rayStart.y();

                if (xiz > bb.minX() + bbOffsetX - bbOffW / 2
                        && xiz < bb.maxX() + bbOffsetX + bbOffW / 2
                        && yiz > bb.minY() + bbOffsetY - bbOffH / 2
                        && yiz < bb.maxY() + bbOffsetY + bbOffH / 2) {
                    return true;
                }
            }
        }

        // Intersect Y
        if (ray.y() != 0) {
            {
                double yFac = bbOffMin.y() / ray.y();
                double xiy = ray.x() * yFac + rayStart.x();
                double ziy = ray.z() * yFac + rayStart.z();

                if (xiy > bb.minX() + bbOffsetX - bbOffW / 2
                        && xiy < bb.maxX() + bbOffsetX + bbOffW / 2
                        && ziy > bb.minZ() + bbOffsetZ - bbOffD / 2
                        && ziy < bb.maxZ() + bbOffsetZ + bbOffD / 2) {
                    return true;
                }
            }
            {
                double yFac = bbOffMax.y() / ray.y();
                double xiy = ray.x() * yFac + rayStart.x();
                double ziy = ray.z() * yFac + rayStart.z();

                if (xiy > bb.minX() + bbOffsetX - bbOffW / 2
                        && xiy < bb.maxX() + bbOffsetX + bbOffW / 2
                        && ziy > bb.minZ() + bbOffsetZ - bbOffD / 2
                        && ziy < bb.maxZ() + bbOffsetZ + bbOffD / 2) {
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

        writeTo.res = entryTime;
        writeTo.normalx = normalx;
        writeTo.normaly = normaly;
        writeTo.normalz = normalz;
    }

    public static class SweepResult {
        public double res;
        public double normalx, normaly, normalz;
        public Pos collisionBlock;
        public Block blockType;

        public SweepResult (double res, double normalx, double normaly, double normalz, Pos collisionBlock, Block blockType) {
            this.res = res;
            this.normalx = normalx;
            this.normaly = normaly;
            this.normalz = normalz;
        }
    }
}
