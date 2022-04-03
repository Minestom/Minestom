package net.minestom.server.utils.position

import net.minestom.server.coordinate.Pos
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object PositionUtils {
    fun lookAlong(position: Pos, dx: Double, dy: Double, dz: Double): Pos {
        val yaw = getLookYaw(dx, dz)
        val pitch = getLookPitch(dx, dy, dz)
        return position.withView(yaw, pitch)
    }

    @JvmStatic
    fun getLookYaw(dx: Double, dz: Double): Float {
        val radians = Math.atan2(dz, dx)
        val degrees = Math.toDegrees(radians).toFloat() - 90
        if (degrees < -180) return degrees + 360
        return if (degrees > 180) degrees - 360 else degrees
    }

    @JvmStatic
    fun getLookPitch(dx: Double, dy: Double, dz: Double): Float {
        val radians = -Math.atan2(dy, Math.max(Math.abs(dx), Math.abs(dz)))
        return Math.toDegrees(radians).toFloat()
    }
}