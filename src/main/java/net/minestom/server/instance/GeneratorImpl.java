package net.minestom.server.instance;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.UnitModifier;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static net.minestom.server.utils.chunk.ChunkUtils.*;

final class GeneratorImpl {
    private static final Vec SECTION_SIZE = new Vec(16);

    static GenerationUnit section(Section section, int chunkX, int sectionY, int chunkZ) {
        final Vec start = SECTION_SIZE.mul(chunkX, sectionY, chunkZ);
        final Vec end = start.add(SECTION_SIZE);
        final UnitModifier modifier = new SectionModifierImpl(SECTION_SIZE, start, end, section, false);
        return unit(modifier, start, end, null);
    }

    static GenerationUnit fork(int chunkX, int sectionY, int chunkZ) {
        final Vec start = SECTION_SIZE.mul(chunkX, sectionY, chunkZ);
        final Vec end = start.add(SECTION_SIZE);
        Section memory = Section.inMemory();
        final UnitModifier modifier = new SectionModifierImpl(SECTION_SIZE, start, end, memory, true);
        return unit(modifier, start, end, null);
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

    static final class DynamicFork implements Block.Setter {
        Vec minSection;
        int width, height, depth;
        List<GenerationUnit> sections;

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            resize(x, y, z);
            GenerationUnit section = findAbsolute(sections, minSection, width, height, depth, x, y, z);
            assert section.absoluteStart().chunkX() == getChunkCoordinate(x) &&
                    section.absoluteStart().section() == getChunkCoordinate(y) &&
                    section.absoluteStart().chunkZ() == getChunkCoordinate(z) :
                    "Invalid section " + section.absoluteStart() + " for " + x + ", " + y + ", " + z;
            section.modifier().setBlock(x, y, z, block);
        }

        private void resize(int x, int y, int z) {
            final int sectionX = getChunkCoordinate(x);
            final int sectionY = getChunkCoordinate(y);
            final int sectionZ = getChunkCoordinate(z);
            if (sections == null) {
                this.minSection = new Vec(sectionX * Section.SIZE_X, sectionY * Section.SIZE_Y, sectionZ * Section.SIZE_Z);
                this.width = 1;
                this.height = 1;
                this.depth = 1;
                this.sections = List.of(fork(sectionX, sectionY, sectionZ));
            } else if (x < minSection.x() || y < minSection.y() || z < minSection.z() ||
                    x >= minSection.x() + width * 16 || y >= minSection.y() + height * 16 || z >= minSection.z() + depth * 16) {
                // Resize necessary
                final Vec newMin = new Vec(Math.min(minSection.x(), sectionX * 16),
                        Math.min(minSection.y(), sectionY * 16),
                        Math.min(minSection.z(), sectionZ * 16));
                final Vec newMax = new Vec(Math.max(minSection.x() + width * 16, sectionX * 16 + 16),
                        Math.max(minSection.y() + height * 16, sectionY * 16 + 16),
                        Math.max(minSection.z() + depth * 16, sectionZ * 16 + 16));
                final int newWidth = getChunkCoordinate(newMax.x() - newMin.x());
                final int newHeight = getChunkCoordinate(newMax.y() - newMin.y());
                final int newDepth = getChunkCoordinate(newMax.z() - newMin.z());
                // Resize
                GenerationUnit[] newSections = new GenerationUnit[newWidth * newHeight * newDepth];
                // Copy old sections
                for (GenerationUnit s : sections) {
                    final Point start = s.absoluteStart();
                    final int newX = getChunkCoordinate(start.x() - newMin.x());
                    final int newY = getChunkCoordinate(start.y() - newMin.y());
                    final int newZ = getChunkCoordinate(start.z() - newMin.z());
                    final int index = findIndex(newWidth, newHeight, newDepth, newX, newY, newZ);
                    newSections[index] = s;
                }
                // Fill new sections
                final int startX = newMin.chunkX();
                final int startY = newMin.section();
                final int startZ = newMin.chunkZ();
                for (int i = 0; i < newSections.length; i++) {
                    if (newSections[i] == null) {
                        final Point coordinates = to3D(i, newWidth, newHeight, newDepth);
                        final int newX = coordinates.blockX() + startX;
                        final int newY = coordinates.blockY() + startY;
                        final int newZ = coordinates.blockZ() + startZ;
                        final GenerationUnit unit = fork(newX, newY, newZ);
                        newSections[i] = unit;
                    }
                }
                this.sections = List.of(newSections);
                this.minSection = newMin;
                this.width = newWidth;
                this.height = newHeight;
                this.depth = newDepth;
            }
        }
    }

