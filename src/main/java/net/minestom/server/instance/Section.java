package net.minestom.server.instance;

import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.palette.Palette;

public record Section(Palette blockPalette, Palette biomePalette, Light skyLight, Light blockLight) {
    public Section(Palette blockPalette, Palette biomePalette) {
        this(blockPalette, biomePalette, Light.sky(), Light.block());
    }

    public Section() {
        this(Palette.blocks(), Palette.biomes());
    }

    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
    }

    @Override
    public Section clone() {
        final Light skyLight = Light.sky();
        final Light blockLight = Light.block();

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
