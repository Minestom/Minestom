package net.minestom.server.entity.pathfinding.followers;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.pathfinding.PathWalker;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroundNodeFollower implements NodeFollower {
    private final PathWalker pathWalker;

    public GroundNodeFollower(@NotNull PathWalker pathWalker) {
        this.pathWalker = pathWalker;
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
        final Pos position = pathWalker.getPosition();
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

        final double radians = Math.atan2(dz, dx);
        final double speedX = Math.cos(radians) * speed;
        final double speedZ = Math.sin(radians) * speed;
        final float yaw = PositionUtils.getLookYaw(dxLook, dzLook);
        final float pitch = PositionUtils.getLookPitch(dxLook, dyLook, dzLook);

        pathWalker.updateNewPosition(new Vec(speedX, 0, speedZ), yaw, pitch);
    }

    @Override
    public void jump(@Nullable Point point, @Nullable Point target) {
        if (pathWalker.isOnGround()) {
            jump(4f);
        }
    }

    @Override
    public boolean isAtPoint(@NotNull Point point) {
        return pathWalker.getPosition().sameBlock(point);
    }

    public void jump(float height) {
        this.pathWalker.setVelocity(new Vec(0, height * 2.5f, 0));
    }

    @Override
    public double movementSpeed() {
        if (pathWalker instanceof LivingEntity living) {
            return living.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
        }

        return 0.1f;
    }
}
