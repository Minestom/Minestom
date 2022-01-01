package net.minestom.server.instance.generator;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;

public record LegacySectionData(SectionBlockCache blockCache, Palette biomePalette, byte[] blockLight, byte[] skyLight) implements GeneratedData {
}
