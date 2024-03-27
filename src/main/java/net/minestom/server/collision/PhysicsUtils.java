package net.minestom.server.collision;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PhysicsUtils {
    /**
     * Simulate the entity's movement physics
     * <p>
     * This is done by first attempting to move the entity forward with the
     * current velocity passed in. Then adjusting the velocity by applying
     * air resistance and friction.
     *
     * @param entityPosition the current entity position
     * @param entityVelocity the current entity velocity
     * @param entityBoundingBox the current entity bounding box
     * @param entityChunk the current entity chunk
     * @param aerodynamics the current entity aerodynamics
     * @param entityNoGravity whether the entity has gravity
     * @param entityHasPhysics whether the entity has physics
     * @param entityOnGround whether the entity is on the ground
     * @param entityFlying whether the entity is flying
     * @param previousPhysicsResult the physics result from the previous simulation or null
     * @return a {@link PhysicsResult} containing the resulting physics state of this simulation
     */
    public static @NotNull PhysicsResult simulateMovement(@NotNull Pos entityPosition, @NotNull Vec entityVelocity, @NotNull BoundingBox entityBoundingBox,
                                                          @NotNull Chunk entityChunk, @NotNull Aerodynamics aerodynamics, boolean entityNoGravity,
                                                          boolean entityHasPhysics, boolean entityOnGround, boolean entityFlying, @Nullable PhysicsResult previousPhysicsResult) {
        Vec velocityPerTick = entityVelocity.div(ServerFlag.SERVER_TICKS_PER_SECOND);
        final PhysicsResult physicsResult = entityHasPhysics ?
                CollisionUtils.handlePhysics(entityChunk.getInstance(), entityChunk, entityBoundingBox, entityPosition, velocityPerTick, previousPhysicsResult, false) :
                CollisionUtils.blocklessCollision(entityPosition, velocityPerTick);

        Pos newPosition = physicsResult.newPosition();
        Vec newVelocity = physicsResult.newVelocity();

        Pos positionWithinBorder = CollisionUtils.applyWorldBorder(entityChunk.getInstance(), entityPosition, newPosition);
        newVelocity = updateVelocity(entityPosition, newVelocity, entityChunk, aerodynamics, !positionWithinBorder.samePoint(entityPosition), entityFlying, entityOnGround, entityNoGravity);
        return new PhysicsResult(positionWithinBorder, newVelocity, physicsResult.isOnGround(), physicsResult.collisionX(), physicsResult.collisionY(), physicsResult.collisionZ(),
                physicsResult.originalDelta(), physicsResult.collisionPoints(), physicsResult.collisionShapes(), physicsResult.hasCollision(), physicsResult.res());
    }

    private static @NotNull Vec updateVelocity(@NotNull Pos entityPosition, @NotNull Vec currentVelocity, @NotNull Chunk entityChunk, @NotNull Aerodynamics aerodynamics,
                                               boolean positionChanged, boolean entityFlying, boolean entityOnGround, boolean entityNoGravity) {
        if (!positionChanged) {
            if (entityOnGround || entityFlying) return Vec.ZERO;
            return new Vec(0, entityNoGravity ? 0 : -aerodynamics.gravity() * ServerFlag.SERVER_TICKS_PER_SECOND * aerodynamics.verticalAirResistance(), 0);
        }

        final double drag;
        if (entityOnGround) {
            synchronized (entityChunk) {
                drag = entityChunk.getBlock(entityPosition.sub(0, 0.5000001, 0)).registry().friction() * aerodynamics.horizontalAirResistance();
            }
        } else drag = aerodynamics.horizontalAirResistance();

        double gravity = entityFlying ? 0 : aerodynamics.gravity();
        double gravityDrag = entityFlying ? 0.6 : aerodynamics.verticalAirResistance();

        return currentVelocity.apply((x, y, z) -> new Vec(
                        x * drag,
                        !entityNoGravity ? (y - gravity) * gravityDrag : y,
                        z * drag)
                ).mul(ServerFlag.SERVER_TICKS_PER_SECOND)
                .apply(Vec.Operator.EPSILON);
    }

    private PhysicsUtils() {}
}
