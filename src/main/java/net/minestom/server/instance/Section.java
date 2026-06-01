package net.minestom.server.instance;

import net.minestom.server.instance.palette.Palette;

public record Section(Palette blockPalette, Palette biomePalette) {

    public Section() {
        this(Palette.blocks(), Palette.biomes());
    }

    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
    }

    @Override
    public Section clone() {
        return new Section(this.blockPalette.clone(), this.biomePalette.clone());
    }
}
