package net.minestom.server.instance.light;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface Light {
    static Light sky(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

    static Light block(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

    @ApiStatus.Internal
    byte[] array();

    void copyFrom(byte @NotNull [] array);

    void applyPropagations(Instance instance, Chunk chunk, int sectionY);

    byte[] getBorderPropagation(BlockFace oppositeFace);

    void invalidatePropagation();

    int getLevel(int x, int y, int z);

    void invalidate(Instance instance, int chunkX, int chunkY, int chunkZ);
}
