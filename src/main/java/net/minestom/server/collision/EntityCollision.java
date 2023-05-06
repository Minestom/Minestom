package net.minestom.server.collision;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.block.BlockIterator;

final class EntityCollision {
    public static PhysicsResult checkCollision(Entity entity, Point point, Vec entityVelocity, double extendRadius, PhysicsResult res) {
        double minimumRes = res != null ? res.percentage() : Double.MAX_VALUE;

        if (entity.getInstance() == null) return null;
        SweepResult sweepResult = new SweepResult(minimumRes, 0, 0, 0, null);

        double closestDistance = minimumRes;
        Entity closestEntity = null;

        var boundingBox = entity.getBoundingBox();
        var maxDistance = Math.pow(boundingBox.height() * boundingBox.height() + boundingBox.depth()/2 * boundingBox.depth()/2 + boundingBox.width()/2 * boundingBox.width()/2, 1/3.0);

        BlockIterator iterator = new BlockIterator(Vec.fromPoint(point), entityVelocity, 0, entityVelocity.length());

        while (iterator.hasNext()) {
            var pos = iterator.next();

            for (Entity e : entity.getInstance().getNearbyEntities(pos, extendRadius + maxDistance)) {
                if (e == entity) continue;

                // Overlapping with entity, math can't be done we return the entity
                if (e.getBoundingBox().intersectBox(point.sub(e.getPosition()), entity.getBoundingBox())) {
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
                            0);
                }

                // Check collisions with entity
                e.getBoundingBox().intersectBoxSwept(point, entityVelocity, e.getPosition(), boundingBox, sweepResult);

                if (sweepResult.res < closestDistance && sweepResult.res < 1) {
                    closestDistance = sweepResult.res;
                    closestEntity = e;
                }
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
                sweepResult.res
        );
    }
}
