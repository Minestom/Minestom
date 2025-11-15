package net.minestom.server.instance.palette;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

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
            assertEquals(0, palette.count());
            palette.set(0, 0, 0, 64);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(1, palette.count());

            palette.set(1, 0, 0, 65);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(65, palette.get(1, 0, 0));
            assertEquals(2, palette.count());

            palette.set(0, 1, 0, 66);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(65, palette.get(1, 0, 0));
            assertEquals(66, palette.get(0, 1, 0));
            assertEquals(3, palette.count());

            palette.set(0, 0, 1, 67);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(65, palette.get(1, 0, 0));
            assertEquals(66, palette.get(0, 1, 0));
            assertEquals(67, palette.get(0, 0, 1));
            assertEquals(4, palette.count());

            palette.set(0, 0, 1, 68);
            assertEquals(4, palette.count());
        }
    }

    @Test
    public void placementHighValue() {
        final int value = 250_000;
        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 1, value);
            assertEquals(value, palette.get(0, 0, 1));
        }
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
    public void fill() {
        for (Palette palette : testPalettes()) {
            assertEquals(0, palette.count());
            palette.set(0, 0, 0, 5);
            assertEquals(1, palette.count());
            assertEquals(5, palette.get(0, 0, 0));
            palette.fill(6);
            assertEquals(6, palette.get(0, 0, 0));
            assertEquals(palette.maxSize(), palette.count());
            for (int x = 0; x < palette.dimension(); x++) {
                for (int y = 0; y < palette.dimension(); y++) {
                    for (int z = 0; z < palette.dimension(); z++) {
                        assertEquals(6, palette.get(x, y, z));
                    }
                }
            }

            palette.fill(0);
            assertEquals(0, palette.count());
            for (int x = 0; x < palette.dimension(); x++) {
                for (int y = 0; y < palette.dimension(); y++) {
                    for (int z = 0; z < palette.dimension(); z++) {
                        assertEquals(0, palette.get(x, y, z));
                    }
                }
            }
        }
    }

    @Test
    public void offset() {
        for (Palette palette : testPalettes()) {
            palette.fill(0);
            palette.offset(1);
            for (int x = 0; x < palette.dimension(); x++) {
                for (int y = 0; y < palette.dimension(); y++) {
                    for (int z = 0; z < palette.dimension(); z++) {
                        assertEquals(1, palette.get(x, y, z));
                    }
                }
            }

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
            assertEquals(0, palette.count());
            palette.fill(0);
            assertEquals(0, palette.count());
            palette.offset(1);
            assertEquals(palette.maxSize(), palette.count());
            palette.offset(-1);
            assertEquals(0, palette.count());
        }
        for (Palette palette : testPalettes()) {
            palette.fill(1);
            assertEquals(palette.maxSize(), palette.count());
            palette.set(0, 0, 1, 2);
            palette.set(0, 1, 0, 3);
            palette.set(1, 0, 0, 4);
            palette.offset(-1);
            assertEquals(3, palette.count());
            palette.offset(1);
            assertEquals(palette.maxSize(), palette.count());
        }
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            assertEquals(palette.maxSize(), palette.count());
            palette.offset(50);
            assertEquals(palette.maxSize(), palette.count());
            palette.offset(-50);
            assertEquals(palette.maxSize(), palette.count());
        }
    }

    @Test
    public void replace() {
        for (Palette palette : testPalettes()) {
            palette.fill(0);
            palette.replace(0, 1);
            for (int x = 0; x < palette.dimension(); x++) {
                for (int y = 0; y < palette.dimension(); y++) {
                    for (int z = 0; z < palette.dimension(); z++) {
                        assertEquals(1, palette.get(x, y, z));
                    }
                }
            }

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
            assertEquals(palette.maxSize(), palette.count());
            palette.replace(1, 0);
            assertEquals(0, palette.count());
        }
        for (Palette palette : testPalettes()) {
            palette.set(0, 0, 1, 1);
            palette.set(1, 1, 1, 1);
            palette.set(0, 1, 0, 2);
            palette.set(1, 0, 0, 3);
            assertEquals(4, palette.count());
            palette.replace(1, 0);
            assertEquals(2, palette.count());
        }
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            assertEquals(palette.maxSize(), palette.count());
            palette.replace(100, 0);
            assertEquals(palette.maxSize() - 1, palette.count());
        }
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
    public void countValueEdgeCases() {
        for (Palette palette : testPalettes()) {
            // All zero
            assertEquals(palette.maxSize(), palette.count(0));
            assertEquals(0, palette.count(-1));
            assertEquals(0, palette.count(Integer.MAX_VALUE));
            // Fill with negative value
            palette.fill(-7);
            assertEquals(palette.maxSize(), palette.count(-7));
            assertEquals(0, palette.count(0));
            // Fill with max int
            palette.fill(Integer.MAX_VALUE);
            assertEquals(palette.maxSize(), palette.count(Integer.MAX_VALUE));
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
            assertEquals(palette.maxSize(), palette.count());
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
    public void bulkAllOrder() {
        for (Palette palette : testPalettes()) {
            AtomicInteger count = new AtomicInteger();

            // Ensure that the lambda is called for every entry
            // even if the array is initialized
            palette.getAll((x, y, z, value) -> count.incrementAndGet());
            assertEquals(count.get(), palette.maxSize());

            // Fill all entries
            count.set(0);
            Set<Point> points = new HashSet<>();
            palette.setAll((x, y, z) -> {
                assertTrue(points.add(new Vec(x, y, z)), "Duplicate point: " + x + ", " + y + ", " + z + ", dimension " + palette.dimension());
                return count.incrementAndGet();
            });
            assertEquals(palette.maxSize(), palette.count());
            assertEquals(palette.count(), count.get());

            count.set(0);
            palette.getAll((x, y, z, value) -> assertEquals(count.incrementAndGet(), value));
            assertEquals(count.get(), palette.count());

            // Replacing
            count.set(0);
            palette.replaceAll((x, y, z, value) -> {
                assertEquals(count.incrementAndGet(), value);
                return count.get();
            });
            assertEquals(count.get(), palette.count());

            count.set(0);
            palette.getAll((x, y, z, value) -> assertEquals(count.incrementAndGet(), value));
        }
    }

    @Test
    public void setAllConstant() {
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> 1);
            palette.getAll((x, y, z, value) -> assertEquals(1, value));
        }
    }

    @Test
    public void setAllBig() {
        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            assertEquals(palette.maxSize(), palette.count());
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
            palette.getAll((x, y, z, value) -> assertEquals(0, value));
        }
    }

    @Test
    public void getAllPresent() {
        for (Palette palette : testPalettes()) {
            palette.getAllPresent((x, y, z, value) -> fail("The palette should be empty"));
            palette.set(0, 0, 1, 1);
            palette.getAllPresent((x, y, z, value) -> {
                assertEquals(0, x);
                assertEquals(0, y);
                assertEquals(1, z);
                assertEquals(1, value);
            });
        }
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
            palette.replaceAll((x, y, z, value) -> {
                assertEquals(0, value);
                return value + 1;
            });
            palette.getAll((x, y, z, value) -> assertEquals(1, value));
        }

        for (Palette palette : testPalettes()) {
            palette.fill(1);
            palette.replaceAll((x, y, z, value) -> {
                assertEquals(1, value);
                return value + 1;
            });
            palette.getAll((x, y, z, value) -> assertEquals(2, value));
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
        assertThrows(Exception.class, () -> Palette.empty(-4, 5, 3, 15));
        assertThrows(Exception.class, () -> Palette.empty(0, 5, 3, 15));
        assertThrows(Exception.class, () -> Palette.empty(1, 5, 3, 15));
        assertDoesNotThrow(() -> Palette.empty(2, 5, 3, 15));
        assertThrows(Exception.class, () -> Palette.empty(3, 5, 3, 15));
        assertDoesNotThrow(() -> Palette.empty(4, 5, 3, 15));
        assertThrows(Exception.class, () -> Palette.empty(6, 5, 3, 15));
        assertDoesNotThrow(() -> Palette.empty(16, 5, 3, 15));
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
    public void serializationBlockDirect() {
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Random random = new Random(12345);
        Palette palette = Palette.blocks();
        palette.setAll((x, y, z) -> random.nextInt(2048));

        buffer.write(Palette.BLOCK_SERIALIZER, palette);

        Palette deserialized = buffer.read(Palette.BLOCK_SERIALIZER);
        assertTrue(palette.compare(deserialized));
    }

    @Test
    public void serializationBiomeEmpty() {
        final var serializer = Palette.biomeSerializer(128);
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Palette palette = Palette.biomes();
        buffer.write(serializer, palette);

        Palette deserialized = buffer.read(serializer);
        assertTrue(palette.compare(deserialized));
    }

    @Test
    public void serializationBiomePalette() {
        final var serializer = Palette.biomeSerializer(128);
        NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        Palette palette = Palette.biomes();
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
        Palette palette = Palette.biomes();
        Random random = new Random(12345);
        palette.setAll((_, _, _) -> random.nextInt(2048));

        buffer.write(serializer, palette);

        Palette deserialized = buffer.read(serializer);
        assertTrue(palette.compare(deserialized));
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
        assertNull(((PaletteImpl) palette).paletteIndexMap);
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
        assertNotNull(((PaletteImpl) palette).paletteIndexMap);

        // Verify palette contents
        assertEquals(5,  ((PaletteImpl) palette).paletteIndexMap.size());
        assertEquals(0,  ((PaletteImpl) palette).paletteIndexMap.indexToValue(0));
        assertEquals(10, ((PaletteImpl) palette).paletteIndexMap.indexToValue(1));
        assertEquals(20, ((PaletteImpl) palette).paletteIndexMap.indexToValue(2));
        assertEquals(30, ((PaletteImpl) palette).paletteIndexMap.indexToValue(3));
        assertEquals(40, ((PaletteImpl) palette).paletteIndexMap.indexToValue(4));
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
        assertNotNull(((PaletteImpl) palette).paletteIndexMap);
        assertEquals(8, ((PaletteImpl) palette).paletteIndexMap.size());
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
        assertNotNull(((PaletteImpl) palette).paletteIndexMap);
        assertEquals(16, ((PaletteImpl) palette).paletteIndexMap.size());
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
        assertNotNull(((PaletteImpl) palette).paletteIndexMap);
        assertEquals(1, ((PaletteImpl) palette).paletteIndexMap.size());
        assertEquals(0, ((PaletteImpl) palette).paletteIndexMap.valueToIndexOrDefault(0));
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
        assertNull(impl.paletteIndexMap,
                "Direct palette should not have paletteIndexMap");

        // Verify we can still read some values correctly
        // In direct mode, palette indices become the actual values
        int firstValue = palette.get(0, 0, 0);
        assertTrue(firstValue >= 1000 && firstValue < 1000 + uniqueValueCount,
                "Value should be within expected range for direct palette: " + firstValue);

        // Verify the palette has proper count (non-zero blocks)
        assertTrue(palette.count() > 0, "Palette should have non-zero count");
        assertTrue(palette.count() <= palette.maxSize(), "Count should not exceed max size");
    }

    @Test
    public void height() {
        for (Palette palette : testPalettes()) {
            final int dimension = palette.dimension();

            // Test with empty palette - predicate that always returns true should find the
            // top
            assertEquals(dimension - 1, palette.height(0, 0, (x, y, z, value) -> true));
            // Predicate that always returns false should return -1
            assertEquals(-1, palette.height(0, 0, (x, y, z, value) -> false));

            // Set a block at the top
            palette.set(0, dimension - 1, 0, 1);
            assertEquals(dimension - 1, palette.height(0, 0, (x, y, z, value) -> value != 0));

            // Set a block in the middle
            if (dimension > 1) {
                palette.set(1, dimension / 2, 1, 2);
                assertEquals(dimension / 2, palette.height(1, 1, (x, y, z, value) -> value != 0));
            }

            // Set blocks at multiple heights - should return the highest one
            if (dimension > 2) {
                palette.set(2, 1, 2, 3);
                palette.set(2, dimension - 2, 2, 4);
                assertEquals(dimension - 2, palette.height(2, 2, (x, y, z, value) -> value != 0));
            }

            // Test with predicate that matches air (value 0)
            palette.fill(5); // Fill with non-zero value
            int testX = Math.min(1, dimension - 1);
            int testZ = Math.min(1, dimension - 1);
            palette.set(testX, dimension / 2, testZ, 0); // Set one block to air
            assertEquals(dimension / 2, palette.height(testX, testZ, (x, y, z, value) -> value == 0));

            // Test edge cases - coordinates at boundaries
            palette.fill(0);
            palette.set(dimension - 1, dimension - 1, dimension - 1, 10);
            assertEquals(dimension - 1, palette.height(dimension - 1, dimension - 1, (x, y, z, value) -> value != 0));

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
            assertEquals(expectedHeight, palette.height(0, 0, (x, y, z, value) -> value > 5));
        }
    }

    @Test
    public void heightValidation() {
        Palette palette = Palette.blocks();
        final int dimension = palette.dimension();

        // Test invalid coordinates
        assertThrows(IllegalArgumentException.class, () -> palette.height(-1, 0, (x, y, z, value) -> true));
        assertThrows(IllegalArgumentException.class, () -> palette.height(0, -1, (x, y, z, value) -> true));
        assertThrows(IllegalArgumentException.class, () -> palette.height(dimension, 0, (x, y, z, value) -> true));
        assertThrows(IllegalArgumentException.class, () -> palette.height(0, dimension, (x, y, z, value) -> true));
    }

    @Test
    public void heightOptimization() {
        // Test single-value palette optimization
        Palette singleValuePalette = Palette.blocks();
        singleValuePalette.fill(42);
        
        // Should find the value at the top
        assertEquals(15, singleValuePalette.height(0, 0, (x, y, z, value) -> value == 42));
        assertEquals(-1, singleValuePalette.height(0, 0, (x, y, z, value) -> value == 0));
        
        // Test multi-value palette optimization
        Palette multiValuePalette = Palette.blocks();
        multiValuePalette.set(5, 10, 5, 100);
        multiValuePalette.set(5, 8, 5, 200);
        multiValuePalette.set(5, 12, 5, 300);
        
        // Should find the highest matching block
        assertEquals(12, multiValuePalette.height(5, 5, (x, y, z, value) -> value != 0));
        assertEquals(10, multiValuePalette.height(5, 5, (x, y, z, value) -> value == 100));
        assertEquals(8, multiValuePalette.height(5, 5, (x, y, z, value) -> value == 200));
        assertEquals(12, multiValuePalette.height(5, 5, (x, y, z, value) -> value == 300));
        assertEquals(-1, multiValuePalette.height(5, 5, (x, y, z, value) -> value == 999));
    }

    @Test
    public void count() {
        Palette testPalette = Palette.blocks();
        testPalette.fill(5000);
        assertEquals(4096, testPalette.count());

        // Should correctly count
        testPalette.set(0, 0, 0, 0);
        testPalette.set(0, 0, 1, 1);
        testPalette.set(0, 0, 2, 2);
        testPalette.set(0, 0, 3, 3);
        assertEquals(4095, testPalette.count());

        testPalette.set(0, 0, 0, 5000);
        assertEquals(4096, testPalette.count());

        testPalette.replace(5000, 0);
        assertEquals(3, testPalette.count());
    }

    @Test
    public void loadCount() {
        Palette testPalette = Palette.empty(4, 4, 8, 12);
        int[] palette = new int[] { 10, 2, 4, 0 };
        // 12 palette values that lead to 0 and 6 zeroed palette values
        long[] values = new long[] { 0x01230123, 0x00130013, 0x33333333, 0x22222222 };
        testPalette.load(palette, values);
        assertEquals(testPalette.maxSize() - 12, testPalette.count());
    }

    @Test
    public void partialFill() {
        for (Palette palette : testPalettes()) {
            final int dimension = palette.dimension();
            final int dimensionMinus = dimension - 1;

            palette.fill(0, 0, 0, dimensionMinus - 1, dimensionMinus - 1, dimensionMinus - 1, 10);
            assertEquals(dimensionMinus * dimensionMinus * dimensionMinus, palette.count());

            for (int x = 0; x < dimension; x++) {
                for (int y = 0; y < dimension; y++) {
                    for (int z = 0; z < dimension; z++) {
                        if (x == dimensionMinus || y == dimensionMinus || z == dimensionMinus) {
                            assertEquals(0, palette.get(x, y, z));
                        } else {
                            assertEquals(10, palette.get(x, y, z));
                        }
                    }
                }
            }

            palette.fill(0);

            palette.fill(1, 1, 1, dimensionMinus, dimensionMinus, dimensionMinus, 10);
            assertEquals(dimensionMinus * dimensionMinus * dimensionMinus, palette.count());

            for (int x = 0; x < dimension; x++) {
                for (int y = 0; y < dimension; y++) {
                    for (int z = 0; z < dimension; z++) {
                        if (x == 0 || y == 0 || z == 0) {
                            assertEquals(0, palette.get(x, y, z));
                        } else {
                            assertEquals(10, palette.get(x, y, z));
                        }
                    }
                }
            }
        }
    }

    @Test
    public void partialFillThrowsOutOfBounds() {
        final Palette palette = Palette.blocks();
        assertThrows(Exception.class, () -> palette.fill(-1, 0, 0, 0, 0, 0, 10));
        assertThrows(Exception.class, () -> palette.fill(0, 0, 0, 16, 0, 0, 11));
        assertThrows(Exception.class, () -> palette.fill(0, 0, 0, 100, 0, 0, 12));
    }

    @Test
    public void partialFillThrowsUnordered() {
        final Palette palette = Palette.blocks();
        assertThrows(Exception.class, () -> palette.fill(10, 0, 0, 0, 10, 10, 1));
        assertDoesNotThrow(() -> palette.fill(0, 2, 4, 0, 2, 4, 2));
    }

    @Test
    public void partialFillDoesTotalFill() {
        for (final Palette palette : testPalettes()) {
            final int dimension = palette.dimension();
            final int dimensionMinus = dimension - 1;

            palette.fill(0, 0, 0, dimensionMinus, dimensionMinus, dimensionMinus, 10);
            assertEquals(10, palette.singleValue());
        }
    }

    @Test
    public void bulkPlacementHighValue() {
        int value = 1 << 16;
        for (Palette palette : testPalettes()) {
            final int dimension = palette.dimension();

            for (int x = 0, idx = 0; x < dimension; x++) {
                for (int y = 0; y < dimension; y++) {
                    for (int z = 0; z < dimension; z++, idx++) {
                        palette.set(x, y, z, value + idx);
                    }
                }
            }

            for (int x = 0, idx = 0; x < dimension; x++) {
                for (int y = 0; y < dimension; y++) {
                    for (int z = 0; z < dimension; z++, idx++) {
                        assertEquals(value + idx, palette.get(x, y, z));
                    }
                }
            }
        }
    }

    @Test
    public void directPlacementHighValue() {
        final PaletteImpl palette = (PaletteImpl) Palette.blocks();

        palette.makeDirect();
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, palette.bitsPerEntry());

        palette.set(0, 0, 0, 1 << 20);
        assertEquals(21, palette.bitsPerEntry()); // 1 << 20 needs 21 bits to represent

        assertEquals(1 << 20, palette.get(0, 0, 0));
    }

    @Test
    public void fillHighValue() {
        final PaletteImpl palette = (PaletteImpl) Palette.blocks();

        palette.fill(1 << 20);
        palette.set(0, 0, 0, 10);

        palette.makeDirect();

        assertEquals(10, palette.get(0, 0, 0));
        assertEquals(1 << 20, palette.get(1, 1, 1));
    }

    @Test
    public void setAllHighValue() {
        final Palette palette = Palette.blocks();
        final int value = 1 << 16;
        final AtomicInteger index = new AtomicInteger();
        palette.setAll((_, _, _) -> {
            final int idx = index.getPlain();
            index.setPlain(idx + 1);
            return value + idx;
        });

        for (int y = 0, idx = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++, idx++) {
                    assertEquals(value + idx, palette.get(x, y, z));
                }
            }
        }
    }

    private static List<Palette> testPalettes() {
        return List.of(
                Palette.sized(2, 1, 5, 15, 3),
                Palette.sized(4, 1, 5, 15, 3),
                Palette.sized(8, 1, 5, 15, 3),
                Palette.sized(16, 1, 5, 15, 3),
                Palette.blocks()
        );
    }
}
