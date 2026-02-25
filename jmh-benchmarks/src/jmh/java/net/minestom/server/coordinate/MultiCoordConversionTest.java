package net.minestom.server.coordinate;

import net.minestom.server.instance.Chunk;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@Threads(2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(time = 2, iterations = 5)
@Measurement(time = 6, iterations = 8)
public class MultiCoordConversionTest {
    private static final int CHUNK_X = 0;
    private static final int CHUNK_Y = 0;

    @Param({"0", "-16", "-64"})
    public int yMin;
    @Param({"16", "64", "320"})
    public int yMaX;

    private int[] blockIndexes;

    @Setup
    public void setup() {
        blockIndexes = new int[Chunk.CHUNK_SIZE_Z * (Math.abs(yMin) + yMaX) * Chunk.CHUNK_SIZE_X];

        final int yMinAbs = Math.abs(yMin);
        for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
            for (int y = yMin; y < yMaX; y++) {
                for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                    blockIndexes[x + (y + yMinAbs) + z] = CoordConversion.chunkBlockIndex(x, y, z);
                }
            }
        }
    }

    @Benchmark
    public void chunkBlockIndexGetGlobalMulti(Blackhole blackhole) {
        for (final int index : blockIndexes) {
            blackhole.consume(CoordConversion.chunkBlockIndexGetGlobal(index, CHUNK_X, CHUNK_Y));
        }
    }

    @Benchmark
    public void chunkBlockIndexMulti(Blackhole blackhole) {
        for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
            for (int y = yMin; y < yMaX; y++) {
                for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                    blackhole.consume(CoordConversion.chunkBlockIndex(x, y, z));
                }
            }
        }
    }

    @TearDown
    public void teardown(Blackhole blackhole) {
        blackhole.consume(blockIndexes);
    }
}
