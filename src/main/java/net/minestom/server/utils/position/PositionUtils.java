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
        final double horizontalAngle = Math.atan2(dz, dx);
        return (float) (horizontalAngle * (180.0 / Math.PI)) - 90;
    }

    public static float getLookPitch(double dx, double dy, double dz) {
        return (float) Math.atan2(dy, Math.max(Math.abs(dx), Math.abs(dz)));
    }
}
