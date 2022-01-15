package net.minestom.jmh.palette;

import net.minestom.server.instance.palette.Palette;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class PaletteGetPresentBenchmark {

    @Param({"0", "0.25", "0.5", "0.75", "1"})
    public double fullness;

    private Palette palette;

    @Setup
    public void setup() {
        palette = Palette.blocks();
        var random = new Random(18932365);
        final int dimension = palette.dimension();
        for (int y = 0; y < dimension; y++)
            for (int z = 0; z < dimension; z++)
                for (int x = 0; x < dimension; x++)
                    if (random.nextDouble() < fullness)
                        palette.set(x, y, z, random.nextInt(1, 16));
    }

    @Benchmark
    public void readAll(Blackhole blackHole) {
        palette.getAll((x, y, z, value) -> blackHole.consume(value));
    }

    @Benchmark
    public void readAllPresent(Blackhole blackHole) {
        palette.getAllPresent((x, y, z, value) -> blackHole.consume(value));
    }

    @Benchmark
    public void readAllPresentAlt(Blackhole blackHole) {
        palette.getAll((x, y, z, value) -> {
            if (value != 0) {
                blackHole.consume(value);
            }
        });
    }
}
