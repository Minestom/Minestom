package net.minestom.server.instance;

import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.palette.Palette;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.minestom.server.instance.light.LightCompute.CONTENT_FULLY_LIT;
import static net.minestom.server.instance.light.LightCompute.EMPTY_CONTENT;

public final class Section {
    private final Palette blockPalette;
    private final Palette biomePalette;
    private final Light skyLight;
    private final Light blockLight;

    private Section(Palette blockPalette, Palette biomePalette, Light skyLight, Light blockLight) {
        this.blockPalette = blockPalette;
        this.biomePalette = biomePalette;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
    }

    private Section(Palette blockPalette, Palette biomePalette) {
        this(blockPalette, biomePalette, Light.sky(), Light.block());
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
        final Light skyLight = Light.sky();
        final Light blockLight = Light.block();

        skyLight.set(this.skyLight.array());
        blockLight.set(this.blockLight.array());

        return new Section(this.blockPalette.clone(), this.biomePalette.clone(), skyLight, blockLight);
    }

    public void setSkyLight(byte[] copyArray) {
        if (copyArray == null || copyArray.length == 0) this.skyLight.set(EMPTY_CONTENT);
        else if (Arrays.equals(copyArray, EMPTY_CONTENT)) this.skyLight.set(EMPTY_CONTENT);
        else if (Arrays.equals(copyArray, CONTENT_FULLY_LIT)) this.skyLight.set(CONTENT_FULLY_LIT);
        else this.skyLight.set(copyArray);
    }

    public void setBlockLight(byte[] copyArray) {
        if (copyArray == null || copyArray.length == 0) this.blockLight.set(EMPTY_CONTENT);
        else if (Arrays.equals(copyArray, EMPTY_CONTENT)) this.blockLight.set(EMPTY_CONTENT);
        else if (Arrays.equals(copyArray, CONTENT_FULLY_LIT)) this.blockLight.set(CONTENT_FULLY_LIT);
        else this.blockLight.set(copyArray);
    }

    public Light skyLight() {
        return skyLight;
    }

    public Light blockLight() {
        return blockLight;
    }
}
