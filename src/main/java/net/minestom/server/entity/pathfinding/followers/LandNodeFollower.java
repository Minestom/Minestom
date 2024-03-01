package net.minestom.server.entity.pathfinding.followers;

import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.position.PositionUtils;
import org.jetbrains.annotations.NotNull;

public class LandNodeFollower implements NodeFollower {
    private final Entity entity;

    public LandNodeFollower(Entity entity) {
        this.entity = entity;
    }

    /**
     * Used to move the entity toward {@code direction} in the X and Z axis
     * Gravity is still applied but the entity will not attempt to jump
     * Also update the yaw/pitch of the entity to look along 'direction'
     *
     * @param direction    the targeted position
     * @param speed        define how far the entity will move
     */
    public PhysicsResult moveTowards(@NotNull Point direction, double speed, Point lookAt) {
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

        boolean inWater = false;
        var instance = entity.getInstance();
        if (instance != null)
            if (instance.getBlock(position).isLiquid()) {
                //    speed *= capabilities.swimSpeedModifier();
                inWater = true;
            }

        final double radians = Math.atan2(dz, dx);
        final double speedX = Math.cos(radians) * speed;
        final double speedZ = Math.sin(radians) * speed;
        final float yaw = PositionUtils.getLookYaw(dxLook, dzLook);
        final float pitch = PositionUtils.getLookPitch(dxLook, dyLook, dzLook);

        // final double speedY = (capabilities.type() == PPath.PathfinderType.AQUATIC
        //         || capabilities.type() == PPath.PathfinderType.FLYING
        //         || (capabilities.type() == PPath.PathfinderType.AMPHIBIOUS && inWater))
        //         ? Math.signum(dy) * 0.5 * speed
        //         : 0;

        final double speedY = 0;

        final var physicsResult = CollisionUtils.handlePhysics(entity, new Vec(speedX, speedY, speedZ));
        this.entity.refreshPosition(Pos.fromPoint(physicsResult.newPosition()).withView(yaw, pitch));

        return physicsResult;
    }

    @Override
    public void jump() {
        if (entity.isOnGround()) {
            jump(4f);
        }
    }

    public void jump(float height) {
        this.entity.setVelocity(new Vec(0, height * 2.5f, 0));
    }
}
