package net.minestom.server.instance.generator;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static net.minestom.server.coordinate.CoordConversion.*;

@ApiStatus.Internal
public final class GeneratorImpl {
    public record GenSection(Palette blocks, Palette biomes, Int2ObjectMap<Block> specials) {
        public GenSection(Palette blocks, Palette biomes) {
            this(blocks, biomes, new Int2ObjectOpenHashMap<>(0));
        }

        public GenSection() {
            this(Palette.blocks(), Palette.biomes());
        }
    }

    static GenerationUnit section(DynamicRegistry<Biome> biomeRegistry, GenSection section,
                                  int sectionX, int sectionY, int sectionZ,
                                  boolean fork) {
        final Vec start = Vec.SECTION.mul(sectionX, sectionY, sectionZ);
        final Vec end = start.add(Vec.SECTION);
        final UnitModifier modifier = new SectionModifierImpl(biomeRegistry, start, end, section, fork);
        return unit(biomeRegistry, modifier, start, end, null);
    }

    public static GenerationUnit section(DynamicRegistry<Biome> biomeRegistry, GenSection section, int sectionX, int sectionY, int sectionZ) {
        return section(biomeRegistry, section, sectionX, sectionY, sectionZ, false);
    }

    public static UnitImpl chunk(DynamicRegistry<Biome> biomeRegistry, GenSection[] chunkSections, int chunkX, int minSection, int chunkZ) {
        final Vec start = Vec.SECTION.mul(chunkX, minSection, chunkZ);
        return area(biomeRegistry, start, 1, chunkSections.length, 1, chunkSections);
    }

    public static UnitImpl area(DynamicRegistry<Biome> biomeRegistry, Vec start, int width, int height, int depth, GenSection[] areaSections) {
        if (width == 0 || height == 0 || depth == 0) {
            throw new IllegalArgumentException("Width, height and depth must be greater than 0, got " + width + ", " + height + ", " + depth);
        }
        if (areaSections.length != width * height * depth) {
            throw new IllegalArgumentException("Invalid section count, expected " + width * height * depth + " but got " + areaSections.length);
        }

        final int sectionCount = areaSections.length;
        GenerationUnit[] sectionsArray = new GenerationUnit[sectionCount];
        for (int i = 0; i < sectionCount; i++) {
            GenSection section = areaSections[i];
            final Vec point = to3D(i, width, height, depth);
            final int sectionX = (int) point.x() + start.sectionX();
            final int sectionY = (int) point.y() + start.sectionY();
            final int sectionZ = (int) point.z() + start.sectionZ();
            final GenerationUnit sectionUnit = section(biomeRegistry, section, sectionX, sectionY, sectionZ);
            sectionsArray[i] = sectionUnit;
        }
        final List<GenerationUnit> sections = List.of(sectionsArray);
        final Vec size = Vec.SECTION.mul(width, height, depth);
        final Vec end = start.add(size);
        final UnitModifier modifier = new AreaModifierImpl(size, start, end, width, height, depth, sections);
        return unit(biomeRegistry, modifier, start, end, sections);
    }

    public static UnitImpl unit(DynamicRegistry<Biome> biomeRegistry, UnitModifier modifier, Vec start, Vec end,
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
        final Vec size = end.sub(start);
        return new UnitImpl(biomeRegistry, modifier, size, start, end, divided, new CopyOnWriteArrayList<>());
    }

    static final class DynamicFork implements Block.Setter {
        final DynamicRegistry<Biome> biomeRegistry;
        Vec minSection;
        int width, height, depth;
        List<GenerationUnit> sections;

        DynamicFork(DynamicRegistry<Biome> biomeRegistry) {
            this.biomeRegistry = biomeRegistry;
        }

        @Override
        public void setBlock(int x, int y, int z, Block block) {
            resize(x, y, z);
            GenerationUnit section = findAbsolute(sections, minSection, width, height, depth, x, y, z);
            assert section.absoluteStart().sectionX() == globalToChunk(x) &&
                    section.absoluteStart().sectionY() == globalToChunk(y) &&
                    section.absoluteStart().sectionZ() == globalToChunk(z) :
                    "Invalid section " + section.absoluteStart() + " for " + x + ", " + y + ", " + z;
            section.modifier().setBlock(x, y, z, block);
        }

