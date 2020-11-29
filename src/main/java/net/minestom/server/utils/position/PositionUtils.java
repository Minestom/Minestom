package net.minestom.server.utils.position;

import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public final class PositionUtils {

    public static void lookAlong(@NotNull Position position, float dx, float dy, float dz) {
        final float horizontalAngle = (float) Math.atan2(dz, dx);
        final float yaw = (float) (horizontalAngle * (180.0 / Math.PI)) - 90;
        final float pitch = (float) Math.atan2(dy, Math.max(Math.abs(dx), Math.abs(dz)));

        position.setYaw(yaw);
        position.setPitch(pitch);
    }

}
