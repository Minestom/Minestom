package net.minestom.server.utils.location

import net.minestom.server.command.CommandSender
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.utils.location.RelativeVec.CoordinateType
import net.minestom.server.utils.location.RelativeVec.CoordinateConverter
import org.jetbrains.annotations.ApiStatus
import java.util.*

/**
 * Represents a location which can have fields relative to an [Entity] position.
 */
class RelativeVec(
    private val vec: Vec, private val coordinateType: CoordinateType,
    /**
     * Gets if the 'x' field is relative.
     *
     * @return true if the 'x' field is relative
     */
    val isRelativeX: Boolean,
    /**
     * Gets if the 'y' field is relative.
     *
     * @return true if the 'y' field is relative
     */
    val isRelativeY: Boolean,
    /**
     * Gets if the 'z' field is relative.
     *
     * @return true if the 'z' field is relative
     */
    val isRelativeZ: Boolean
) {

    fun coordinateType(): CoordinateType {
        return coordinateType
    }

    /**
     * Gets the location based on the relative fields and `position`.
     *
     * @param origin the origin position, null if none
     * @return the location
     */
    fun from(origin: Pos?): Vec {
        var origin = origin
        origin = Objects.requireNonNullElse(origin, Pos.ZERO)
        return coordinateType.convert(vec, origin, isRelativeX, isRelativeY, isRelativeZ)
    }

    @ApiStatus.Experimental
    fun fromView(point: Pos?): Vec {
        if (!isRelativeX && !isRelativeY && !isRelativeZ) {
            return vec
        }
        val absolute = Objects.requireNonNullElse(point, Pos.ZERO)
        val x: Double = vec.x() + if (isRelativeX) absolute!!.yaw() else 0
        val z: Double = vec.z() + if (isRelativeZ) absolute!!.pitch() else 0
        return Vec(x, 0, z)
    }

    /**
     * Gets the location based on the relative fields and `entity`.
     *
     * @param entity the entity to get the relative position from
     * @return the location
     */
    fun from(entity: Entity?): Vec {
        return if (entity != null) {
            from(entity.position)
        } else {
            from(Pos.ZERO)
        }
    }

    fun fromSender(sender: CommandSender?): Vec {
        val entityPosition = if (sender is Player) sender.position else Pos.ZERO
        return from(entityPosition)
    }

    @ApiStatus.Experimental
    fun fromView(entity: Entity?): Vec {
        val entityPosition = entity?.position ?: Pos.ZERO
        return fromView(entityPosition)
    }

    enum class CoordinateType(private val converter: CoordinateConverter) {
        RELATIVE(CoordinateConverter { relative: Vec, origin: Pos?, relativeX: Boolean, relativeY: Boolean, relativeZ: Boolean ->
            val absolute: Record = Objects.requireNonNullElse(origin, Vec.ZERO)!!
            val x: Double = relative.x() + if (relativeX) absolute.x() else 0
            val y: Double = relative.y() + if (relativeY) absolute.y() else 0
            val z: Double = relative.z() + if (relativeZ) absolute.z() else 0
            Vec(x, y, z)
        }),
        LOCAL(CoordinateConverter { local: Vec, origin: Pos, relativeX: Boolean, relativeY: Boolean, relativeZ: Boolean ->
            val vec1 = Vec(
                Math.cos(Math.toRadians((origin.yaw() + 90.0f).toDouble())),
                0,
                Math.sin(Math.toRadians((origin.yaw() + 90.0f).toDouble()))
            )
            val a = vec1.mul(Math.cos(Math.toRadians(-origin.pitch().toDouble())))
                .withY(Math.sin(Math.toRadians(-origin.pitch().toDouble())))
            val b = vec1.mul(Math.cos(Math.toRadians((-origin.pitch() + 90.0f).toDouble())))
                .withY(Math.sin(Math.toRadians((-origin.pitch() + 90.0f).toDouble())))
            val c = a.cross(b).neg()
            val relativePos = a.mul(local.z()).add(b.mul(local.y())).add(c.mul(local.x()))
            origin.add(relativePos).asVec()
        }),
        ABSOLUTE(CoordinateConverter { vec: Vec?, origin: Pos?, relativeX1: Boolean, relativeY1: Boolean, relativeZ1: Boolean -> vec });

        fun convert(vec: Vec, origin: Pos?, relativeX: Boolean, relativeY: Boolean, relativeZ: Boolean): Vec {
            return converter.convert(vec, origin, relativeX, relativeY, relativeZ)
        }
    }

    private interface CoordinateConverter {
        fun convert(vec: Vec?, origin: Pos?, relativeX: Boolean, relativeY: Boolean, relativeZ: Boolean): Vec
    }
}