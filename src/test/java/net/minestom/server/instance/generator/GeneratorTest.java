package net.minestom.server.instance.generator;

import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GeneratorImpl.GenSection;
import net.minestom.server.utils.MathUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static net.minestom.server.coordinate.CoordConversion.ceilSection;
import static net.minestom.server.coordinate.CoordConversion.floorSection;
import static net.minestom.server.instance.generator.GeneratorImpl.unit;
import static org.junit.jupiter.api.Assertions.*;

public class GeneratorTest {
    @Test
    public void unitSize() {
        assertDoesNotThrow(() -> dummyUnit(Vec.ZERO, Vec.SECTION));
        assertDoesNotThrow(() -> dummyUnit(Vec.SECTION, new Vec(32)));
        assertThrows(IllegalArgumentException.class, () -> dummyUnit(new Vec(15), Vec.ZERO));
        assertThrows(IllegalArgumentException.class, () -> dummyUnit(new Vec(15), new Vec(32)));
        assertThrows(IllegalArgumentException.class, () -> dummyUnit(new Vec(15), new Vec(31)));
        assertThrows(IllegalArgumentException.class, () -> dummyUnit(Vec.ZERO, new Vec(15)));
    }

    @ParameterizedTest
    @MethodSource("sectionFloorParam")
    public void sectionFloor(int expected, int input) {
        assertEquals(expected, floorSection(input), "floorSection(" + input + ")");
    }

    private static Stream<Arguments> sectionFloorParam() {
        return Stream.of(Arguments.of(-32, -32),
                Arguments.of(-32, -31),
                Arguments.of(-32, -17),
                Arguments.of(-16, -16),
                Arguments.of(-16, -15),
                Arguments.of(0, 0),
                Arguments.of(0, 1),
                Arguments.of(0, 2),
                Arguments.of(16, 16),
                Arguments.of(16, 17));
    }

    @ParameterizedTest
    @MethodSource("sectionCeilParam")
    public void sectionCeil(int expected, int input) {
        assertEquals(expected, ceilSection(input), "ceilSection(" + input + ")");
    }

    private static Stream<Arguments> sectionCeilParam() {
        return Stream.of(Arguments.of(-32, -32),
                Arguments.of(-16, -31),
                Arguments.of(-16, -17),
                Arguments.of(-16, -16),
                Arguments.of(-0, -15),
                Arguments.of(0, 0),
                Arguments.of(16, 1),
                Arguments.of(16, 2),
                Arguments.of(16, 16),
                Arguments.of(32, 17));
    }

    @Test
    public void chunkSize() {
        final int minSection = 0;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        GenerationUnit chunk = GeneratorImpl.chunk(null, sections, chunkX, minSection, chunkZ);
        assertEquals(new Vec(16, sectionCount * 16, 16), chunk.size());
        assertEquals(new Vec(chunkX * 16, minSection * 16, chunkZ * 16), chunk.absoluteStart());
        assertEquals(new Vec(chunkX * 16 + 16, maxSection * 16, chunkZ * 16 + 16), chunk.absoluteEnd());
    }

    @Test
    public void chunkSizeNeg() {
        final int minSection = -1;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        GenerationUnit chunk = GeneratorImpl.chunk(null, sections, chunkX, minSection, chunkZ);
        assertEquals(new Vec(16, sectionCount * 16, 16), chunk.size());
        assertEquals(new Vec(chunkX * 16, minSection * 16, chunkZ * 16), chunk.absoluteStart());
        assertEquals(new Vec(chunkX * 16 + 16, maxSection * 16, chunkZ * 16 + 16), chunk.absoluteEnd());
    }

    @Test
    public void sectionSize() {
        final int sectionX = 3;
        final int sectionY = -5;
        final int sectionZ = -2;
        GenerationUnit section = GeneratorImpl.section(null, new GenSection(), sectionX, sectionY, sectionZ);
        assertEquals(Vec.SECTION, section.size());
        assertEquals(new Vec(sectionX * 16, sectionY * 16, sectionZ * 16), section.absoluteStart());
        assertEquals(new Vec(sectionX * 16 + 16, sectionY * 16 + 16, sectionZ * 16 + 16), section.absoluteEnd());
    }

    @Test
    public void chunkSubdivide() {
        final int minSection = -1;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        GenerationUnit chunk = GeneratorImpl.chunk(null, sections, chunkX, minSection, chunkZ);
        var subUnits = chunk.subdivide();
        assertEquals(sectionCount, subUnits.size());
        for (int i = 0; i < sectionCount; i++) {
            var subUnit = subUnits.get(i);
            assertEquals(Vec.SECTION, subUnit.size());
            assertEquals(new Vec(chunkX * 16, (i + minSection) * 16, chunkZ * 16), subUnit.absoluteStart());
            assertEquals(subUnit.absoluteStart().add(16), subUnit.absoluteEnd());
        }
    }

