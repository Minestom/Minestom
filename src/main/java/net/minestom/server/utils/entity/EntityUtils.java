package net.minestom.server.utils.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public final class EntityUtils {

    private EntityUtils() {
    }

    public static boolean isOnGround(@NotNull Entity entity) {
        final Chunk chunk = entity.getChunk();
        if (chunk == null)
            return false;
        final Pos entityPosition = entity.getPosition();
        // TODO: check entire bounding box
        try {
            final Block block = chunk.getBlock(entityPosition.sub(0, 1, 0));
            return block.isSolid();
        } catch (NullPointerException e) {
            // Probably an entity at the border of an unloaded chunk
            return false;
        }
    }
}
