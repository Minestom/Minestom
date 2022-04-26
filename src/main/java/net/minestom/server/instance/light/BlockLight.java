package net.minestom.server.instance.light;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.SectionLinkManager;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

final class BlockLight implements Light {
    private final Palette blockPalette;
    private volatile byte[] content;
    private volatile byte[][] borders;

    BlockLight(Palette blockPalette) {
        this.blockPalette = blockPalette;
    }

    @Override
    public byte @NotNull [] array(Instance instance, Section section) {
        byte[] content = this.content;
        if (content == null) {
            synchronized (this) {
                content = this.content;
                if (content == null) {
                    var result = BlockLightCompute.compute(blockPalette, instance.getSectionManager().getNeighbors(section));
                    this.content = content = result.light();
                    this.borders = result.borders();
                }
            }
        }
        return content.clone();
    }

    public byte[][] getBorders() {
        return borders;
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
