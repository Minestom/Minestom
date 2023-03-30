package net.minestom.server.instance;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

import static net.minestom.server.network.NetworkBuffer.SHORT;

public final class Section implements NetworkBuffer.Writer {
    private Palette blockPalette;
    private Palette biomePalette;
    private byte[] skyLight;
    private byte[] blockLight;

    private Section(Palette blockPalette, Palette biomePalette,
                    byte[] skyLight, byte[] blockLight) {
        this.blockPalette = blockPalette;
        this.biomePalette = biomePalette;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
    }

    public Section() {
        this(Palette.blocks(), Palette.biomes(),
                new byte[0], new byte[0]);
    }

    public Palette blockPalette() {
        return blockPalette;
    }

    public Palette biomePalette() {
        return biomePalette;
    }

    public byte[] getSkyLight() {
        return skyLight;
    }

    public void setSkyLight(byte[] skyLight) {
        this.skyLight = skyLight;
    }

    public byte[] getBlockLight() {
        return blockLight;
    }

    public void setBlockLight(byte[] blockLight) {
        this.blockLight = blockLight;
    }

    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
        this.skyLight = new byte[0];
        this.blockLight = new byte[0];
    }

    @Override
    public @NotNull Section clone() {
        return new Section(blockPalette.clone(), biomePalette.clone(),
                skyLight.clone(), blockLight.clone());
    }

    @Override
    public void write(@NotNull NetworkBuffer writer) {
        writer.write(SHORT, (short) blockPalette.count());
        writer.write(blockPalette);
        writer.write(biomePalette);
    }
}
