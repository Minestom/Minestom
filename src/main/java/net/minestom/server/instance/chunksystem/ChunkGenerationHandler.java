package net.minestom.server.instance.chunksystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.instance.generator.GeneratorImpl;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.chunk.ChunkSupplier;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ChunkGenerationHandler {
    private final Instance instance;
    // TODO this is in theory a memory leak...
    //  but to fix this the entire chunk generation must be reworked
    private final Map<Long, List<GeneratorImpl.SectionModifierImpl>> generationForks = new ConcurrentHashMap<>();

    public ChunkGenerationHandler(Instance instance) {
        this.instance = instance;
    }

    public Chunk createChunk(ChunkSupplier chunkSupplier, @Nullable Generator generator, int chunkX, int chunkZ) {
        final Chunk chunk = chunkSupplier.createChunk(instance, chunkX, chunkZ);
        Check.notNull(chunk, "Chunks supplied by a ChunkSupplier cannot be null.");
        if (generator == null || !chunk.shouldGenerate()) {
            // No chunk generator, execute the callback with the empty chunk
            processFork(chunk);
            return chunk;
        }
        GeneratorImpl.GenSection[] genSections = new GeneratorImpl.GenSection[chunk.getSections().size()];
        Arrays.setAll(genSections, i -> {
            Section section = chunk.getSections().get(i);
            return new GeneratorImpl.GenSection(section.blockPalette(), section.biomePalette());
        });
        var chunkUnit = GeneratorImpl.chunk(MinecraftServer.getBiomeRegistry(), genSections, chunk.getChunkX(), chunk.getMinSection(), chunk.getChunkZ());
        try {
            // Generate block/biome palette
            generator.generate(chunkUnit);
            // Apply nbt/handler
            if (chunkUnit.modifier() instanceof GeneratorImpl.AreaModifierImpl chunkModifier) {
                for (var section : chunkModifier.sections()) {
                    if (section.modifier() instanceof GeneratorImpl.SectionModifierImpl sectionModifier) {
                        applyGenerationData(chunk, sectionModifier);
                    }
                }
            }
            // Register forks or apply locally
            for (var fork : chunkUnit.forks()) {
                var sections = ((GeneratorImpl.AreaModifierImpl) fork.modifier()).sections();
                for (var section : sections) {
                    if (section.modifier() instanceof GeneratorImpl.SectionModifierImpl sectionModifier) {
                        if (sectionModifier.genSection().blocks().count() == 0) continue;
                        final Point start = section.absoluteStart();
                        final Chunk forkChunk = start.chunkX() == chunkX && start.chunkZ() == chunkZ ? chunk : instance.getChunkAt(start);
                        if (forkChunk != null) {
                            applyFork(forkChunk, sectionModifier);
                            // Update players
                            forkChunk.invalidate();
                            forkChunk.sendChunk();
                        } else {
                            final long index = CoordConversion.chunkIndex(start);
                            this.generationForks.compute(index, (_, sectionModifiers) -> {
                                if (sectionModifiers == null) sectionModifiers = new ArrayList<>();
                                sectionModifiers.add(sectionModifier);
                                return sectionModifiers;
                            });
                        }
                    }
                }
            }
            // Apply awaiting forks
            processFork(chunk);
        } catch (Throwable e) {
            MinecraftServer.getExceptionManager().handleException(e);
        } finally {
            // End generation
            if (instance instanceof InstanceContainer container) {
                container.refreshLastBlockChangeTime();
            }
        }
        return chunk;
    }

    private void processFork(Chunk chunk) {
        this.generationForks.compute(CoordConversion.chunkIndex(chunk.getChunkX(), chunk.getChunkZ()), (_, sectionModifiers) -> {
            if (sectionModifiers != null) {
                for (var sectionModifier : sectionModifiers) {
                    applyFork(chunk, sectionModifier);
                }
            }
            return null;
        });
    }

    private void applyFork(Chunk chunk, GeneratorImpl.SectionModifierImpl sectionModifier) {
        synchronized (chunk) {
            Section section = chunk.getSectionAt(sectionModifier.start().blockY());
            Palette currentBlocks = section.blockPalette();
            // -1 is necessary because forked units handle explicit changes by changing AIR 0 to 1
            sectionModifier.genSection().blocks().getAllPresent((x, y, z, value) -> currentBlocks.set(x, y, z, value - 1));
            applyGenerationData(chunk, sectionModifier);
        }
    }

    private void applyGenerationData(Chunk chunk, GeneratorImpl.SectionModifierImpl section) {
        var cache = section.genSection().specials();
        if (cache.isEmpty()) return;
        final int height = section.start().blockY();
        synchronized (chunk) {
            Int2ObjectMaps.fastForEach(cache, blockEntry -> {
                final int index = blockEntry.getIntKey();
                final Block block = blockEntry.getValue();
                final int x = CoordConversion.chunkBlockIndexGetX(index);
                final int y = CoordConversion.chunkBlockIndexGetY(index) + height;
                final int z = CoordConversion.chunkBlockIndexGetZ(index);
                chunk.setBlock(x, y, z, block);
            });
        }
    }

}
