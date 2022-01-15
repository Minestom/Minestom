package net.minestom.jmh.palette;

import net.minestom.server.instance.palette.Palette;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class PaletteReplaceBenchmark {

    //@Param({"4", "16"})
    //public int dimension;

    private Palette palette;

    @Setup
    public void setup() {
        // FIXME: StackOverflowError
        // palette = Palette.newPalette(dimension, 15, 4, 1);
        palette = Palette.blocks();
        palette.setAll((x, y, z) -> x + y + z + 1);
    }

    @Benchmark
    public void replaceAll() {
        palette.replaceAll((x, y, z, value) -> value + 1);
    }

    @Benchmark
    public void replaceLoop() {
        final int dimension = palette.dimension();
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    palette.replace(x, y, z, value -> value + 1);
                }
            }
        }
    }
}
