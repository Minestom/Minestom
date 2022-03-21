package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.UnitModifier;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static net.minestom.server.utils.chunk.ChunkUtils.getChunkCoordinate;
import static net.minestom.server.utils.chunk.ChunkUtils.toSectionRelativeCoordinate;

final class GeneratorImpl {
    private static final Vec SECTION_SIZE = new Vec(16);

    record ChunkEntry(List<Section> sections, int x, int z) {
        public ChunkEntry(Chunk chunk) {
            this(chunk.getSections(), chunk.getChunkX(), chunk.getChunkZ());
        }
    }

    static GenerationUnit section(Section section, int sectionX, int sectionY, int sectionZ) {
        final var start = SECTION_SIZE.mul(sectionX, sectionY, sectionZ);
        final var end = start.add(SECTION_SIZE);
        final UnitModifier modifier = new ModifierImpl(SECTION_SIZE, start, end) {
            @Override
            public void setBiome(int x, int y, int z, @NotNull Biome biome) {
                section.biomePalette().set(
                        toSectionRelativeCoordinate(x) / 4,
                        toSectionRelativeCoordinate(y) / 4,
                        toSectionRelativeCoordinate(z) / 4, biome.id());
            }

            @Override
            public void setBlock(int x, int y, int z, @NotNull Block block) {
                final int localX = toSectionRelativeCoordinate(x);
                final int localY = toSectionRelativeCoordinate(y);
                final int localZ = toSectionRelativeCoordinate(z);
                section.blockPalette().set(localX, localY, localZ, block.stateId());
            }

            @Override
            public void setRelative(int x, int y, int z, @NotNull Block block) {
                section.blockPalette().set(x, y, z, block.stateId());
            }

            @Override
            public void setAllRelative(@NotNull Supplier supplier) {
                section.blockPalette().setAll((x, y, z) -> supplier.get(x, y, z).stateId());
            }

            @Override
            public void fill(@NotNull Block block) {
                section.blockPalette().fill(block.stateId());
            }

            @Override
            public void fillBiome(@NotNull Biome biome) {
                section.biomePalette().fill(biome.id());
            }
        };
        return unit(modifier, start, end, null);
    }

    static GenerationUnit chunk(int minSection, int maxSection, ChunkEntry chunk) {
        final int minY = minSection * 16;
        AtomicInteger sectionCounterY = new AtomicInteger(minSection);
        List<GenerationUnit> sections = chunk.sections().stream()
                .map(section -> section(section, chunk.x(), sectionCounterY.getAndIncrement(), chunk.z()))
                .toList();

        final int chunkX = chunk.x();
        final int chunkZ = chunk.z();
        final var size = new Vec(16, (maxSection - minSection) * 16, 16);
        final var start = new Vec(chunkX * 16, minY, chunkZ * 16);
        final var end = new Vec(chunkX * 16 + 16, size.y() + minY, chunkZ * 16 + 16);
        final UnitModifier modifier = new ModifierImpl(size, start, end) {
            @Override
            public void setBlock(int x, int y, int z, @NotNull Block block) {
                checkChunk(x, chunkX, z, chunkZ);
                y -= minY;
                final int sectionY = getChunkCoordinate(y);
                final GenerationUnit section = sections.get(sectionY);
                section.modifier().setBlock(x, y, z, block);
            }

            @Override
            public void setBiome(int x, int y, int z, @NotNull Biome biome) {
                checkChunk(x, chunkX, z, chunkZ);
                y -= minY;
                final int sectionY = getChunkCoordinate(y);
                final GenerationUnit section = sections.get(sectionY);
                section.modifier().setBiome(x, y, z, biome);
            }

            @Override
            public void setRelative(int x, int y, int z, @NotNull Block block) {
                if (x < 0 || x >= size.x() || y < 0 || y >= size.y() || z < 0 || z >= size.z()) {
                    throw new IllegalArgumentException("x, y and z must be in the chunk: " + x + ", " + y + ", " + z);
                }
                final GenerationUnit section = sections.get(y / 16);
                section.modifier().setBlock(x, y % 16, z, block);
            }

            @Override
            public void setAll(@NotNull Supplier supplier) {
                for (GenerationUnit section : sections) {
                    final var start = section.absoluteStart();
                    final int startX = start.blockX();
                    final int startY = start.blockY();
                    final int startZ = start.blockZ();
                    section.modifier().setAllRelative((x, y, z) ->
                            supplier.get(x + startX, y + startY, z + startZ));
                }
            }

            @Override
            public void setAllRelative(@NotNull Supplier supplier) {
                for (int i = 0; i < sections.size(); i++) {
                    final GenerationUnit section = sections.get(i);
                    final int offset = i * 16;
                    section.modifier().setAllRelative((x, y, z) ->
                            supplier.get(x, y + offset, z));
                }
            }

            @Override
            public void fill(@NotNull Block block) {
                for (GenerationUnit section : sections) {
                    section.modifier().fill(block);
                }
            }

            @Override
            public void fillBiome(@NotNull Biome biome) {
                for (GenerationUnit section : sections) {
                    section.modifier().fillBiome(biome);
                }
            }
        };
        return unit(modifier, start, end, generationUnit -> sections);
    }

