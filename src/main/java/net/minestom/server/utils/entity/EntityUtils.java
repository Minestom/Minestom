package net.minestom.server.utils.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkUtils;

public final class EntityUtils {

    private EntityUtils() {

    }

    public static boolean areVisible(Entity ent1, Entity ent2) {
        if (ent1.getInstance() == null || ent2.getInstance() == null)
            return false;
        if (!ent1.getInstance().equals(ent2.getInstance()))
            return false;

        final Chunk chunk = ent1.getInstance().getChunkAt(ent1.getPosition());

        long[] visibleChunksEntity = ChunkUtils.getChunksInRange(ent2.getPosition(), MinecraftServer.ENTITY_VIEW_DISTANCE);
        for (long visibleChunk : visibleChunksEntity) {
            final int chunkX = ChunkUtils.getChunkCoordX(visibleChunk);
            final int chunkZ = ChunkUtils.getChunkCoordZ(visibleChunk);

            if (chunk.getChunkX() == chunkX && chunk.getChunkZ() == chunkZ)
                return true;
        }

        return false;
    }

    public static boolean isOnGround(Entity entity) {
        final Instance instance = entity.getInstance();
        if (instance == null)
            return false;

        final Position entityPosition = entity.getPosition();

        // TODO: check entire bounding box
        final BlockPosition blockPosition = entityPosition.toBlockPosition().subtract(0, 1, 0);
        try {
            final short blockStateId = instance.getBlockStateId(blockPosition);
            final Block block = Block.fromStateId(blockStateId);
            return block.isSolid();
        } catch (NullPointerException e) {
            // Probably an entity at the border of an unloaded chunk
            return false;
        }
    }

}
