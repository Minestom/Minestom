package net.minestom.server.utils.entity;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityQuery;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class EntityUtils {
    private static final Set<EntityType> SITTING_ENTITIES = Set.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED,
            EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE,
            EntityType.ZOMBIFIED_PIGLIN);

    /**
     * @param vehicle   the target vehicle
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

    public static boolean validateQuery(Entity entity, Point trackedPosition, EntityQuery query, Point origin) {
        for (EntityQuery.Condition<Object> condition : query.conditions()) {
            final Object propertyValue = queryPropertyValue(entity, trackedPosition, origin, condition.property());
            if (propertyValue == null || !condition.test(origin, propertyValue)) return false;
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    public static Object queryPropertyValue(Entity entity, Point pos, Point origin, EntityQuery.Property property) {
        if (property == EntityQuery.PLAYER) return entity instanceof Player;
        if (property == EntityQuery.ID) return entity.getEntityId();
        if (property == EntityQuery.UUID) return entity.getUuid();
        if (property == EntityQuery.COORD) return pos;
        if (property == EntityQuery.POS_X) return pos.x();
        if (property == EntityQuery.POS_Y) return pos.y();
        if (property == EntityQuery.POS_Z) return pos.z();
        if (property == EntityQuery.YAW) return entity.getPosition().yaw();
        if (property == EntityQuery.PITCH) return entity.getPosition().pitch();
        if (property == EntityQuery.CHUNK_X) return pos.chunkX();
        if (property == EntityQuery.CHUNK_Z) return pos.chunkZ();
        if (property == EntityQuery.DISTANCE) return pos.distance(origin);
        if (property == EntityQuery.TYPE) return entity.getEntityType();
        if (property == EntityQuery.GAME_MODE) return entity instanceof Player player ? player.getGameMode() : null;
        if (property == EntityQuery.EXPERIENCE) return entity instanceof Player player ? player.getExp() : null;

        throw new IllegalArgumentException("Unknown property: " + property);
    }

    private EntityUtils() {
    }
}
