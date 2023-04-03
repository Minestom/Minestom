package net.minestom.server.collision;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;

final class EntityCollision {
    public static Entity checkCollision(Entity entity, Vec entityVelocity, double extendRadius) {
        if (entity.getInstance() == null) return null;

        SweepResult sweepResult = new SweepResult(Double.MAX_VALUE, 0, 0, 0, null);

        double closestDistance = Double.MAX_VALUE;
        Entity closestEntity = null;

        var boundingBox = entity.getBoundingBox();
        var maxDistance = Math.pow(boundingBox.height() * boundingBox.height() + boundingBox.depth()/2 * boundingBox.depth()/2 + boundingBox.width()/2 * boundingBox.width()/2, 1/3.0);

        for (Entity e : entity.getInstance().getNearbyEntities(entity.getPosition(), extendRadius + maxDistance)) {
            if (e == entity) continue;

            // Overlapping with entity, math can't be done we return the entity
            if (entity.getBoundingBox().intersectBox(entity.getPosition().sub(e.getPosition()), e.getBoundingBox())) {
                return e;
            }

            // Check collisions with entity
            e.getBoundingBox().intersectBoxSwept(entity.getPosition(), entityVelocity, e.getPosition(), boundingBox, sweepResult);

            if (sweepResult.res < closestDistance) {
                closestDistance = sweepResult.res;
                closestEntity = e;
            }
        }

        return closestEntity;
    }
}
