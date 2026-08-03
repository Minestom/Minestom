package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
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
public class PaletteMutationBenchmark {
    private Palette linear4;
    private Palette linear8;
    private Palette full16;
    private Palette sparseLinear8;
    private Palette hash9;
    private Palette hash64;
    private Palette hash256;
    private Palette direct;
    private Palette biomeLinear8;
    private Palette copyTarget;
    private int[] loadPalette64;
    private long[] loadValues64;
    private NetworkBuffer encodedHash64;
    private NetworkBuffer encodedDirect;

    @Setup
    public void setup() {
        linear4 = blocks(4);
        linear8 = blocks(8);
        full16 = blocks(16);
        sparseLinear8 = Palette.blocks();
        for (int value = 1; value < 8; value++) sparseLinear8.set(value, 0, 0, value);
        hash9 = blocks(9);
        hash64 = blocks(64);
        hash256 = blocks(256);
        direct = blocks(300);
        biomeLinear8 = Palette.sized(4, 1, 3, 6, 0);
        populate(biomeLinear8, 8, 0);
        copyTarget = blocks(4);
        loadPalette64 = new int[64];
        for (int value = 0; value < loadPalette64.length; value++) loadPalette64[value] = value;
        loadValues64 = hash64.indexedValues();
        encodedHash64 = encode(hash64);
        encodedDirect = encode(direct);
    }

    private static Palette blocks(int cardinality) {
        final Palette palette = Palette.blocks();
        populate(palette, cardinality, 0);
        return palette;
    }

