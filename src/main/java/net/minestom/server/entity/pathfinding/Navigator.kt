package net.minestom.server.entity.pathfinding

import net.minestom.server.utils.position.PositionUtils.getLookYaw
import net.minestom.server.utils.position.PositionUtils.getLookPitch
import net.minestom.server.utils.chunk.ChunkUtils.isLoaded
import com.extollit.gaming.ai.path.model.IBlockDescription
import com.extollit.gaming.ai.path.model.IBlockObject
import space.vectrix.flare.fastutil.Short2ObjectSyncMap
import net.minestom.server.entity.pathfinding.PFBlock
import net.minestom.server.entity.pathfinding.PFPathingEntity
import com.extollit.gaming.ai.path.HydrazinePathFinder
import net.minestom.server.collision.PhysicsResult
import net.minestom.server.collision.CollisionUtils
import net.minestom.server.coordinate.Vec
import net.minestom.server.instance.WorldBorder
import net.minestom.server.utils.chunk.ChunkUtils
import com.extollit.gaming.ai.path.PathOptions
import com.extollit.gaming.ai.path.model.IPath
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.pathfinding.PFInstanceSpace
import com.extollit.gaming.ai.path.model.IColumnarSpace
import com.extollit.gaming.ai.path.model.ColumnarOcclusionFieldList
import com.extollit.gaming.ai.path.model.IInstanceSpace
import net.minestom.server.entity.pathfinding.PFColumnarSpace
import java.util.concurrent.ConcurrentHashMap
import com.extollit.gaming.ai.path.model.IPathingEntity
import com.extollit.gaming.ai.path.model.Passibility
import com.extollit.gaming.ai.path.model.Gravitation
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import org.jetbrains.annotations.ApiStatus

// TODO all pathfinding requests could be processed in another thread
/**
 * Necessary object for all [NavigableEntity].
 */
class Navigator(val entity: Entity) {
    @get:ApiStatus.Internal
    val pathingEntity: PFPathingEntity
    private var pathFinder: HydrazinePathFinder? = null

    /**
     * Gets the target pathfinder position.
     *
     * @return the target pathfinder position, null if there is no one
     */
    var pathPosition: Point? = null
        private set

    init {
        pathingEntity = PFPathingEntity(this)
    }

    /**
     * Used to move the entity toward `direction` in the X and Z axis
     * Gravity is still applied but the entity will not attempt to jump
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param direction the targeted position
     * @param speed     define how far the entity will move
     */
    fun moveTowards(direction: Point, speed: Double) {
        var speed = speed
        val position = entity.position
        val dx = direction.x() - position.x()
        val dy = direction.y() - position.y()
        val dz = direction.z() - position.z()
        // the purpose of these few lines is to slow down entities when they reach their destination
        val distSquared = dx * dx + dy * dy + dz * dz
        if (speed > distSquared) {
            speed = distSquared
        }
        val radians = Math.atan2(dz, dx)
        val speedX = Math.cos(radians) * speed
        val speedY = dy * speed
        val speedZ = Math.sin(radians) * speed
        val yaw = getLookYaw(dx, dz)
        val pitch = getLookPitch(dx, dy, dz)
        // Prevent ghosting
        val physicsResult = CollisionUtils.handlePhysics(entity, Vec(speedX, speedY, speedZ))
        entity.refreshPosition(physicsResult.newPosition().withView(yaw, pitch))
    }

    fun jump(height: Float) {
        // FIXME magic value
        entity.velocity = Vec(0, (height * 2.5f).toDouble(), 0)
    }

    /**
     * Retrieves the path to `position` and ask the entity to follow the path.
     *
     *
     * Can be set to null to reset the pathfinder.
     *
     *
     * The position is cloned, if you want the entity to continually follow this position object
     * you need to call this when you want the path to update.
     *
     * @param point      the position to find the path to, null to reset the pathfinder
     * @param bestEffort whether to use the best-effort algorithm to the destination,
     * if false then this method is more likely to return immediately
     * @return true if a path has been found
     */
    @Synchronized
    fun setPathTo(point: Point?, bestEffort: Boolean): Boolean {
        if (point != null && pathPosition != null && point.samePoint(pathPosition!!)) {
            // Tried to set path to the same target position
            return false
        }
        val instance = entity.instance
        if (pathFinder == null) {
            // Unexpected error
            return false
        }
        pathFinder!!.reset()
        if (point == null) {
            return false
        }
        // Can't path with a null instance.
        if (instance == null) {
            return false
        }
        // Can't path outside the world border
        val worldBorder = instance.worldBorder
        if (!worldBorder.isInside(point)) {
            return false
        }
        // Can't path in an unloaded chunk
        val chunk = instance.getChunkAt(point)
        if (!isLoaded(chunk)) {
            return false
        }
        val pathOptions = PathOptions()
            .targetingStrategy(if (bestEffort) PathOptions.TargetingStrategy.gravitySnap else PathOptions.TargetingStrategy.none)
        val path = pathFinder!!.initiatePathTo(
            point.x(),
            point.y(),
            point.z(),
            pathOptions
        )
        val success = path != null
        pathPosition = if (success) point else null
        return success
    }

    /**
     * @see .setPathTo
     */
    fun setPathTo(position: Point?): Boolean {
        return setPathTo(position, true)
    }

    @ApiStatus.Internal
    @Synchronized
    fun tick() {
        if (pathPosition == null) return  // No path
        if (entity is LivingEntity && entity.isDead) return  // No pathfinding tick for dead entities
        if (pathFinder!!.updatePathFor(pathingEntity) == null) {
            reset()
        }
    }

    @ApiStatus.Internal
    fun setPathFinder(pathFinder: HydrazinePathFinder?) {
        this.pathFinder = pathFinder
    }

    private fun reset() {
        pathPosition = null
        pathFinder!!.reset()
    }
}