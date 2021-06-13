package net.minestom.server.utils.entity;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.world.Chunk;
import net.minestom.server.world.World;
import net.minestom.server.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class EntityUtils {

    private EntityUtils() {

    }

    public static void forEachRange(@NotNull World world, @NotNull Position position,
                                    int viewDistance,
                                    @NotNull Consumer<Entity> consumer) {
        final long[] chunksInRange = ChunkUtils.getChunksInRange(position, viewDistance);

        for (long chunkIndex : chunksInRange) {
            final int chunkX = ChunkUtils.getChunkCoordX(chunkIndex);
            final int chunkZ = ChunkUtils.getChunkCoordZ(chunkIndex);
            final Chunk chunk = world.getChunk(chunkX, chunkZ);
            if (chunk == null)
                continue;
            world.getChunkEntities(chunk).forEach(consumer);
        }
    }

    public static boolean areVisible(@NotNull Entity ent1, @NotNull Entity ent2) {
        if (ent1.getWorld() == null || ent2.getWorld() == null)
            return false;
        if (!ent1.getWorld().equals(ent2.getWorld()))
            return false;

        final Chunk chunk = ent1.getWorld().getChunkAt(ent1.getPosition());

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
        final Chunk chunk = entity.getChunk();
        if (chunk == null)
            return false;

        final Position entityPosition = entity.getPosition();

        // TODO: check entire bounding box
        final BlockPosition blockPosition = entityPosition.toBlockPosition().subtract(0, 1, 0);
        try {
            final Block block = chunk.getBlock(blockPosition.getX(),
                    blockPosition.getY(),
                    blockPosition.getZ());
            return block.isSolid();
        } catch (NullPointerException e) {
            // Probably an entity at the border of an unloaded chunk
            return false;
        }
    }

}
