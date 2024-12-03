package net.minestom.server.utils.position;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.RelativeFlags;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class PositionUtils {
    public static Pos lookAlong(@NotNull Pos position, double dx, double dy, double dz) {
        final float yaw = getLookYaw(dx, dz);
        final float pitch = getLookPitch(dx, dy, dz);
        return position.withView(yaw, pitch);
    }

    public static float getLookYaw(double dx, double dz) {
        final double radians = Math.atan2(dz, dx);
        final float degrees = (float)Math.toDegrees(radians) - 90;
        if (degrees < -180) return degrees + 360;
        if (degrees > 180) return degrees - 360;
        return degrees;
    }

    public static float getLookPitch(double dx, double dy, double dz) {
        final double radians = -Math.atan2(dy, Math.max(Math.abs(dx), Math.abs(dz)));
        return (float) Math.toDegrees(radians);
    }

    public static @NotNull Pos getPositionWithRelativeFlags(@NotNull Pos start, @NotNull Pos modifier, @MagicConstant(flagsFromClass = RelativeFlags.class) int flags) {
        double x = (flags & RelativeFlags.X) == 0 ? modifier.x() : start.x() + modifier.x();
        double y = (flags & RelativeFlags.Y) == 0 ? modifier.y() : start.y() + modifier.y();
        double z = (flags & RelativeFlags.Z) == 0 ? modifier.z() : start.z() + modifier.z();
        float yaw = (flags & RelativeFlags.YAW) == 0 ? modifier.yaw() : start.yaw() + modifier.yaw();
        float pitch = (flags & RelativeFlags.PITCH) == 0 ? modifier.pitch() : start.pitch() + modifier.pitch();
        return new Pos(x, y, z, yaw, pitch);
    }

    public static @NotNull Vec getVelocityWithRelativeFlags(@NotNull Vec start, @NotNull Vec modifier, @MagicConstant(flagsFromClass = RelativeFlags.class) int flags) {
        double x = (flags & RelativeFlags.DELTA_X) == 0 ? modifier.x() : start.x() + modifier.x();
        double y = (flags & RelativeFlags.DELTA_Y) == 0 ? modifier.y() : start.y() + modifier.y();
        double z = (flags & RelativeFlags.DELTA_Z) == 0 ? modifier.z() : start.z() + modifier.z();
        return new Vec(x, y, z);
    }
}