    @Test
    public void chunkAbsolute() {
        final int minSection = 0;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        var chunkUnits = GeneratorImpl.chunk(null, sections, chunkX, minSection, chunkZ);
        Generator generator = chunk -> {
            var modifier = chunk.modifier();
            assertThrows(Exception.class, () -> modifier.setBlock(0, 0, 0, Block.STONE), "Block outside of chunk");
            modifier.setBlock(56, 0, -25, Block.STONE);
            modifier.setBlock(56, 17, -25, Block.STONE);
        };
        generator.generate(chunkUnits);
        assertEquals(Block.STONE.stateId(), sections[0].blocks().get(8, 0, 7));
        assertEquals(Block.STONE.stateId(), sections[1].blocks().get(8, 1, 7));
    }

    @Test
    public void chunkAbsoluteAll() {
        final int minSection = 0;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        var chunkUnits = GeneratorImpl.chunk(null, sections, chunkX, minSection, chunkZ);
        Generator generator = chunk -> {
            var modifier = chunk.modifier();
            Set<Point> points = new HashSet<>();
            modifier.setAll((x, y, z) -> {
                assertTrue(points.add(new Vec(x, y, z)), "Duplicate point: " + x + ", " + y + ", " + z);
                assertEquals(chunkX, CoordConversion.globalToChunk(x));
                assertEquals(chunkZ, CoordConversion.globalToChunk(z));
                return Block.STONE;
            });
            assertEquals(16 * 16 * 16 * sectionCount, points.size());
        };
        generator.generate(chunkUnits);
        for (var section : sections) {
            section.blocks().getAll((x, y, z, value) ->
                    assertEquals(Block.STONE.stateId(), value));
        }
    }

    @Test
    public void chunkRelative() {
        final int minSection = -1;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        var chunkUnits = GeneratorImpl.chunk(null, sections, chunkX, minSection, chunkZ);
        Generator generator = chunk -> {
            var modifier = chunk.modifier();
            assertThrows(Exception.class, () -> modifier.setRelative(-1, 0, 0, Block.STONE));
            assertThrows(Exception.class, () -> modifier.setRelative(16, 0, 0, Block.STONE));
            assertThrows(Exception.class, () -> modifier.setRelative(17, 0, 0, Block.STONE));
            assertThrows(Exception.class, () -> modifier.setRelative(0, -1, 0, Block.STONE));
            assertThrows(Exception.class, () -> modifier.setRelative(0, 96, 0, Block.STONE));
            modifier.setRelative(0, 0, 0, Block.STONE);
            modifier.setRelative(0, 16, 2, Block.STONE);
            modifier.setRelative(5, 33, 5, Block.STONE);
        };
        generator.generate(chunkUnits);
        assertEquals(Block.STONE.stateId(), sections[0].blocks().get(0, 0, 0));
        assertEquals(Block.STONE.stateId(), sections[1].blocks().get(0, 0, 2));
        assertEquals(Block.STONE.stateId(), sections[2].blocks().get(5, 1, 5));
    }

    @Test
    public void chunkRelativeAll() {
        final int minSection = -1;
        final int maxSection = 5;
        final int chunkX = 3;
        final int chunkZ = -2;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        var chunkUnits = GeneratorImpl.chunk(null, sections, chunkX, minSection, chunkZ);
        Generator generator = chunk -> {
            var modifier = chunk.modifier();
            Set<Point> points = new HashSet<>();
            modifier.setAllRelative((x, y, z) -> {
                assertTrue(MathUtils.isBetween(x, 0, 16), "x out of bounds: " + x);
                assertTrue(MathUtils.isBetween(y, 0, sectionCount * 16), "y out of bounds: " + y);
                assertTrue(MathUtils.isBetween(z, 0, 16), "z out of bounds: " + z);
                assertTrue(points.add(new Vec(x, y, z)), "Duplicate point: " + x + ", " + y + ", " + z);
                return Block.STONE;
            });
            assertEquals(16 * 16 * 16 * sectionCount, points.size());
        };
        generator.generate(chunkUnits);
        for (var section : sections) {
            section.blocks().getAll((x, y, z, value) ->
                    assertEquals(Block.STONE.stateId(), value));
        }
    }

