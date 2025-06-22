package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Light {
    /**
     * @return a new skylight instance
     */
    static Light sky() {
        return new SkyLight();
    }

    /**
     * @return a new block-light instance
     */
    static Light block() {
        return new BlockLight();
    }

    /**
     * Check if this light (section) needs to be resent to clients because something changed.
     * Calling this method clears the {@code requiresSend} status
     *
     * @return whether this light (section) needs to be resent
     */
    boolean requiresSend();

    /**
     * @return the underlying array which is used in the packet. This usually bakes the array, which is computationally expensive.
     */
    @ApiStatus.Internal
    byte @NotNull [] array();

    void flip();

    /**
     * Get the light level at some position
     *
     * @param x x position, 0-15
     * @param y y position, 0-15
     * @param z z position, 0-15
     * @return the light level at the given position
     */
    int getLevel(int x, int y, int z);

    /**
     * Invalidates this light.
     * The next time this light is used, it will recalculate.
     */
    void invalidate();

    /**
     * @return whether this light (section) needs to be recalculated
     */
    boolean requiresUpdate();

    /**
     * Sets the light to some data. This is usually used by initializers to set initial values.
     * This light will likely change when the light is invalidated and recalculated
     *
     * @param copyArray the data to set
     */
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

    @FunctionalInterface
    interface LightLookup {
        @Nullable Light light(int x, int y, int z);
    }

    @FunctionalInterface
    interface PaletteLookup {
        @Nullable Palette palette(int x, int y, int z);
    }
}
