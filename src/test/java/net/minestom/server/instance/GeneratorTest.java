package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.world.biomes.Biome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static net.minestom.server.instance.GeneratorImpl.unit;
import static net.minestom.server.utils.chunk.ChunkUtils.ceilSection;
import static net.minestom.server.utils.chunk.ChunkUtils.floorSection;
import static org.junit.jupiter.api.Assertions.*;

public class GeneratorTest {

    @Test
    public void unitSize() {
        assertDoesNotThrow(() -> dummyUnit(Vec.ZERO, new Vec(16)));
        assertDoesNotThrow(() -> dummyUnit(new Vec(16), new Vec(32)));
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
    public void sectionSize() {
        final int sectionX = 3;
        final int sectionY = -5;
        final int sectionZ = -2;
        GenerationUnit section = GeneratorImpl.section(Section.inMemory(), sectionX, sectionY, sectionZ);
        assertEquals(new Vec(16), section.size());
        assertEquals(new Vec(sectionX * 16, sectionY * 16, sectionZ * 16), section.absoluteStart());
        assertEquals(new Vec(sectionX * 16 + 16, sectionY * 16 + 16, sectionZ * 16 + 16), section.absoluteEnd());
    }

    static GenerationUnit dummyUnit(Point start, Point end) {
        return unit(null, start, end, null);
    }
}