        private void resize(int x, int y, int z) {
            final int sectionX = globalToChunk(x);
            final int sectionY = globalToChunk(y);
            final int sectionZ = globalToChunk(z);
            if (sections == null) {
                this.minSection = Vec.SECTION.mul(sectionX, sectionY, sectionZ);
                this.width = 1;
                this.height = 1;
                this.depth = 1;
                this.sections = List.of(section(biomeRegistry, new GenSection(), sectionX, sectionY, sectionZ, true));
            } else if (x < minSection.x() || y < minSection.y() || z < minSection.z() ||
                    x >= minSection.x() + width * 16 || y >= minSection.y() + height * 16 || z >= minSection.z() + depth * 16) {
                // Resize necessary
                final Vec newMin = new Vec(Math.min(minSection.x(), sectionX * 16),
                        Math.min(minSection.y(), sectionY * 16),
                        Math.min(minSection.z(), sectionZ * 16));
                final Vec newMax = new Vec(Math.max(minSection.x() + width * 16, sectionX * 16 + 16),
                        Math.max(minSection.y() + height * 16, sectionY * 16 + 16),
                        Math.max(minSection.z() + depth * 16, sectionZ * 16 + 16));
                final int newWidth = globalToChunk(newMax.x() - newMin.x());
                final int newHeight = globalToChunk(newMax.y() - newMin.y());
                final int newDepth = globalToChunk(newMax.z() - newMin.z());
                // Resize
                GenerationUnit[] newSections = new GenerationUnit[newWidth * newHeight * newDepth];
                // Copy old sections
                for (GenerationUnit s : sections) {
                    final Point start = s.absoluteStart();
                    final int newX = globalToChunk(start.x() - newMin.x());
                    final int newY = globalToChunk(start.y() - newMin.y());
                    final int newZ = globalToChunk(start.z() - newMin.z());
                    final int index = findIndex(newWidth, newHeight, newDepth, newX, newY, newZ);
                    newSections[index] = s;
                }
                // Fill new sections
                final int startX = newMin.sectionX();
                final int startY = newMin.sectionY();
                final int startZ = newMin.sectionZ();
                for (int i = 0; i < newSections.length; i++) {
                    if (newSections[i] == null) {
                        final Vec coordinates = to3D(i, newWidth, newHeight, newDepth);
                        final int newX = coordinates.blockX() + startX;
                        final int newY = coordinates.blockY() + startY;
                        final int newZ = coordinates.blockZ() + startZ;
                        final GenerationUnit unit = section(biomeRegistry, new GenSection(), newX, newY, newZ, true);
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

    public record UnitImpl(DynamicRegistry<Biome> biomeRegistry, UnitModifier modifier,
                           Vec size, Vec absoluteStart, Vec absoluteEnd,
                           List<GenerationUnit> divided,
                           List<UnitImpl> forks) implements GenerationUnit {
        @Override
        public GenerationUnit fork(Point start, Point end) {
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
            // Z -> Y -> X order is important for indexing
            for (int sectionZ = minSectionZ; sectionZ < maxSectionZ; sectionZ++) {
                for (int sectionY = minSectionY; sectionY < maxSectionY; sectionY++) {
                    for (int sectionX = minSectionX; sectionX < maxSectionX; sectionX++) {
                        final GenerationUnit unit = section(biomeRegistry, new GenSection(), sectionX, sectionY, sectionZ, true);
                        units[index++] = unit;
                    }
                }
            }
            final List<GenerationUnit> sections = List.of(units);
            final Vec startSection = Vec.SECTION.mul(minSectionX, minSectionY, minSectionZ);
            return registerFork(startSection, sections, width, height, depth);
        }

        @Override
        public void fork(Consumer<Block.Setter> consumer) {
            DynamicFork dynamicFork = new DynamicFork(biomeRegistry);
            consumer.accept(dynamicFork);
            final Vec startSection = dynamicFork.minSection;
            if (startSection == null)
                return; // No block has been placed
            final int width = dynamicFork.width;
            final int height = dynamicFork.height;
            final int depth = dynamicFork.depth;
            final List<GenerationUnit> sections = dynamicFork.sections;
            registerFork(startSection, sections, width, height, depth);
        }

        @Override
        public List<GenerationUnit> subdivide() {
            return Objects.requireNonNullElseGet(divided, GenerationUnit.super::subdivide);
        }

        private GenerationUnit registerFork(Vec start, List<GenerationUnit> sections,
                                            int width, int height, int depth) {
            final Vec end = start.add(width * 16, height * 16, depth * 16);
            final Vec size = end.sub(start);
            final AreaModifierImpl modifier = new AreaModifierImpl(size, start, end, width, height, depth, sections);
            final UnitImpl fork = new UnitImpl(biomeRegistry, modifier, size, start, end, sections, forks);
            forks.add(fork);
            return fork;
        }
    }

    public record SectionModifierImpl(DynamicRegistry<Biome> biomeRegistry, Vec start, Vec end,
                                      GenSection genSection, boolean fork) implements GenericModifier {

        @Override
        public void setBiome(int x, int y, int z, RegistryKey<Biome> biome) {
            if (fork) throw new IllegalStateException("Cannot modify biomes of a fork");
            final int id = biomeRegistry.getId(biome);
            Check.argCondition(id == -1, "Biome has not been registered: {0}", biome);
            this.genSection.biomes.set(
                    globalToSectionRelative(x) / 4,
                    globalToSectionRelative(y) / 4,
                    globalToSectionRelative(z) / 4, id);
        }

        @Override
        public void setBlock(int x, int y, int z, Block block) {
            final int localX = globalToSectionRelative(x);
            final int localY = globalToSectionRelative(y);
            final int localZ = globalToSectionRelative(z);
            handleCache(localX, localY, localZ, block);
            this.genSection.blocks.set(localX, localY, localZ, retrieveBlockId(block));
        }

        @Override
        public void setRelative(int x, int y, int z, Block block) {
            handleCache(x, y, z, block);
            this.genSection.blocks.set(x, y, z, retrieveBlockId(block));
        }

        @Override
        public void setAllRelative(Supplier supplier) {
            this.genSection.blocks.setAll((x, y, z) -> {
                final Block block = supplier.get(x, y, z);
                handleCache(x, y, z, block);
                return retrieveBlockId(block);
            });
        }

        @Override
        public void fill(Block block) {
            if (requireCache(block)) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            this.genSection.specials.put(chunkBlockIndex(x, y, z), block);
                        }
                    }
                }
            }
            this.genSection.blocks.fill(retrieveBlockId(block));
        }

        @Override
        public void fillBiome(RegistryKey<Biome> biome) {
            if (fork) throw new IllegalStateException("Cannot modify biomes of a fork");
            final int id = biomeRegistry.getId(biome);
            Check.argCondition(id == -1, "Biome has not been registered: {0}", biome);
            this.genSection.biomes.fill(id);
        }

        @Override
        public Vec size() {
            return Vec.SECTION;
        }

        private int retrieveBlockId(Block block) {
            final int stateId = block.stateId();
            return fork ? stateId + 1 : stateId;
        }

        private void handleCache(int x, int y, int z, Block block) {
            if (requireCache(block)) {
                this.genSection.specials.put(chunkBlockIndex(x, y, z), block);
            } else if (!genSection.specials.isEmpty()) {
                this.genSection.specials.remove(chunkBlockIndex(x, y, z));
            }
        }

        private boolean requireCache(Block block) {
            return block.hasNbt() || block.handler() != null || block.registry().isBlockEntity();
        }
    }

