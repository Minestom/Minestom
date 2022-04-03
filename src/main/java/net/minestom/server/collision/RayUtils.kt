package net.minestom.server.collision

import net.minestom.server.coordinate.Vec
import net.minestom.server.collision.BoundingBox
import net.minestom.server.collision.SweepResult
import net.minestom.server.collision.BlockCollision
import net.minestom.server.collision.RayUtils
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Pos
import net.minestom.server.instance.block.Block

internal object RayUtils {
    @JvmStatic
    fun RaycastCollision(
        rayDirection: Vec,
        rayStart: Point,
        getter: Block.Getter?,
        boundingBox: BoundingBox?,
        entityCentre: Pos?,
        finalResult: SweepResult
    ) {
        // This works by finding all the x, y and z grid line intersections and calculating the value of the point at that intersection
        // Finding all the intersections will give us all the full blocks that are traversed by the ray
        if (rayDirection.x() != 0.0) {
            // Which direction we're stepping the block boundary in
            val xStep: Double = if (rayDirection.x() < 0) -1 else 1.toDouble()

            // If we are going in the positive direction, the block that we stepped over is the one we want
            val xFix = if (rayDirection.x() > 0) 1 else 0

            // Total number of axis block boundaries that will be passed
            val xStepCount = Math.ceil(rayDirection.x() / xStep).toInt() + xFix
            var xStepsCompleted = xFix
            while (xStepsCompleted <= xStepCount) {
                // Get the axis value
                var xi = (xStepsCompleted * xStep + rayStart.blockX()).toInt()
                val factor = (xi - rayStart.x()) / rayDirection.x()
                if (Math.abs(rayDirection.x() * finalResult.res) - Math.abs(rayStart.x() - xi) < -2) break

                // Solve for y and z
                val yi = Math.floor(rayDirection.y() * factor + rayStart.y()).toInt()

                // If the y distance is much greater than the collision point that is currently being used, break
                if (Math.abs(rayDirection.y() * finalResult.res) - Math.abs(rayStart.y() - yi) < -2) break
                val zi = Math.floor(rayDirection.z() * factor + rayStart.z()).toInt()
                if (Math.abs(rayDirection.z() * finalResult.res) - Math.abs(rayStart.z() - zi) < -2) break
                xi -= xFix
                xStepsCompleted++

                // Check for collisions with the found block
                // If a collision was found, break
                if (BlockCollision.checkBoundingBox(
                        xi,
                        yi,
                        zi,
                        rayDirection,
                        entityCentre,
                        boundingBox,
                        getter,
                        finalResult
                    )
                ) break
            }
        }
        if (rayDirection.z() != 0.0) {
            val zStep: Double = if (rayDirection.z() < 0) -1 else 1.toDouble()
            val zFix = if (rayDirection.z() > 0) 1 else 0
            var zStepsCompleted = zFix
            val zStepCount = Math.ceil(rayDirection.z() / zStep).toInt() + zFix
            while (zStepsCompleted <= zStepCount) {
                var zi = (zStepsCompleted * zStep + rayStart.blockZ()).toInt()
                val factor = (zi - rayStart.z()) / rayDirection.z()
                if (Math.abs(rayDirection.z() * finalResult.res) - Math.abs(rayStart.z() - zi) < -2) break
                val xi = Math.floor(rayDirection.x() * factor + rayStart.x()).toInt()
                if (Math.abs(rayDirection.x() * finalResult.res) - Math.abs(rayStart.x() - xi) < -2) break
                val yi = Math.floor(rayDirection.y() * factor + rayStart.y()).toInt()
                if (Math.abs(rayDirection.y() * finalResult.res) - Math.abs(rayStart.y() - yi) < -2) break
                zi -= zFix
                zStepsCompleted++
                if (BlockCollision.checkBoundingBox(
                        xi,
                        yi,
                        zi,
                        rayDirection,
                        entityCentre,
                        boundingBox,
                        getter,
                        finalResult
                    )
                ) break
            }
        }
        if (rayDirection.y() != 0.0) {
            val yFix = if (rayDirection.y() > 0) 1 else 0
            val yStep: Double = if (rayDirection.y() < 0) -1 else 1.toDouble()
            var yStepsCompleted = yFix
            val yStepCount = Math.ceil(rayDirection.y() / yStep).toInt() + yFix
            while (yStepsCompleted <= yStepCount) {
                var yi = (yStepsCompleted * yStep + rayStart.blockY()).toInt()
                val factor = (yi - rayStart.y()) / rayDirection.y()
                if (Math.abs(rayDirection.y() * finalResult.res) - Math.abs(rayStart.y() - yi) < -2) break
                val xi = Math.floor(rayDirection.x() * factor + rayStart.x()).toInt()
                if (Math.abs(rayDirection.x() * finalResult.res) - Math.abs(rayStart.x() - xi) < -2) break
                val zi = Math.floor(rayDirection.z() * factor + rayStart.z()).toInt()
                if (Math.abs(rayDirection.z() * finalResult.res) - Math.abs(rayStart.z() - zi) < -2) break
                yi -= yFix
                yStepsCompleted++
                if (BlockCollision.checkBoundingBox(
                        xi,
                        yi,
                        zi,
                        rayDirection,
                        entityCentre,
                        boundingBox,
                        getter,
                        finalResult
                    )
                ) break
            }
        }
    }

