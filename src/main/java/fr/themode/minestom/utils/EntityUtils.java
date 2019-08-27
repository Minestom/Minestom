package fr.themode.minestom.utils;

import fr.themode.minestom.Main;
import fr.themode.minestom.entity.Entity;
import fr.themode.minestom.instance.Chunk;

public class EntityUtils {

    public static boolean areVisible(Entity ent1, Entity ent2) {
        if (ent1.getInstance() == null || ent2.getInstance() == null)
            return false;
        if (!ent1.getInstance().equals(ent2.getInstance()))
            return false;

        Chunk chunk = ent1.getInstance().getChunkAt(ent1.getPosition());

        long[] visibleChunksEntity = ChunkUtils.getChunksInRange(ent2.getPosition(), Main.ENTITY_VIEW_DISTANCE);
        for (long visibleChunk : visibleChunksEntity) {
            int[] chunkPos = ChunkUtils.getChunkCoord(visibleChunk);
            int chunkX = chunkPos[0];
            int chunkZ = chunkPos[1];
            if (chunk.getChunkX() == chunkX && chunk.getChunkZ() == chunkZ)
                return true;
        }

        return false;
    }

}
