package net.minestom.server.instance.palette;

import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class PaletteScanBenchmark {

    @Param({"indirect", "direct"})
    public String mode;

    private Palette palette;
    private int presentValue;
    private int absentValue;

    @Setup
    public void setup() {
        palette = Palette.blocks();
        var random = new Random(18932365);
        final int dimension = palette.dimension();
        for (int y = 0; y < dimension; y++)
            for (int z = 0; z < dimension; z++)
                for (int x = 0; x < dimension; x++)
                    if (random.nextDouble() < 0.5)
                        palette.set(x, y, z, random.nextInt(1, 16));
        if (mode.equals("direct")) palette.optimize(Palette.Optimization.SPEED);
        presentValue = 7;
        absentValue = 9999;
    }

    @Benchmark
    public int count() {
        return palette.count(presentValue);
    }

    @Benchmark
    public boolean any() {
        return palette.any(presentValue);
    }

    @Benchmark
    public boolean anyAbsent() {
        return palette.any(absentValue);
    }
}
