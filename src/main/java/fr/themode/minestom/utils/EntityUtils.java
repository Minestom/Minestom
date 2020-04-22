package fr.themode.minestom.utils;

import fr.themode.minestom.MinecraftServer;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.instance.Chunk;
import fr.themode.minestom.instance.Instance;
import fr.themode.minestom.instance.block.Block;

public class EntityUtils {

    public static boolean areVisible(Entity ent1, Entity ent2) {
        if (ent1.getInstance() == null || ent2.getInstance() == null)
            return false;
        if (!ent1.getInstance().equals(ent2.getInstance()))
            return false;

        Chunk chunk = ent1.getInstance().getChunkAt(ent1.getPosition());

        long[] visibleChunksEntity = ChunkUtils.getChunksInRange(ent2.getPosition(), MinecraftServer.ENTITY_VIEW_DISTANCE);
        for (long visibleChunk : visibleChunksEntity) {
            int[] chunkPos = ChunkUtils.getChunkCoord(visibleChunk);
            int chunkX = chunkPos[0];
            int chunkZ = chunkPos[1];
            if (chunk.getChunkX() == chunkX && chunk.getChunkZ() == chunkZ)
                return true;
        }

        return false;
    }

    public static boolean isOnGround(Entity entity) {
        Instance instance = entity.getInstance();
        if (instance == null)
            return false;

        Position entityPosition = entity.getPosition();

        BlockPosition blockPosition = entityPosition.toBlockPosition();
        blockPosition = blockPosition.subtract(0, 1, 0);
        try {
            short blockId = instance.getBlockId(blockPosition);
            Block block = Block.fromId(blockId);
            return block.isSolid();
        } catch (NullPointerException e) {
            // Probably an entity at the border of an unloaded chunk
            return false;
        }
    }

}
