package net.minestom.server.instance;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.palette.Palette;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
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
            final int dimension = palette.dimension();
            assertEquals(0, palette.get(0, 0, 0), "Default value should be 0");
            assertEquals(0, palette.count());
            palette.set(0, 0, 0, 64);
            assertEquals(64, palette.get(0, 0, 0));
            assertEquals(64, palette.get(dimension, 0, 0), "Coordinate must be rounded to the palette dimension");
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
        Palette palette = Palette.newPalette(16, 5, 2, 1);
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
        var palette = Palette.newPalette(2, 15, 4, 1);
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
        assertThrows(Exception.class, () -> Palette.newPalette(-4, 5, 3, 1));
        assertThrows(Exception.class, () -> Palette.newPalette(0, 5, 3, 1));
        assertThrows(Exception.class, () -> Palette.newPalette(1, 5, 3, 1));
        assertDoesNotThrow(() -> Palette.newPalette(2, 5, 3, 1));
        assertThrows(Exception.class, () -> Palette.newPalette(3, 5, 3, 1));
        assertDoesNotThrow(() -> Palette.newPalette(4, 5, 3, 1));
        assertThrows(Exception.class, () -> Palette.newPalette(6, 5, 3, 1));
        assertDoesNotThrow(() -> Palette.newPalette(16, 5, 3, 1));
    }

    private static List<Palette> testPalettes() {
        return List.of(
                Palette.newPalette(2, 5, 3, 1),
                Palette.newPalette(4, 5, 3, 1),
                Palette.newPalette(8, 5, 3, 1),
                Palette.newPalette(16, 5, 3, 1));
    }
}
