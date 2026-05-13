package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class EntityCollision {
    static List<EntityCollisionResult> checkCollision(Instance instance, BoundingBox boundingBox, Point point, Vec entityVelocity, double extendRadius, Function<Entity, Boolean> entityFilter, @Nullable PhysicsResult physicsResult) {
        double minimumRes = physicsResult != null ? physicsResult.res().res : Double.MAX_VALUE;

        List<EntityCollisionResult> result = new ArrayList<>();

        final double halfWidth = boundingBox.width() / 2;
        final double halfDepth = boundingBox.depth() / 2;
        final double maxDistance = Math.pow(boundingBox.height() * boundingBox.height() + halfDepth * halfDepth + halfWidth * halfWidth, 1 / 3.0);
        final double projectileDistance = entityVelocity.length();
        final var startPosition = point.asPos();

        for (Entity e : instance.getNearbyEntities(point, extendRadius + maxDistance + projectileDistance)) {
            if (!entityFilter.apply(e)) continue;
            if (!e.hasEntityCollision()) continue;

            final BoundingBox targetBoundingBox = e.getBoundingBox();
            final Point targetPosition = e.getPosition();

            // Overlapping with entity, math can't be done we return the entity
            if (targetBoundingBox.intersectBox(targetPosition.sub(point), boundingBox)) {
                result.add(new EntityCollisionResult(startPosition, e, Vec.ZERO, 0));
                continue;
            }

            // Check collisions with entity
            SweepResult sweepResult = new SweepResult(minimumRes, 0, 0, 0, null, 0, 0, 0, 0, 0, 0);
            boolean intersected = targetBoundingBox.intersectBoxSwept(point, entityVelocity, targetPosition, boundingBox, sweepResult);

            if (intersected && sweepResult.res < 1) {
                var p = startPosition.add(entityVelocity.mul(sweepResult.res));
                Vec direction = new Vec(sweepResult.collidedPositionX, sweepResult.collidedPositionY, sweepResult.collidedPositionZ);
                result.add(new EntityCollisionResult(p, e, direction, sweepResult.res));
            }
        }

        return result;
    }
}
