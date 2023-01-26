package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface Light {
    static Light sky(@NotNull Palette blockPalette) {
        return new SkyLight(blockPalette);
    }

    static Light block(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

    boolean requiresSend();

    @ApiStatus.Internal
    byte[] array();

    Set<Point> flip();

    void copyFrom(byte @NotNull [] array);

    @ApiStatus.Internal
    Light calculateExternal(Instance instance, Chunk chunk, int sectionY);

    @ApiStatus.Internal
    byte[] getBorderPropagation(BlockFace oppositeFace);

    @ApiStatus.Internal
    void invalidatePropagation();

    int getLevel(int x, int y, int z);

    @ApiStatus.Internal
    Light calculateInternal(Instance instance, int chunkX, int chunkY, int chunkZ);

    void invalidate();

    boolean requiresUpdate();

    void set(byte[] copyArray);

    @ApiStatus.Internal
    static Map<BlockFace, Point> getNeighbors(Chunk chunk, int sectionY) {
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();

        Map<BlockFace, Point> links = new HashMap<>();

        for (BlockFace face : BlockFace.values()) {
            Direction direction = face.toDirection();
            int x = chunkX + direction.normalX();
            int z = chunkZ + direction.normalZ();
            int y = sectionY + direction.normalY();

            Chunk foundChunk = chunk.getInstance().getChunk(x, z);

            if (foundChunk == null) continue;
            if (y - foundChunk.getMinSection() > foundChunk.getMaxSection() || y - foundChunk.getMinSection() < 0) continue;

            links.put(face, new Vec(foundChunk.getChunkX(), y, foundChunk.getChunkZ()));
        }

        return links;
    }
}
