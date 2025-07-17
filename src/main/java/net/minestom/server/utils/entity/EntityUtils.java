package net.minestom.server.utils.entity;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class EntityUtils {
    private static final Set<EntityType> SITTING_ENTITIES = Set.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED,
            EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE,
            EntityType.ZOMBIFIED_PIGLIN);

    /**
     * @param vehicle the target vehicle
     * @param passenger the target passenger
     * @return the height offset for the passenger of this vehicle
     */
    public static double getPassengerHeightOffset(@NotNull Entity vehicle, @NotNull Entity passenger) {
        // TODO: Refactor this in 1.20.5
        if (vehicle.getEntityType().name().contains("boat")) return -0.1;
        if (vehicle.getEntityType() == EntityType.MINECART) return 0.0;
        if (SITTING_ENTITIES.contains(passenger.getEntityType()))
            return vehicle.getBoundingBox().height() * 0.75;
        return vehicle.getBoundingBox().height();
    }

    private EntityUtils() {
    }
}