    /**
     * Check if a bounding box intersects a ray
     *
     * @param rayStart         Ray start position
     * @param rayDirection     Ray to check
     * @param collidableStatic Bounding box
     * @return true if an intersection between the ray and the bounding box was found
     */
    @JvmStatic
    fun BoundingBoxIntersectionCheck(
        moving: BoundingBox,
        rayStart: Point,
        rayDirection: Point,
        collidableStatic: BoundingBox,
        staticCollidableOffset: Point
    ): Boolean {
        val bbCentre: Point = Pos(
            moving.minX() + moving.width() / 2,
            moving.minY() + moving.height() / 2,
            moving.minZ() + moving.depth() / 2
        )
        val rayCentre = rayStart.add(bbCentre)

        // Translate bounding box
        val bbOffMin = Vec(
            collidableStatic.minX() - rayCentre.x() + staticCollidableOffset.x() - moving.width() / 2,
            collidableStatic.minY() - rayCentre.y() + staticCollidableOffset.y() - moving.height() / 2,
            collidableStatic.minZ() - rayCentre.z() + staticCollidableOffset.z() - moving.depth() / 2
        )
        val bbOffMax = Vec(
            collidableStatic.maxX() - rayCentre.x() + staticCollidableOffset.x() + moving.width() / 2,
            collidableStatic.maxY() - rayCentre.y() + staticCollidableOffset.y() + moving.height() / 2,
            collidableStatic.maxZ() - rayCentre.z() + staticCollidableOffset.z() + moving.depth() / 2
        )

        // This check is done in 2d. it can be visualised as a rectangle (the face we are checking), and a point.
        // If the point is within the rectangle, we know the vector intersects the face.
        val signumRayX = Math.signum(rayDirection.x())
        val signumRayY = Math.signum(rayDirection.y())
        val signumRayZ = Math.signum(rayDirection.z())

        // Intersect X
        if (rayDirection.x() != 0.0) {
            // Left side of bounding box
            run {
                val xFac = bbOffMin.x() / rayDirection.x()
                val yix = rayDirection.y() * xFac + rayCentre.y()
                val zix = rayDirection.z() * xFac + rayCentre.z()

                // Check if ray passes through y/z plane
                if (rayDirection.x() > 0 && (yix - rayCentre.y()) * signumRayY >= 0 && (zix - rayCentre.z()) * signumRayZ >= 0 && yix >= collidableStatic.minY() + staticCollidableOffset.y() - moving.height() / 2 && yix <= collidableStatic.maxY() + staticCollidableOffset.y() + moving.height() / 2 && zix >= collidableStatic.minZ() + staticCollidableOffset.z() - moving.depth() / 2 && zix <= collidableStatic.maxZ() + staticCollidableOffset.z() + moving.depth() / 2) {
                    return true
                }
            }
            // Right side of bounding box
            run {
                val xFac = bbOffMax.x() / rayDirection.x()
                val yix = rayDirection.y() * xFac + rayCentre.y()
                val zix = rayDirection.z() * xFac + rayCentre.z()
                if (rayDirection.x() < 0 && (yix - rayCentre.y()) * signumRayY >= 0 && (zix - rayCentre.z()) * signumRayZ >= 0 && yix >= collidableStatic.minY() + staticCollidableOffset.y() - moving.height() / 2 && yix <= collidableStatic.maxY() + staticCollidableOffset.y() + moving.height() / 2 && zix >= collidableStatic.minZ() + staticCollidableOffset.z() - moving.depth() / 2 && zix <= collidableStatic.maxZ() + staticCollidableOffset.z() + moving.depth() / 2) {
                    return true
                }
            }
        }

        // Intersect Z
        if (rayDirection.z() != 0.0) {
            run {
                val zFac = bbOffMin.z() / rayDirection.z()
                val xiz = rayDirection.x() * zFac + rayCentre.x()
                val yiz = rayDirection.y() * zFac + rayCentre.y()
                if (rayDirection.z() > 0 && (yiz - rayCentre.y()) * signumRayY >= 0 && (xiz - rayCentre.x()) * signumRayX >= 0 && xiz >= collidableStatic.minX() + staticCollidableOffset.x() - moving.width() / 2 && xiz <= collidableStatic.maxX() + staticCollidableOffset.x() + moving.width() / 2 && yiz >= collidableStatic.minY() + staticCollidableOffset.y() - moving.height() / 2 && yiz <= collidableStatic.maxY() + staticCollidableOffset.y() + moving.height() / 2) {
                    return true
                }
            }
            run {
                val zFac = bbOffMax.z() / rayDirection.z()
                val xiz = rayDirection.x() * zFac + rayCentre.x()
                val yiz = rayDirection.y() * zFac + rayCentre.y()
                if (rayDirection.z() < 0 && (yiz - rayCentre.y()) * signumRayY >= 0 && (xiz - rayCentre.x()) * signumRayX >= 0 && xiz >= collidableStatic.minX() + staticCollidableOffset.x() - moving.width() / 2 && xiz <= collidableStatic.maxX() + staticCollidableOffset.x() + moving.width() / 2 && yiz >= collidableStatic.minY() + staticCollidableOffset.y() - moving.height() / 2 && yiz <= collidableStatic.maxY() + staticCollidableOffset.y() + moving.height() / 2) {
                    return true
                }
            }
        }

        // Intersect Y
        if (rayDirection.y() != 0.0) {
            run {
                val yFac = bbOffMin.y() / rayDirection.y()
                val xiy = rayDirection.x() * yFac + rayCentre.x()
                val ziy = rayDirection.z() * yFac + rayCentre.z()
                if (rayDirection.y() > 0 && (ziy - rayCentre.z()) * signumRayZ >= 0 && (xiy - rayCentre.x()) * signumRayX >= 0 && xiy >= collidableStatic.minX() + staticCollidableOffset.x() - moving.width() / 2 && xiy <= collidableStatic.maxX() + staticCollidableOffset.x() + moving.width() / 2 && ziy >= collidableStatic.minZ() + staticCollidableOffset.z() - moving.depth() / 2 && ziy <= collidableStatic.maxZ() + staticCollidableOffset.z() + moving.depth() / 2) {
                    return true
                }
            }
            run {
                val yFac = bbOffMax.y() / rayDirection.y()
                val xiy = rayDirection.x() * yFac + rayCentre.x()
                val ziy = rayDirection.z() * yFac + rayCentre.z()
                if (rayDirection.y() < 0 && (ziy - rayCentre.z()) * signumRayZ >= 0 && (xiy - rayCentre.x()) * signumRayX >= 0 && xiy >= collidableStatic.minX() + staticCollidableOffset.x() - moving.width() / 2 && xiy <= collidableStatic.maxX() + staticCollidableOffset.x() + moving.width() / 2 && ziy >= collidableStatic.minZ() + staticCollidableOffset.z() - moving.depth() / 2 && ziy <= collidableStatic.maxZ() + staticCollidableOffset.z() + moving.depth() / 2) {
                    return true
                }
            }
        }
        return false
    }

