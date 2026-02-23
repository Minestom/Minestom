package net.minestom.server.entity.pathfinding.followers;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.Nullable;

public class GroundNodeFollower implements NodeFollower {
    private final Entity entity;
    private double maxStepHeight = 0.6;

    public GroundNodeFollower(Entity entity) {
        this.entity = entity;
    }

    public void setMaxStepHeight(double maxStepHeight) {
        this.maxStepHeight = maxStepHeight;
    }

    public double getMaxStepHeight() {
        return maxStepHeight;
    }

    /**
     * Used to move the entity toward {@code direction} in the X and Z axis
     * Gravity is still applied but the entity will not attempt to jump
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param target the targeted position
     * @param speed  define how far the entity will move
     * @param lookAt the position to look at
     */
    @Override
    public void moveTowards(Point target, double speed, Point lookAt) {
        final Pos pos = entity.getPosition();
        final double dx = target.x() - pos.x();
        final double dy = target.y() - pos.y();
        final double dz = target.z() - pos.z();

        final double dxLook = lookAt.x() - pos.x();
        final double dyLook = lookAt.y() - pos.y();
        final double dzLook = lookAt.z() - pos.z();

        final double horizDistSq = dx * dx + dz * dz;
        final double horizDist = Math.sqrt(horizDistSq);

        if (horizDistSq < 2.5E-7) {
            entity.setVelocity(new Vec(0, entity.getVelocity().y(), 0));
            return;
        }

        speed = Math.min(speed, horizDist);

        final double radians = Math.atan2(dz, dx);
        final double velX = Math.cos(radians) * speed * 20;
        final double velZ = Math.sin(radians) * speed * 20;

        final float yaw = PositionUtils.getLookYaw(dxLook, dzLook);
        final float pitch = PositionUtils.getLookPitch(dxLook, dyLook, dzLook);

        Vec currentVel = entity.getVelocity();
        entity.setVelocity(new Vec(velX, currentVel.y(), velZ));
        entity.setView(yaw, pitch);
    }

    @Override
    public void jump(@Nullable Point target, @Nullable Point next) {
        if (!entity.isOnGround() || target == null) return;

        Pos pos = entity.getPosition();
        double dx = target.x() - pos.x();
        double dz = target.z() - pos.z();

        double horizDist = Math.sqrt(dx * dx + dz * dz);
        if (horizDist < 0.01) horizDist = 0.01;

        double speed = movementSpeed() * 20;
        double velX = (dx / horizDist) * speed;
        double velZ = (dz / horizDist) * speed;

        entity.setVelocity(new Vec(velX, 8.4, velZ));
    }

    @Override
    public boolean isAtPoint(Point point) {
        final double tolerance = Math.max(0.35, entity.getBoundingBox().width() * 0.6);
        return entity.getPosition().distance(point) <= tolerance;
    }

    @Override
    public double movementSpeed() {
        if (entity instanceof LivingEntity living) {
            return living.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
        }
        return 0.1;
    }
}
