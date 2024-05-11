package net.minestom.server.instance;

import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static net.minestom.server.instance.light.LightCompute.contentFullyLit;
import static net.minestom.server.instance.light.LightCompute.emptyContent;
import static net.minestom.server.network.NetworkBuffer.SHORT;

public final class Section implements NetworkBuffer.Writer {
    private final Palette blockPalette;
    private final Palette biomePalette;
    private final Light skyLight;
    private final Light blockLight;

    private Section(Palette blockPalette, Palette biomePalette) {
        this.blockPalette = blockPalette;
        this.biomePalette = biomePalette;
        this.skyLight = Light.sky(blockPalette);
        this.blockLight = Light.block(blockPalette);
    }

    private Section(Palette blockPalette, Palette biomePalette, Light skyLight, Light blockLight) {
        this.blockPalette = blockPalette;
        this.biomePalette = biomePalette;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
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
        final Light skyLight = Light.sky(blockPalette);
        final Light blockLight = Light.block(blockPalette);

        setSkyLight(this.skyLight.array());
        setBlockLight(this.blockLight.array());

        return new Section(this.blockPalette.clone(), this.biomePalette.clone(), skyLight, blockLight);
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(SHORT, (short) blockPalette.count());
        writer.write(blockPalette);
        writer.write(biomePalette);
    }

    public void setSkyLight(byte[] copyArray) {
        if (copyArray == null || copyArray.length == 0) this.skyLight.set(emptyContent);
        else if (Arrays.equals(copyArray, emptyContent)) this.skyLight.set(emptyContent);
        else if (Arrays.equals(copyArray, contentFullyLit)) this.skyLight.set(contentFullyLit);
        else this.skyLight.set(copyArray);
    }

    public void setBlockLight(byte[] copyArray) {
        if (copyArray == null || copyArray.length == 0) this.blockLight.set(emptyContent);
        else if (Arrays.equals(copyArray, emptyContent)) this.blockLight.set(emptyContent);
        else if (Arrays.equals(copyArray, contentFullyLit)) this.blockLight.set(contentFullyLit);
        else this.blockLight.set(copyArray);
    }

    public Light skyLight() {
        return skyLight;
    }

    public Light blockLight() {
        return blockLight;
    }
}
