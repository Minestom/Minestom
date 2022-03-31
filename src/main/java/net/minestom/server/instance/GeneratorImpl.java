package net.minestom.server.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.UnitModifier;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minestom.server.utils.chunk.ChunkUtils.getChunkCoordinate;
import static net.minestom.server.utils.chunk.ChunkUtils.toSectionRelativeCoordinate;

final class GeneratorImpl {
    private static final Vec SECTION_SIZE = new Vec(16);

    static GenerationUnit section(Section section, int sectionX, int sectionY, int sectionZ) {
        final Vec start = SECTION_SIZE.mul(sectionX, sectionY, sectionZ);
        final Vec end = start.add(SECTION_SIZE);
        final UnitModifier modifier = new SectionModifierImpl(SECTION_SIZE, start, end,
                section.blockPalette(), section.biomePalette(), new Int2ObjectOpenHashMap<>(0));
        return unit(modifier, start, end, null);
    }

    static UnitImpl chunk(Chunk chunk, int minSection, int maxSection,
                          List<Section> chunkSections, int chunkX, int chunkZ) {
        final int minY = minSection * 16;
        AtomicInteger sectionCounterY = new AtomicInteger(minSection);
        List<GenerationUnit> sections = chunkSections.stream()
                .map(section -> section(section, chunkX, sectionCounterY.getAndIncrement(), chunkZ))
                .toList();

        final Vec size = new Vec(16, (maxSection - minSection) * 16, 16);
        final Vec start = new Vec(chunkX * 16, minY, chunkZ * 16);
        final Vec end = new Vec(chunkX * 16 + 16, size.y() + minY, chunkZ * 16 + 16);
        final UnitModifier modifier = new AreaModifierImpl(chunk,
                size, start, end, 1, sections.size(), 1, sections);
        return unit(modifier, start, end, sections);
    }

    static UnitImpl chunk(int minSection, int maxSection,
                          List<Section> chunkSections, int chunkX, int chunkZ) {
        return chunk(null, minSection, maxSection, chunkSections, chunkX, chunkZ);
    }

    static UnitImpl chunk(Chunk chunk) {
        return chunk(chunk, chunk.minSection, chunk.maxSection, chunk.getSections(), chunk.getChunkX(), chunk.getChunkZ());
    }

    static UnitImpl unit(UnitModifier modifier, Point start, Point end,
                         List<GenerationUnit> divided) {
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
        return new UnitImpl(modifier, size, start, end, divided, new CopyOnWriteArrayList<>());
    }

    record UnitImpl(UnitModifier modifier, Point size,
                    Point absoluteStart, Point absoluteEnd,
                    List<GenerationUnit> divided,
                    List<UnitImpl> forks) implements GenerationUnit {
        @Override
        public @NotNull GenerationUnit fork(@NotNull Point start, @NotNull Point end) {
            final int minSectionX = start.chunkX();
            final int minSectionY = start.section();
            final int minSectionZ = start.chunkZ();

            final int maxSectionX = end.chunkX();
            final int maxSectionY = end.section();
            final int maxSectionZ = end.chunkZ();

            final int width = maxSectionX - minSectionX;
            final int height = maxSectionY - minSectionY;
            final int depth = maxSectionZ - minSectionZ;

            GenerationUnit[] units = new GenerationUnit[width * height * depth];
            int index = 0;
            for (int sectionX = minSectionX; sectionX < maxSectionX; sectionX++) {
                for (int sectionY = minSectionY; sectionY < maxSectionY; sectionY++) {
                    for (int sectionZ = minSectionZ; sectionZ < maxSectionZ; sectionZ++) {
                        final GenerationUnit unit = section(new Section(), sectionX, sectionY, sectionZ);
                        units[index++] = unit;
                    }
                }
            }
            final List<GenerationUnit> sections = List.of(units);

            final Point startSection = new Vec(minSectionX * 16, minSectionY * 16, minSectionZ * 16);
            final Point endSection = new Vec(maxSectionX * 16, maxSectionY * 16, maxSectionZ * 16);

            final Point size = endSection.sub(startSection);
            final AreaModifierImpl modifier = new AreaModifierImpl(null,
                    size, startSection, endSection,
                    width, height, depth, sections);
            final UnitImpl fork = new UnitImpl(modifier, size, startSection, endSection, sections, forks);
            forks.add(fork);
            return fork;
        }

        @Override
        public @NotNull List<GenerationUnit> subdivide() {
            return Objects.requireNonNullElseGet(divided, GenerationUnit.super::subdivide);
        }
    }

