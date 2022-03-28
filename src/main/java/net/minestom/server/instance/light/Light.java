package net.minestom.server.instance.light;

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
    byte @NotNull [] array();

    void copyFrom(byte @NotNull [] array);

    void invalidate();
}
