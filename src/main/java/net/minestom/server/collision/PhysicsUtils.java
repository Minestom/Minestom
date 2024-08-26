package net.minestom.server.collision;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
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
     * @param entityVelocityPerTick the current entity velocity in blocks/tick
     * @param entityBoundingBox the current entity bounding box
     * @param worldBorder the world border to test bounds against
     * @param blockGetter the block getter to test block collisions against
     * @param aerodynamics the current entity aerodynamics
     * @param entityNoGravity whether the entity has gravity
     * @param entityHasPhysics whether the entity has physics
     * @param entityOnGround whether the entity is on the ground
     * @param entityFlying whether the entity is flying
     * @param previousPhysicsResult the physics result from the previous simulation or null
     * @return a {@link PhysicsResult} containing the resulting physics state of this simulation
     */
    public static @NotNull PhysicsResult simulateMovement(@NotNull Pos entityPosition, @NotNull Vec entityVelocityPerTick, @NotNull BoundingBox entityBoundingBox,
                                                          @NotNull WorldBorder worldBorder, @NotNull Block.Getter blockGetter, @NotNull Aerodynamics aerodynamics, boolean entityNoGravity,
                                                          boolean entityHasPhysics, boolean entityOnGround, boolean entityFlying, @Nullable PhysicsResult previousPhysicsResult) {
        final PhysicsResult physicsResult = entityHasPhysics ?
                CollisionUtils.handlePhysics(blockGetter, entityBoundingBox, entityPosition, entityVelocityPerTick, previousPhysicsResult, false) :
                CollisionUtils.blocklessCollision(entityPosition, entityVelocityPerTick);

        Pos newPosition = physicsResult.newPosition();
        Vec newVelocity = physicsResult.newVelocity();

        Pos positionWithinBorder = CollisionUtils.applyWorldBorder(worldBorder, entityPosition, newPosition);
        newVelocity = updateVelocity(entityPosition, newVelocity, blockGetter, aerodynamics, !positionWithinBorder.samePoint(entityPosition), entityFlying, entityOnGround, entityNoGravity);
        return new PhysicsResult(positionWithinBorder, newVelocity, physicsResult.isOnGround(), physicsResult.collisionX(), physicsResult.collisionY(), physicsResult.collisionZ(),
                physicsResult.originalDelta(), physicsResult.collisionPoints(), physicsResult.collisionShapes(), physicsResult.collisionShapePositions(), physicsResult.hasCollision(), physicsResult.res());
    }

    private static @NotNull Vec updateVelocity(@NotNull Pos entityPosition, @NotNull Vec currentVelocity, @NotNull Block.Getter blockGetter, @NotNull Aerodynamics aerodynamics,
                                               boolean positionChanged, boolean entityFlying, boolean entityOnGround, boolean entityNoGravity) {
        if (!positionChanged) {
            if (entityFlying) return Vec.ZERO;
            return new Vec(0, entityNoGravity ? 0 : -aerodynamics.gravity() * aerodynamics.verticalAirResistance(), 0);
        }

        double drag = entityOnGround ? blockGetter.getBlock(entityPosition.sub(0, 0.5000001, 0)).registry().friction() * aerodynamics.horizontalAirResistance() :
                aerodynamics.horizontalAirResistance();
        double gravity = entityFlying ? 0 : aerodynamics.gravity();
        double gravityDrag = entityFlying ? 0.6 : aerodynamics.verticalAirResistance();

        double x = currentVelocity.x() * drag;
        double y = entityNoGravity ? currentVelocity.y() : (currentVelocity.y() - gravity) * gravityDrag;
        double z = currentVelocity.z() * drag;
        return new Vec(Math.abs(x) < Vec.EPSILON ? 0 : x, Math.abs(y) < Vec.EPSILON ? 0 : y, Math.abs(z) < Vec.EPSILON ? 0 : z);
    }

    private PhysicsUtils() {}
}