    record SectionModifierImpl(Point size, Point start, Point end,
                               Palette blockPalette, Palette biomePalette,
                               Int2ObjectMap<Block> cache) implements GenericModifier {
        @Override
        public void setBiome(int x, int y, int z, @NotNull Biome biome) {
            this.biomePalette.set(
                    toSectionRelativeCoordinate(x) / 4,
                    toSectionRelativeCoordinate(y) / 4,
                    toSectionRelativeCoordinate(z) / 4, biome.id());
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            final int localX = toSectionRelativeCoordinate(x);
            final int localY = toSectionRelativeCoordinate(y);
            final int localZ = toSectionRelativeCoordinate(z);
            handleCache(localX, localY, localZ, block);
            this.blockPalette.set(localX, localY, localZ, block.stateId());
        }

        @Override
        public void setRelative(int x, int y, int z, @NotNull Block block) {
            handleCache(x, y, z, block);
            this.blockPalette.set(x, y, z, block.stateId());
        }

        @Override
        public void setAllRelative(@NotNull Supplier supplier) {
            this.blockPalette.setAll((x, y, z) -> {
                final Block block = supplier.get(x, y, z);
                handleCache(x, y, z, block);
                return block.stateId();
            });
        }

        @Override
        public void fill(@NotNull Block block) {
            if (requireCache(block)) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            this.cache.put(ChunkUtils.getBlockIndex(x, y, z), block);
                        }
                    }
                }
            }
            this.blockPalette.fill(block.stateId());
        }

        @Override
        public void fillBiome(@NotNull Biome biome) {
            this.biomePalette.fill(biome.id());
        }

        private void handleCache(int x, int y, int z, Block block) {
            if (requireCache(block)) {
                this.cache.put(ChunkUtils.getBlockIndex(x, y, z), block);
            } else if (!cache.isEmpty()) {
                this.cache.remove(ChunkUtils.getBlockIndex(x, y, z));
            }
        }

        private boolean requireCache(Block block) {
            return block.hasNbt() || block.handler() != null || block.registry().isBlockEntity();
        }
    }

    record AreaModifierImpl(Chunk chunk,
                            Point size, Point start, Point end,
                            int width, int height, int depth,
                            List<GenerationUnit> sections) implements GenericModifier {
        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            checkBorder(x, y, z);
            final GenerationUnit section = findAbsoluteSection(x, y, z);
            y -= start.y();
            section.modifier().setBlock(x, y, z, block);
        }

        @Override
        public void setBiome(int x, int y, int z, @NotNull Biome biome) {
            checkBorder(x, y, z);
            final GenerationUnit section = findAbsoluteSection(x, y, z);
            y -= start.y();
            section.modifier().setBiome(x, y, z, biome);
        }

        @Override
        public void setRelative(int x, int y, int z, @NotNull Block block) {
            if (x < 0 || x >= size.x() || y < 0 || y >= size.y() || z < 0 || z >= size.z()) {
                throw new IllegalArgumentException("x, y and z must be in the chunk: " + x + ", " + y + ", " + z);
            }
            final GenerationUnit section = findRelativeSection(x, y, z);
            x = ChunkUtils.toSectionRelativeCoordinate(x);
            y = ChunkUtils.toSectionRelativeCoordinate(y);
            z = ChunkUtils.toSectionRelativeCoordinate(z);
            section.modifier().setBlock(x, y, z, block);
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

        @Override
        public void fillHeight(int minHeight, int maxHeight, @NotNull Block block) {
            final int minY = (int) start.y();
            final int minMultiple = floorSection(minHeight);
            final int maxMultiple = ceilSection(maxHeight);
            // First section
            if (minMultiple != minHeight) {
                assert minHeight % 16 != 0;
                final int sectionY = getChunkCoordinate(minHeight - minY);
                final GenerationUnit section = sections.get(sectionY);
                section.modifier().fillHeight(minHeight, Math.min(minMultiple, maxHeight), block);
            }
            // Last section
            if (maxMultiple != maxHeight) {
                assert maxHeight % 16 != 0;
                final int sectionY = getChunkCoordinate(maxMultiple - minY);
                final GenerationUnit section = sections.get(sectionY);
                section.modifier().fillHeight(maxMultiple, maxHeight, block);
            }
            // Middle sections (to fill)
            final int startSection = (minMultiple - minY) / 16;
            final int endSection = (maxMultiple - 1 - minY) / 16;
            for (int i = startSection; i <= endSection; i++) {
                final GenerationUnit section = sections.get(i);
                section.modifier().fill(block);
            }
        }

        private GenerationUnit findAbsoluteSection(int x, int y, int z) {
            final int sectionX = getChunkCoordinate(x - start.x());
            final int sectionY = getChunkCoordinate(y - start.y());
            final int sectionZ = getChunkCoordinate(z - start.z());
            final int index = sectionZ + sectionY * depth + sectionX * depth * height;
            return sections.get(index);
        }

        private GenerationUnit findRelativeSection(int x, int y, int z) {
            final int sectionX = getChunkCoordinate(x);
            final int sectionY = getChunkCoordinate(y);
            final int sectionZ = getChunkCoordinate(z);
            final int index = sectionZ + sectionY * depth + sectionX * depth * height;
            return sections.get(index);
        }

        private void checkBorder(int x, int y, int z) {
            if (x < start.x() || x >= end.x() ||
                    y < start.y() || y >= end.y() ||
                    z < start.z() || z >= end.z()) {
                final String format = String.format("Invalid coordinates: %d, %d, %d for area %s %s", x, y, z, start, end);
                throw new IllegalArgumentException(format);
            }
        }
    }

    sealed interface GenericModifier extends UnitModifier
            permits AreaModifierImpl, SectionModifierImpl {
        Point size();

        Point start();

        Point end();

        @Override
        default void setAll(@NotNull Supplier supplier) {
            final Point start = start();
            final Point end = end();
            final int endX = end.blockX();
            final int endY = end.blockY();
            final int endZ = end.blockZ();
            for (int x = start.blockX(); x < endX; x++) {
                for (int y = start.blockY(); y < endY; y++) {
                    for (int z = start.blockZ(); z < endZ; z++) {
                        setBlock(x, y, z, supplier.get(x, y, z));
                    }
                }
            }
        }

        @Override
        default void setAllRelative(@NotNull Supplier supplier) {
            final Point size = size();
            final int endX = size.blockX();
            final int endY = size.blockY();
            final int endZ = size.blockZ();
            for (int x = 0; x < endX; x++) {
                for (int y = 0; y < endY; y++) {
                    for (int z = 0; z < endZ; z++) {
                        setRelative(x, y, z, supplier.get(x, y, z));
                    }
                }
            }
        }

        @Override
        default void fill(@NotNull Block block) {
            fill(start(), end(), block);
        }

        @Override
        default void fill(@NotNull Point start, @NotNull Point end, @NotNull Block block) {
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
        default void fillHeight(int minHeight, int maxHeight, @NotNull Block block) {
            final Point start = start();
            final Point end = end();
            final int startY = start.blockY();
            final int endY = end.blockY();
            if (startY >= minHeight && endY <= maxHeight) {
                // Fast path if the unit is fully contained in the height range
                fill(start, end, block);
            } else {
                // Slow path if the unit is not fully contained in the height range
                fill(start.withY(Math.max(minHeight, startY)), end.withY(Math.min(maxHeight, endY)), block);
            }
        }
    }

    private static int floorSection(int coordinate) {
        return ((coordinate - 1) | 15) + 1;
    }

    private static int ceilSection(int coordinate) {
        return coordinate - (coordinate & 0xF);
    }
}
