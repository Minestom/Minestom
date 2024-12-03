package net.minestom.server.entity.pathfinding.followers;

import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WaterNodeFollower implements NodeFollower {
    private final Entity entity;
    private static final double WATER_SPEED_MULTIPLIER = 0.5;

    public WaterNodeFollower(@NotNull Entity entity) {
        this.entity = entity;
    }

    /**
     * Used to move the entity toward {@code direction} in the X and Z axis
     * Gravity is still applied but the entity will not attempt to jump
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param direction the targeted position
     * @param speed     define how far the entity will move
     */
    public void moveTowards(@NotNull Point direction, double speed, @NotNull Point lookAt) {
        final Pos position = entity.getPosition();
        final double dx = direction.x() - position.x();
        final double dy = direction.y() - position.y();
        final double dz = direction.z() - position.z();

        final double dxLook = lookAt.x() - position.x();
        final double dyLook = lookAt.y() - position.y();
        final double dzLook = lookAt.z() - position.z();

        // the purpose of these few lines is to slow down entities when they reach their destination
        final double distSquared = dx * dx + dy * dy + dz * dz;
        if (speed > distSquared) {
            speed = distSquared;
        }

        var instance = entity.getInstance();
        if (instance != null)
            if (instance.getBlock(position).isLiquid()) {
                speed *= WATER_SPEED_MULTIPLIER;
            }

        final double radians = Math.atan2(dz, dx);
        final double speedX = Math.cos(radians) * speed;
        final double speedZ = Math.sin(radians) * speed;
        final float yaw = PositionUtils.getLookYaw(dxLook, dzLook);
        final float pitch = PositionUtils.getLookPitch(dxLook, dyLook, dzLook);

        double speedY = Math.signum(dy) * 0.5 * speed;
        if (Math.min(Math.abs(dy), Math.abs(speedY)) == Math.abs(dy)) {
            speedY = dy;
        }

        final var physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        this.entity.refreshPosition(Pos.fromPoint(physicsResult.newPosition()).withView(yaw, pitch));
    }

    @Override
    public void jump(@Nullable Point point, @Nullable Point target) {
    }

    @Override
    public boolean isAtPoint(@NotNull Point point) {
        return entity.getPosition().sameBlock(point);
    }

    @Override
    public double movementSpeed() {
        if (entity instanceof LivingEntity living) {
            return living.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
        }

        return 0.1f;
    }
}
