package net.minestom.server.utils.position;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.utils.MathUtils;
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
        return (float) (Math.atan2(dz, dx) * MathUtils.RADIANS_TO_DEGREES) - 90;
    }

    public static float getLookPitch(double dx, double dy, double dz) {
        return (float) (Math.atan2(dy, MathUtils.distance2(dx, dz)) * -MathUtils.RADIANS_TO_DEGREES);
    }
}
