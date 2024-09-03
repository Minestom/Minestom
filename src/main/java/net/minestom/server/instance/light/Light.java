package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

public interface Light {
    static Light sky() {
        return new SkyLight();
    }

    static Light block() {
        return new BlockLight();
    }

    boolean requiresSend();

    @ApiStatus.Internal
    byte[] array();

    void flip();

    @ApiStatus.Internal
    Set<Point> calculateExternal(Instance instance, Chunk chunk, int sectionY, Palette blockPalette);

    int getLevel(int x, int y, int z);

    @ApiStatus.Internal
    Set<Point> calculateInternal(Instance instance, int chunkX, int chunkY, int chunkZ, Palette blockPalette);

    void invalidate();

    boolean requiresUpdate();

    void set(byte[] copyArray);

    @ApiStatus.Internal
    static Point[] getNeighbors(Chunk chunk, int sectionY) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();

        Point[] links = new Vec[BlockFace.values().length];
        for (BlockFace face : BlockFace.values()) {
            final Direction direction = face.toDirection();
            final int x = chunkX + direction.normalX();
            final int z = chunkZ + direction.normalZ();
            final int y = sectionY + direction.normalY();

            Chunk foundChunk = chunk.getInstance().getChunk(x, z);
            if (foundChunk == null) continue;
            if (y - foundChunk.getMinSection() > foundChunk.getMaxSection() || y - foundChunk.getMinSection() < 0)
                continue;

            links[face.ordinal()] = new Vec(foundChunk.getChunkX(), y, foundChunk.getChunkZ());
        }
        return links;
    }
}
