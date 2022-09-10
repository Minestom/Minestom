package net.minestom.server.utils.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

public final class EntityUtils {

    private EntityUtils() {
    }

    public static boolean isOnGround(@NotNull Entity entity) {
        Instance instance = entity.getInstance();
        Check.notNull(instance, "Cannot check if an entity is on ground if it is not in an instance");
        if (!instance.isChunkLoaded(entity.getPosition()))
            return false;
        final Pos entityPosition = entity.getPosition();
        // TODO: check entire bounding box
        try {
            final Block block = instance.getBlock(entityPosition.sub(0, 1, 0));
            return block.isSolid();
        } catch (NullPointerException e) {
            // Probably an entity at the border of an unloaded chunk
            return false;
        }
    }
}
