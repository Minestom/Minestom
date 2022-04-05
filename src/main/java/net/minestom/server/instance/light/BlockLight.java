package net.minestom.server.instance.light;

import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

final class BlockLight implements Light {
    private final Palette blockPalette;
    private volatile byte[] content;

    BlockLight(Palette blockPalette) {
        this.blockPalette = blockPalette;
    }

    @Override
    public byte @NotNull [] array() {
        byte[] content = this.content;
        if (content == null) {
            synchronized (this) {
                content = this.content;
                if (content == null) {
                    var result = BlockLightCompute.compute(blockPalette);
                    this.content = content = result.light();
                }
            }
        }
        return content.clone();
    }

    @Override
    public void copyFrom(byte @NotNull [] array) {
        this.content = array.clone();
    }

    @Override
    public void invalidate() {
        this.content = null;
    }
}
