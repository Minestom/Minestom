package net.minestom.server.instance;

import net.minestom.server.instance.light.OldLight;
import net.minestom.server.instance.palette.Palette;

public final class Section {
    private final Palette blockPalette;
    private final Palette biomePalette;
    private final OldLight skyLight;
    private final OldLight blockLight;

    private Section(Palette blockPalette, Palette biomePalette, OldLight skyLight, OldLight blockLight) {
        this.blockPalette = blockPalette;
        this.biomePalette = biomePalette;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
    }

    private Section(Palette blockPalette, Palette biomePalette) {
        this(blockPalette, biomePalette, OldLight.sky(), OldLight.block());
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

    public OldLight skyLight() {
        return skyLight;
    }

    public OldLight blockLight() {
        return blockLight;
    }
}
