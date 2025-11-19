package net.minestom.server.utils;

import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.ChunkRange;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnvTest
public class ChunkUtilsTest {

    @ParameterizedTest
    @MethodSource("testForDifferingChunksInRangeParams")
    public void testForDifferingChunksInRange(int nx, int nz, int ox, int oz, int r) {
        final Set<ChunkCoordinate> n = new HashSet<>();
        final Set<ChunkCoordinate> o = new HashSet<>();
        ChunkRange.chunksInRange(nx, nz, r, (x, z) -> n.add(new ChunkCoordinate(x, z)));
        ChunkRange.chunksInRange(ox, oz, r, (x, z) -> o.add(new ChunkCoordinate(x, z)));

        final List<ChunkCoordinate> actualNew = new ArrayList<>();
        final List<ChunkCoordinate> actualOld = new ArrayList<>();
        ChunkRange.chunksInRangeDiffering(nx, nz, ox, oz, r, ((x, z) -> actualNew.add(new ChunkCoordinate(x, z))),
                ((x, z) -> actualOld.add(new ChunkCoordinate(x, z))));

        final Comparator<ChunkCoordinate> sorter = Comparator.comparingInt(ChunkCoordinate::x).thenComparingInt(ChunkCoordinate::z);
        final List<ChunkCoordinate> expectedNew = n.stream().filter(x -> !o.contains(x)).sorted(sorter).toList();
        final List<ChunkCoordinate> expectedOld = o.stream().filter(x -> !n.contains(x)).sorted(sorter).toList();

        Assertions.assertIterableEquals(expectedNew, actualNew.stream().sorted(sorter).toList());
        Assertions.assertIterableEquals(expectedOld, actualOld.stream().sorted(sorter).toList());
    }

    private static Stream<Arguments> testForDifferingChunksInRangeParams() {
        return Stream.of(
                Arguments.of(1, 0, 0, 0, 16),
                Arguments.of(1, 1, 0, 0, 16),
                Arguments.of(3, 1, 1, 0, 16),
                Arguments.of(10, 1, 3, 5, 16),
                Arguments.of(10, 10, -10, -10, 16),
                Arguments.of(1, 0, 0, 0, 3),
                Arguments.of(1, 1, 0, 0, 3),
                Arguments.of(3, 1, 1, 0, 3),
                Arguments.of(10, 1, 3, 5, 3),
                Arguments.of(10, 10, -10, -10, 3)
        );
    }

    private record ChunkCoordinate(int x, int z) {}

    @Test
    public void effectiveViewDistanceUsesServerFlagWhenNull() {
        int result = ChunkUtils.computeEffectiveViewDistance((byte) 10, null);
        assertEquals(Math.min(10, ServerFlag.CHUNK_VIEW_DISTANCE) + 1, result);
    }

    @Test
    public void effectiveViewDistanceUsesInstanceViewDistance(Env env) {
        Instance instance = env.createFlatInstance();
        instance.viewDistance(8);
        
        assertEquals(9, ChunkUtils.computeEffectiveViewDistance((byte) 16, instance));
    }

    @Test
    public void serverViewDistanceUsesServerFlagWhenNull() {
        int result = ChunkUtils.computeServerViewDistance(null);
        assertEquals(MathUtils.clamp(ServerFlag.CHUNK_VIEW_DISTANCE, 2, 32), result);
    }

    @Test
    public void serverViewDistanceClampsValue(Env env) {
        Instance instance = env.createFlatInstance();
        
        instance.viewDistance(1);
        assertEquals(2, ChunkUtils.computeServerViewDistance(instance));
        
        instance.viewDistance(50);
        assertEquals(32, ChunkUtils.computeServerViewDistance(instance));
    }
}
