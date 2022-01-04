package net.minestom.jmh.palette;

import net.minestom.server.instance.palette.Palette;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class PaletteSetBenchmark {

    @Param({"4", "16"})
    public int dimension;

    private Palette palette;

    @Setup
    public void setup() {
        palette = Palette.newPalette(dimension, 15, 4, 1);
    }

    @Benchmark
    public void incrWrite() {
        int value = 0;
        final int dimension = palette.dimension();
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    palette.set(x, y, z, value++);
                }
            }
        }
    }

    @Benchmark
    public void constantWrite() {
        final int dimension = palette.dimension();
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    palette.set(x, y, z, 5);
                }
            }
        }
    }

    @Benchmark
    public void constantWriteAll() {
        palette.setAll((x, y, z) -> 5);
    }

    @Benchmark
    public void fill() {
        palette.fill(5);
    }
}
