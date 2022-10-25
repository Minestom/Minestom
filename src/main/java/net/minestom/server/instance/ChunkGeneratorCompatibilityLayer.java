package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Provides full compatibility for the deprecated {@link ChunkGenerator}
 */
@SuppressWarnings("deprecation")
record ChunkGeneratorCompatibilityLayer(@NotNull ChunkGenerator chunkGenerator, @NotNull DimensionType dimensionType) implements Generator {
    @Override
    public void generate(@NotNull GenerationUnit unit) {

        final Point start = unit.absoluteStart();
        final Point end = unit.absoluteEnd();
        final int startY = start.blockY();

        // Check if this unit contains minY
        boolean containsYZero = startY <= dimensionType.getMinY() && end.blockY() >= dimensionType.getMinY();
        if (!containsYZero) return;

        // Check if this unit contains a section
        int sectionX = ChunkUtils.getChunkCoordinate(end.blockX());
        int sectionY = ChunkUtils.getChunkCoordinate(end.blockY());
        int sectionZ = ChunkUtils.getChunkCoordinate(end.blockZ());

        boolean containsSection = start.blockX() <= sectionX && start.blockY() <= sectionY && start.blockZ() <= sectionZ;
        if (!containsSection) return;

        Point forkStart = new Vec(sectionX * Chunk.CHUNK_SIZE_X, dimensionType.getMinY(), sectionZ * Chunk.CHUNK_SIZE_Z);
        Point forkEnd = forkStart.withY(dimensionType.getMaxY()).add(Chunk.CHUNK_SIZE_X, 0, Chunk.CHUNK_SIZE_Z);

        // Fork the unit
        GenerationUnit fork = unit.fork(forkStart, forkEnd);

        ChunkBatch batch = new ChunkBatch() {
            @Override
            public void setBlock(int x, int y, int z, @NotNull Block block) {
                fork.modifier().setRelative(x, y - startY, z, block);
            }
        };

        // Generate using deprecated api
        chunkGenerator.generateChunkData(batch, start.chunkX(), start.chunkZ());
    }
}
