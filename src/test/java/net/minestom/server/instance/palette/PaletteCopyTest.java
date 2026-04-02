package net.minestom.server.instance.palette;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class PaletteCopyTest {

    @Nested
    @DisplayName("Basic Copy Operations")
    class BasicCopyOperations {

        @Test
        @DisplayName("Copy from empty palette to empty palette")
        void copyEmptyToEmpty() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            target.copyFrom(source);

            assertEquals(0, target.count());
            assertEquals(0, target.bitsPerEntry());
            assertTrue(target.compare(source));
        }

        @Test
        @DisplayName("Copy from single value palette to empty palette")
        void copySingleValueToEmpty() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            source.fill(42);
            target.copyFrom(source);

            assertEquals(source.count(), target.count());
            assertEquals(source.bitsPerEntry(), target.bitsPerEntry());
            assertTrue(target.compare(source));

            // Verify all positions have the same value
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        assertEquals(42, target.get(x, y, z));
                    }
                }
            }
        }

        @Test
        @DisplayName("Copy from multi-value palette to empty palette")
        void copyMultiValueToEmpty() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            // Set up source with multiple values
            source.set(0, 0, 0, 10);
            source.set(1, 1, 1, 20);
            source.set(2, 2, 2, 30);
            source.set(15, 15, 15, 40);

            target.copyFrom(source);

            assertEquals(source.count(), target.count());
            assertEquals(source.bitsPerEntry(), target.bitsPerEntry());
            assertTrue(target.compare(source));

            // Verify specific values
            assertEquals(10, target.get(0, 0, 0));
            assertEquals(20, target.get(1, 1, 1));
            assertEquals(30, target.get(2, 2, 2));
            assertEquals(40, target.get(15, 15, 15));
            assertEquals(0, target.get(5, 5, 5)); // Default value
        }

        @Test
        @DisplayName("Copy to non-empty palette overwrites existing data")
        void copyToNonEmptyPalette() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            // Set up target with initial data
            target.set(0, 0, 0, 99);
            target.set(1, 1, 1, 88);

            // Set up source with different data
            source.set(2, 2, 2, 77);
            source.set(3, 3, 3, 66);

            target.copyFrom(source);

            assertTrue(target.compare(source));
            assertEquals(0, target.get(0, 0, 0)); // Original data overwritten
            assertEquals(0, target.get(1, 1, 1)); // Original data overwritten
            assertEquals(77, target.get(2, 2, 2)); // Source data copied
            assertEquals(66, target.get(3, 3, 3)); // Source data copied
        }
    }

    @Nested
    @DisplayName("Different Palette Types")
    class DifferentPaletteTypes {

        @Test
        @DisplayName("Copy between block palettes")
        void copyBetweenBlockPalettes() {
            List<Palette> palettes = List.of(
                    Palette.blocks(),
                    Palette.sized(16, 4, 8, 15, 4),
                    Palette.sized(16, 4, 8, 15, 6),
                    Palette.sized(16, 4, 8, 15, 8)
            );

            for (Palette source : palettes) {
                for (Palette target : palettes) {
                    // Set up source data
                    source.set(0, 0, 0, 100);
                    source.set(5, 10, 15, 200);
                    source.set(15, 0, 0, 300);

                    target.copyFrom(source);

                    assertTrue(target.compare(source),
                            String.format("Copy failed from %d bits to %d bits",
                                    source.bitsPerEntry(), target.bitsPerEntry()));
                }
            }
        }

        @Test
        @DisplayName("Copy between biome palettes")
        void copyBetweenBiomePalettes() {
            Palette source = Palette.biomes();
            Palette target = Palette.biomes();

            // Set up source with biome data
            source.set(0, 0, 0, 1); // Plains
            source.set(1, 1, 1, 2); // Desert
            source.set(2, 2, 2, 3); // Forest
            source.set(3, 3, 3, 4); // Ocean

            target.copyFrom(source);

            assertTrue(target.compare(source));
            assertEquals(1, target.get(0, 0, 0));
            assertEquals(2, target.get(1, 1, 1));
            assertEquals(3, target.get(2, 2, 2));
            assertEquals(4, target.get(3, 3, 3));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Conditions")
    class EdgeCasesAndErrorConditions {

        @Test
        @DisplayName("Copy from palette with dimension mismatch throws exception")
        void copyDimensionMismatchThrowsException() {
            Palette blockPalette = Palette.blocks(); // 16x16x16
            Palette biomePalette = Palette.biomes();  // 4x4x4

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> blockPalette.copyFrom(biomePalette)
            );

            assertTrue(exception.getMessage().contains("dimension"));
        }

        @Test
        @DisplayName("Copy from zero bits per entry palette")
        void copyFromZeroBitsPerEntry() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            // Source has zero bits per entry (single value)
            assertEquals(0, source.bitsPerEntry());

            target.copyFrom(source);

            assertEquals(0, target.bitsPerEntry());
            assertEquals(0, target.count());
            assertTrue(target.compare(source));
        }

        @Test
        @DisplayName("Copy from palette with zero count")
        void copyFromZeroCount() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            // Ensure source has zero count
            assertEquals(0, source.count());

            target.copyFrom(source);

            assertEquals(0, target.count());
            assertEquals(0, target.bitsPerEntry());
            assertTrue(target.compare(source));
        }
    }

    @Nested
    @DisplayName("Internal Data Structure Integrity")
    class InternalDataStructureIntegrity {

        @Test
        @DisplayName("Copied palette maintains independence from source")
        void copiedPaletteMaintainsIndependence() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            // Set up source
            source.set(0, 0, 0, 10);
            source.set(1, 1, 1, 20);

            target.copyFrom(source);

            // Verify initial copy is correct
            assertTrue(target.compare(source));

            // Modify source after copy
            source.set(2, 2, 2, 30);
            source.set(0, 0, 0, 99); // Change existing value

            // Target should remain unchanged
            assertEquals(10, target.get(0, 0, 0));
            assertEquals(20, target.get(1, 1, 1));
            assertEquals(0, target.get(2, 2, 2)); // Should not have new value
            assertFalse(target.compare(source)); // Should no longer be equal
        }

        @Test
        @DisplayName("Copy preserves exact palette state")
        void copyPreservesExactPaletteState() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            // Create a complex palette state
            Random random = new Random(12345); // Fixed seed for reproducibility
            for (int i = 0; i < 50; i++) {
                int x = random.nextInt(16);
                int y = random.nextInt(16);
                int z = random.nextInt(16);
                int value = random.nextInt(1000) + 1;
                source.set(x, y, z, value);
            }

            int originalCount = source.count();
            int originalBitsPerEntry = source.bitsPerEntry();

            target.copyFrom(source);

            assertEquals(originalCount, target.count());
            assertEquals(originalBitsPerEntry, target.bitsPerEntry());
            assertTrue(target.compare(source));

            // Verify every position matches
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        assertEquals(source.get(x, y, z), target.get(x, y, z),
                                String.format("Mismatch at position (%d, %d, %d)", x, y, z));
                    }
                }
            }
        }

        @Test
        @DisplayName("Copy handles palette resize scenarios")
        void copyHandlesPaletteResizeScenarios() {
            // Test copying from a palette that has undergone resizing
            Palette source = Palette.sized(16, 1, 5, 15, 2);
            Palette target = Palette.blocks();

            // Fill with values that will cause resize in source
            source.set(0, 0, 0, 1);
            source.set(0, 0, 1, 2);
            source.set(0, 0, 2, 3);
            assertEquals(2, source.bitsPerEntry());

            source.set(0, 0, 3, 4); // This should trigger resize to 3 bits
            assertEquals(3, source.bitsPerEntry());

            // Add more values to increase palette size
            for (int i = 5; i <= 10; i++) {
                source.set(i, 0, 0, i);
            }

            target.copyFrom(source);

            assertTrue(target.compare(source));
            assertEquals(source.bitsPerEntry(), target.bitsPerEntry());

            // Verify all values are preserved
            for (int i = 1; i <= 4; i++) {
                assertEquals(i, target.get(0, 0, i - 1));
            }
            for (int i = 5; i <= 10; i++) {
                assertEquals(i, target.get(i, 0, 0));
            }
        }
    }

    @Nested
    @DisplayName("Performance and Stress Tests")
    class PerformanceAndStressTests {

        @Test
        @DisplayName("Copy fully populated palette")
        void copyFullyPopulatedPalette() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            // Fill entire palette with unique values
            int value = 1;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        source.set(x, y, z, value++);
                    }
                }
            }

            assertEquals(4096, source.count()); // 16^3 = 4096

            target.copyFrom(source);

            assertTrue(target.compare(source));
            assertEquals(4096, target.count());

            // Verify all values are preserved
            value = 1;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        assertEquals(value++, target.get(x, y, z));
                    }
                }
            }
        }

        @Test
        @DisplayName("Copy sparse palette")
        void copySparsePalette() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            // Set only a few values in a large palette
            source.set(0, 0, 0, 100);
            source.set(7, 8, 9, 200);
            source.set(15, 15, 15, 300);

            assertEquals(3, source.count());

            target.copyFrom(source);

            assertTrue(target.compare(source));
            assertEquals(3, target.count());
            assertEquals(100, target.get(0, 0, 0));
            assertEquals(200, target.get(7, 8, 9));
            assertEquals(300, target.get(15, 15, 15));

            // Verify other positions are default (0)
            assertEquals(0, target.get(1, 1, 1));
            assertEquals(0, target.get(8, 8, 8));
            assertEquals(0, target.get(14, 14, 14));
        }
    }

    @Nested
    @DisplayName("Multiple Copy Operations")
    class MultipleCopyOperations {

        @Test
        @DisplayName("Chain multiple copy operations")
        void chainMultipleCopyOperations() {
            Palette palette1 = Palette.blocks();
            Palette palette2 = Palette.blocks();
            Palette palette3 = Palette.blocks();

            // Set up initial data
            palette1.set(0, 0, 0, 111);
            palette1.set(5, 5, 5, 222);

            // Copy chain: palette1 -> palette2 -> palette3
            palette2.copyFrom(palette1);
            palette3.copyFrom(palette2);

            assertTrue(palette3.compare(palette1));
            assertTrue(palette3.compare(palette2));

            assertEquals(111, palette3.get(0, 0, 0));
            assertEquals(222, palette3.get(5, 5, 5));
        }

        @Test
        @DisplayName("Copy operation is idempotent")
        void copyOperationIsIdempotent() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            // Set up source
            source.set(1, 2, 3, 456);
            source.set(4, 5, 6, 789);

            // Copy once
            target.copyFrom(source);
            assertTrue(target.compare(source));

            // Create a backup to compare against
            Palette backup = target.clone();

            // Copy again - should not change anything
            target.copyFrom(source);
            assertTrue(target.compare(source));
            assertTrue(target.compare(backup));
        }
    }

    @Nested
    @DisplayName("Offset Copy Operations")
    class OffsetCopyOperations {
        @Test
        @DisplayName("Copy with positive offset")
        void CopyWithPositiveOffset() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            source.fill(10);

            target.copyFrom(source, 2, 4, 8);
            assertEquals((16 - 2) * (16 - 4) * (16 - 8), target.count());

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (x < 2 || y < 4 || z < 8) {
                            assertEquals(0, target.get(x, y, z));
                        } else {
                            assertEquals(10, target.get(x, y, z));
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Copy Multi with positive offset")
        void copyMultiWithPositiveOffset() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            source.fill(10);
            source.set(0, 0, 0, 11);
            source.set(5, 5, 5, 12);
            source.set(2, 4, 8, 13);

            target.copyFrom(source, 8, 4, 2);
            assertEquals((16 - 8) * (16 - 4) * (16 - 2), target.count());

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (x < 8 || y < 4 || z < 2) {
                            assertEquals(0, target.get(x, y, z));
                        } else if (x == 8 && y == 4 && z == 2) {
                            assertEquals(11, target.get(x, y, z));
                        } else if (x == (8 + 5) && y == (4 + 5) && z == (2 + 5)) {
                            assertEquals(12, target.get(x, y, z));
                        } else if (x == (8 + 2) && y == (4 + 4) && z == (2 + 8)) {
                            assertEquals(13, target.get(x, y, z));
                        } else {
                            assertEquals(10, target.get(x, y, z));
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Copy with negative offset")
        void copyWithNegativeOffset() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            source.fill(10);

            target.copyFrom(source, -2, -4, -8);
            assertEquals((16 - 2) * (16 - 4) * (16 - 8), target.count());

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (x >= 16 - 2 || y >= 16 - 4 || z >= 16 - 8) {
                            assertEquals(0, target.get(x, y, z));
                        } else {
                            assertEquals(10, target.get(x, y, z));
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Copy Multi with negative offset")
        void copyMultiWithNegativeOffset() {
            final Palette source = Palette.blocks();
            final Palette target = Palette.blocks();

            source.fill(10);
            source.set(15, 15, 15, 11);
            source.set(10, 10, 10, 12);
            source.set(11, 12, 13, 13);

            target.copyFrom(source, -8, -4, -2);
            assertEquals((16 - 8) * (16 - 4) * (16 - 2), target.count());

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (x >= 16 - 8 || y >= 16 - 4 || z >= 16 - 2) {
                            assertEquals(0, target.get(x, y, z));
                        } else if (x == (15 - 8) && y == (15 - 4) && z == (15 - 2)) {
                            assertEquals(11, target.get(x, y, z));
                        } else if (x == (10 - 8) && y == (10 - 4) && z == (10 - 2)) {
                            assertEquals(12, target.get(x, y, z));
                        } else if (x == (11 - 8) && y == (12 - 4) && z == (13 - 2)) {
                            assertEquals(13, target.get(x, y, z));
                        } else {
                            assertEquals(10, target.get(x, y, z));
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("Copy out of bounds throws")
        void copyOutOfBoundsThrows() {
            final Palette source = Palette.blocks();
            final Palette target = Palette.blocks();

            source.fill(16);
            source.set(0, 0, 0, 8);
            source.set(4, 4, 4, 9);
            source.set(8, 8, 8, 10);
            source.set(15, 15, 15, 11);
            assertThrows(IllegalArgumentException.class, () -> target.copyFrom(source, 20, 0, 0));
            assertThrows(IllegalArgumentException.class, () -> target.copyFrom(source, 0, 16, 0));
            assertThrows(IllegalArgumentException.class, () -> target.copyFrom(source, 0, 0, -16));
        }
    }
}
