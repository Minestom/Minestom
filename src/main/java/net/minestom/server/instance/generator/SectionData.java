package net.minestom.server.instance.generator;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;

public record SectionData(SectionBlockCache blockCache, Palette biomePalette, int y) {
}
