package net.minestom.server.instance.light.parallel;

import net.minestom.server.instance.palette.Palette;

import java.util.Arrays;

public record SectionBlockData(Palette blockPalette, int[] occlusionMap) {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SectionBlockData(
                var p, var o
        ) && p.compare(blockPalette) && Arrays.equals(o, occlusionMap);
    }
}
