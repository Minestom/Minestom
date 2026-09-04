package net.minestom.server.instance.palette;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PaletteFullScanBenchmark {
    @State(Scope.Benchmark)
    public static class ModeState {
        @Param({"indirect", "direct"})
        public String mode;

        Palette palette;
        Palette compareLateDifferTarget;

        @Setup
        public void setup() {
            final Random random = new Random(1234);
            palette = Palette.blocks();
            if (mode.equals("indirect")) {
                palette.setAll((_, _, _) -> random.nextInt(60));
                palette.optimize(Palette.Optimization.SIZE);
            } else {
                palette.setAll((x, y, z) -> x | z << 4 | y << 8);
            }
            compareLateDifferTarget = palette.clone();
            final int last = palette.dimension() - 1;
            compareLateDifferTarget.set(last, last, last, palette.get(last, last, last) + 1);
        }
    }

    @State(Scope.Benchmark)
    public static class PackState {
        @Param({"64", "32", "40"})
        public int paletteSize;

        long[] packedIndices;

        @Setup
        public void setup() {
            final Random random = new Random(1234);
            final int[] rawValues = new int[Palettes.maxSize(16)];
            for (int i = 0; i < rawValues.length; i++) rawValues[i] = random.nextInt(paletteSize);
            packedIndices = Palettes.pack(rawValues, 6);
        }
    }

    @State(Scope.Benchmark)
    public static class FixedState {
        Palette indirectA;
        Palette indirectB;
        int[] rawValues;

        @Setup
        public void setup() {
            indirectA = Palette.blocks();
            indirectA.setAll((x, y, z) -> (x + y + z) % 40);
            indirectA.optimize(Palette.Optimization.SIZE);
            indirectB = indirectA.clone();

            final Random random = new Random(1234);
            rawValues = new int[Palettes.maxSize(16)];
            for (int i = 0; i < rawValues.length; i++) rawValues[i] = random.nextInt(64);
        }
    }

    @Benchmark
    public int heightAllColumns(ModeState state) {
        int total = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                total += state.palette.height(x, z, (_, _, _, value) -> value != 0);
            }
        }
        return total;
    }

    @Benchmark
    public int chunkCountersViaGetAllCounts(ModeState state) {
        final int[] counts = new int[2];
        state.palette.getAllCounts((value, count) -> {
            if (value != 0) counts[0] += count;
            if ((value & 1) == 0) counts[1] += count;
        });
        return counts[0] + counts[1];
    }

    @Benchmark
    public int chunkCountersViaPredicates(ModeState state) {
        return state.palette.count(value -> value != 0) + state.palette.count(value -> (value & 1) == 0);
    }

    @Benchmark
    public boolean compareDifferingAtEnd(ModeState state) {
        return state.palette.compare(state.compareLateDifferTarget);
    }

    @Benchmark
    public boolean compareIndirectPair(FixedState state) {
        return state.indirectA.compare(state.indirectB);
    }

    @Benchmark
    public void validateIndices(PackState state, Blackhole blackhole) {
        Palettes.validateIndices(6, 16, state.packedIndices, state.paletteSize);
        blackhole.consume(state.packedIndices);
    }

    @Benchmark
    public long[] pack(FixedState state) {
        return Palettes.pack(state.rawValues, 6);
    }
}
