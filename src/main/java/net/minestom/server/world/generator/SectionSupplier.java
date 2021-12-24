package net.minestom.server.world.generator;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.block.SectionBlockCache;

import java.util.concurrent.CompletableFuture;

public interface SectionSupplier {
    GenerationContext createGenerationContext(Instance instance);

    CompletableFuture<Void> generateSection(Instance instance, SectionBlockCache blockCache, Palette biomePalette, int sectionX, int sectionY, int sectionZ);
}