    private static void checkChunk(int x, int chunkX,
                                   int z, int chunkZ) {
        if (getChunkCoordinate(x) != chunkX) {
            throw new IllegalArgumentException("x must be in the same chunk (" + chunkX + " != " + getChunkCoordinate(x) + ")");
        }
        if (getChunkCoordinate(z) != chunkZ) {
            throw new IllegalArgumentException("z must be in the same chunk (" + chunkZ + " != " + getChunkCoordinate(z) + ")");
        }
    }

    static GenerationUnit unit(UnitModifier modifier, Point start, Point end,
                               Function<GenerationUnit, List<GenerationUnit>> divider) {
        if (start.x() > end.x() || start.y() > end.y() || start.z() > end.z()) {
            throw new IllegalArgumentException("absoluteStart must be before absoluteEnd");
        }
        if (start.x() % 16 != 0 || start.y() % 16 != 0 || start.z() % 16 != 0) {
            throw new IllegalArgumentException("absoluteStart must be a multiple of 16");
        }
        if (end.x() % 16 != 0 || end.y() % 16 != 0 || end.z() % 16 != 0) {
            throw new IllegalArgumentException("absoluteEnd must be a multiple of 16");
        }
        final Point size = end.sub(start);
        return new GenerationUnit() {
            @Override
            public @NotNull UnitModifier modifier() {
                return modifier;
            }

            @Override
            public @NotNull Point size() {
                return size;
            }

            @Override
            public @NotNull Point absoluteStart() {
                return start;
            }

            @Override
            public @NotNull Point absoluteEnd() {
                return end;
            }

            @Override
            public @NotNull List<GenerationUnit> subdivide() {
                return divider != null ? divider.apply(this) : GenerationUnit.super.subdivide();
            }
        };
    }

    static GenerationUnit dummyUnit(Point start, Point end) {
        return unit(null, start, end, null);
    }

    static abstract class ModifierImpl implements UnitModifier {
        private final Point size, start, end;

        public ModifierImpl(Point size, Point start, Point end) {
            this.size = size;
            this.start = start;
            this.end = end;
        }

        @Override
        public void setAll(@NotNull Supplier supplier) {
            for (int x = start.blockX(); x < end.blockX(); x++) {
                for (int y = start.blockY(); y < end.blockY(); y++) {
                    for (int z = start.blockZ(); z < end.blockZ(); z++) {
                        setBlock(x, y, z, supplier.get(x, y, z));
                    }
                }
            }
        }

        @Override
        public void setAllRelative(@NotNull Supplier supplier) {
            for (int x = 0; x < size.blockX(); x++) {
                for (int y = 0; y < size.blockY(); y++) {
                    for (int z = 0; z < size.blockZ(); z++) {
                        setRelative(x, y, z, supplier.get(x, y, z));
                    }
                }
            }
        }

        @Override
        public void fill(@NotNull Block block) {
            fill(start, end, block);
        }

        @Override
        public void fill(@NotNull Point start, @NotNull Point end, @NotNull Block block) {
            final int endX = end.blockX();
            final int endY = end.blockY();
            final int endZ = end.blockZ();
            for (int x = start.blockX(); x < endX; x++) {
                for (int y = start.blockY(); y < endY; y++) {
                    for (int z = start.blockZ(); z < endZ; z++) {
                        setBlock(x, y, z, block);
                    }
                }
            }
        }

        @Override
        public void fillHeight(int minHeight, int maxHeight, @NotNull Block block) {
            final int startY = start.blockY();
            final int endY = end.blockY();
            if (startY >= minHeight && endY <= maxHeight) {
                // Fast path if the unit is fully contained in the height range
                fill(start, end, block);
            } else {
                // Slow path if the unit is not fully contained in the height range
                final int startLoopY = Math.max(minHeight, startY);
                final int endLoopY = Math.min(maxHeight, endY);
                for (int x = start.blockX(); x < end.blockX(); x++) {
                    for (int z = start.blockZ(); z < end.blockZ(); z++) {
                        for (int y = startLoopY; y < endLoopY; y++) {
                            if (y >= minHeight && y <= maxHeight) {
                                setBlock(x, y, z, block);
                            }
                        }
                    }
                }
            }
        }
    }
}
