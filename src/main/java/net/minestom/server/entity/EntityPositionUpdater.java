package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;

@ApiStatus.Internal
public final class EntityPositionUpdater {
    public static void update(Instance instance) {
        // Tick in priority entities without vehicles
        Set<Entity> entities = new HashSet<>(instance.getEntities());
        while (!entities.isEmpty()) {
            Set<Entity> toRemove = new HashSet<>(entities);
            for (Entity entity : entities) {
                final Entity vehicle = entity.vehicle;
                assert vehicle != entity : "vehicle shouldn't be itself";
                if (entity instanceof Player && !(vehicle instanceof Player)) {
                    toRemove.add(entity);
                } else {
                    if (vehicle == null || (!entities.contains(vehicle) || toRemove.contains(vehicle))) {
                        toRemove.add(entity);
                    }
                }
            }
            entities.removeAll(toRemove);
            for (Entity entity : toRemove) {
                final Pos currentPosition = entity.getPosition();
                final Pos nextPosition = entity.movementTick();
                if (!currentPosition.equals(nextPosition)) {
                    entity.nextPosition = nextPosition;
                    entity.updatedPosition(currentPosition, nextPosition);
                }
            }
        }
    }
}
