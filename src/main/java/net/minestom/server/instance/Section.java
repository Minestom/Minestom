package net.minestom.server.instance;

import net.minestom.server.instance.light.Light;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.binary.BinaryWriter;
import net.minestom.server.utils.binary.Writeable;
import org.jetbrains.annotations.NotNull;

public record Section(Palette blockPalette, Palette biomePalette,
                      Light skyLight, Light blockLight) implements Writeable {
    static Section create() {
        final Palette blockPalette = Palette.blocks();
        final Palette biomePalette = Palette.biomes();
        final Light skyLight = Light.sky(blockPalette);
        final Light blockLight = Light.block(blockPalette);
        return new Section(blockPalette, biomePalette, skyLight, blockLight);
    }

    public void invalidate() {
        skyLight.invalidate();
        blockLight.invalidate();
    }

    public void clear() {
        this.blockPalette.fill(0);
        this.biomePalette.fill(0);
        this.skyLight.invalidate();
        this.blockLight.invalidate();
    }

    @Override
    public @NotNull Section clone() {
        final Palette blockPalette = this.blockPalette.clone();
        final Palette biomePalette = this.biomePalette.clone();
        final Light skyLight = Light.sky(blockPalette);
        final Light blockLight = Light.block(blockPalette);
        return new Section(blockPalette, biomePalette, skyLight, blockLight);
    }

    @Override
    public void write(@NotNull BinaryWriter writer) {
        writer.writeShort((short) blockPalette.count());
        writer.write(blockPalette);
        writer.write(biomePalette);
    }
}
