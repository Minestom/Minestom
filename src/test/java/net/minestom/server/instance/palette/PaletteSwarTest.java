package net.minestom.server.instance.palette;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/// Cross-checks the SWAR lane helpers in {@link Palettes} against a naive per-lane reference
/// across every supported bit width and many sizes (including partial final longs).
public class PaletteSwarTest {
    private static final int ITERATIONS = 2000;

    @Test
    public void countEqualsMatchesNaive() {
        final Random random = new Random(1234567);
        for (int it = 0; it < ITERATIONS; it++) {
            final int bits = random.nextInt(1, 17);
            final int range = 1 << bits;
            final int size = random.nextInt(1, 5000);
            final int[] indices = randomIndices(random, size, range);
            final long[] packed = Palettes.pack(indices, bits);
            final int target = random.nextInt(0, range);
            assertEquals(naiveCount(indices, target), Palettes.countEquals(bits, packed, size, target),
                    () -> "count bits=" + bits + " size=" + size + " target=" + target);
        }
    }

    @Test
    public void anyEqualsMatchesNaive() {
        final Random random = new Random(7654321);
        for (int it = 0; it < ITERATIONS; it++) {
            final int bits = random.nextInt(1, 17);
            final int range = 1 << bits;
            final int size = random.nextInt(1, 5000);
            // Bias towards small ranges so both present/absent outcomes are common.
            final int effectiveRange = Math.max(2, Math.min(range, random.nextInt(2, 9)));
            final int[] indices = randomIndices(random, size, effectiveRange);
            final long[] packed = Palettes.pack(indices, bits);
            final int target = random.nextInt(0, range);
            assertEquals(naiveAny(indices, target), Palettes.anyEquals(bits, packed, size, target),
                    () -> "any bits=" + bits + " size=" + size + " target=" + target);
        }
    }

    @Test
    public void replaceEqualsMatchesNaive() {
        final Random random = new Random(192837465);
        for (int it = 0; it < ITERATIONS; it++) {
            final int bits = random.nextInt(1, 17);
            final int range = 1 << bits;
            final int size = random.nextInt(1, 5000);
            final int[] indices = randomIndices(random, size, range);
            final long[] packed = Palettes.pack(indices, bits);
            final int oldValue = random.nextInt(0, range);
            final int newValue = random.nextInt(0, range);

            final int replaced = Palettes.replaceEquals(bits, packed, size, oldValue, newValue);
            assertEquals(naiveCount(indices, oldValue), replaced,
                    () -> "replace count bits=" + bits + " size=" + size + " old=" + oldValue + " new=" + newValue);

            // Verify the mutated array unpacks to the expected content.
            final int[] expected = indices.clone();
            for (int i = 0; i < expected.length; i++) if (expected[i] == oldValue) expected[i] = newValue;
            final int[] actual = new int[size];
            Palettes.unpack(actual, packed, bits);
            assertArrayEquals(expected, actual,
                    () -> "replace content bits=" + bits + " size=" + size + " old=" + oldValue + " new=" + newValue);
        }
    }

    @Test
    public void countAllZeroAndAllSet() {
        for (int bits = 1; bits <= 16; bits++) {
            final int range = 1 << bits;
            final int size = 4096;
            final int[] zeros = new int[size];
            final long[] packedZeros = Palettes.pack(zeros, bits);
            assertEquals(size, Palettes.countEquals(bits, packedZeros, size, 0), "all-zero bits=" + bits);
            assertFalse(Palettes.anyEquals(bits, packedZeros, size, Math.min(range - 1, 1)), "all-zero any bits=" + bits);

            final int fillValue = range - 1;
            final int[] full = new int[size];
            java.util.Arrays.fill(full, fillValue);
            final long[] packedFull = Palettes.pack(full, bits);
            assertEquals(size, Palettes.countEquals(bits, packedFull, size, fillValue), "all-set bits=" + bits);
            assertEquals(0, Palettes.countEquals(bits, packedFull, size, 0), "all-set zero count bits=" + bits);
        }
    }

    private static int[] randomIndices(Random random, int size, int range) {
        final int[] indices = new int[size];
        for (int i = 0; i < size; i++) indices[i] = random.nextInt(0, range);
        return indices;
    }

    private static int naiveCount(int[] indices, int target) {
        int count = 0;
        for (int v : indices) if (v == target) count++;
        return count;
    }

    private static boolean naiveAny(int[] indices, int target) {
        for (int v : indices) if (v == target) return true;
        return false;
    }
}