    record UnitImpl(UnitModifier modifier, Point size,
                    Point absoluteStart, Point absoluteEnd,
                    List<GenerationUnit> divided,
                    List<GenerationUnit> forks) implements GenerationUnit {
        @Override
        public @NotNull GenerationUnit fork(@NotNull Point start, @NotNull Point end) {
            final int minSectionX = floorSection(start.blockX()) / 16;
            final int minSectionY = floorSection(start.blockY()) / 16;
            final int minSectionZ = floorSection(start.blockZ()) / 16;

            final int maxSectionX = ceilSection(end.blockX()) / 16;
            final int maxSectionY = ceilSection(end.blockY()) / 16;
            final int maxSectionZ = ceilSection(end.blockZ()) / 16;

            final int width = maxSectionX - minSectionX;
            final int height = maxSectionY - minSectionY;
            final int depth = maxSectionZ - minSectionZ;

            GenerationUnit[] units = new GenerationUnit[width * height * depth];
            int index = 0;
            for (int sectionX = minSectionX; sectionX < maxSectionX; sectionX++) {
                for (int sectionY = minSectionY; sectionY < maxSectionY; sectionY++) {
                    for (int sectionZ = minSectionZ; sectionZ < maxSectionZ; sectionZ++) {
                        final GenerationUnit unit = GeneratorImpl.fork(sectionX, sectionY, sectionZ);
                        units[index++] = unit;
                    }
                }
            }
            final List<GenerationUnit> sections = List.of(units);
            final Point startSection = new Vec(minSectionX * 16, minSectionY * 16, minSectionZ * 16);
            return registerFork(startSection, sections, width, height, depth);
        }

        @Override
        public void fork(@NotNull Consumer<Block.@NotNull Setter> consumer) {
            DynamicFork dynamicFork = new DynamicFork();
            consumer.accept(dynamicFork);
            final Point startSection = dynamicFork.minSection;
            if (startSection == null)
                return; // No block has been placed
            final int width = dynamicFork.width;
            final int height = dynamicFork.height;
            final int depth = dynamicFork.depth;
            final List<GenerationUnit> sections = dynamicFork.sections;
            registerFork(startSection, sections, width, height, depth);
        }

        @Override
        public @NotNull List<GenerationUnit> subdivide() {
            return Objects.requireNonNullElseGet(divided, GenerationUnit.super::subdivide);
        }

        private GenerationUnit registerFork(Point start, List<GenerationUnit> sections,
                                            int width, int height, int depth) {
            final Point end = start.add(width * Section.SIZE_X, height * Section.SIZE_Y, depth * Section.SIZE_Z);
            final Point size = end.sub(start);
            forks.addAll(sections);
            MultiSectionModifier modifier = new MultiSectionModifier(sections, start, end);
            return new UnitImpl(modifier, size, start, end, null, forks);
        }

        private record MultiSectionModifier(Long2ObjectMap<GenerationUnit> sections, Point absoluteStart, Point absoluteEnd) implements UnitModifier {

            public MultiSectionModifier(List<GenerationUnit> sections, Point absoluteStart, Point absoluteEnd) {
                this(fromList(sections), absoluteStart, absoluteEnd);
            }

            private static Long2ObjectMap<GenerationUnit> fromList(List<GenerationUnit> sections) {
                Long2ObjectMap<GenerationUnit> map = new Long2ObjectOpenHashMap<>(sections.size());
                for (GenerationUnit section : sections) {
                    final Point start = section.absoluteStart();
                    final int x = start.blockX();
                    final int y = start.blockY();
                    final int z = start.blockZ();
                    map.put(ChunkUtils.getSectionIndex(x, y, z), section);
                }
                return map;
            }

            private @UnknownNullability UnitModifier modifier(int x, int y, int z) {
                long index = ChunkUtils.getSectionIndex(x, y, z);
                GenerationUnit section = sections.get(index);
                if (section == null) {
                    throw new IllegalStateException("No section at " + x + ", " + y + ", " + z);
                }
                return section.modifier();
            }

            @Override
            public void setBlock(int x, int y, int z, @NotNull Block block) {
                modifier(x, y, z).setBlock(x, y, z, block);
            }

            @Override
            public void setRelative(int x, int y, int z, @NotNull Block block) {
                modifier(x, y, z).setRelative(x, y, z, block);
            }

            @Override
            public void setAll(@NotNull Supplier supplier) {
                for (GenerationUnit section : sections.values()) {
                    section.modifier().setAll(supplier);
                }
            }

            @Override
            public void setAllRelative(@NotNull Supplier supplier) {
                for (GenerationUnit unit : sections.values()) {
                    Point start = unit.absoluteStart();
                    Point end = unit.absoluteEnd();

                    int startX = start.blockX();
                    int startY = start.blockY();
                    int startZ = start.blockZ();

                    int relX = startX - absoluteStart.blockX();
                    int relY = startY - absoluteStart.blockY();
                    int relZ = startZ - absoluteStart.blockZ();

                    for (int x = start.blockX(); x < end.blockX(); x++) {
                        for (int y = start.blockY(); y < end.blockY(); y++) {
                            for (int z = start.blockZ(); z < end.blockZ(); z++) {
                                Block block = supplier.get(x + relX, y + relY, z + relZ);
                                unit.modifier().setRelative(x, y, z, block);
                            }
                        }
                    }
                }
            }

            @Override
            public void fill(@NotNull Point start, @NotNull Point end, @NotNull Block block) {
                int startX = start.blockX();
                int startY = start.blockY();
                int startZ = start.blockZ();

                int endX = end.blockX();
                int endY = end.blockY();
                int endZ = end.blockZ();

                for (int x = startX; x < endX; x++) {
                    for (int y = startY; y < endY; y++) {
                        for (int z = startZ; z < endZ; z++) {
                            modifier(x, y, z).setBlock(x, y, z, block);
                        }
                    }
                }
            }

            @Override
            public void fillHeight(int minHeight, int maxHeight, @NotNull Block block) {
                sections.values().forEach(section -> section.modifier().fillHeight(minHeight, maxHeight, block));
            }

            @Override
            public void fillBiome(@NotNull Biome biome) {
                sections.values().forEach(section -> section.modifier().fillBiome(biome));
            }

            @Override
            public void setBiome(int x, int y, int z, @NotNull Biome biome) {
                modifier(x, y, z).setBiome(x, y, z, biome);
            }
        }
    }

