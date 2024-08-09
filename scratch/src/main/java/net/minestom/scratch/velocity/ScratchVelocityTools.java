package net.minestom.scratch.velocity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;

public final class ScratchVelocityTools {
    public static Vec knockback(Pos source, float strength, Vec velocity, boolean onGround) {
        final double x = Math.sin(source.yaw() * 0.017453292);
        final double z = -Math.cos(source.yaw() * 0.017453292);
        final Vec velocityModifier = new Vec(x, z).normalize().mul(strength);
        final double verticalLimit = .4d;
        return new Vec(velocity.x() / 2d - velocityModifier.x(),
                onGround ? Math.min(verticalLimit, velocity.y() / 2d + strength) : velocity.y(),
                velocity.z() / 2d - velocityModifier.z()
        );
    }
}
