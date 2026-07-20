package net.minestom.server.instance.generator;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.coordinate.Area;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.palette.Palette;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

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
        final BlockVec start = BlockVec.SECTION.mul(sectionX, sectionY, sectionZ);
        final BlockVec end = start.add(BlockVec.SECTION);
        final UnitModifier modifier = new SectionModifierImpl(biomeRegistry, start, end, section, fork);
        return unit(biomeRegistry, modifier, start, end, null);
    }

    public static GenerationUnit section(DynamicRegistry<Biome> biomeRegistry, GenSection section, int sectionX, int sectionY, int sectionZ) {
        return section(biomeRegistry, section, sectionX, sectionY, sectionZ, false);
    }

    public static UnitImpl chunk(DynamicRegistry<Biome> biomeRegistry, GenSection[] chunkSections, int chunkX, int minSection, int chunkZ) {
        final BlockVec start = BlockVec.SECTION.mul(chunkX, minSection, chunkZ);
        return area(biomeRegistry, start, 1, chunkSections.length, 1, chunkSections);
    }

    public static UnitImpl area(DynamicRegistry<Biome> biomeRegistry, BlockVec start, int width, int height, int depth, GenSection[] areaSections) {
        Check.argCondition(width <= 0 || height <= 0 || depth <= 0, "Width, height and depth must be greater than 0, got {0}, {1}, {2}", width, height, depth);
        final int sectionCount = width * height * depth;
        Check.argCondition(sectionCount != areaSections.length, "Invalid section count, expected {0} but got {1}", sectionCount, areaSections.length);
        GenerationUnit[] sectionsArray = new GenerationUnit[sectionCount];
        final int startSectionX = start.sectionX(), startSectionY = start.sectionY(), startSectionZ = start.sectionZ();
        for (int i = 0; i < sectionCount; i++) {
            GenSection section = areaSections[i];
            final int sectionX = indexToX(i, width) + startSectionX;
            final int sectionY = indexToY(i, width, height) + startSectionY;
            final int sectionZ = indexToZ(i, width, height) + startSectionZ;
            final GenerationUnit sectionUnit = section(biomeRegistry, section, sectionX, sectionY, sectionZ);
            sectionsArray[i] = sectionUnit;
        }
        final List<GenerationUnit> sections = List.of(sectionsArray);
        final BlockVec size = BlockVec.SECTION.mul(width, height, depth);
        final BlockVec end = start.add(size);
        final UnitModifier modifier = new AreaModifierImpl(size, start, end, width, height, depth, sections);
        return unit(biomeRegistry, modifier, start, end, sections);
    }

    public static UnitImpl unit(DynamicRegistry<Biome> biomeRegistry, UnitModifier modifier, BlockVec start, BlockVec end,
                                @Nullable List<GenerationUnit> divided) {
        Check.argCondition(start.blockX() > end.blockX() || start.blockY() > end.blockY() || start.blockZ() > end.blockZ(), "absoluteStart must be before absoluteEnd, got {0} and {1}", start, end);
        Check.argCondition(start.blockX() % SECTION_SIZE != 0 || start.blockY() % SECTION_SIZE != 0 || start.blockZ() % SECTION_SIZE != 0, "absoluteStart must be a multiple of 16");
        Check.argCondition(end.blockX() % SECTION_SIZE != 0 || end.blockY() % SECTION_SIZE != 0 || end.blockZ() % SECTION_SIZE != 0, "absoluteEnd must be a multiple of 16");
        final BlockVec size = end.sub(start);
        return new UnitImpl(biomeRegistry, modifier, size, start, end, divided, new CopyOnWriteArrayList<>());
    }

    static final class DynamicFork implements Block.Setter {
        final DynamicRegistry<Biome> biomeRegistry;
        @Nullable Fork fork;

        record Fork(BlockVec minSection, int width, int height, int depth, List<GenerationUnit> sections) {}

        DynamicFork(DynamicRegistry<Biome> biomeRegistry) {
            this.biomeRegistry = biomeRegistry;
        }

        @Override
        public void setBlock(int x, int y, int z, Block block) {
            resize(x, y, z);
            final Fork fork = this.fork;
            GenerationUnit section = findAbsolute(fork.sections(), fork.minSection(), fork.width(), fork.height(), fork.depth(), x, y, z);
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
            final Fork fork = this.fork;
            if (fork == null) {
                var minSection = BlockVec.SECTION.mul(sectionX, sectionY, sectionZ);
                var sections = List.of(section(biomeRegistry, new GenSection(), sectionX, sectionY, sectionZ, true));
                this.fork = new Fork(minSection, 1, 1, 1, sections);
                return;
            }
            // Section exists, we need to check the bounds
            final BlockVec minSection = fork.minSection();
            final int maxX = minSection.blockX() + fork.width() * SECTION_SIZE;
            final int maxY = minSection.blockY() + fork.height() * SECTION_SIZE;
            final int maxZ = minSection.blockZ() + fork.depth() * SECTION_SIZE;
            if (x >= minSection.blockX() && y >= minSection.blockY() && z >= minSection.blockZ() &&
                    x < maxX && y < maxY && z < maxZ) {
                return; // inside bounds, no resize needed
            }
            // Find new min and max
            final BlockVec section = BlockVec.SECTION.mul(sectionX, sectionY, sectionZ);
            final BlockVec newMin = minSection.min(section);
            final BlockVec newMax = section.add(SECTION_SIZE).max(maxX, maxY, maxZ);
            // Determine new fork size
            final int newWidth = globalToChunk(newMax.blockX() - newMin.blockX());
            final int newHeight = globalToChunk(newMax.blockY() - newMin.blockY());
            final int newDepth = globalToChunk(newMax.blockZ() - newMin.blockZ());
            final int sectionCount = newWidth * newHeight * newDepth;
            // Resize
            @UnknownNullability GenerationUnit[] newSections = new GenerationUnit[sectionCount];
            // Copy old sections
            for (GenerationUnit s : fork.sections()) {
                final Point start = s.absoluteStart();
                final int newX = globalToChunk(start.blockX() - newMin.blockX());
                final int newY = globalToChunk(start.blockY() - newMin.blockY());
                final int newZ = globalToChunk(start.blockZ() - newMin.blockZ());
                final int index = findIndex(newWidth, newHeight, newDepth, newX, newY, newZ);
                newSections[index] = s;
            }
            // Fill new sections
            final int startX = newMin.sectionX();
            final int startY = newMin.sectionY();
            final int startZ = newMin.sectionZ();
            for (int i = 0; i < sectionCount; i++) {
                if (newSections[i] == null) {
                    final int newX = indexToX(i, newWidth) + startX;
                    final int newY = indexToY(i, newWidth, newHeight) + startY;
                    final int newZ = indexToZ(i, newWidth, newHeight) + startZ;
                    final GenerationUnit unit = section(biomeRegistry, new GenSection(), newX, newY, newZ, true);
                    newSections[i] = unit;
                }
            }
            this.fork = new Fork(newMin, newWidth, newHeight, newDepth, List.of(newSections));
        }
    }

    public record UnitImpl(DynamicRegistry<Biome> biomeRegistry, UnitModifier modifier,
                           BlockVec size, BlockVec absoluteStart, BlockVec absoluteEnd,
                           @Nullable List<GenerationUnit> divided,
                           List<UnitImpl> forks) implements GenerationUnit {
        @Override
        public GenerationUnit fork(Point start, Point end) {
            final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
            final int endX = end.blockX(), endY = end.blockY(), endZ = end.blockZ();

            final int minSectionX = floorSection(startX) / SECTION_SIZE, minSectionY = floorSection(startY) / SECTION_SIZE, minSectionZ = floorSection(startZ) / SECTION_SIZE;
            final int maxSectionX = ceilSection(endX) / SECTION_SIZE, maxSectionY = ceilSection(endY) / SECTION_SIZE, maxSectionZ = ceilSection(endZ) / SECTION_SIZE;

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
            final BlockVec startSection = BlockVec.SECTION.mul(minSectionX, minSectionY, minSectionZ);
            return registerFork(startSection, sections, width, height, depth);
        }

        @Override
        public void fork(Consumer<Block.Setter> consumer) {
            DynamicFork dynamicFork = new DynamicFork(biomeRegistry);
            consumer.accept(dynamicFork);
            final DynamicFork.Fork dynamicForkData = dynamicFork.fork;
            if (dynamicForkData == null)
                return; // No block has been placed
            registerFork(dynamicForkData.minSection(), dynamicForkData.sections(), dynamicForkData.width(), dynamicForkData.height(), dynamicForkData.depth());
        }

        @Override
        public List<GenerationUnit> subdivide() {
            return Objects.requireNonNullElseGet(divided, GenerationUnit.super::subdivide);
        }

        private GenerationUnit registerFork(BlockVec start, List<GenerationUnit> sections,
                                            int width, int height, int depth) {
            final BlockVec end = start.add(width * SECTION_SIZE, height * SECTION_SIZE, depth * SECTION_SIZE);
            final BlockVec size = end.sub(start);
            final AreaModifierImpl modifier = new AreaModifierImpl(size, start, end, width, height, depth, sections);
            final UnitImpl fork = new UnitImpl(biomeRegistry, modifier, size, start, end, sections, forks);
            forks.add(fork);
            return fork;
        }
    }

    public record SectionModifierImpl(DynamicRegistry<Biome> biomeRegistry, BlockVec start, BlockVec end,
                                      GenSection genSection, boolean fork) implements GenericModifier {

        @Override
        public void setBiome(int x, int y, int z, RegistryKey<Biome> biome) {
            Check.stateCondition(fork, "Cannot modify biomes of a fork");
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
            this.genSection.specials.clear();
            if (requireCache(block)) {
                for (int x = 0; x < SECTION_SIZE; x++) {
                    for (int y = 0; y < SECTION_SIZE; y++) {
                        for (int z = 0; z < SECTION_SIZE; z++) {
                            this.genSection.specials.put(chunkBlockIndex(x, y, z), block);
                        }
                    }
                }
            }
            this.genSection.blocks.fill(retrieveBlockId(block));
        }

        @Override
        public void fill(Point start, Point end, Block block) {
            final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
            final int endX = end.blockX(), endY = end.blockY(), endZ = end.blockZ();
            final int sectionStartX = this.start.blockX(), sectionStartY = this.start.blockY(), sectionStartZ = this.start.blockZ();
            final int sectionEndX = this.end.blockX(), sectionEndY = this.end.blockY(), sectionEndZ = this.end.blockZ();
            if (startX >= sectionStartX && startY >= sectionStartY && startZ >= sectionStartZ &&
                    endX <= sectionEndX && endY <= sectionEndY && endZ <= sectionEndZ) {
                fillRelative(startX - sectionStartX, startY - sectionStartY, startZ - sectionStartZ,
                        endX - sectionStartX, endY - sectionStartY, endZ - sectionStartZ, block);
            } else {
                for (int x = startX; x < endX; x++) {
                    for (int y = startY; y < endY; y++) {
                        for (int z = startZ; z < endZ; z++) {
                            setBlock(x, y, z, block);
                        }
                    }
                }
            }
        }

        @Override
        public void fillHeight(int minHeight, int maxHeight, Block block) {
            final int sectionStartY = start.blockY(), sectionEndY = end.blockY();
            final int localMinY = Math.max(minHeight, sectionStartY) - sectionStartY;
            final int localMaxY = Math.min(maxHeight, sectionEndY) - sectionStartY;
            if (localMinY >= localMaxY) return;
            fillRelative(0, localMinY, 0, 16, localMaxY, 16, block);
        }

        @Override
        public void fillBiome(RegistryKey<Biome> biome) {
            Check.stateCondition(fork, "Cannot modify biomes of a fork");
            final int id = biomeRegistry.getId(biome);
            Check.argCondition(id == -1, "Biome has not been registered: {0}", biome);
            this.genSection.biomes.fill(id);
        }

        @Override
        public BlockVec size() {
            return BlockVec.SECTION;
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

        private static boolean requireCache(Block block) {
            return block.hasNbt() || block.handler() != null || block.registry().isBlockEntity();
        }

        private void fillRelative(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Block block) {
            if (minX == 0 && minY == 0 && minZ == 0 && maxX == 16 && maxY == 16 && maxZ == 16) {
                fill(block);
                return;
            }
            final int stateId = retrieveBlockId(block);
            final boolean requireCache = requireCache(block);
            final boolean clearCache = !requireCache && !genSection.specials.isEmpty();
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    for (int z = minZ; z < maxZ; z++) {
                        if (requireCache) {
                            this.genSection.specials.put(chunkBlockIndex(x, y, z), block);
                        } else if (clearCache) {
                            this.genSection.specials.remove(chunkBlockIndex(x, y, z));
                        }
                        this.genSection.blocks.set(x, y, z, stateId);
                    }
                }
            }
        }
    }

    public record AreaModifierImpl(BlockVec size, BlockVec start, BlockVec end,
                                   int width, int height, int depth,
                                   List<GenerationUnit> sections) implements GenericModifier {
        @Override
        public void setBlock(int x, int y, int z, Block block) {
            checkBorder(x, y, z);
            final GenerationUnit section = findAbsoluteSection(x, y, z);
            y -= start.blockY();
            section.modifier().setBlock(x, y, z, block);
        }

        @Override
        public void setBiome(int x, int y, int z, RegistryKey<Biome> biome) {
            checkBorder(x, y, z);
            final GenerationUnit section = findAbsoluteSection(x, y, z);
            y -= start.blockY();
            section.modifier().setBiome(x, y, z, biome);
        }

        @Override
        public void setRelative(int x, int y, int z, Block block) {
            Check.argCondition(x < 0 || x >= size.blockX() || y < 0 || y >= size.blockY() || z < 0 || z >= size.blockZ(),
                    "x, y and z must be in the chunk: {0}, {1}, {2}", x, y, z);
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
                final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
                section.modifier().setAllRelative((x, y, z) -> supplier.get(x + startX, y + startY, z + startZ));
            }
        }

        @Override
        public void setAllRelative(Supplier supplier) {
            final Point start = this.start;
            final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
            for (GenerationUnit section : sections) {
                final Point sectionStart = section.absoluteStart();
                final int offsetX = sectionStart.blockX() - startX, offsetY = sectionStart.blockY() - startY, offsetZ = sectionStart.blockZ() - startZ;
                section.modifier().setAllRelative((x, y, z) -> supplier.get(x + offsetX, y + offsetY, z + offsetZ));
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
            final BlockVec start = this.start;
            final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
            final int endY = end.blockY();
            minHeight = Math.max(minHeight, startY);
            maxHeight = Math.min(maxHeight, endY);
            if (minHeight >= maxHeight) return;
            final int width = this.width, depth = this.depth;
            final int minMultiple = floorSection(minHeight);
            final int maxMultiple = ceilSection(maxHeight);
            final boolean startOffset = minMultiple != minHeight;
            final boolean endOffset = maxMultiple != maxHeight;
            if (startOffset || endOffset) {
                final int firstFill = Math.min(minMultiple + SECTION_SIZE, maxHeight);
                final int lastFill = startOffset ? Math.max(firstFill, floorSection(maxHeight)) : floorSection(maxHeight);
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        final int sectionX = startX + x * SECTION_SIZE;
                        final int sectionZ = startZ + z * SECTION_SIZE;
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
            final int startSection = minMultiple / SECTION_SIZE + (startOffset ? 1 : 0);
            final int endSection = maxMultiple / SECTION_SIZE + (endOffset ? -1 : 0);
            for (int i = startSection; i < endSection; i++) {
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        final GenerationUnit section = findAbsoluteSection(startX + x * SECTION_SIZE, i * SECTION_SIZE, startZ + z * SECTION_SIZE);
                        section.modifier().fill(block);
                    }
                }
            }
        }

        private GenerationUnit findAbsoluteSection(int x, int y, int z) {
            return findAbsolute(sections, start, width, height, depth, x, y, z);
        }

        private GenerationUnit findRelativeSection(int x, int y, int z) {
            return findAbsolute(sections, BlockVec.ZERO, width, height, depth, x, y, z);
        }

        private void checkBorder(int x, int y, int z) {
            final boolean outside = x < start.blockX() || x >= end.blockX()
                    || y < start.blockY() || y >= end.blockY()
                    || z < start.blockZ() || z >= end.blockZ();
            Check.argCondition(outside, "Invalid coordinates: {0}, {1}, {2} for area {3} {4}", x, y, z, start, end);
        }
    }

    sealed interface GenericModifier extends UnitModifier
            permits AreaModifierImpl, SectionModifierImpl {
        BlockVec size();

        BlockVec start();

        BlockVec end();

        @Override
        default void setAll(Supplier supplier) {
            final BlockVec start = start(), end = end();
            final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
            final int endX = end.blockX(), endY = end.blockY(), endZ = end.blockZ();
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    for (int z = startZ; z < endZ; z++) {
                        setBlock(x, y, z, supplier.get(x, y, z));
                    }
                }
            }
        }

        @Override
        default void setAllRelative(Supplier supplier) {
            final BlockVec size = size();
            final int endX = size.blockX(), endY = size.blockY(), endZ = size.blockZ();
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
            final BlockVec start = start();
            final BlockVec end = end();
            final int startY = start.blockY(), endY = end.blockY();
            if (startY >= minHeight && endY <= maxHeight) {
                // Fast path if the unit is fully contained in the height range
                fill(start, end, block);
            } else {
                // Slow path if the unit is not fully contained in the height range
                fill(start.withY(Math.max(minHeight, startY)), end.withY(Math.min(maxHeight, endY)), block);
            }
        }
    }

    private static GenerationUnit findAbsolute(List<GenerationUnit> units, BlockVec start,
                                               int width, int height, int depth,
                                               int x, int y, int z) {
        final int startX = start.blockX(), startY = start.blockY(), startZ = start.blockZ();
        final int sectionX = globalToChunk(x - startX), sectionY = globalToChunk(y - startY), sectionZ = globalToChunk(z - startZ);
        final int index = findIndex(width, height, depth, sectionX, sectionY, sectionZ);
        return units.get(index);
    }

    private static int findIndex(int width, int height, int depth,
                                 int x, int y, int z) {
        assert width > 0 && height > 0 && depth > 0;
        return (z * width * height) + (y * width) + x;
    }

    private static int indexToX(int index, int width) {
        return index % width;
    }

    private static int indexToY(int index, int width, int height) {
        return (index / width) % height;
    }

    private static int indexToZ(int index, int width, int height) {
        return index / (width * height);
    }
}