    // Extended from 2d implementation found here https://www.gamedev.net/tutorials/programming/general-and-gameplay-programming/swept-aabb-collision-detection-and-response-r3084/
    @JvmStatic
    fun SweptAABB(
        collidableMoving: BoundingBox,
        rayStart: Point,
        rayDirection: Point,
        collidableStatic: BoundingBox,
        staticCollidableOffset: Point,
        finalResult: SweepResult
    ): Boolean {
        val normalx: Double
        val normaly: Double
        val normalz: Double
        val xInvEntry: Double
        val yInvEntry: Double
        val zInvEntry: Double
        val xInvExit: Double
        val yInvExit: Double
        val zInvExit: Double

        // find the distance between the objects on the near and far sides for x, y, z
        if (rayDirection.x() > 0.0f) {
            xInvEntry = staticCollidableOffset.x() + collidableStatic.minX() - (rayStart.x() + collidableMoving.maxX())
            xInvExit = staticCollidableOffset.x() + collidableStatic.maxX() - (rayStart.x() + collidableMoving.minX())
        } else {
            xInvEntry = staticCollidableOffset.x() + collidableStatic.maxX() - (rayStart.x() + collidableMoving.minX())
            xInvExit = staticCollidableOffset.x() + collidableStatic.minX() - (rayStart.x() + collidableMoving.maxX())
        }
        if (rayDirection.y() > 0.0f) {
            yInvEntry = staticCollidableOffset.y() + collidableStatic.minY() - (rayStart.y() + collidableMoving.maxY())
            yInvExit = staticCollidableOffset.y() + collidableStatic.maxY() - (rayStart.y() + collidableMoving.minY())
        } else {
            yInvEntry = staticCollidableOffset.y() + collidableStatic.maxY() - (rayStart.y() + collidableMoving.minY())
            yInvExit = staticCollidableOffset.y() + collidableStatic.minY() - (rayStart.y() + collidableMoving.maxY())
        }
        if (rayDirection.z() > 0.0f) {
            zInvEntry = staticCollidableOffset.z() + collidableStatic.minZ() - (rayStart.z() + collidableMoving.maxZ())
            zInvExit = staticCollidableOffset.z() + collidableStatic.maxZ() - (rayStart.z() + collidableMoving.minZ())
        } else {
            zInvEntry = staticCollidableOffset.z() + collidableStatic.maxZ() - (rayStart.z() + collidableMoving.minZ())
            zInvExit = staticCollidableOffset.z() + collidableStatic.minZ() - (rayStart.z() + collidableMoving.maxZ())
        }

        // find time of collision and time of leaving for each axis (if statement is to prevent divide by zero)
        val xEntry: Double
        val yEntry: Double
        val zEntry: Double
        val xExit: Double
        val yExit: Double
        val zExit: Double
        if (rayDirection.x() == 0.0) {
            xEntry = -Double.MAX_VALUE
            xExit = Double.MAX_VALUE
        } else {
            xEntry = xInvEntry / rayDirection.x()
            xExit = xInvExit / rayDirection.x()
        }
        if (rayDirection.y() == 0.0) {
            yEntry = -Double.MAX_VALUE
            yExit = Double.MAX_VALUE
        } else {
            yEntry = yInvEntry / rayDirection.y()
            yExit = yInvExit / rayDirection.y()
        }
        if (rayDirection.z() == 0.0) {
            zEntry = -Double.MAX_VALUE
            zExit = Double.MAX_VALUE
        } else {
            zEntry = zInvEntry / rayDirection.z()
            zExit = zInvExit / rayDirection.z()
        }

        // find the earliest/latest times of collision
        val entryTime = Math.max(Math.max(xEntry, yEntry), zEntry)
        val exitTime = Math.min(Math.max(xExit, yExit), zExit)
        val moveAmount = entryTime * 0.99999
        if (entryTime > exitTime || xEntry > 1.0f || yEntry > 1.0f || zEntry > 1.0f || xEntry < 0.0f && yEntry < 0.0f && zEntry < 0.0f
            || moveAmount > finalResult.res
        ) {
            return false
        }

        // calculate normal of collided surface
        if (xEntry > yEntry && xEntry > zEntry) {
            if (xInvEntry < 0.0f) {
                normalx = 1.0
                normaly = 0.0
                normalz = 0.0
            } else {
                normalx = -1.0
                normaly = 0.0
                normalz = 0.0
            }
        } else if (yEntry > zEntry) {
            if (yInvEntry < 0.0f) {
                normalx = 0.0
                normaly = 1.0
                normalz = 0.0
            } else {
                normalx = 0.0
                normaly = -1.0
                normalz = 0.0
            }
        } else {
            if (zInvEntry < 0.0f) {
                normalx = 0.0
                normaly = 0.0
                normalz = 1.0
            } else {
                normalx = 0.0
                normaly = 0.0
                normalz = -1.0
            }
        }
        finalResult.res = moveAmount
        finalResult.normalX = normalx
        finalResult.normalY = normaly
        finalResult.normalZ = normalz
        return true
    }

    @JvmStatic
    fun BoundingBoxRayIntersectionCheck(start: Vec, direction: Vec, boundingBox: BoundingBox, position: Pos): Boolean {
        return BoundingBoxIntersectionCheck(BoundingBox.ZERO, start, direction, boundingBox, position)
    }
}