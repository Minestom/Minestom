package net.minestom.server.instance.light;

import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface Light {
    static Light sky(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

    static Light block(@NotNull Palette blockPalette) {
        return new BlockLight(blockPalette);
    }

    @ApiStatus.Internal
    byte @NotNull [] array(Section section);

    void copyFrom(byte @NotNull [] array);

    void invalidate();
}
