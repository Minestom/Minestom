package net.minestom.server.instance;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public final class Section implements Writeable {
    private Palette blockPalette;
    private Palette biomePalette;

    private Section(Palette blockPalette, Palette biomePalette) {
        this.blockPalette = blockPalette;
        this.biomePalette = biomePalette;
    }

    public Section() {
        this(Palette.blocks(), Palette.biomes());
    }

    public Palette blockPalette() {
        return blockPalette;
    }

    public Palette biomePalette() {
        return biomePalette;
    }

    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
    }

    @Override
    public @NotNull Section clone() {
        return new Section(blockPalette.clone(), biomePalette.clone());
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeShort((short) blockPalette.count());
        writer.write(blockPalette);
        writer.write(biomePalette);
    }

    public boolean hasOnlyAir() {
        return blockPalette.count() == 0;
    }
}
