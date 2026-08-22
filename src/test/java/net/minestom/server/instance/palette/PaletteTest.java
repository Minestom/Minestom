package net.minestom.server.instance.palette;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static net.minestom.server.instance.palette.PaletteAssertions.assertAllEquals;
import static net.minestom.server.instance.palette.PaletteAssertions.assertCountsMatchContent;
import static net.minestom.server.instance.palette.PaletteAssertions.nonZeroCount;
import static net.minestom.server.instance.palette.PaletteAssertions.testPalettes;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaletteTest {

    @Test
    public void singlePlacement() {
        var palette = Palette.blocks();
        palette.set(0, 0, 1, 1);
        assertEquals(1, palette.get(0, 0, 1));
    }

    @Test
    public void placement() {
        for (Palette palette : testPalettes()) {
            assertEquals(0, palette.get(0, 0, 0), "Default value should be 0");
            assertEquals(0, nonZeroCount(palette));
            palette.set(0, 0, 0, 64);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(1, nonZeroCount(palette));

            palette.set(1, 0, 0, 65);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(65, palette.get(1, 0, 0));
            assertEquals(2, nonZeroCount(palette));

            palette.set(0, 1, 0, 66);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(65, palette.get(1, 0, 0));
            assertEquals(66, palette.get(0, 1, 0));
            assertEquals(3, nonZeroCount(palette));

            palette.set(0, 0, 1, 67);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(65, palette.get(1, 0, 0));
            assertEquals(66, palette.get(0, 1, 0));
            assertEquals(67, palette.get(0, 0, 1));
            assertEquals(4, nonZeroCount(palette));

            palette.set(0, 0, 1, 68);
            assertEquals(4, nonZeroCount(palette));
        }
    }

    @Test
    public void placementHighValue() {
        final int value = (1 << 15) - 1;
        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 1, value);
            assertEquals(value, palette.get(0, 0, 1));
        }
    }

    @Test
    public void setSameSingleValueIsNoOp() {
        final PaletteImpl palette = (PaletteImpl) Palette.blocks();
        palette.set(0, 0, 0, 0);
        assertEquals(0, palette.bitsPerEntry());
        assertNull(palette.values);
        assertEquals(0, palette.get(0, 0, 0));

        palette.fill(7);
        palette.set(3, 3, 3, 7);
        assertEquals(0, palette.bitsPerEntry());
        assertNull(palette.values);
        assertEquals(7, palette.get(3, 3, 3));
    }

    @Test
    public void negPlacement() {
        for (Palette palette : testPalettes()) {
            assertThrows(IllegalArgumentException.class, () -> palette.set(-1, 0, 0, 64));
            assertThrows(IllegalArgumentException.class, () -> palette.set(0, -1, 0, 64));
            assertThrows(IllegalArgumentException.class, () -> palette.set(0, 0, -1, 64));

            assertThrows(IllegalArgumentException.class, () -> palette.get(-1, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> palette.get(0, -1, 0));
            assertThrows(IllegalArgumentException.class, () -> palette.get(0, 0, -1));
        }
    }

    @Test
    public void coordinateUpperBoundsRejected() {
        final Palette palette = Palette.blocks();
        final int dimension = palette.dimension();
        for (int axis = 0; axis < 3; axis++) {
            final int x = axis == 0 ? dimension : 0;
            final int y = axis == 1 ? dimension : 0;
            final int z = axis == 2 ? dimension : 0;
            final String expected = "Coordinates must be less than the dimension size, got "
                    + x + ", " + y + ", " + z + " for dimension " + dimension;
            assertEquals(expected, assertThrows(IllegalArgumentException.class,
                    () -> palette.get(x, y, z)).getMessage());
            assertEquals(expected, assertThrows(IllegalArgumentException.class,
                    () -> palette.set(x, y, z, 1)).getMessage());
        }
    }

    @Test
    public void resize() {
        Palette palette = Palette.sized(16, 1, 5, 15, 2);
        palette.set(0, 0, 0, 1);
        assertEquals(2, palette.bitsPerEntry());
        palette.set(0, 0, 1, 2);
        assertEquals(2, palette.bitsPerEntry());
        palette.set(0, 0, 2, 3);
        assertEquals(2, palette.bitsPerEntry());

        palette.set(0, 0, 3, 4);
        assertEquals(3, palette.bitsPerEntry());
        assertEquals(1, palette.get(0, 0, 0));
        assertEquals(2, palette.get(0, 0, 1));
        assertEquals(3, palette.get(0, 0, 2));
        assertEquals(4, palette.get(0, 0, 3));
    }

    @Test
    public void resizeCascadeAcrossBlockWidths() {
        final PaletteImpl palette = (PaletteImpl) Palette.blocks();
        final int[] shadow = new int[palette.maxSize()];
        for (int i = 1; i <= 300; i++) {
            final int x = i & 15;
            final int z = (i >> 4) & 15;
            final int y = i >> 8;
            palette.set(x, y, z, 1000 + i);
            shadow[i] = 1000 + i;
            final int expectedBits = switch (i) {
                case 15 -> 4;
                case 16, 31 -> 5;
                case 32, 63 -> 6;
                case 64, 127 -> 7;
                case 128, 255 -> 8;
                case 256, 300 -> 15;
                default -> -1;
            };
            if (expectedBits == -1) continue;
            assertEquals(expectedBits, palette.bitsPerEntry(), "after " + i + " distinct values");
            palette.getAll((px, py, pz, value) ->
                    assertEquals(shadow[Palettes.sectionIndex(16, px, py, pz)], value));
            final AtomicInteger sum = new AtomicInteger();
            palette.getAllCounts((_, count) -> sum.addAndGet(count));
            assertEquals(palette.maxSize(), sum.get());
        }
        assertNull(palette.table);
    }

    @Test
    public void resizeCascadeAcrossBiomeWidths() {
        final PaletteImpl palette = (PaletteImpl) Palette.biomes(8);
        final int[] shadow = new int[palette.maxSize()];
        for (int i = 1; i <= 7; i++) {
            final int x = i & 3;
            final int z = (i >> 2) & 3;
            palette.set(x, 0, z, i);
            shadow[i] = i;
            final int expectedBits = switch (i) {
                case 1 -> 1;
                case 2, 3 -> 2;
                default -> 3;
            };
            assertEquals(expectedBits, palette.bitsPerEntry(), "after " + i + " distinct values");
            palette.getAll((px, py, pz, value) ->
                    assertEquals(shadow[Palettes.sectionIndex(4, px, py, pz)], value));
            final AtomicInteger sum = new AtomicInteger();
            palette.getAllCounts((_, count) -> sum.addAndGet(count));
            assertEquals(palette.maxSize(), sum.get());
        }
        assertThrows(IllegalArgumentException.class, () -> palette.set(0, 0, 2, 8));
        assertEquals(3, palette.bitsPerEntry());
    }

    @Test
    public void fill() {
        for (Palette palette : testPalettes()) {
            assertEquals(0, nonZeroCount(palette));
            palette.set(0, 0, 0, 5);
            assertEquals(1, nonZeroCount(palette));
            assertEquals(5, palette.get(0, 0, 0));
            palette.fill(6);
            assertEquals(6, palette.get(0, 0, 0));
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            assertAllEquals(6, palette);

            palette.fill(0);
            assertEquals(0, nonZeroCount(palette));
            assertAllEquals(0, palette);
        }
    }

    @Test
    public void offset() {
        for (Palette palette : testPalettes()) {
            palette.fill(0);
            palette.offset(1);
            assertAllEquals(1, palette);

            palette.fill(1);
            palette.set(0, 0, 1, 2);
            palette.offset(-1);
            for (int x = 0; x < palette.dimension(); x++) {
                for (int y = 0; y < palette.dimension(); y++) {
                    for (int z = 0; z < palette.dimension(); z++) {
                        if (x == 0 && y == 0 && z == 1) {
                            assertEquals(1, palette.get(x, y, z));
                        } else {
                            assertEquals(0, palette.get(x, y, z));
                        }
                    }
                }
            }
        }
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            palette.offset(50);
            palette.getAll((x, y, z, value) -> {
                int expected = x + y + z + 100 + 50;
                assertEquals(expected, value);
            });
        }

        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 1, 1);
            palette.set(0, 1, 0, 2);
            palette.set(1, 0, 0, 3);
            palette.offset(50);
            palette.getAll((x, y, z, value) -> {
                if (x == 0 && y == 0 && z == 1) {
                    assertEquals(51, value);
                } else if (x == 0 && y == 1 && z == 0) {
                    assertEquals(52, value);
                } else if (x == 1 && y == 0 && z == 0) {
                    assertEquals(53, value);
                } else {
                    assertEquals(50, value);
                }
            });
        }
    }

    @Test
    public void offsetCount() {
        for (Palette palette : testPalettes()) {
            assertEquals(0, nonZeroCount(palette));
            palette.fill(0);
            assertEquals(0, nonZeroCount(palette));
            palette.offset(1);
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            palette.offset(-1);
            assertEquals(0, nonZeroCount(palette));
        }
        for (Palette palette : testPalettes()) {
            palette.fill(1);
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            palette.set(0, 0, 1, 2);
            palette.set(0, 1, 0, 3);
            palette.set(1, 0, 0, 4);
            palette.offset(-1);
            assertEquals(3, nonZeroCount(palette));
            palette.offset(1);
            assertEquals(palette.maxSize(), nonZeroCount(palette));
        }
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            palette.offset(50);
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            palette.offset(-50);
            assertEquals(palette.maxSize(), nonZeroCount(palette));
        }
    }

    @Test
    public void offsetZeroAndSelfReplaceAreNoOps() {
        final PaletteImpl palette = (PaletteImpl) Palette.blocks();
        palette.set(0, 0, 0, 5);
        palette.set(1, 0, 0, 6);
        assertNotNull(palette.table);
        final Palette before = palette.clone();
        final PaletteTable table = palette.table;
        final long[] values = palette.values;

        palette.offset(0);
        palette.replace(5, 5);

        assertSame(table, palette.table);
        assertSame(values, palette.values);
        assertTrue(palette.compare(before));
    }

    @Test
    public void replace() {
        for (Palette palette : testPalettes()) {
            palette.fill(0);
            palette.replace(0, 1);
            assertAllEquals(1, palette);

            palette.fill(1);
            palette.set(0, 0, 1, 2);
            palette.replace(2, 3);
            for (int x = 0; x < palette.dimension(); x++) {
                for (int y = 0; y < palette.dimension(); y++) {
                    for (int z = 0; z < palette.dimension(); z++) {
                        if (x == 0 && y == 0 && z == 1) {
                            assertEquals(3, palette.get(x, y, z));
                        } else {
                            assertEquals(1, palette.get(x, y, z));
                        }
                    }
                }
            }
        }

        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 1, 1);
            palette.set(0, 1, 0, 2);
            palette.set(1, 0, 0, 3);
            palette.replace(0, 50);
            palette.getAll((x, y, z, value) -> {
                if (x == 0 && y == 0 && z == 1) {
                    assertEquals(1, value);
                } else if (x == 0 && y == 1 && z == 0) {
                    assertEquals(2, value);
                } else if (x == 1 && y == 0 && z == 0) {
                    assertEquals(3, value);
                } else {
                    assertEquals(50, value);
                }
            });
        }
    }

    @Test
    public void replaceCount() {
        for (Palette palette : testPalettes()) {
            palette.fill(0);
            palette.replace(0, 1);
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            palette.replace(1, 0);
            assertEquals(0, nonZeroCount(palette));
        }
        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 1, 1);
            palette.set(1, 1, 1, 1);
            palette.set(0, 1, 0, 2);
            palette.set(1, 0, 0, 3);
            assertEquals(4, nonZeroCount(palette));
            palette.replace(1, 0);
            assertEquals(2, nonZeroCount(palette));
        }
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            palette.replace(100, 0);
            assertEquals(palette.maxSize() - 1, nonZeroCount(palette));
        }
    }

    @Test
    public void replaceWithExistingValue() {
        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 0, 1);
            palette.set(1, 0, 0, 2);
            palette.set(0, 1, 0, 2);

            palette.replace(1, 2);

            assertEquals(2, palette.get(0, 0, 0));
            assertEquals(2, palette.get(1, 0, 0));
            assertEquals(2, palette.get(0, 1, 0));
            assertEquals(3, palette.count(2));
            assertEquals(0, palette.count(1));
            assertFalse(palette.any(1));
            assertTrue(palette.any(2));

            palette.set(1, 1, 0, 1);
            assertEquals(1, palette.get(1, 1, 0));
            assertEquals(1, palette.count(1));
            assertEquals(3, palette.count(2));
        }
    }

    @Test
    public void replaceMissesAreNoOps() {
        final PaletteImpl single = (PaletteImpl) Palette.blocks();
        single.replace(3, 9);
        assertEquals(0, single.bitsPerEntry());
        assertEquals(0, single.get(0, 0, 0));

        final PaletteImpl indirect = (PaletteImpl) Palette.blocks();
        indirect.set(0, 0, 0, 5);
        indirect.set(1, 0, 0, 6);
        final Palette before = indirect.clone();
        indirect.replace(42, 9);
        assertTrue(indirect.compare(before));
        assertFalse(indirect.any(9));

        final PaletteImpl dead = (PaletteImpl) Palette.blocks();
        dead.set(0, 0, 0, 1);
        dead.replace(0, 1);
        dead.replace(0, 500);
        assertEquals(dead.maxSize(), dead.count(1));
        assertFalse(dead.any(500));
    }

    @Test
    public void countValue() {
        for (Palette palette : testPalettes()) {
            assertEquals(palette.maxSize(), palette.count(0));
            assertEquals(0, palette.count(1));
        }
        for (Palette palette : testPalettes()) {
            palette.fill(0);
            assertEquals(palette.maxSize(), palette.count(0));
            palette.replace(0, 1);
            assertEquals(0, palette.count(0));
            assertEquals(palette.maxSize(), palette.count(1));
        }
        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 1, 1);
            palette.set(1, 1, 1, 1);
            palette.set(0, 1, 0, 2);
            palette.set(1, 0, 0, 3);
            assertEquals(palette.maxSize() - 4, palette.count(0));
            assertEquals(2, palette.count(1));
            assertEquals(1, palette.count(2));
            assertEquals(1, palette.count(3));
            assertEquals(0, palette.count(4));
        }
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            assertEquals(0, palette.count(0));
            assertEquals(1, palette.count(100));
        }
    }

    @Test
    public void countPredicate() {
        for (Palette palette : testPalettes()) {
            assertEquals(palette.maxSize(), palette.count(value -> value == 0));
            assertEquals(0, palette.count(value -> value == 1));
        }
        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 0, 1);
            palette.set(1, 0, 0, 2);
            palette.set(0, 1, 0, 3);

            assertEquals(2, palette.count(value -> value > 1));
            assertEquals(palette.maxSize() - 3, palette.count(value -> value == 0));
            assertEquals(0, palette.count(value -> value < 0));

            palette.optimize(Palette.Optimization.SPEED);
            assertEquals(2, palette.count(value -> value > 1));
            assertEquals(palette.maxSize() - 3, palette.count(value -> value == 0));
        }
    }

    @Test
    public void anyValue() {
        for (Palette palette : testPalettes()) {
            // Initially all zero
            assertFalse(palette.any(1));
            assertTrue(palette.any(0));
            palette.set(0, 0, 1, 1);
            assertTrue(palette.any(1));
            assertTrue(palette.any(0));
            palette.set(0, 0, 1, 0);
            assertFalse(palette.any(1));
            assertTrue(palette.any(0));
            palette.set(0, 0, 1, 1);
            palette.replace(0, 2);
            assertTrue(palette.any(1));
            assertFalse(palette.any(0));
            assertTrue(palette.any(2));
            palette.replace(1, 2);
            assertFalse(palette.any(1));
            assertTrue(palette.any(2));
        }
        for (Palette palette : testPalettes()) {
            palette.fill(5);
            assertTrue(palette.any(5));
            assertFalse(palette.any(0));
            palette.fill(0);
            assertFalse(palette.any(5));
            assertTrue(palette.any(0));
        }
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> (x + y + z) % 3);
            assertTrue(palette.any(0));
            assertTrue(palette.any(1));
            assertTrue(palette.any(2));
            assertFalse(palette.any(3));
        }
    }

    @Test
    public void anyPredicate() {
        for (Palette palette : testPalettes()) {
            assertTrue(palette.any(value -> value == 0));
            assertFalse(palette.any(value -> value != 0));

            palette.set(0, 0, 0, 42);
            assertTrue(palette.any(value -> value == 42));
            assertTrue(palette.any(value -> value == 0));
            assertFalse(palette.any(value -> value < 0));

            palette.optimize(Palette.Optimization.SPEED);
            assertTrue(palette.any(value -> value == 42));
            assertFalse(palette.any(value -> value < 0));
        }
    }

    @Test
    public void anyPredicateStopsAtFirstMatch() {
        final Palette palette = Palette.blocks();
        palette.set(0, 0, 0, 1);

        final AtomicInteger invocations = new AtomicInteger();
        assertTrue(palette.any(_ -> {
            invocations.incrementAndGet();
            return true;
        }));
        assertEquals(1, invocations.get());

        palette.optimize(Palette.Optimization.SPEED);
        invocations.set(0);
        assertTrue(palette.any(_ -> {
            invocations.incrementAndGet();
            return true;
        }));
        assertEquals(1, invocations.get());
    }

    @Test
    public void allValue() {
        for (Palette palette : testPalettes()) {
            // Initially all zero
            assertTrue(palette.all(0));
            assertFalse(palette.all(1));
            palette.set(0, 0, 1, 1);
            assertFalse(palette.all(0));
            assertFalse(palette.all(1));
            assertFalse(palette.all(5)); // Value absent from the palette
            palette.set(0, 0, 1, 0);
            assertTrue(palette.all(0));
            palette.replace(0, 2);
            assertTrue(palette.all(2));
            assertFalse(palette.all(0));
        }
        for (Palette palette : testPalettes()) {
            palette.fill(5);
            assertTrue(palette.all(5));
            assertFalse(palette.all(0));
            palette.fill(0);
            assertTrue(palette.all(0));
            assertFalse(palette.all(5));
        }
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> (x + y + z) % 2 == 0 ? 3 : 7);
            assertFalse(palette.all(3));
            assertFalse(palette.all(7));
            palette.replaceAll((_, _, _, _) -> 7); // Direct storage afterwards
            assertTrue(palette.all(7));
            assertFalse(palette.all(3));
        }
    }

    @Test
    public void allValueOnDirectStorage() {
        final PaletteImpl palette = (PaletteImpl) Palette.blocks();
        palette.setAll((x, y, z) -> (x + y + z) % 2);
        palette.optimize(Palette.Optimization.SPEED);
        assertEquals(15, palette.bitsPerEntry());
        assertNull(palette.table);
        assertFalse(palette.all(0));
        assertFalse(palette.all(1));
        assertFalse(palette.all(7));

        palette.replace(1, 0);
        assertEquals(15, palette.bitsPerEntry());
        assertNull(palette.table);
        assertTrue(palette.all(0));
        assertFalse(palette.all(1));
    }

    @Test
    public void allPredicate() {
        for (Palette palette : testPalettes()) {
            assertTrue(palette.all(value -> value == 0));
            assertFalse(palette.all(value -> value != 0));

            palette.set(0, 0, 0, 42);
            assertTrue(palette.all(value -> value >= 0));
            assertFalse(palette.all(value -> value == 0));

            palette.optimize(Palette.Optimization.SPEED);
            assertTrue(palette.all(value -> value >= 0));
            assertFalse(palette.all(value -> value == 0));
        }
    }

    @Test
    public void allPredicateStopsAtFirstMismatch() {
        final Palette palette = Palette.blocks();
        palette.set(0, 0, 0, 1);

        final AtomicInteger invocations = new AtomicInteger();
        assertFalse(palette.all(_ -> {
            invocations.incrementAndGet();
            return false;
        }));
        assertEquals(1, invocations.get());

        palette.optimize(Palette.Optimization.SPEED);
        invocations.set(0);
        assertFalse(palette.all(_ -> {
            invocations.incrementAndGet();
            return false;
        }));
        assertEquals(1, invocations.get());
    }

    @Test
    public void countValueEdgeCases() {
        for (Palette palette : testPalettes()) {
            // All zero
            assertEquals(palette.maxSize(), palette.count(0));
            assertEquals(0, palette.count(-1));
            assertEquals(0, palette.count(Integer.MAX_VALUE));
            // Fill with the largest direct value
            final int maxValue = (1 << 15) - 1;
            palette.fill(maxValue);
            assertEquals(palette.maxSize(), palette.count(maxValue));
            assertEquals(0, palette.count(0));
        }
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> (x == 0 && y == 0 && z == 0) ? 42 : 0);
            assertEquals(1, palette.count(42));
            assertEquals(palette.maxSize() - 1, palette.count(0));
        }
    }

    @Test
    public void bulk() {
        for (Palette palette : testPalettes()) {
            final int dimension = palette.dimension();
            // Place
            for (int x = 0; x < dimension; x++) {
                for (int y = 0; y < dimension; y++) {
                    for (int z = 0; z < dimension; z++) {
                        palette.set(x, y, z, x + y + z + 1);
                    }
                }
            }
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            // Verify
            for (int x = 0; x < dimension; x++) {
                for (int y = 0; y < dimension; y++) {
                    for (int z = 0; z < dimension; z++) {
                        assertEquals(x + y + z + 1, palette.get(x, y, z));
                    }
                }
            }
        }
    }

    @Test
    public void bulkAll() {
        for (Palette palette : testPalettes()) {
            // Fill all entries
            palette.setAll((x, y, z) -> x + y + z + 1);
            palette.getAll((x, y, z, value) -> assertEquals(x + y + z + 1, value,
                    "x: " + x + ", y: " + y + ", z: " + z + ", dimension: " + palette.dimension()));

            // Replacing
            palette.replaceAll((x, y, z, value) -> {
                assertEquals(x + y + z + 1, value);
                return x + y + z + 2;
            });
            palette.getAll((x, y, z, value) -> assertEquals(x + y + z + 2, value));
        }

        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            assertEquals(100, palette.get(0, 0, 0));
            palette.getAll((x, y, z, value) -> assertEquals(x + y + z + 100, value,
                    "x: " + x + ", y: " + y + ", z: " + z + ", dimension: " + palette.dimension()));
        }
    }

    @Test
    public void setAllMayInvokeBulkOperationOnAnotherPalette() {
        final Palette palette = Palette.blocks();
        final Palette nested = Palette.blocks();
        final AtomicInteger nestedCalls = new AtomicInteger();
        palette.setAll((x, y, z) -> {
            if (x == 0 && y == 8 && z == 0) {
                nestedCalls.incrementAndGet();
                nested.setAll((_, _, _) -> 7);
            }
            return Palettes.sectionIndex(palette.dimension(), x, y, z) + 1;
        });

        assertEquals(1, nestedCalls.get());
        palette.getAll((x, y, z, value) ->
                assertEquals(Palettes.sectionIndex(palette.dimension(), x, y, z) + 1, value));
        assertAllEquals(7, nested);
    }

    @Test
    public void replaceAllMayInvokeBulkOperationOnAnotherPalette() {
        final Palette palette = Palette.blocks();
        final Palette nested = Palette.blocks();
        palette.setAll((x, y, z) -> Palettes.sectionIndex(palette.dimension(), x, y, z) + 1);
        nested.fill(4);

        final AtomicInteger nestedCalls = new AtomicInteger();
        palette.replaceAll((x, y, z, value) -> {
            if (x == 0 && y == 8 && z == 0) {
                nestedCalls.incrementAndGet();
                nested.replaceAll((_, _, _, nestedValue) -> nestedValue + 1);
            }
            return value + 1;
        });

        assertEquals(1, nestedCalls.get());
        palette.getAll((x, y, z, value) ->
                assertEquals(Palettes.sectionIndex(palette.dimension(), x, y, z) + 2, value));
        assertAllEquals(5, nested);
    }

    @Test
    public void bulkAllOrder() {
        for (Palette palette : testPalettes()) {
            AtomicInteger count = new AtomicInteger();

            // Ensure that the lambda is called for every entry
            // even if the array is initialized
            palette.getAll((_, _, _, _) -> count.incrementAndGet());
            assertEquals(count.get(), palette.maxSize());

            // Fill all entries
            count.set(0);
            Set<Point> points = new HashSet<>();
            palette.setAll((x, y, z) -> {
                assertTrue(points.add(new Vec(x, y, z)), "Duplicate point: " + x + ", " + y + ", " + z + ", dimension " + palette.dimension());
                return count.incrementAndGet();
            });
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            assertEquals(nonZeroCount(palette), count.get());

            count.set(0);
            palette.getAll((_, _, _, value) -> assertEquals(count.incrementAndGet(), value));
            assertEquals(count.get(), nonZeroCount(palette));

            // Replacing
            count.set(0);
            palette.replaceAll((_, _, _, value) -> {
                assertEquals(count.incrementAndGet(), value);
                return count.get();
            });
            assertEquals(count.get(), nonZeroCount(palette));

            count.set(0);
            palette.getAll((_, _, _, value) -> assertEquals(count.incrementAndGet(), value));
        }
    }

    @Test
    public void setAllConstant() {
        for (Palette palette : testPalettes()) {
            palette.setAll((_, _, _) -> 1);
            palette.getAll((_, _, _, value) -> assertEquals(1, value));
        }
    }

    @Test
    public void setAllBig() {
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            assertEquals(palette.maxSize(), nonZeroCount(palette));
            assertEquals(100, palette.get(0, 0, 0));
            palette.getAll((x, y, z, value) -> {
                int expected = x + y + z + 100;
                assertEquals(expected, value);
            });
        }
    }

    @Test
    public void getAllEmpty() {
        for (Palette palette : testPalettes()) {
            palette.getAll((_, _, _, value) -> assertEquals(0, value));
        }
    }

    @Test
    public void getAllCounts() {
        for (Palette palette : testPalettes()) {
            final Map<Integer, Integer> initial = new HashMap<>();
            palette.getAllCounts((value, count) ->
                    assertNull(initial.put(value, count), "each value must be reported once"));
            assertEquals(Map.of(0, palette.maxSize()), initial);

            palette.fill(5);
            final Map<Integer, Integer> filled = new HashMap<>();
            palette.getAllCounts((value, count) ->
                    assertNull(filled.put(value, count), "each value must be reported once"));
            assertEquals(Map.of(5, palette.maxSize()), filled);
        }
        for (Palette palette : testPalettes()) {
            palette.fill(5);
            palette.set(0, 0, 0, 0);
            palette.set(1, 0, 0, 7);
            final Map<Integer, Integer> counts = new HashMap<>();
            palette.getAllCounts((value, count) ->
                    assertNull(counts.put(value, count), "each value must be reported once"));
            assertEquals(Map.of(
                    0, 1,
                    5, palette.maxSize() - 2,
                    7, 1), counts);
            assertEquals(palette.maxSize(), counts.values().stream().mapToInt(Integer::intValue).sum());
        }
    }

    @Test
    public void getAllCountsMergesDuplicatePaletteValues() {
        final PaletteImpl palette = (PaletteImpl) Palette.blocks();
        final int[] indices = new int[palette.maxSize()];
        for (int i = 0; i < indices.length; i++) indices[i] = i % 4;
        palette.load(new int[]{5, 5, 7, 5}, Palettes.pack(indices, Palette.BLOCK_PALETTE_MIN_BITS));

        assertEquals(Palette.BLOCK_PALETTE_MIN_BITS, palette.bitsPerEntry());
        assertNotNull(palette.table);
        assertEquals(2, palette.table.size());
        final Map<Integer, Integer> counts = new HashMap<>();
        palette.getAllCounts((value, count) ->
                assertNull(counts.put(value, count), "each value must be reported once"));
        assertEquals(Map.of(
                5, palette.maxSize() * 3 / 4,
                7, palette.maxSize() / 4), counts);
        palette.getAll((x, y, z, value) ->
                assertEquals(Palettes.sectionIndex(16, x, y, z) % 4 == 2 ? 7 : 5, value));
    }

    @Test
    public void replaceAll() {
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 1);
            palette.replaceAll((x, y, z, value) -> {
                assertEquals(x + y + z + 1, value);
                return x + y + z + 2;
            });
            palette.getAll((x, y, z, value) -> assertEquals(x + y + z + 2, value));
        }

        for (Palette palette : testPalettes()) {
            palette.fill(0);
            palette.replaceAll((_, _, _, value) -> {
                assertEquals(0, value);
                return value + 1;
            });
            palette.getAll((_, _, _, value) -> assertEquals(1, value));
        }

        for (Palette palette : testPalettes()) {
            palette.fill(1);
            palette.replaceAll((_, _, _, value) -> {
                assertEquals(1, value);
                return value + 1;
            });
            palette.getAll((_, _, _, value) -> assertEquals(2, value));
        }
    }

    @Test
    public void replaceUnary() {
        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 0, 1);
            palette.replace(0, 0, 0, operand -> {
                assertEquals(1, operand);
                return 2;
            });
            assertEquals(2, palette.get(0, 0, 0));
        }
    }

    @Test
    public void replaceUnaryIdentityLeavesStorageUntouched() {
        final PaletteImpl single = (PaletteImpl) Palette.blocks();
        single.fill(7);
        single.replace(0, 0, 0, value -> value);
        assertEquals(0, single.bitsPerEntry());
        assertNull(single.values);
        assertEquals(7, single.get(0, 0, 0));

        final PaletteImpl indirect = (PaletteImpl) Palette.blocks();
        indirect.set(0, 0, 0, 5);
        indirect.set(1, 0, 0, 6);
        final PaletteTable table = indirect.table;
        final long[] values = indirect.values;
        indirect.replace(1, 0, 0, value -> value);
        assertSame(table, indirect.table);
        assertSame(values, indirect.values);
        assertEquals(6, indirect.get(1, 0, 0));
    }

    @Test
    public void replaceLoop() {
        var palette = Palette.sized(2, 1, 8, 15, 4);
        palette.setAll((x, y, z) -> x + y + z);
        final int dimension = palette.dimension();
        for (int x = 0; x < dimension; x++) {
            for (int y = 0; y < dimension; y++) {
                for (int z = 0; z < dimension; z++) {
                    palette.replace(x, y, z, value -> value + 1);
                }
            }
        }
    }

    @Test
    public void dimension() {
        assertThrows(Exception.class, () -> Palette.empty(-4, 3, 5, 15));
        assertThrows(Exception.class, () -> Palette.empty(0, 3, 5, 15));
        assertThrows(Exception.class, () -> Palette.empty(1, 3, 5, 15));
        assertDoesNotThrow(() -> Palette.empty(2, 3, 5, 15));
        assertThrows(Exception.class, () -> Palette.empty(3, 3, 5, 15));
        assertDoesNotThrow(() -> Palette.empty(4, 3, 5, 15));
        assertThrows(Exception.class, () -> Palette.empty(6, 3, 5, 15));
        assertDoesNotThrow(() -> Palette.empty(16, 3, 5, 15));
    }

    @Test
    public void configurationValidation() {
        assertThrows(IllegalArgumentException.class, () -> Palette.empty(4, 0, 3, 15));
        assertThrows(IllegalArgumentException.class, () -> Palette.empty(4, 5, 3, 15));
        assertThrows(IllegalArgumentException.class, () -> Palette.empty(4, 1, 31, 31));
        assertThrows(IllegalArgumentException.class, () -> Palette.empty(4, 1, 3, 0));
        assertThrows(IllegalArgumentException.class, () -> Palette.empty(4, 1, 3, -1));
        assertThrows(IllegalArgumentException.class, () -> Palette.empty(4, 1, 3, 32));

        // The widest accepted direct width holds every non negative int
        final Palette widest = Palette.empty(4, 1, 3, 31);
        widest.set(0, 0, 0, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, widest.get(0, 0, 0));
    }

    @Test
    public void serializationBlockEmpty() {
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Palette palette = Palette.blocks();
        buffer.write(Palette.BLOCK_SERIALIZER, palette);

        Palette deserialized = buffer.read(Palette.BLOCK_SERIALIZER);
        assertTrue(palette.compare(deserialized));
    }

    @Test
    public void serializationBlockPalette() {
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Palette palette = Palette.blocks();
        palette.set(0, 0, 0, 1);
        palette.set(1, 0, 0, 2);
        buffer.write(Palette.BLOCK_SERIALIZER, palette);

        Palette deserialized = buffer.read(Palette.BLOCK_SERIALIZER);
        assertTrue(palette.compare(deserialized));
    }

    @Test
    public void serializationBlockLinearMutation() {
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Palette palette = Palette.blocks();
        palette.set(0, 0, 0, 1);
        palette.set(1, 0, 0, 2);

        buffer.write(Palette.BLOCK_SERIALIZER, palette);
        Palette deserialized = buffer.read(Palette.BLOCK_SERIALIZER);

        deserialized.set(2, 0, 0, 3);

        assertEquals(1, deserialized.get(0, 0, 0));
        assertEquals(2, deserialized.get(1, 0, 0));
        assertEquals(3, deserialized.get(2, 0, 0));
    }

    @Test
    public void serializationBlockDirect() {
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Random random = new Random(12345);
        Palette palette = Palette.blocks();
        palette.setAll((_, _, _) -> random.nextInt(2048));

        buffer.write(Palette.BLOCK_SERIALIZER, palette);

        Palette deserialized = buffer.read(Palette.BLOCK_SERIALIZER);
        assertTrue(palette.compare(deserialized));
    }

    @Test
    public void serializationBiomeEmpty() {
        final var serializer = Palette.biomeSerializer(128);
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Palette palette = Palette.biomes(128);
        buffer.write(serializer, palette);

        Palette deserialized = buffer.read(serializer);
        assertTrue(palette.compare(deserialized));
    }

    @Test
    public void serializationBiomePalette() {
        final var serializer = Palette.biomeSerializer(128);
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Palette palette = Palette.biomes(128);
        palette.set(0, 0, 0, 1);
        palette.set(1, 0, 0, 2);
        buffer.write(serializer, palette);

        Palette deserialized = buffer.read(serializer);
        assertTrue(palette.compare(deserialized));
    }

    @Test
    public void serializationBiomeDirect() {
        final var serializer = Palette.biomeSerializer(128);
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Palette palette = Palette.biomes(128);
        Random random = new Random(12345);
        palette.setAll((_, _, _) -> random.nextInt(128));

        buffer.write(serializer, palette);

        Palette deserialized = buffer.read(serializer);
        assertTrue(palette.compare(deserialized));
    }

    @Test
    public void serializationBiomeDirectBoundary() {
        final var serializer = Palette.biomeSerializer(9);
        final PaletteImpl palette = (PaletteImpl) Palette.biomes(9);
        for (int i = 1; i <= 8; i++) palette.set(i & 3, 0, (i >> 2) & 3, i);
        assertEquals(4, palette.bitsPerEntry());
        assertNull(palette.table);

        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(serializer, palette);
        final PaletteImpl deserialized = (PaletteImpl) buffer.read(serializer);
        assertEquals(4, deserialized.bitsPerEntry());
        assertNull(deserialized.table);
        assertTrue(palette.compare(deserialized));
    }

    @Test
    public void mutationsRejectOutOfRangeValues() {
        for (Palette palette : testPalettes()) {
            assertThrows(IllegalArgumentException.class, () -> palette.set(0, 0, 0, -1));
            assertThrows(IllegalArgumentException.class, () -> palette.set(0, 0, 0, 1 << 15));
            assertThrows(IllegalArgumentException.class, () -> palette.fill(-5));
            assertThrows(IllegalArgumentException.class, () -> palette.replace(0, -2));
            assertThrows(IllegalArgumentException.class, () -> palette.valueToPaletteIndex(-6));
            assertThrows(IllegalArgumentException.class, () -> palette.setAll((_, _, _) -> -1));
            assertThrows(IllegalArgumentException.class, () -> palette.replace(0, 0, 0, _ -> -3));
            assertThrows(IllegalArgumentException.class, () -> palette.replaceAll((_, _, _, _) -> -4));
            assertThrows(IllegalArgumentException.class, () -> palette.offset(-1));
            assertThrows(IllegalArgumentException.class, () -> palette.load(new int[]{0, -7}, new long[0]));
            assertAllEquals(0, palette);
        }
    }

    @Test
    public void biomeDirectBitsFollowRegistrySize() {
        final int[] counts = {1, 8, 9, 64, 65};
        final int[] expectedBits = {1, 3, 4, 6, 7};
        for (int i = 0; i < counts.length; i++) {
            final PaletteImpl palette = (PaletteImpl) Palette.biomes(counts[i]);
            assertEquals(expectedBits[i], palette.directBits, "biome count " + counts[i]);
        }
    }

    @Test
    public void publicFactoriesRetainBitsPerEntrySemantics() {
        final PaletteImpl blocks = (PaletteImpl) Palette.blocks(4);
        assertEquals(4, blocks.bitsPerEntry());
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, blocks.directBits);

        final PaletteImpl biomes = (PaletteImpl) Palette.biomes(64, 2);
        assertEquals(2, biomes.bitsPerEntry());
        assertEquals(6, biomes.directBits);
        assertTrue(biomes.all(0));

        final PaletteImpl directBiomes = (PaletteImpl) Palette.biomes(64, 6);
        assertEquals(6, directBiomes.bitsPerEntry());
        assertNull(directBiomes.table);

        final PaletteImpl emptyBiomes = (PaletteImpl) Palette.biomes(64);
        assertEquals(0, emptyBiomes.bitsPerEntry());
        assertEquals(6, emptyBiomes.directBits);

        assertThrows(IllegalArgumentException.class, () -> Palette.biomes(64, 5));
    }

    @Test
    public void sizedAcceptsDirectWidthAndRejectsInvalidWidths() {
        final PaletteImpl direct = (PaletteImpl) Palette.sized(16, 4, 8, 15, 15);
        assertEquals(15, direct.bitsPerEntry());
        assertNull(direct.table);
        assertNotNull(direct.values);
        assertEquals(0, direct.get(0, 0, 0));

        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Palette.sized(16, 4, 8, 15, 9));
        assertEquals("Bits per entry must be 0, within [4, 8], or the direct width 15, got 9",
                failure.getMessage());
        failure = assertThrows(IllegalArgumentException.class,
                () -> Palette.sized(16, 4, 8, 15, 2));
        assertEquals("Bits per entry must be 0, within [4, 8], or the direct width 15, got 2",
                failure.getMessage());
    }

    @Test
    public void loadBelowMinBitsPerEntry() {
        // Test loading with bpe below minBitsPerEntry - should resize to minBitsPerEntry
        Palette palette = Palette.sized(4, 4, 8, 15, 4); // min=4, max=8, direct=15

        int[] paletteData = {0, 1, 2, 3}; // 4 values need 2 bits, but min is 4
        long[] values = new long[]{0x3210L}; // packed with 2 bits per entry

        palette.load(paletteData, values);

        // Should be resized to minBitsPerEntry (4)
        assertEquals(4, palette.bitsPerEntry());

        // Values should still be accessible correctly
        assertEquals(0, palette.get(0, 0, 0));
        assertEquals(1, palette.get(1, 0, 0));
        assertEquals(2, palette.get(2, 0, 0));
        assertEquals(3, palette.get(3, 0, 0));
    }

    @Test
    public void loadAboveMaxBitsPerEntry() {
        // Test loading with bpe above maxBitsPerEntry - should become direct palette
        Palette palette = Palette.sized(4, 1, 3, 15, 1); // min=1, max=3, direct=15

        // Create palette that would need more than 3 bits (max) - 16 values need 4 bits
        int[] paletteData = new int[16];
        for (int i = 0; i < 16; i++) {
            paletteData[i] = i + 100; // arbitrary values
        }

        // Create values array with 4 bits per entry
        long[] values = new long[4]; // 64 entries, 4 bits each = 16 longs per entry, 4 longs total
        for (int i = 0; i < 64; i++) {
            int longIndex = i / 16;
            int bitIndex = (i % 16) * 4;
            values[longIndex] |= ((long) (i % 16)) << bitIndex;
        }

        palette.load(paletteData, values);

        // Should become direct palette (directBits = 15)
        assertEquals(15, palette.bitsPerEntry());

        // Should not have a palette anymore (direct mode)
        assertNull(((PaletteImpl) palette).table);
    }

    @Test
    public void loadOversizedPaletteWithoutDirectModeStaysIndirect() {
        final PaletteImpl palette = (PaletteImpl) Palette.empty(4, 1, 3, 3);
        final int[] paletteData = new int[16];
        for (int i = 0; i < paletteData.length; i++) paletteData[i] = i % 6;
        final int[] indices = new int[palette.maxSize()];
        for (int i = 0; i < indices.length; i++) indices[i] = i % 16;
        palette.load(paletteData, Palettes.pack(indices, 4));

        assertEquals(3, palette.bitsPerEntry());
        assertNotNull(palette.table);
        palette.getAll((x, y, z, value) ->
                assertEquals(paletteData[Palettes.sectionIndex(4, x, y, z) % 16], value));
        assertCountsMatchContent(palette);

        final int[] oversized = new int[16];
        for (int i = 0; i < oversized.length; i++) oversized[i] = i % 9;
        final Palette fresh = Palette.empty(4, 1, 3, 3);
        assertThrows(IllegalArgumentException.class, () -> fresh.load(oversized, Palettes.pack(indices, 4)));
    }

    @Test
    public void loadWithinRange() {
        // Test loading with bpe within min-max range - should use calculated bpe
        Palette palette = Palette.sized(4, 2, 6, 15, 2); // min=2, max=6, direct=15

        int[] paletteData = {0, 10, 20, 30, 40}; // 5 values need 3 bits
        long[] values = new long[12]; // 64 entries, 3 bits each

        // Fill with some test pattern
        for (int i = 0; i < 64; i++) {
            int longIndex = i / 21; // 21 values per long with 3 bits each (63 bits used)
            int bitIndex = (i % 21) * 3;
            values[longIndex] |= ((long) (i % 5)) << bitIndex;
        }

        palette.load(paletteData, values);

        // Should use 3 bits (calculated from palette size)
        assertEquals(3, palette.bitsPerEntry());

        // Should have palette
        assertNotNull(((PaletteImpl) palette).table);

        // Verify palette contents
        assertEquals(5, ((PaletteImpl) palette).table.size());
        assertEquals(0, ((PaletteImpl) palette).table.value(0));
        assertEquals(10, ((PaletteImpl) palette).table.value(1));
        assertEquals(20, ((PaletteImpl) palette).table.value(2));
        assertEquals(30, ((PaletteImpl) palette).table.value(3));
        assertEquals(40, ((PaletteImpl) palette).table.value(4));
    }

    @Test
    public void loadExactlyMinBitsPerEntry() {
        // Test loading where calculated bpe equals minBitsPerEntry
        Palette palette = Palette.sized(4, 3, 8, 15, 3); // min=3, max=8, direct=15

        int[] paletteData = {0, 1, 2, 3, 4, 5, 6, 7}; // 8 values need exactly 3 bits
        long[] values = new long[12]; // 64 entries, 3 bits each

        palette.load(paletteData, values);

        // Should use exactly minBitsPerEntry (3)
        assertEquals(3, palette.bitsPerEntry());

        // Should have palette
        assertNotNull(((PaletteImpl) palette).table);
        assertEquals(8, ((PaletteImpl) palette).table.size());
    }

    @Test
    public void loadExactlyMaxBitsPerEntry() {
        // Test loading where calculated bpe equals maxBitsPerEntry
        Palette palette = Palette.sized(4, 2, 4, 15, 2); // min=2, max=4, direct=15

        int[] paletteData = new int[16]; // 16 values need exactly 4 bits
        for (int i = 0; i < 16; i++) {
            paletteData[i] = i * 10;
        }
        long[] values = new long[16]; // 64 entries, 4 bits each

        palette.load(paletteData, values);

        // Should use exactly maxBitsPerEntry (4)
        assertEquals(4, palette.bitsPerEntry());

        // Should still have palette (not direct)
        assertNotNull(((PaletteImpl) palette).table);
        assertEquals(16, ((PaletteImpl) palette).table.size());
    }

    @Test
    public void loadEmptyPalette() {
        // Test loading with empty palette
        Palette palette = Palette.sized(4, 1, 8, 15, 1);

        int[] paletteData = {0}; // Single value palette
        long[] values = new long[4]; // All zeros

        palette.load(paletteData, values);

        // Should use minBitsPerEntry since 1 value needs 0 bits but min is 1
        assertEquals(1, palette.bitsPerEntry());

        // Should have palette with single entry
        assertNotNull(((PaletteImpl) palette).table);
        assertEquals(1, ((PaletteImpl) palette).table.size());
        assertEquals(0, ((PaletteImpl) palette).table.value(0));
    }

    @Test
    public void loadEmptyPaletteArrayThrows() {
        final Palette palette = Palette.blocks();
        final IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> palette.load(new int[0], new long[0]));
        assertEquals("Palette cannot be empty", failure.getMessage());
    }

    @Test
    public void loadValuesCloned() {
        // Test that values array is properly cloned
        Palette palette = Palette.sized(4, 2, 6, 15, 2);

        int[] paletteData = {0, 1, 2};
        long[] originalValues = {0x123456789ABCDEFL, 0xFEDCBA9876543210L};

        palette.load(paletteData, originalValues);

        // Modify original array
        originalValues[0] = 0L;
        originalValues[1] = 0L;

        // Palette should still have the original values
        long[] paletteValues = palette.indexedValues();
        assertNotNull(paletteValues);
        assertEquals(0x123456789ABCDEFL, paletteValues[0]);
        assertEquals(0xFEDCBA9876543210L, paletteValues[1]);
    }

    @Test
    public void loadThousandsOfIndicesBecomesDirectPalette() {
        // Test loading with thousands of indices to ensure it becomes a direct palette
        Palette palette = Palette.blocks(); // min=4, max=8, direct=15

        // Create palette with thousands of unique values (way more than max palette size of 2^8=256)
        final int uniqueValueCount = 5000;
        int[] paletteData = new int[uniqueValueCount];
        for (int i = 0; i < uniqueValueCount; i++) {
            paletteData[i] = i + 1000; // Use offset to avoid zero values
        }

        // Calculate bits needed: log2(5000) ≈ 13 bits, which exceeds maxBitsPerEntry (8)
        // This should force direct palette mode
        int calculatedBits = 13; // Math.ceil(Math.log(uniqueValueCount) / Math.log(2))

        // Create values array for 4096 entries (16x16x16) with calculated bits per entry
        final int totalEntries = 16 * 16 * 16; // 4096 entries
        final int valuesPerLong = 64 / calculatedBits;
        final int valuesArrayLength = (totalEntries + valuesPerLong - 1) / valuesPerLong;
        long[] values = new long[valuesArrayLength];

        // Fill with pattern using modulo to cycle through available palette indices
        final long mask = (1L << calculatedBits) - 1;
        for (int i = 0; i < totalEntries; i++) {
            int paletteIndex = i % uniqueValueCount;
            int longIndex = i / valuesPerLong;
            int bitIndex = (i % valuesPerLong) * calculatedBits;
            values[longIndex] |= ((long) paletteIndex & mask) << bitIndex;
        }

        palette.load(paletteData, values);

        // Should become direct palette since uniqueValueCount >> 2^maxBitsPerEntry
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, palette.bitsPerEntry(),
                "Palette should use direct bits when loaded with thousands of indices");

        // Should not have indirect palette structures (direct mode)
        PaletteImpl impl = (PaletteImpl) palette;
        assertNull(impl.table, "Direct palette should not have an indirect table");

        // Verify we can still read some values correctly
        // In direct mode, palette indices become the actual values
        int firstValue = palette.get(0, 0, 0);
        assertTrue(firstValue >= 1000 && firstValue < 1000 + uniqueValueCount,
                "Value should be within expected range for direct palette: " + firstValue);

        // Verify the palette has proper count (non-zero blocks)
        assertTrue(nonZeroCount(palette) > 0, "Palette should have non-zero count");
        assertTrue(nonZeroCount(palette) <= palette.maxSize(), "Count should not exceed max size");
    }

    @Test
    public void loadDirectMapsOutOfRangeIndicesToZero() {
        final PaletteImpl palette = (PaletteImpl) Palette.blocks();
        final int[] paletteData = new int[600];
        for (int i = 0; i < paletteData.length; i++) paletteData[i] = i + 1000;
        final int[] indices = new int[palette.maxSize()];
        for (int i = 0; i < indices.length; i++) indices[i] = i % 600;
        final int poisonedIndex = Palettes.sectionIndex(16, 3, 2, 1);
        indices[poisonedIndex] = 700;
        palette.load(paletteData, Palettes.pack(indices, 10));

        assertEquals(15, palette.bitsPerEntry());
        assertNull(palette.table);
        assertEquals(0, palette.get(3, 2, 1));
        palette.getAll((x, y, z, value) -> {
            final int index = Palettes.sectionIndex(16, x, y, z);
            if (index == poisonedIndex) assertEquals(0, value);
            else assertEquals(1000 + (index % 600), value);
        });
    }

    @Test
    public void height() {
        for (Palette palette : testPalettes()) {
            final int dimension = palette.dimension();

            // Test with empty palette - predicate that always returns true should find the
            // top
            assertEquals(dimension - 1, palette.height(0, 0, (_, _, _, _) -> true));
            // Predicate that always returns false should return -1
            assertEquals(-1, palette.height(0, 0, (_, _, _, _) -> false));

            // Set a block at the top
            palette.set(0, dimension - 1, 0, 1);
            assertEquals(dimension - 1, palette.height(0, 0, (_, _, _, value) -> value != 0));

            // Set a block in the middle
            if (dimension > 1) {
                palette.set(1, dimension / 2, 1, 2);
                assertEquals(dimension / 2, palette.height(1, 1, (_, _, _, value) -> value != 0));
            }

            // Set blocks at multiple heights - should return the highest one
            if (dimension > 2) {
                palette.set(2, 1, 2, 3);
                palette.set(2, dimension - 2, 2, 4);
                assertEquals(dimension - 2, palette.height(2, 2, (_, _, _, value) -> value != 0));
            }

            // Test with predicate that matches air (value 0)
            palette.fill(5); // Fill with non-zero value
            int testX = Math.min(1, dimension - 1);
            int testZ = Math.min(1, dimension - 1);
            palette.set(testX, dimension / 2, testZ, 0); // Set one block to air
            assertEquals(dimension / 2, palette.height(testX, testZ, (_, _, _, value) -> value == 0));

            // Test edge cases - coordinates at boundaries
            palette.fill(0);
            palette.set(dimension - 1, dimension - 1, dimension - 1, 10);
            assertEquals(dimension - 1, palette.height(dimension - 1, dimension - 1, (_, _, _, value) -> value != 0));

            // Test with complex predicate
            palette.fill(0);
            for (int y = 0; y < dimension; y++) {
                palette.set(0, y, 0, y + 1);
            }
            // Find highest block with value > 5
            int expectedHeight = -1;
            for (int y = dimension - 1; y >= 0; y--) {
                if (y + 1 > 5) {
                    expectedHeight = y;
                    break;
                }
            }
            assertEquals(expectedHeight, palette.height(0, 0, (_, _, _, value) -> value > 5));
        }
    }

    @Test
    public void heightValidation() {
        Palette palette = Palette.blocks();
        final int dimension = palette.dimension();

        // Test invalid coordinates
        assertThrows(IllegalArgumentException.class, () -> palette.height(-1, 0, (_, _, _, _) -> true));
        assertThrows(IllegalArgumentException.class, () -> palette.height(0, -1, (_, _, _, _) -> true));
        assertThrows(IllegalArgumentException.class, () -> palette.height(dimension, 0, (_, _, _, _) -> true));
        assertThrows(IllegalArgumentException.class, () -> palette.height(0, dimension, (_, _, _, _) -> true));
    }

    @Test
    public void heightOptimization() {
        // Test single-value palette optimization
        Palette singleValuePalette = Palette.blocks();
        singleValuePalette.fill(42);
        
        // Should find the value at the top
        assertEquals(15, singleValuePalette.height(0, 0, (_, _, _, value) -> value == 42));
        assertEquals(-1, singleValuePalette.height(0, 0, (_, _, _, value) -> value == 0));
        
        // Test multi-value palette optimization
        Palette multiValuePalette = Palette.blocks();
        multiValuePalette.set(5, 10, 5, 100);
        multiValuePalette.set(5, 8, 5, 200);
        multiValuePalette.set(5, 12, 5, 300);
        
        // Should find the highest matching block
        assertEquals(12, multiValuePalette.height(5, 5, (_, _, _, value) -> value != 0));
        assertEquals(10, multiValuePalette.height(5, 5, (_, _, _, value) -> value == 100));
        assertEquals(8, multiValuePalette.height(5, 5, (_, _, _, value) -> value == 200));
        assertEquals(12, multiValuePalette.height(5, 5, (_, _, _, value) -> value == 300));
        assertEquals(-1, multiValuePalette.height(5, 5, (_, _, _, value) -> value == 999));
    }

    @Test
    public void count() {
        Palette testPalette = Palette.blocks();
        testPalette.fill(5000);
        assertEquals(4096, nonZeroCount(testPalette));

        // Should correctly count
        testPalette.set(0, 0, 0, 0);
        testPalette.set(0, 0, 1, 1);
        testPalette.set(0, 0, 2, 2);
        testPalette.set(0, 0, 3, 3);
        assertEquals(4095, nonZeroCount(testPalette));

        testPalette.set(0, 0, 0, 5000);
        assertEquals(4096, nonZeroCount(testPalette));

        testPalette.replace(5000, 0);
        assertEquals(3, nonZeroCount(testPalette));
    }

    @Test
    public void loadCount() {
        Palette testPalette = Palette.empty(4, 4, 8, 12);
        int[] palette = new int[] { 10, 2, 4, 0 };
        // 12 palette values that lead to 0 and 6 zeroed palette values
        long[] values = new long[] { 0x01230123, 0x00130013, 0x33333333, 0x22222222 };
        testPalette.load(palette, values);
        assertEquals(testPalette.maxSize() - 12, nonZeroCount(testPalette));
    }

}
