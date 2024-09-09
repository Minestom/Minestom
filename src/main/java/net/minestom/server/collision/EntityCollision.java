package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

final class EntityCollision {
    static @NotNull List<EntityCollisionResult> checkCollision(@NotNull Instance instance, @NotNull BoundingBox boundingBox, @NotNull Point point, @NotNull Vec entityVelocity, double extendRadius, @NotNull Function<Entity, Boolean> entityFilter, @Nullable PhysicsResult physicsResult) {
        double minimumRes = physicsResult != null ? physicsResult.res().res : Double.MAX_VALUE;

        List<EntityCollisionResult> result = new ArrayList<>();

        var maxDistance = Math.pow(boundingBox.height() * boundingBox.height() + boundingBox.depth()/2 * boundingBox.depth()/2 + boundingBox.width()/2 * boundingBox.width()/2, 1/3.0);
        double projectileDistance = entityVelocity.length();

        for (Entity e : instance.getNearbyEntities(point, extendRadius + maxDistance + projectileDistance)) {
            SweepResult sweepResult = new SweepResult(minimumRes, 0, 0, 0, null, 0, 0, 0, 0, 0, 0);

            if (!entityFilter.apply(e)) continue;
            if (!e.hasEntityCollision()) continue;

            // Overlapping with entity, math can't be done we return the entity
            if (e.getBoundingBox().intersectBox(e.getPosition().sub(point), boundingBox)) {
                var p = Pos.fromPoint(point);
                result.add(new EntityCollisionResult(p, e, Vec.ZERO, 0));
                continue;
            }

            // Check collisions with entity
            boolean intersected = e.getBoundingBox().intersectBoxSwept(point, entityVelocity, e.getPosition(), boundingBox, sweepResult);

            if (intersected && sweepResult.res < 1) {
                var p = Pos.fromPoint(point).add(entityVelocity.mul(sweepResult.res));
                Vec direction = new Vec(sweepResult.collidedPositionX, sweepResult.collidedPositionY, sweepResult.collidedPositionZ);
                result.add(new EntityCollisionResult(p, e, direction, sweepResult.res));
            }
        }

        return result;
    }
}
