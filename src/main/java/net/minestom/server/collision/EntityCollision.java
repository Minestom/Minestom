package net.minestom.server.collision;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.utils.block.BlockIterator;

final class EntityCollision {
    public static Entity checkCollision(Entity entity, Vec entityVelocity, double extendRadius) {
        if (entity.getInstance() == null) return null;

        SweepResult sweepResult = new SweepResult(Double.MAX_VALUE, 0, 0, 0, null);

        double closestDistance = Double.MAX_VALUE;
        Entity closestEntity = null;

        // var boundingBox = entity.getBoundingBox();
        var boundingBox = new BoundingBox(0.00, 0.00, 0.00);
        var maxDistance = Math.pow(boundingBox.height() * boundingBox.height() + boundingBox.depth()/2 * boundingBox.depth()/2 + boundingBox.width()/2 * boundingBox.width()/2, 1/3.0);

        BlockIterator iterator = new BlockIterator(Vec.fromPoint(entity.getPosition()), entityVelocity, 0, entityVelocity.length());

        while (iterator.hasNext()) {
            var pos = iterator.next();

            for (Entity e : entity.getInstance().getNearbyEntities(pos, extendRadius + maxDistance)) {
                if (e == entity) continue;

                // Overlapping with entity, math can't be done we return the entity
                if (e.getBoundingBox().intersectBox(entity.getPosition().sub(e.getPosition()), entity.getBoundingBox())) {
                    return e;
                }

                // Check collisions with entity
                e.getBoundingBox().intersectBoxSwept(entity.getPosition(), entityVelocity, e.getPosition(), boundingBox, sweepResult);

                if (sweepResult.res < closestDistance && sweepResult.res < 1) {
                    closestDistance = sweepResult.res;
                    closestEntity = e;
                }
            }
        }

        return closestEntity;
    }
}