    record SectionModifierImpl(Point size, Point start, Point end,
                               Section section, boolean fork) implements GenericModifier {
        @Override
        public void setBiome(int x, int y, int z, @NotNull Biome biome) {
            this.section.setBiome(
                    toSectionRelativeCoordinate(x) / 4,
                    toSectionRelativeCoordinate(y) / 4,
                    toSectionRelativeCoordinate(z) / 4, biome);
        }

        @Override
        public void setBlock(int x, int y, int z, @NotNull Block block) {
            final int localX = toSectionRelativeCoordinate(x);
            final int localY = toSectionRelativeCoordinate(y);
            final int localZ = toSectionRelativeCoordinate(z);
            this.section.setBlock(localX, localY, localZ, block);
        }

        @Override
        public void setRelative(int x, int y, int z, @NotNull Block block) {
            setBlock(x, y, z, block);
        }

        @Override
        public void setAllRelative(@NotNull Supplier supplier) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                        final Block block = supplier.get(x, y, z);
                        setBlock(x, y, z, block);
                    }
                }
            }
        }

        @Override
        public void fill(@NotNull Block block) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                        this.section.setBlock(x, y, z, block);
                    }
                }
            }
        }

        @Override
        public void fillBiome(@NotNull Biome biome) {
            for (int x = 0; x < Chunk.CHUNK_SIZE_X / 4; x++) {
                for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE / 4; y++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z / 4; z++) {
                        setBiome(x, y, z, biome);
                    }
                }
            }
        }
    }

    sealed interface GenericModifier extends UnitModifier
            permits SectionModifierImpl {
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

    private static GenerationUnit findAbsolute(List<GenerationUnit> units, Point start,
                                               int width, int height, int depth,
                                               int x, int y, int z) {
        final int sectionX = getChunkCoordinate(x - start.x());
        final int sectionY = getChunkCoordinate(y - start.y());
        final int sectionZ = getChunkCoordinate(z - start.z());
        final int index = findIndex(width, height, depth, sectionX, sectionY, sectionZ);
        return units.get(index);
    }

    private static int findIndex(int width, int height, int depth,
                                 int x, int y, int z) {
        return (z * width * height) + (y * width) + x;
    }

    private static Point to3D(int idx, int width, int height, int depth) {
        final int z = idx / (width * height);
        idx -= (z * width * height);
        final int y = idx / width;
        final int x = idx % width;
        return new Vec(x, y, z);
    }
}
