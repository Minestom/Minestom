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

import java.util.concurrent.TimeUnit;
@Warmup(iterations = 3, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class PaletteReadBenchmark {
    private static final int ABSENT_VALUE = 1_000_000;

    @Param({
            "single",
            "block_linear_4",
            "block_hash_9",
            "block_hash_16",
            "block_hash_64",
            "block_hash_256",
            "block_direct",
            "biome_linear_2",
            "biome_linear_8",
            "biome_direct"
    })
    public String mode;

    private Palette palette;
    private Palette equalPalette;
    private Palette differentPalette;
    private int presentValue;
    private int coordinateMask;
    private int cursor;
    private int scanResult;
    private final Palette.EntryConsumer entryConsumer = (_, _, _, value) -> scanResult += value;
    private final Palette.ValueCountConsumer countConsumer = (value, count) -> scanResult += value * count;

    @Setup
    public void setup() {
        switch (mode) {
            case "single" -> {
                palette = Palette.blocks();
                palette.fill(7);
                presentValue = 7;
            }
            case "block_linear_4" -> setupBlocks(4);
            case "block_hash_9" -> setupBlocks(9);
            case "block_hash_16" -> setupBlocks(16);
            case "block_hash_64" -> setupBlocks(64);
            case "block_hash_256" -> setupBlocks(256);
            case "block_direct" -> setupBlocks(300);
            case "biome_linear_2" -> setupBiomes(2);
            case "biome_linear_8" -> setupBiomes(8);
            case "biome_direct" -> setupBiomes(9);
            default -> throw new IllegalArgumentException("Unknown mode: " + mode);
        }
        coordinateMask = palette.dimension() - 1;
        equalPalette = palette.clone();
        differentPalette = palette.clone();
        differentPalette.set(0, 0, 0, ABSENT_VALUE);
    }

    private void setupBlocks(int cardinality) {
        palette = Palette.blocks();
        populate(palette, cardinality);
        presentValue = cardinality >>> 1;
    }

    private void setupBiomes(int cardinality) {
        palette = Palette.sized(4, 1, 3, 6, 0);
        populate(palette, cardinality);
        presentValue = cardinality >>> 1;
    }

    private static void populate(Palette palette, int cardinality) {
        final int dimension = palette.dimension();
        int index = 0;
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    palette.set(x, y, z, index++ % cardinality);
                }
            }
        }
    }

    private static boolean even(int value) {
        return (value & 1) == 0;
    }

    @Benchmark
    public int getHot() {
        final int coordinate = cursor++ & coordinateMask;
        return palette.get(coordinate, coordinate, coordinate);
    }

    @Benchmark
    public int getRandom() {
        cursor = cursor * 1664525 + 1013904223;
        final int value = cursor;
        return palette.get(value & coordinateMask, value >>> 8 & coordinateMask,
                value >>> 16 & coordinateMask);
    }

    @Benchmark
    public int getAll() {
        scanResult = 0;
        palette.getAll(entryConsumer);
        return scanResult;
    }

    @Benchmark
    public int countPresent() {
        return palette.count(presentValue);
    }

    @Benchmark
    public int countAbsent() {
        return palette.count(ABSENT_VALUE);
    }

    @Benchmark
    public int countPredicate() {
        return palette.count(PaletteReadBenchmark::even);
    }

    @Benchmark
    public boolean anyPresent() {
        return palette.any(presentValue);
    }

    @Benchmark
    public boolean anyAbsent() {
        return palette.any(ABSENT_VALUE);
    }

    @Benchmark
    public boolean allPresent() {
        return palette.all(presentValue);
    }

    @Benchmark
    public boolean allPredicate() {
        return palette.all(PaletteReadBenchmark::even);
    }

    @Benchmark
    public boolean anyPredicateAbsent() {
        return palette.any(value -> value < 0);
    }

    @Benchmark
    public boolean allPredicateTrue() {
        return palette.all(value -> value >= 0);
    }

    @Benchmark
    public int heightAbsent() {
        return palette.height(cursor++ & coordinateMask, cursor >>> 8 & coordinateMask,
                (_, _, _, value) -> value == ABSENT_VALUE);
    }

    @Benchmark
    public boolean compareEqual() {
        return palette.compare(equalPalette);
    }

    @Benchmark
    public boolean compareDifferent() {
        return palette.compare(differentPalette);
    }

    @Benchmark
    public int getAllCounts() {
        scanResult = 0;
        palette.getAllCounts(countConsumer);
        return scanResult;
    }

    @Benchmark
    public Palette clonePalette() {
        return palette.clone();
    }
}
