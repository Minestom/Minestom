package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides full compatibility for the deprecated {@link ChunkGenerator}
 */
@SuppressWarnings("deprecation")
record ChunkGeneratorCompatibilityLayer(@NotNull ChunkGenerator chunkGenerator) implements Generator {
    @Override
    public void generate(@NotNull GenerationUnit unit) {
        if (!(unit instanceof GeneratorImpl.UnitImpl impl) || !(impl.modifier() instanceof GeneratorImpl.ChunkModifierImpl chunkModifier)) {
            throw new IllegalArgumentException("Invalid unit");
        }

        final int startY = unit.absoluteStart().blockY();
        ChunkBatch batch = new ChunkBatch() {
            @Override
            public void setBlock(int x, int y, int z, @NotNull Block block) {
                unit.modifier().setRelative(x, y - startY, z, block);
            }
        };
        final Point start = unit.absoluteStart();
        chunkGenerator.generateChunkData(batch, start.chunkX(), start.chunkZ());

        final List<ChunkPopulator> populators = chunkGenerator.getPopulators();
        final boolean hasPopulator = populators != null && !populators.isEmpty();
        if (hasPopulator) {
            for (ChunkPopulator chunkPopulator : populators) {
                chunkPopulator.populateChunk(batch, chunkModifier.chunk());
            }
        }
    }
}
