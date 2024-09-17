package net.minestom.server.entity;

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
                entity.nextPosition = entity.movementTick();
            }
        }
    }
}
