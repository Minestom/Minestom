package net.minestom.server.instance.generator;

import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;

public interface GeneratedData {
    SectionBlockCache blockCache();
    Palette biomePalette();
}
