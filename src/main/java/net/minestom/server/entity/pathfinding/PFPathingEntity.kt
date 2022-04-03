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
import com.extollit.linalg.immutable.Vec3d
import net.minestom.server.attribute.Attribute
import net.minestom.server.coordinate.Point
import net.minestom.server.entity.Entity
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class PFPathingEntity(private val navigator: Navigator) : IPathingEntity {
    private val entity: Entity
    private var searchRange: Float

    // Capacities
    var isFireResistant = false
    var isCautious = false
    var isClimber = false
    var isSwimmer = false
    var isAquatic = false
    var isAvian = false
    var isAquaphobic = false
    var isAvoidsDoorways = false
    var isOpensDoors = false

    init {
        entity = navigator.entity
        searchRange = getAttributeValue(Attribute.FOLLOW_RANGE)
    }

    override fun age(): Int {
        return entity.aliveTicks.toInt()
    }

    override fun bound(): Boolean {
        return entity.hasVelocity()
    }

    override fun searchRange(): Float {
        return searchRange
    }

    /**
     * Changes the search range of the entity
     *
     * @param searchRange the new entity's search range
     */
    fun setSearchRange(searchRange: Float) {
        this.searchRange = searchRange
    }

    override fun capabilities(): IPathingEntity.Capabilities {
        return object : IPathingEntity.Capabilities {
            override fun speed(): Float {
                return getAttributeValue(Attribute.MOVEMENT_SPEED)
            }

            override fun fireResistant(): Boolean {
                return isFireResistant
            }

            override fun cautious(): Boolean {
                return isCautious
            }

            override fun climber(): Boolean {
                return isClimber
            }

            override fun swimmer(): Boolean {
                return isSwimmer
            }

            override fun aquatic(): Boolean {
                return isAquatic
            }

            override fun avian(): Boolean {
                return isAvian
            }

            override fun aquaphobic(): Boolean {
                return isAquaphobic
            }

            override fun avoidsDoorways(): Boolean {
                return isAvoidsDoorways
            }

            override fun opensDoors(): Boolean {
                return isOpensDoors
            }
        }
    }

    override fun moveTo(position: Vec3d, passibility: Passibility, gravitation: Gravitation) {
        val targetPosition: Point = Vec(position.x, position.y, position.z)
        navigator.moveTowards(targetPosition, getAttributeValue(Attribute.MOVEMENT_SPEED).toDouble())
        val entityY = entity.position.y()
        if (entityY < targetPosition.y()) {
            navigator.jump(1f)
        }
    }

    override fun coordinates(): Vec3d {
        val position = entity.position
        return Vec3d(position.x(), position.y(), position.z())
    }

    override fun width(): Float {
        return entity.boundingBox.width().toFloat()
    }

    override fun height(): Float {
        return entity.boundingBox.height().toFloat()
    }

    private fun getAttributeValue(attribute: Attribute): Float {
        return if (entity is LivingEntity) {
            entity.getAttributeValue(attribute)
        } else 0f
    }
}