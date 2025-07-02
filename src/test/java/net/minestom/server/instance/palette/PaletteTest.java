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
        var palettes = testPalettes();
        for (Palette palette : palettes) {
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
        var palettes = testPalettes();
        for (Palette palette : palettes) {
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
        var palettes = testPalettes();
        for (Palette palette : palettes) {
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

            palette.fill(0);
            palette.set(0, 0, 1, 2);
            palette.offset(-1);
            for (int x = 0; x < palette.dimension(); x++) {
                for (int y = 0; y < palette.dimension(); y++) {
                    for (int z = 0; z < palette.dimension(); z++) {
                        if (x == 0 && y == 0 && z == 1) {
                            assertEquals(1, palette.get(x, y, z));
                        } else {
                            assertEquals(-1, palette.get(x, y, z));
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
    }

    @Test
    public void bulk() {
        var palettes = testPalettes();
        for (Palette palette : palettes) {
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
        var palettes = testPalettes();
        for (Palette palette : palettes) {
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
    }

    @Test
    public void bulkAllOrder() {
        var palettes = testPalettes();
        for (Palette palette : palettes) {
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

        for (Palette palette : testPalettes()) {
            palette.setAll((x, y, z) -> x + y + z + 100);
            palette.getAll((x, y, z, value) -> assertEquals(x + y + z + 100, value));
        }
    }

    @Test
    public void setAllConstant() {
        var palettes = testPalettes();
        for (Palette palette : palettes) {
            palette.setAll((x, y, z) -> 1);
            palette.getAll((x, y, z, value) -> assertEquals(1, value));
        }
    }

    @Test
    public void getAllPresent() {
        var palettes = testPalettes();
        for (Palette palette : palettes) {
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
        var palettes = testPalettes();
        for (Palette palette : palettes) {
            palette.setAll((x, y, z) -> x + y + z + 1);
            palette.replaceAll((x, y, z, value) -> {
                assertEquals(x + y + z + 1, value);
                return x + y + z + 2;
            });
            palette.getAll((x, y, z, value) -> assertEquals(x + y + z + 2, value));
        }

        for (Palette palette : palettes) {
            palette.fill(0);
            palette.replaceAll((x, y, z, value) -> {
                assertEquals(0, value);
                return value + 1;
            });
            palette.getAll((x, y, z, value) -> assertEquals(1, value));
        }

        for (Palette palette : palettes) {
            palette.fill(1);
            palette.replaceAll((x, y, z, value) -> {
                assertEquals(1, value);
                return value + 1;
            });
            palette.getAll((x, y, z, value) -> assertEquals(2, value));
        }
    }

    @Test
    public void replace() {
        var palettes = testPalettes();
        for (Palette palette : palettes) {
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
        palette.setAll((x, y, z) -> random.nextInt(2048));

        buffer.write(serializer, palette);

        Palette deserialized = buffer.read(serializer);
        assertTrue(palette.compare(deserialized));
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
