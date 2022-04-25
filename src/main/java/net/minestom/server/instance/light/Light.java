package net.minestom.server.instance.light;

import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

public interface Light {
    static Light sky(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

    static Light block(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

    void copyFrom(byte @NotNull [] array);

    byte[] bake();

    void applyPropagations(Instance instance, Chunk chunk, int sectionY);

    byte[] getBorderPropagation(BlockFace oppositeFace);

    void invalidatePropagation();

    int getLevel(int x, int y, int z);

    void invalidate(Instance instance, int chunkX, int chunkY, int chunkZ);
}
