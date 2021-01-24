package net.minestom.server.utils.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class EntityUtils {

    private EntityUtils() {

    }

    public static void forEachRange(@NotNull Instance instance, @NotNull Position position,
                                    int viewDistance,
                                    @NotNull Consumer<Entity> consumer) {
        final long[] chunksInRange = ChunkUtils.getChunksInRange(position, viewDistance);

        for (long chunkIndex : chunksInRange) {
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
            final Chunk chunk = instance.getChunk(chunkX, chunkZ);
            if (chunk == null)
                continue;
            instance.getChunkEntities(chunk).forEach(consumer::accept);
        }
    }

    public static boolean areVisible(@NotNull Entity ent1, @NotNull Entity ent2) {
        if (ent1.getInstance() == null || ent2.getInstance() == null)
            return false;
        if (!ent1.getInstance().equals(ent2.getInstance()))
            return false;

        final Chunk chunk = ent1.getInstance().getChunkAt(ent1.getPosition());

        final long[] visibleChunksEntity = ChunkUtils.getChunksInRange(ent2.getPosition(), MinecraftServer.getEntityViewDistance());
        for (long visibleChunk : visibleChunksEntity) {
            final int chunkX = ChunkUtils.getChunkCoordX(visibleChunk);
            final int chunkZ = ChunkUtils.getChunkCoordZ(visibleChunk);
            if (chunk.getChunkX() == chunkX && chunk.getChunkZ() == chunkZ)
                return true;
        }

        return false;
    }

    public static boolean isOnGround(@NotNull Entity entity) {
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
