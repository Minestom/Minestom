package net.minestom.server.coordinate;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@State(Scope.Thread)
@Threads(2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(2)
@Warmup(time = 2, iterations = 10)
@Measurement(time = 6, iterations = 100)
public class SingleCoordConversionTest {
    private static final int CHUNK_X = 0;
    private static final int CHUNK_Y = 0;

    private int zeroIndex;

    @Setup
    public void setup() {
        zeroIndex = CoordConversion.chunkBlockIndex(0, 0, 0);
    }

    @Benchmark
    public void chunkBlockIndexGetGlobalSingle(Blackhole blackhole) {
        blackhole.consume(CoordConversion.chunkBlockIndexGetGlobal(zeroIndex, CHUNK_X, CHUNK_Y));
    }

    @Benchmark
    public void chunkBlockIndexSingle(Blackhole blackhole) {
        blackhole.consume(CoordConversion.chunkBlockIndex(0, 0, 0));
    }

    @TearDown
    public void tearDown(Blackhole blackhole) {
        blackhole.consume(zeroIndex);
    }
}
