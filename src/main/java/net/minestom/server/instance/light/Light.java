package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
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

    int getLevel(int x, int y, int z);

    void invalidate();

    boolean requiresUpdate();

    void set(byte[] copyArray);

    @ApiStatus.Internal
    Set<Point> calculateInternal(Palette blockPalette,
                                 int chunkX, int chunkY, int chunkZ,
                                 int[] heightmap, int maxY,
                                 LightLookup lightLookup);

    @ApiStatus.Internal
    Set<Point> calculateExternal(Palette blockPalette,
                                 Point[] neighbors,
                                 LightLookup lightLookup,
                                 PaletteLookup paletteLookup);

    @ApiStatus.Internal
    static Point[] getNeighbors(Chunk chunk, int sectionY) {
        final int chunkX = chunk.getChunkX();
        final int chunkZ = chunk.getChunkZ();

        final BlockFace[] faces = BlockFace.getValues();

        Point[] links = new Vec[faces.length];
        for (BlockFace face : faces) {
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

    @FunctionalInterface
    interface LightLookup {
        Light light(int x, int y, int z);
    }

    @FunctionalInterface
    interface PaletteLookup {
        Palette palette(int x, int y, int z);
    }
}
