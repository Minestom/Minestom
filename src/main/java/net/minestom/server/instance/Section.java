package net.minestom.server.instance;

import net.minestom.server.instance.light.OldLight;
import net.minestom.server.instance.palette.Palette;

public record Section(Palette blockPalette, Palette biomePalette, OldLight skyLight, OldLight blockLight) {
    public Section(Palette blockPalette, Palette biomePalette) {
        this(blockPalette, biomePalette, OldLight.sky(), OldLight.block());
    }

    public Section() {
        this(Palette.blocks(), Palette.biomes());
    }

    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
    }

    public void invalidate() {
        this.skyLight.invalidate();
        this.blockLight.invalidate();
    }

    @Override
    public Section clone() {
        final OldLight skyLight = OldLight.sky();
        final OldLight blockLight = OldLight.block();

        skyLight.set(this.skyLight.array());
        blockLight.set(this.blockLight.array());

        return new Section(this.blockPalette.clone(), this.biomePalette.clone(), skyLight, blockLight);
    }

    public void setSkyLight(byte[] copyArray) {
        this.skyLight.set(copyArray);
    }

    public void setBlockLight(byte[] copyArray) {
        this.blockLight.set(copyArray);
    }
}
