package net.minestom.server.utils.position;

import net.minestom.server.coordinate.Pos;
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
}
