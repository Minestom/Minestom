package net.minestom.server.instance.palette;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Palette clone functionality.
 * Tests cloning behavior, independence of cloned palettes, resizing effects, and data integrity.
 */
public class PaletteCloneTest {

    @Test
    public void basicClone() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Set some initial values
            original.set(0, 0, 0, 42);
            original.set(1, 1, 1, 84);

            Palette cloned = original.clone();

            // Verify clone has same values
            assertEquals(original.get(0, 0, 0), cloned.get(0, 0, 0));
            assertEquals(original.get(1, 1, 1), cloned.get(1, 1, 1));
            assertEquals(original.count(), cloned.count());
            assertEquals(original.dimension(), cloned.dimension());
            assertEquals(original.bitsPerEntry(), cloned.bitsPerEntry());

            // Verify compare method works
            assertTrue(original.compare(cloned));
            assertTrue(cloned.compare(original));
        }
    }

    @Test
    public void cloneIndependence() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Fill original with pattern
            original.setAll((x, y, z) -> x + y * 10 + z * 100);

            Palette cloned = original.clone();

            // Modify original
            original.set(0, 0, 0, 999);
            original.set(1, 0, 0, 888);

            // Verify clone is unaffected
            assertEquals(0, cloned.get(0, 0, 0)); // x=0, y=0, z=0: 0 + 0*10 + 0*100 = 0
            assertEquals(1, cloned.get(1, 0, 0)); // x=1, y=0, z=0: 1 + 0*10 + 0*100 = 1

            // Verify original was changed
            assertEquals(999, original.get(0, 0, 0));
            assertEquals(888, original.get(1, 0, 0));

            if (cloned.dimension() > 2) {
                // Modify clone
                cloned.set(2, 2, 2, 777);

                // Verify original is unaffected by clone modification
                int expected = 2 + 2 * 10 + 2 * 100; // Should be 222
                assertEquals(expected, original.get(2, 2, 2));
            }
        }
    }

    @Test
    public void cloneEmptyPalette() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Clone empty palette
            Palette cloned = original.clone();

            assertEquals(0, cloned.count());
            assertEquals(original.dimension(), cloned.dimension());
            assertTrue(original.compare(cloned));

            // Verify all values are 0
            for (int x = 0; x < cloned.dimension(); x++) {
                for (int y = 0; y < cloned.dimension(); y++) {
                    for (int z = 0; z < cloned.dimension(); z++) {
                        assertEquals(0, cloned.get(x, y, z));
                    }
                }
            }
        }
    }

    @Test
    public void cloneFullPalette() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Fill entire palette
            original.fill(123);

            Palette cloned = original.clone();

            assertEquals(original.count(), cloned.count());
            assertEquals(original.maxSize(), cloned.count());
            assertTrue(original.compare(cloned));

            // Verify all values are correct
            for (int x = 0; x < cloned.dimension(); x++) {
                for (int y = 0; y < cloned.dimension(); y++) {
                    for (int z = 0; z < cloned.dimension(); z++) {
                        assertEquals(123, cloned.get(x, y, z));
                    }
                }
            }
        }
    }

    @Test
    public void cloneWithPatternData() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Create complex pattern
            original.setAll((x, y, z) -> {
                int value = x * 1000 + y * 100 + z * 10;
                return Math.abs(value) % 65536; // Keep within reasonable range
            });

            Palette cloned = original.clone();

            assertEquals(original.count(), cloned.count());
            assertTrue(original.compare(cloned));

            // Verify pattern is preserved
            cloned.getAll((x, y, z, value) -> {
                int expected = Math.abs(x * 1000 + y * 100 + z * 10) % 65536;
                assertEquals(expected, value,
                        String.format("Mismatch at (%d,%d,%d)", x, y, z));
            });
        }
    }

    @Test
    public void cloneAfterResize() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Fill with initial data to force one storage type
            for (int i = 0; i < 10; i++) {
                original.set(i % original.dimension(), 0, 0, i + 1);
            }
            for (int i = 0; i < 10; i++) {
                assertEquals(i + 1, original.get(i % original.dimension(), 0, 0));
            }

            int initialDimension = original.dimension();
            int initialBitsPerEntry = original.bitsPerEntry();
            int initialCount = original.count();

            Palette cloned = original.clone();
            // Verify basic properties
            assertEquals(initialDimension, cloned.dimension());
            assertEquals(initialBitsPerEntry, cloned.bitsPerEntry());
            assertEquals(initialCount, cloned.count());
            assertTrue(original.compare(cloned));
            for (int i = 0; i < 10; i++) {
                assertEquals(i + 1, cloned.get(i % cloned.dimension(), 0, 0));
            }

            // Now force resize by adding many unique values to original
            Random random = new Random(42); // Deterministic
            for (int i = 0; i < original.maxSize() / 2; i++) {
                int x = random.nextInt(original.dimension());
                int y = random.nextInt(original.dimension());
                int z = random.nextInt(original.dimension());
                original.set(x, y, z, 1000 + i); // Unique large values
            }

            // Verify original may have resized
            // (bitsPerEntry might have increased)

            // Verify clone still has original data and hasn't been affected
            assertEquals(initialBitsPerEntry, cloned.bitsPerEntry());
            assertEquals(initialCount, cloned.count());

            // Verify clone still has original values
            for (int i = 0; i < 10; i++) {
                assertEquals(i + 1, cloned.get(i % cloned.dimension(), 0, 0));
            }
        }
    }

    @Test
    public void cloneAndModifyBothDirections() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Set initial pattern
            original.setAll((x, y, z) -> (x + y + z) % 256);

            Palette cloned = original.clone();
            assertTrue(original.compare(cloned));

            // Modify original extensively
            original.fill(500);

            // Modify clone extensively
            cloned.fill(600);

            // Verify they're completely independent
            assertFalse(original.compare(cloned));

            // Check original
            for (int x = 0; x < original.dimension(); x++) {
                for (int y = 0; y < original.dimension(); y++) {
                    for (int z = 0; z < original.dimension(); z++) {
                        assertEquals(500, original.get(x, y, z));
                    }
                }
            }

            // Check clone
            for (int x = 0; x < cloned.dimension(); x++) {
                for (int y = 0; y < cloned.dimension(); y++) {
                    for (int z = 0; z < cloned.dimension(); z++) {
                        assertEquals(600, cloned.get(x, y, z));
                    }
                }
            }
        }
    }

    @Test
    public void cloneWithOffset() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Set pattern and apply offset
            original.setAll((x, y, z) -> x + y + z + 100);
            original.offset(50);

            Palette cloned = original.clone();

            assertTrue(original.compare(cloned));

            // Verify offset was preserved in clone
            cloned.getAll((x, y, z, value) -> {
                int expected = x + y + z + 100 + 50;
                assertEquals(expected, value);
            });

            // Apply different offset to original
            original.offset(-25);

            // Verify clone is unaffected
            cloned.getAll((x, y, z, value) -> {
                int expected = x + y + z + 100 + 50;
                assertEquals(expected, value);
            });
        }
    }

    @Test
    public void cloneWithReplace() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Set initial values
            original.setAll((x, y, z) -> x + y + z);

            Palette cloned = original.clone();

            // Apply replace operation to original
            original.replaceAll((x, y, z, value) -> value * 2);

            // Verify clone is unaffected
            cloned.getAll((x, y, z, value) -> assertEquals(x + y + z, value));

            // Apply different replace to clone
            cloned.replaceAll((x, y, z, value) -> value + 1000);

            // Verify both have correct values
            original.getAll((x, y, z, value) -> assertEquals((x + y + z) * 2, value));

            cloned.getAll((x, y, z, value) -> assertEquals(x + y + z + 1000, value));
        }
    }

    @Test
    public void multipleClonesIndependence() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            original.setAll((x, y, z) -> x * 100 + y * 10 + z);

            // Create multiple clones
            Palette clone1 = original.clone();
            Palette clone2 = original.clone();
            Palette clone3 = original.clone();

            // Verify all are equal initially
            assertTrue(original.compare(clone1));
            assertTrue(original.compare(clone2));
            assertTrue(original.compare(clone3));
            assertTrue(clone1.compare(clone2));

            // Modify each differently
            original.fill(1);
            clone1.fill(2);
            clone2.fill(3);
            clone3.fill(4);

            // Verify all are different
            assertFalse(original.compare(clone1));
            assertFalse(original.compare(clone2));
            assertFalse(clone1.compare(clone2));
            assertFalse(clone2.compare(clone3));

            // Verify each has correct values
            assertEquals(original.maxSize(), original.count());
            original.getAll((x, y, z, value) -> assertEquals(1, value));

            clone1.getAll((x, y, z, value) -> assertEquals(2, value));
            clone2.getAll((x, y, z, value) -> assertEquals(3, value));
            clone3.getAll((x, y, z, value) -> assertEquals(4, value));
        }
    }

    @Test
    public void cloneOptimization() {
        var palettes = testPalettes();
        for (Palette original : palettes) {
            // Create sparse data
            original.set(0, 0, 0, 100);
            original.set(original.dimension() - 1, original.dimension() - 1, original.dimension() - 1, 200);

            Palette cloned = original.clone();

            // Apply optimization to original
            original.optimize(Palette.Optimization.SIZE);

            // Verify clone is unaffected by optimization
            assertTrue(original.compare(cloned));
            assertEquals(100, cloned.get(0, 0, 0));
            assertEquals(200, cloned.get(original.dimension() - 1, original.dimension() - 1, original.dimension() - 1));

            // Apply different optimization to clone
            cloned.optimize(Palette.Optimization.SPEED);

            // Both should still have same data despite different optimizations
            assertTrue(original.compare(cloned));
        }
    }

    @Test
    public void cloneDifferentPaletteTypes() {
        // Test blocks vs biomes vs custom sized palettes
        Palette blockPalette = Palette.blocks();
        Palette biomePalette = Palette.biomes();
        Palette customPalette = Palette.sized(8, 2, 6, 12, 4);

        List<Palette> palettes = List.of(blockPalette, biomePalette, customPalette);

        for (Palette original : palettes) {
            original.setAll((x, y, z) -> (x + y + z) % 100);

            Palette cloned = original.clone();

            assertEquals(original.dimension(), cloned.dimension());
            assertEquals(original.bitsPerEntry(), cloned.bitsPerEntry());
            assertEquals(original.count(), cloned.count());
            assertTrue(original.compare(cloned));

            // Verify independence
            original.set(0, 0, 0, 999);
            assertNotEquals(999, cloned.get(0, 0, 0));
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