    private static void populate(Palette palette, int cardinality, int offset) {
        final int dimension = palette.dimension();
        int index = 0;
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    palette.set(x, y, z, offset + index++ % cardinality);
                }
            }
        }
    }

    private static void populateShifted(Palette palette, int cardinality) {
        final int dimension = palette.dimension();
        int index = 0;
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    palette.set(x, y, z, (index++ + 1) % cardinality);
                }
            }
        }
    }

    private static NetworkBuffer encode(Palette palette) {
        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(Palette.BLOCK_SERIALIZER, palette);
        return buffer;
    }

    private static NetworkBuffer readableCopy(NetworkBuffer source) {
        return source.copy(0, source.writeIndex()).index(0, source.writeIndex());
    }

    @Benchmark
    public Palette createSingle() {
        return Palette.blocks();
    }

    @Benchmark
    public Palette singleToLinear() {
        final Palette palette = Palette.blocks();
        palette.set(0, 0, 0, 1);
        return palette;
    }

    @Benchmark
    public Palette createLinear4() {
        return blocks(4);
    }

    @Benchmark
    public Palette createLinear8() {
        return blocks(8);
    }

    @Benchmark
    public Palette createHash9() {
        return blocks(9);
    }

    @Benchmark
    public Palette createHash16() {
        return blocks(16);
    }

    @Benchmark
    public Palette createHash64() {
        return blocks(64);
    }

    @Benchmark
    public Palette createHash256() {
        return blocks(256);
    }

    @Benchmark
    public Palette createDirect() {
        return blocks(300);
    }

    // Clone cost dominates; interpret against the PaletteReadBenchmark clonePalette baseline, never as an
    // absolute number.
    @Benchmark
    public Palette tableHashBuild() {
        final Palette palette = linear4.clone();
        palette.set(0, 0, 0, 4);
        return palette;
    }

    @Benchmark
    public Palette upsizeWidth4To5() {
        final Palette palette = full16.clone();
        palette.set(0, 0, 0, 16);
        return palette;
    }

    @Benchmark
    public Palette resizeHashToDirect() {
        final Palette palette = hash256.clone();
        palette.set(0, 0, 0, 256);
        return palette;
    }

    @Benchmark
    public Palette setSameSweepLinear() {
        final Palette palette = linear8.clone();
        populate(palette, 8, 0);
        return palette;
    }

    @Benchmark
    public Palette setSameSweepHash() {
        final Palette palette = hash64.clone();
        populate(palette, 64, 0);
        return palette;
    }

    @Benchmark
    public Palette setChangedExistingSweepLinear() {
        final Palette palette = linear8.clone();
        populateShifted(palette, 8);
        return palette;
    }

    @Benchmark
    public Palette setChangedExistingSweepHash() {
        final Palette palette = hash64.clone();
        populateShifted(palette, 64);
        return palette;
    }

    @Benchmark
    public Palette churnLinearRename() {
        final Palette palette = sparseLinear8.clone();
        for (int value = 10_000; value < 14_096; value++) palette.set(1, 0, 0, value);
        return palette;
    }

    @Benchmark
    public Palette churnHash() {
        final Palette palette = hash64.clone();
        populate(palette, 64, 10_000);
        return palette;
    }

    @Benchmark
    public Palette setAllConstant() {
        final Palette palette = direct.clone();
        palette.setAll((_, _, _) -> 7);
        return palette;
    }

    // Block-config setAll always produces direct storage regardless of supplier cardinality.
    @Benchmark
    public Palette setAllDirect() {
        final Palette palette = linear4.clone();
        palette.setAll((x, y, z) -> x | z << 4 | y << 8);
        return palette;
    }

    @Benchmark
    public Palette setSweepDirect() {
        final Palette palette = direct.clone();
        populate(palette, 300, 0);
        return palette;
    }

    @Benchmark
    public Palette replaceRenameLinear() {
        final Palette palette = linear8.clone();
        palette.replace(7, 10_000);
        return palette;
    }

    @Benchmark
    public Palette replaceMergeLinear() {
        final Palette palette = linear8.clone();
        palette.replace(7, 6);
        return palette;
    }

    @Benchmark
    public Palette replaceRenameHash() {
        final Palette palette = hash64.clone();
        palette.replace(31, 10_000);
        return palette;
    }

    @Benchmark
    public Palette replaceMergeHash() {
        final Palette palette = hash64.clone();
        palette.replace(31, 32);
        return palette;
    }

    @Benchmark
    public Palette replaceAllIdentity() {
        final Palette palette = hash64.clone();
        palette.replaceAll((_, _, _, value) -> value);
        return palette;
    }

    @Benchmark
    public Palette replaceAllCollapse() {
        final Palette palette = direct.clone();
        palette.replaceAll((_, _, _, value) -> value & 15);
        return palette;
    }

    @Benchmark
    public Palette replaceOpUnchangedSweep() {
        final Palette palette = hash64.clone();
        final int dimension = palette.dimension();
        for (int y = 0; y < dimension; y++) {
            for (int z = 0; z < dimension; z++) {
                for (int x = 0; x < dimension; x++) {
                    palette.replace(x, y, z, value -> value);
                }
            }
        }
        return palette;
    }

    @Benchmark
    public Palette copyWhole() {
        final Palette palette = copyTarget.clone();
        palette.copyFrom(hash64);
        return palette;
    }

    @Benchmark
    public Palette copyOffset() {
        final Palette palette = copyTarget.clone();
        palette.copyFrom(hash9, 1, 1, 1);
        return palette;
    }

    @Benchmark
    public Palette copyRegionToDirect() {
        final Palette palette = direct.clone();
        palette.copyFrom(hash9, 1, 1, 1);
        return palette;
    }

    @Benchmark
    public Palette optimizeSize() {
        final Palette palette = direct.clone();
        palette.replaceAll((_, _, _, value) -> value & 15);
        palette.optimize(Palette.Optimization.SIZE);
        return palette;
    }

    @Benchmark
    public Palette optimizeSpeed() {
        final Palette palette = hash64.clone();
        palette.optimize(Palette.Optimization.SPEED);
        return palette;
    }

    @Benchmark
    public Palette biomeResizeLinearToDirect() {
        final Palette palette = biomeLinear8.clone();
        palette.set(0, 0, 0, 8);
        return palette;
    }

    @Benchmark
    public Palette loadIndirect64() {
        final Palette palette = Palette.blocks();
        palette.load(loadPalette64, loadValues64);
        return palette;
    }

    @Benchmark
    public NetworkBuffer serializeIndirect64() {
        return encode(hash64);
    }

    @Benchmark
    public NetworkBuffer serializeDirect() {
        return encode(direct);
    }

    @Benchmark
    public Palette deserializeIndirect64() {
        return readableCopy(encodedHash64).read(Palette.BLOCK_SERIALIZER);
    }

    @Benchmark
    public Palette deserializeDirect() {
        return readableCopy(encodedDirect).read(Palette.BLOCK_SERIALIZER);
    }

    @Benchmark
    public Palette offsetIndirect() {
        final Palette palette = hash64.clone();
        palette.offset(10_000);
        return palette;
    }

    @Benchmark
    public Palette compactCollapsedHighWater() {
        final Palette palette = hash256.clone();
        for (int value = 1; value < 256; value++) palette.replace(value, 0);
        palette.optimize(Palette.Optimization.SIZE);
        return palette;
    }
}
