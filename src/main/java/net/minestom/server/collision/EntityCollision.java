package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import java.util.function.Function;

final class EntityCollision {
    public static PhysicsResult checkCollision(Instance instance, BoundingBox boundingBox, Point point, Vec entityVelocity, double extendRadius, Function<Entity, Boolean> entityFilter, PhysicsResult res) {
        double minimumRes = res != null ? res.res().res : Double.MAX_VALUE;

        if (instance == null) return null;
        SweepResult sweepResult = new SweepResult(minimumRes, 0, 0, 0, null, 0, 0, 0);

        double closestDistance = minimumRes;
        Entity closestEntity = null;

        var maxDistance = Math.pow(boundingBox.height() * boundingBox.height() + boundingBox.depth()/2 * boundingBox.depth()/2 + boundingBox.width()/2 * boundingBox.width()/2, 1/3.0);
        double projectileDistance = entityVelocity.length();

        for (Entity e : instance.getNearbyEntities(point, extendRadius + maxDistance + projectileDistance)) {
            if (!entityFilter.apply(e)) continue;
            if (!e.hasCollision()) continue;

            // Overlapping with entity, math can't be done we return the entity
            if (e.getBoundingBox().intersectBox(e.getPosition().sub(point), boundingBox)) {
                var p = Pos.fromPoint(point);

                return new PhysicsResult(p,
                        Vec.ZERO,
                        false,
                        true,
                        true,
                        true,
                        entityVelocity,
                        new Pos[] {p, p, p},
                        new Shape[] {e, e, e},
                        true,
                        sweepResult);
            }

            // Check collisions with entity
            e.getBoundingBox().intersectBoxSwept(point, entityVelocity, e.getPosition(), boundingBox, sweepResult);

            if (sweepResult.res < closestDistance && sweepResult.res < 1) {
                closestDistance = sweepResult.res;
                closestEntity = e;
            }
        }

        Pos[] collisionPoints = new Pos[3];

        return new PhysicsResult(Pos.fromPoint(point).add(entityVelocity.mul(closestDistance)),
                Vec.ZERO,
                sweepResult.normalY == -1,
                sweepResult.normalX != 0,
                sweepResult.normalY != 0,
                sweepResult.normalZ != 0,
                entityVelocity,
                collisionPoints,
                new Shape[] {closestEntity, closestEntity, closestEntity},
                sweepResult.normalX != 0 || sweepResult.normalZ != 0 || sweepResult.normalY != 0,
                sweepResult
        );
    }
}