    @Test
    public void chunkFillHeightExact() {
        final int minSection = -1;
        final int maxSection = 5;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        var chunkUnits = GeneratorImpl.chunk(null, sections, 3, minSection, -2);
        Generator generator = chunk -> chunk.modifier().fillHeight(0, 32, Block.STONE);
        generator.generate(chunkUnits);

        AtomicInteger index = new AtomicInteger(minSection);
        for (var section : sections) {
            section.blocks().getAll((x, y, z, value) -> {
                if (index.get() == 0 || index.get() == 1) {
                    assertEquals(Block.STONE.stateId(), value, "filling failed for section " + index.get());
                } else {
                    assertEquals(0, value);
                }
            });
            index.incrementAndGet();
        }
    }

    @Test
    public void chunkFillHeightOneOff() {
        final int minSection = -1;
        final int maxSection = 5;
        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        var chunkUnits = GeneratorImpl.chunk(null, sections, 3, minSection, -2);
        Generator generator = chunk -> chunk.modifier().fillHeight(1, 33, Block.STONE);
        generator.generate(chunkUnits);

        AtomicInteger index = new AtomicInteger(minSection);
        for (var section : sections) {
            section.blocks().getAll((x, y, z, value) -> {
                Block expected;
                if (index.get() == 0) {
                    if (y > 0) {
                        expected = Block.STONE;
                    } else {
                        expected = Block.AIR;
                    }
                } else if (index.get() == 1) {
                    expected = Block.STONE;
                } else if (index.get() == 2) {
                    if (y == 0) {
                        expected = Block.STONE;
                    } else {
                        expected = Block.AIR;
                    }
                } else {
                    expected = Block.AIR;
                }
                assertEquals(expected.stateId(), value, "fail for coordinate: " + x + "," + y + "," + z + " for index " + index.get());
            });
            index.incrementAndGet();
        }
    }

    @Test
    public void sectionFill() {
        GenSection section = new GenSection();
        var chunkUnit = GeneratorImpl.section(null, section, -1, -1, 0);
        Generator generator = chunk -> chunk.modifier().fill(Block.STONE);
        generator.generate(chunkUnit);
        section.blocks().getAll((x, y, z, value) ->
                assertEquals(Block.STONE.stateId(), value));
    }

    @Test
    public void testForkAcrossBorders() {
        final int minSection = -4;
        final int maxSection = 4;

        final int sectionCount = maxSection - minSection;
        GenSection[] sections = new GenSection[sectionCount];
        Arrays.setAll(sections, i -> new GenSection());
        var chunkUnits = GeneratorImpl.chunk(null, sections, 0, minSection, 0);
        Generator generator = unit -> {
            if (unit.absoluteStart().x() == 0 && unit.absoluteStart().z() == 0) {
                var start = unit.absoluteStart().withY(0).add(0, 0, 8).sub(2, 2, 0);
                var end = unit.absoluteStart().withY(0).add(0, 0, 8).add(2, 2, 1);

                var fork = unit.fork(start, end);
                fork.modifier().fill(start, end, Block.STONE);
            }
        };
        generator.generate(chunkUnits);

        Set<Point> stones = new HashSet<>();

        for (GeneratorImpl.UnitImpl fork : chunkUnits.forks()) {
            GeneratorImpl.AreaModifierImpl impl = (GeneratorImpl.AreaModifierImpl) fork.modifier();

            for (GenerationUnit section : impl.sections()) {
                GeneratorImpl.UnitImpl unit = (GeneratorImpl.UnitImpl) section;
                GeneratorImpl.SectionModifierImpl modifier = (GeneratorImpl.SectionModifierImpl) unit.modifier();

                modifier.genSection().blocks().getAllPresent((x, y, z, state) -> {
                    final Point blockPos = modifier.start().add(x, y, z);
                    stones.add(blockPos);
                });
            }
        }

        var expectedStones = Set.of(
                new Vec(-2, -2, 8),
                new Vec(-2, -1, 8),
                new Vec(-2, 0, 8),
                new Vec(-2, 1, 8),
                new Vec(-1, -2, 8),
                new Vec(-1, -1, 8),
                new Vec(-1, 0, 8),
                new Vec(-1, 1, 8),
                new Vec(0, -2, 8),
                new Vec(0, -1, 8),
                new Vec(0, 0, 8),
                new Vec(0, 1, 8),
                new Vec(1, -2, 8),
                new Vec(1, -1, 8),
                new Vec(1, 0, 8),
                new Vec(1, 1, 8)
        );

        assertEquals(expectedStones.size(), stones.size());
        assertEquals(expectedStones, stones);
    }

    static GenerationUnit dummyUnit(Point start, Point end) {
        return unit(null, null, start, end, null);
    }
}
