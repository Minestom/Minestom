package net.minestom.server.utils.position;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

public final class PositionUtils {
    public static Pos lookAlong(@NotNull Pos position, double dx, double dy, double dz) {
        final double horizontalAngle = Math.atan2(dz, dx);
        final float yaw = (float) (horizontalAngle * (180.0 / Math.PI)) - 90;
        final float pitch = (float) Math.atan2(dy, Math.max(Math.abs(dx), Math.abs(dz)));
        return position.withView(yaw, pitch);
    }
}
