package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Cross-checks the stepped and SWAR scan paths in [Palettes] against naive per-entry references.
public class PaletteScanEquivalenceTest {
    @Test
    public void heightMatchesNaiveScanAcrossWidths() {
        final Random random = new Random(0x4E1687L);
        for (int trial = 0; trial < 200; trial++) {
            final Palette palette = Palette.blocks();
            final int distinct = random.nextInt(1, 40);
            palette.setAll((_, _, _) -> random.nextInt(distinct));
            assertHeightMatchesNaive(palette, random.nextInt(distinct));
        }
    }

    @Test
    public void heightMatchesNaiveInEveryStorageMode() {
        // single value
        final Palette single = Palette.blocks();
        single.fill(7);
        assertEquals(0, single.bitsPerEntry());
        assertHeightMatchesNaive(single, 7);
        assertHeightMatchesNaive(single, 8);

        // indirect, minimum width
        final Palette indirect = Palette.blocks();
        indirect.set(3, 9, 5, 1);
        assertEquals(4, indirect.bitsPerEntry());
        assertHeightMatchesNaive(indirect, 1);

        // indirect, widened
        final Palette wide = Palette.blocks();
        wide.setAll((x, y, z) -> (x + y + z) % 40);
        wide.optimize(Palette.Optimization.SIZE);
        assertEquals(6, wide.bitsPerEntry());
        for (int value = 0; value < 40; value += 7) assertHeightMatchesNaive(wide, value);

        // direct
        final Palette direct = Palette.blocks();
        direct.setAll((x, y, z) -> x | z << 4 | y << 8);
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, direct.bitsPerEntry());
        assertHeightMatchesNaive(direct, 0);
        assertHeightMatchesNaive(direct, 4095);
    }

    @Test
    public void heightMatchesNaiveForBiomeDimension() {
        final Random random = new Random(0xB10E5L);
        for (int trial = 0; trial < 100; trial++) {
            final Palette palette = Palette.biomes(64);
            palette.setAll((_, _, _) -> random.nextInt(6));
            assertHeightMatchesNaive(palette, random.nextInt(6));
        }
    }

    @Test
    public void predicateScansResolveThroughPaletteArray() {
        final Random random = new Random(0x5CA95L);
        final int bits = 5;
        final int size = 100;
        final int[] palette = {10, 20, 30, 40};
        final int[] indices = new int[size];
        for (int i = 0; i < size; i++) indices[i] = random.nextInt(palette.length);
        // A lane index past the palette end resolves to itself instead of a table entry
        indices[57] = 6;
        final long[] packed = Palettes.pack(indices, bits);
        final int[] resolved = new int[size];
        for (int i = 0; i < size; i++) {
            resolved[i] = indices[i] < palette.length ? palette[indices[i]] : indices[i];
        }
        for (int target : new int[]{10, 20, 30, 40, 6, 99}) {
            int expectedCount = 0;
            for (int value : resolved) {
                if (value == target) expectedCount++;
            }
            assertEquals(expectedCount > 0, Palettes.anyMatch(bits, packed, size, palette, value -> value == target),
                    "anyMatch target " + target);
            assertEquals(expectedCount == size, Palettes.allMatch(bits, packed, size, palette, value -> value == target),
                    "allMatch target " + target);
            assertEquals(expectedCount, Palettes.countMatches(bits, packed, size, palette, value -> value == target),
                    "countMatches target " + target);
        }
        // The fallback lane alone holds 6, no table entry maps to it
        assertEquals(1, Palettes.countMatches(bits, packed, size, palette, value -> value == 6));
        assertTrue(Palettes.allMatch(bits, packed, size, palette, value -> value == 6 || value >= 10));
    }

    @Test
    public void heightVisitsEveryColumnIndependently() {
        final Palette palette = Palette.blocks();
        // Encode the column into the value so a mis-stepped cursor reads the wrong column
        palette.setAll((x, y, z) -> y == 0 ? 1 + x + z * 16 : 0);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int expected = 1 + x + z * 16;
                assertEquals(0, palette.height(x, z, (_, _, _, value) -> value == expected),
                        "column " + x + ", " + z);
            }
        }
    }

    @Test
    public void validateIndicesAcceptsEveryInRangeLane() {
        for (int bits = 1; bits <= 8; bits++) {
            for (int paletteSize : new int[]{1, 2, 3, 5, 8, 1 << bits}) {
                if (paletteSize > 1 << bits) continue;
                final int[] indices = new int[Palettes.maxSize(16)];
                for (int i = 0; i < indices.length; i++) indices[i] = i % paletteSize;
                final long[] packed = Palettes.pack(indices, bits);
                final int size = paletteSize;
                final int width = bits;
                assertDoesNotThrow(() -> Palettes.validateIndices(width, 16, packed, size),
                        "bits=" + bits + " paletteSize=" + paletteSize);
            }
        }
    }

    @Test
    public void validateIndicesRejectsOutOfRangeLaneAtEveryPosition() {
        for (int bits = 2; bits <= 8; bits++) {
            for (int paletteSize : new int[]{2, 3, 5, 8}) {
                if (paletteSize >= 1 << bits) continue;
                // Place the offending lane last so a fast reject pass cannot miss a late violation
                for (int position : new int[]{0, 1, 63, 4095}) {
                    final int[] indices = new int[Palettes.maxSize(16)];
                    indices[position] = paletteSize; // first invalid index
                    final long[] packed = Palettes.pack(indices, bits);
                    final int width = bits;
                    final int limit = paletteSize;
                    final var thrown = assertThrows(IllegalArgumentException.class,
                            () -> Palettes.validateIndices(width, 16, packed, limit),
                            "bits=" + bits + " paletteSize=" + paletteSize + " position=" + position);
                    assertEquals("Palette index out of range: " + paletteSize + " >= " + paletteSize,
                            thrown.getMessage());
                }
            }
        }
    }

    @Test
    public void validateIndicesIgnoresLanesPastTheEnd() {
        // 4096 entries at 3 bits leaves 21 lanes per long and 4 unused trailing lanes overall
        final int bits = 3;
        final int size = Palettes.maxSize(16);
        final long[] packed = Palettes.pack(new int[size], bits);
        // Poison every lane beyond the palette content in the final long
        final int lanesPerLong = 64 / bits;
        final int used = size - (packed.length - 1) * lanesPerLong;
        for (int lane = used; lane < lanesPerLong; lane++) {
            packed[packed.length - 1] |= 0b111L << (lane * bits);
        }
        assertDoesNotThrow(() -> Palettes.validateIndices(bits, 16, packed, 1));
    }

    @Test
    public void packRoundTripsThroughUnpack() {
        final Random random = new Random(0x9AC7L);
        for (int trial = 0; trial < 500; trial++) {
            final int bits = random.nextInt(1, 17);
            final int size = random.nextInt(1, 5000);
            final int[] original = new int[size];
            for (int i = 0; i < size; i++) original[i] = random.nextInt(1 << bits);
            final long[] packed = Palettes.pack(original, bits);
            assertEquals((size + 64 / bits - 1) / (64 / bits), packed.length,
                    "packed length bits=" + bits + " size=" + size);
            final int[] roundTripped = new int[size];
            Palettes.unpack(roundTripped, packed, bits);
            assertArrayEquals(original, roundTripped, "bits=" + bits + " size=" + size);
        }
    }

    @Test
    public void packLeavesTrailingLanesZero() {
        for (int bits = 1; bits <= 16; bits++) {
            final int lanesPerLong = 64 / bits;
            final int size = lanesPerLong + 1; // one full long plus a single lane
            final int[] values = new int[size];
            Arrays.fill(values, (1 << bits) - 1);
            final long[] packed = Palettes.pack(values, bits);
            assertEquals(2, packed.length, "bits=" + bits);
            final long expectedTail = (1L << bits) - 1L;
            assertEquals(expectedTail, packed[1], "trailing lanes must stay zero, bits=" + bits);
        }
    }

    @Test
    public void getAllCountsMatchesNaiveOnBothSidesOfTheHistogramThreshold() {
        final Random random = new Random(0xC0175L);
        // The width spread covers dividing and non-dividing lane sizes for the indexed tally
        for (int directBits : new int[]{4, 6, 8, 10, 11, 15}) {
            for (int trial = 0; trial < 20; trial++) {
                final Palette palette = Palette.sized(16, 1, 3, directBits, 0);
                final int span = Math.min(1 << directBits, random.nextInt(2, 300));
                palette.setAll((_, _, _) -> random.nextInt(span));
                if (palette.bitsPerEntry() != directBits) continue; // collapsed to single value or indirect

                final Int2IntOpenHashMap expected = new Int2IntOpenHashMap();
                palette.getAll((_, _, _, value) -> expected.addTo(value, 1));
                final Int2IntOpenHashMap reported = new Int2IntOpenHashMap();
                final int[] total = new int[1];
                palette.getAllCounts((value, count) -> {
                    assertTrue(count > 0, "counts must be positive");
                    assertEquals(0, reported.put(value, count), "each value must be reported once");
                    total[0] += count;
                });
                assertEquals(expected, reported, "directBits=" + directBits);
                assertEquals(palette.maxSize(), total[0], "counts must sum to the entry count");
            }
        }
    }

    @Test
    public void getAllCountsIgnoresLanesPastTheEnd() {
        // A biome palette leaves trailing lanes in its final long; they must not be tallied
        final Palette palette = Palette.biomes(1024);
        palette.setAll((x, y, z) -> (x + y + z) & 7);
        final int[] total = new int[1];
        palette.getAllCounts((_, count) -> total[0] += count);
        assertEquals(palette.maxSize(), total[0]);
    }

    private static void assertHeightMatchesNaive(Palette palette, int target) {
        final int dimension = palette.dimension();
        for (int x = 0; x < dimension; x++) {
            for (int z = 0; z < dimension; z++) {
                int expected = -1;
                for (int y = dimension - 1; y >= 0; y--) {
                    if (palette.get(x, y, z) == target) {
                        expected = y;
                        break;
                    }
                }
                final int actual = palette.height(x, z, (_, _, _, value) -> value == target);
                assertEquals(expected, actual,
                        "column " + x + ", " + z + " target " + target + " bpe " + palette.bitsPerEntry());
            }
        }
    }

    @Test
    public void heightPassesTheScannedCoordinates() {
        final Palette palette = Palette.blocks();
        palette.setAll((x, y, z) -> x | z << 4 | y << 8);
        for (int x = 0; x < 16; x += 5) {
            for (int z = 0; z < 16; z += 5) {
                final int columnX = x;
                final int columnZ = z;
                palette.height(x, z, (predX, predY, predZ, value) -> {
                    assertEquals(columnX, predX);
                    assertEquals(columnZ, predZ);
                    assertEquals(columnX | columnZ << 4 | predY << 8, value,
                            "value must match the coordinates it was reported for");
                    return false;
                });
            }
        }
    }
}
