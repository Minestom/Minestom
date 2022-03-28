package net.minestom.server.instance.light;

import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

final class BlockLight implements Light {
    private final Palette blockPalette;

    private byte[] content;
    private volatile boolean updated;

    BlockLight(Palette blockPalette) {
        this.blockPalette = blockPalette;
    }

    @Override
    public byte[] array() {
        if (!updated) {
            updated = true;
            var result = BlockLightCompute.compute(blockPalette);
            return (this.content = result.light());
        }
        return content.clone();
    }

    @Override
    public void copyFrom(byte[] array) {
        this.content = array.clone();
    }

    @Override
    public void invalidate() {
        this.updated = false;
    }

    @Override
    public @NotNull Light clone() {
        BlockLight light = new BlockLight(blockPalette);
        light.content = this.content.clone();
        return light;
    }
}
