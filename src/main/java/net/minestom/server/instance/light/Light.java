package net.minestom.server.instance.light;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Light {
    static Light sky(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

    static Light block(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

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
}
