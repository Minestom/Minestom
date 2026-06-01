package net.minestom.server.instance.light.snapshot;

import net.minestom.server.instance.palette.Palette;

import java.util.Objects;

public record SectionSnapshot(Palette blockPalette) {
    public static final SectionSnapshot EMPTY = new SectionSnapshot(Palette.blocks());

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SectionSnapshot(Palette palette)))
            return false;
        return blockPalette.compare(palette);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPalette);
    }
}
