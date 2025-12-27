package net.minestom.server.collision;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public final class PhysicsUtils {

    private static final double DEFAULT_STEP_HEIGHT = 1;

    /**
     * Simulate the entity's movement physics
     * <p>
     * This is done by first attempting to move the entity forward with the
     * current velocity passed in. If a horizontal collision occurs and the entity
     * is on ground, it will attempt to step up onto the obstacle. Then the velocity
     * is adjusted by applying air resistance and friction.
     *
     * @param position the current entity position
     * @param velocity the current entity velocity in blocks/tick
     * @param boundingBox the current entity bounding box
     * @param worldBorder the world border to test bounds against
     * @param blockGetter the block getter to test block collisions against
     * @param aerodynamics the current entity aerodynamics
     * @param noGravity whether the entity has no gravity
     * @param hasPhysics whether the entity has physics
     * @param onGround whether the entity is on the ground
     * @param flying whether the entity is flying
     * @param previous the physics result from the previous simulation or null
     * @return a {@link PhysicsResult} containing the resulting physics state of this simulation
     */
    public static PhysicsResult simulateMovement(Pos position, Vec velocity, BoundingBox boundingBox,
                                                 WorldBorder worldBorder, Block.Getter blockGetter, Aerodynamics aerodynamics,
                                                 boolean noGravity, boolean hasPhysics, boolean onGround,
                                                 boolean flying, @Nullable PhysicsResult previous) {
        return simulateMovement(position, velocity, boundingBox, worldBorder, blockGetter,
                aerodynamics, noGravity, hasPhysics, onGround, flying, previous, DEFAULT_STEP_HEIGHT);
    }

    public static PhysicsResult simulateMovement(Pos position, Vec velocity, BoundingBox boundingBox,
                                                 WorldBorder worldBorder, Block.Getter blockGetter, Aerodynamics aerodynamics,
                                                 boolean noGravity, boolean hasPhysics, boolean onGround,
                                                 boolean flying, @Nullable PhysicsResult previous,
                                                 double stepHeight) {

        PhysicsResult result;

        if (hasPhysics) {
            result = CollisionUtils.handlePhysics(blockGetter, boundingBox, position, velocity, previous, false);

            if (onGround && stepHeight > 0 && (result.collisionX() || result.collisionZ())) {
                PhysicsResult stepped = tryStepUp(position, velocity, boundingBox, blockGetter, stepHeight);

                if (stepped != null) {
                    double normalDistSq = position.distanceSquared(result.newPosition());
                    double steppedDistSq = position.distanceSquared(stepped.newPosition());

                    if (steppedDistSq > normalDistSq + 0.001) {
                        result = stepped;
                    }
                }
            }
        } else {
            result = CollisionUtils.blocklessCollision(position, velocity);
        }

        Pos newPos = result.newPosition();
        Vec newVel = result.newVelocity();

        Pos withinBorder = CollisionUtils.applyWorldBorder(worldBorder, position, newPos);
        newVel = updateVelocity(position, newVel, blockGetter, aerodynamics,
                !withinBorder.samePoint(position), flying, onGround, noGravity);

        final boolean cached = result.cached() && newVel.samePoint(result.newVelocity())
                && withinBorder.samePoint(result.newPosition());

        return new PhysicsResult(withinBorder, newVel, result.isOnGround(),
                result.collisionX(), result.collisionY(), result.collisionZ(),
                result.originalDelta(), result.collisionPoints(), result.collisionShapes(),
                result.collisionShapePositions(), result.hasCollision(), result.res(), cached);
    }

    private static @Nullable PhysicsResult tryStepUp(Pos position, Vec velocity, BoundingBox boundingBox,
                                                      Block.Getter blockGetter, double stepHeight) {
        if (velocity.x() == 0 && velocity.z() == 0) {
            return null;
        }

        double baseStep = stepHeight + Math.max(0, velocity.y());
        double climbHeight = Math.max(baseStep, Math.min(1.0, stepHeight + 0.5)) + 0.001;

        Vec upVelocity = new Vec(0, climbHeight, 0);
        PhysicsResult upResult = CollisionUtils.handlePhysics(blockGetter, boundingBox,
                position, upVelocity, null, false);

        if (upResult.collisionY()) {
            double actualStep = upResult.newPosition().y() - position.y();
            if (actualStep < 0.1) {
                return null;
            }
        }

        Pos elevated = upResult.newPosition();

        Vec horizVelocity = new Vec(velocity.x(), 0, velocity.z());
        PhysicsResult horizResult = CollisionUtils.handlePhysics(blockGetter, boundingBox,
                elevated, horizVelocity, null, false);

        double movedX = Math.abs(horizResult.newPosition().x() - elevated.x());
        double movedZ = Math.abs(horizResult.newPosition().z() - elevated.z());
        double wantedX = Math.abs(horizVelocity.x());
        double wantedZ = Math.abs(horizVelocity.z());

        if ((wantedX > 0.001 && movedX < wantedX * 0.5) && (wantedZ > 0.001 && movedZ < wantedZ * 0.5)) {
            return null;
        }

        Pos afterHoriz = horizResult.newPosition();

        Vec downVelocity = new Vec(0, -(climbHeight + 0.5), 0);
        PhysicsResult downResult = CollisionUtils.handlePhysics(blockGetter, boundingBox,
                afterHoriz, downVelocity, null, false);

        Pos finalPos = downResult.newPosition();

        if (finalPos.y() < position.y() - 0.5) {
            return null;
        }

        Vec remaining = new Vec(
                horizResult.collisionX() ? 0 : velocity.x() - (finalPos.x() - position.x()),
                velocity.y(),
                horizResult.collisionZ() ? 0 : velocity.z() - (finalPos.z() - position.z())
        );

        return new PhysicsResult(
                finalPos.asPos(),
                remaining,
                downResult.collisionY(),
                horizResult.collisionX(),
                false,
                horizResult.collisionZ(),
                velocity,
                downResult.collisionPoints(),
                downResult.collisionShapes(),
                downResult.collisionShapePositions(),
                true,
                downResult.res(),
                false
        );
    }

    private static Vec updateVelocity(Pos position, Vec velocity, Block.Getter blockGetter,
                                      Aerodynamics aerodynamics, boolean moved, boolean flying,
                                      boolean onGround, boolean noGravity) {
        if (!moved) {
            if (flying) return Vec.ZERO;
            return new Vec(0, noGravity ? 0 : -aerodynamics.gravity() * aerodynamics.verticalAirResistance(), 0);
        }

        double drag = onGround
                ? blockGetter.getBlock(position.sub(0, 0.5000001, 0)).registry().friction() * aerodynamics.horizontalAirResistance()
                : aerodynamics.horizontalAirResistance();
        double gravity = flying ? 0 : aerodynamics.gravity();
        double gravityDrag = flying ? 0.6 : aerodynamics.verticalAirResistance();

        double x = velocity.x() * drag;
        double y = noGravity ? velocity.y() : (velocity.y() - gravity) * gravityDrag;
        double z = velocity.z() * drag;

        return new Vec(
                Math.abs(x) < Vec.EPSILON ? 0 : x,
                Math.abs(y) < Vec.EPSILON ? 0 : y,
                Math.abs(z) < Vec.EPSILON ? 0 : z
        );
    }

    private PhysicsUtils() {}
}