    public record AreaModifierImpl(Vec size, Vec start, Vec end,
                                   int width, int height, int depth,
                                   List<GenerationUnit> sections) implements GenericModifier {
        @Override
        public void setBlock(int x, int y, int z, Block block) {
            checkBorder(x, y, z);
            final GenerationUnit section = findAbsoluteSection(x, y, z);
            y -= start.y();
            section.modifier().setBlock(x, y, z, block);
        }

        @Override
        public void setBiome(int x, int y, int z, RegistryKey<Biome> biome) {
            checkBorder(x, y, z);
            final GenerationUnit section = findAbsoluteSection(x, y, z);
            y -= start.y();
            section.modifier().setBiome(x, y, z, biome);
        }

        @Override
        public void setRelative(int x, int y, int z, Block block) {
            if (x < 0 || x >= size.x() || y < 0 || y >= size.y() || z < 0 || z >= size.z()) {
                throw new IllegalArgumentException("x, y and z must be in the chunk: " + x + ", " + y + ", " + z);
            }
            final GenerationUnit section = findRelativeSection(x, y, z);
            x = globalToSectionRelative(x);
            y = globalToSectionRelative(y);
            z = globalToSectionRelative(z);
            section.modifier().setBlock(x, y, z, block);
        }

        @Override
        public void setAll(Supplier supplier) {
            for (GenerationUnit section : sections) {
                final Point start = section.absoluteStart();
                final int startX = start.blockX();
                final int startY = start.blockY();
                final int startZ = start.blockZ();
                section.modifier().setAllRelative((x, y, z) ->
                        supplier.get(x + startX, y + startY, z + startZ));
            }
        }

        @Override
        public void setAllRelative(Supplier supplier) {
            final Point start = this.start;
            for (GenerationUnit section : sections) {
                final Point sectionStart = section.absoluteStart();
                final int offsetX = sectionStart.blockX() - start.blockX();
                final int offsetY = sectionStart.blockY() - start.blockY();
                final int offsetZ = sectionStart.blockZ() - start.blockZ();
                section.modifier().setAllRelative((x, y, z) ->
                        supplier.get(x + offsetX, y + offsetY, z + offsetZ));
            }
        }

        @Override
        public void fill(Block block) {
            for (GenerationUnit section : sections) {
                section.modifier().fill(block);
            }
        }

        @Override
        public void fillBiome(RegistryKey<Biome> biome) {
            for (GenerationUnit section : sections) {
                section.modifier().fillBiome(biome);
            }
        }

        @Override
        public void fillHeight(int minHeight, int maxHeight, Block block) {
            final Vec start = this.start;
            final int width = this.width, depth = this.depth;
            final int startX = start.blockX(), startZ = start.blockZ();
            final int minMultiple = floorSection(minHeight);
            final int maxMultiple = ceilSection(maxHeight);
            final boolean startOffset = minMultiple != minHeight;
            final boolean endOffset = maxMultiple != maxHeight;
            if (startOffset || endOffset) {
                final int firstFill = Math.min(minMultiple + 16, maxHeight);
                final int lastFill = startOffset ? Math.max(firstFill, floorSection(maxHeight)) : floorSection(maxHeight);
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        final int sectionX = startX + x * 16;
                        final int sectionZ = startZ + z * 16;
                        // Fill start
                        if (startOffset) {
                            final GenerationUnit section = findAbsoluteSection(sectionX, minMultiple, sectionZ);
                            section.modifier().fillHeight(minHeight, firstFill, block);
                        }
                        // Fill end
                        if (endOffset) {
                            final GenerationUnit section = findAbsoluteSection(sectionX, maxHeight, sectionZ);
                            section.modifier().fillHeight(lastFill, maxHeight, block);
                        }
                    }
                }
            }
            // Middle sections (to fill)
            final int startSection = (minMultiple) / 16 + (startOffset ? 1 : 0);
            final int endSection = (maxMultiple) / 16 + (endOffset ? -1 : 0);
            for (int i = startSection; i < endSection; i++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        final GenerationUnit section = findAbsoluteSection(startX + x * 16, i * 16, startZ + z * 16);
                        section.modifier().fill(block);
                    }
                }
            }
        }

        private GenerationUnit findAbsoluteSection(int x, int y, int z) {
            return findAbsolute(sections, start, width, height, depth, x, y, z);
        }

        private GenerationUnit findRelativeSection(int x, int y, int z) {
            return findAbsolute(sections, Vec.ZERO, width, height, depth, x, y, z);
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
        Vec size();

        Vec start();

        Vec end();

        @Override
        default void setAll(Supplier supplier) {
            final Vec start = start(), end = end();
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
        default void setAllRelative(Supplier supplier) {
            final Vec size = size();
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
        default void fill(Block block) {
            fill(start(), end(), block);
        }

        @Override
        default void fill(Point start, Point end, Block block) {
            final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
            final int endX = end.blockX(), endY = end.blockY(), endZ = end.blockZ();
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    for (int z = startZ; z < endZ; z++) {
                        setBlock(x, y, z, block);
                    }
                }
            }
        }

        @Override
        default void fillHeight(int minHeight, int maxHeight, Block block) {
            final Vec start = start();
            final Vec end = end();
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

    private static GenerationUnit findAbsolute(List<GenerationUnit> units, Vec start,
                                               int width, int height, int depth,
                                               int x, int y, int z) {
        final int sectionX = globalToChunk(x - start.x());
        final int sectionY = globalToChunk(y - start.y());
        final int sectionZ = globalToChunk(z - start.z());
        final int index = findIndex(width, height, depth, sectionX, sectionY, sectionZ);
        return units.get(index);
    }

    private static int findIndex(int width, int height, int depth,
                                 int x, int y, int z) {
        assert width > 0 && height > 0 && depth > 0;
        return (z * width * height) + (y * width) + x;
    }

    private static Vec to3D(int idx, int width, int height, int depth) {
        final int z = idx / (width * height);
        idx -= (z * width * height);
        final int y = idx / width;
        final int x = idx % width;
        return new Vec(x, y, z);
    }
}
