package net.minestom.server.utils.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
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
        if (vehicle.getEntityType() == EntityType.BOAT) return -0.1;
        if (vehicle.getEntityType() == EntityType.MINECART) return 0.0;
        if (SITTING_ENTITIES.contains(passenger.getEntityType()))
            return vehicle.getBoundingBox().height() * 0.75;
        return vehicle.getBoundingBox().height();
    }

    private EntityUtils() {
    }

    public static boolean isOnGround(@NotNull Entity entity) {
        final Chunk chunk = entity.getChunk();
        if (chunk == null)
            return false;
        final Pos entityPosition = entity.getPosition();
        // TODO: check entire bounding box
        try {
            final Block block;
            synchronized (chunk) {
                block = chunk.getBlock(entityPosition.sub(0, 1, 0));
            }
            return block.isSolid();
        } catch (NullPointerException e) {
            // Probably an entity at the border of an unloaded chunk
            return false;
        }
    }
}
