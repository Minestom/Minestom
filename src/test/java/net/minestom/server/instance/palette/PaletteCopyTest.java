package net.minestom.server.instance.palette;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static net.minestom.server.instance.palette.PaletteAssertions.assertAllEquals;
import static net.minestom.server.instance.palette.PaletteAssertions.assertCountsMatchContent;
import static net.minestom.server.instance.palette.PaletteAssertions.nonZeroCount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

            assertEquals(0, nonZeroCount(target));
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

            assertEquals(nonZeroCount(source), nonZeroCount(target));
            assertEquals(source.bitsPerEntry(), target.bitsPerEntry());
            assertTrue(target.compare(source));

            assertAllEquals(42, target);
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

            assertEquals(nonZeroCount(source), nonZeroCount(target));
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
            Palette source = Palette.biomes(64);
            Palette target = Palette.biomes(64);

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

        @Test
        @DisplayName("Copy from single value source with different config collapses to single")
        void copyFromSingleSourceWithDifferentConfigCollapsesToSingle() {
            Palette source = Palette.empty(16, 1, 5, 15);
            Palette target = Palette.blocks();

            source.fill(7);
            target.set(0, 0, 0, 1);
            target.set(1, 0, 0, 2);
            assertEquals(4, target.bitsPerEntry());

            target.copyFrom(source);

            assertEquals(0, target.bitsPerEntry());
            assertNull(((PaletteImpl) target).values);
            assertAllEquals(7, target);
            assertTrue(target.compare(source));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Conditions")
    class EdgeCasesAndErrorConditions {

        @Test
        @DisplayName("Copy from palette with dimension mismatch throws exception")
        void copyDimensionMismatchThrowsException() {
            Palette blockPalette = Palette.blocks(); // 16x16x16
            Palette biomePalette = Palette.biomes(64);  // 4x4x4

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> blockPalette.copyFrom(biomePalette)
            );

            assertTrue(exception.getMessage().contains("dimension"));
        }

        @Test
        @DisplayName("Copy high value entries")
        void copyHighValueEntries() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            int highValue = (1 << 15) - 2;
            source.set(0, 0, 0, highValue);
            source.set(15, 15, 15, highValue + 1);

            target.copyFrom(source);

            assertTrue(target.compare(source));
            assertEquals(highValue, target.get(0, 0, 0));
            assertEquals(highValue + 1, target.get(15, 15, 15));
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

            int originalCount = nonZeroCount(source);
            int originalBitsPerEntry = source.bitsPerEntry();

            target.copyFrom(source);

            assertEquals(originalCount, nonZeroCount(target));
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

            assertEquals(4096, nonZeroCount(source)); // 16^3 = 4096

            target.copyFrom(source);

            assertTrue(target.compare(source));
            assertEquals(4096, nonZeroCount(target));

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

            assertEquals(3, nonZeroCount(source));

            target.copyFrom(source);

            assertTrue(target.compare(source));
            assertEquals(3, nonZeroCount(target));
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
        @DisplayName("Offset copy from single value source")
        void offsetCopyFromSingleValueSource() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            source.fill(7);
            target.copyFrom(source, 1, 1, 1);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        final int expected = x >= 1 && y >= 1 && z >= 1 ? 7 : 0;
                        assertEquals(expected, target.get(x, y, z),
                                String.format("Mismatch at (%d, %d, %d)", x, y, z));
                    }
                }
            }
            assertCountsMatchContent(target);
        }

        @Test
        @DisplayName("Offset copy from direct source")
        void offsetCopyFromDirectSource() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            source.setAll((x, y, z) -> x | z << 4 | y << 8);
            assertEquals(15, source.bitsPerEntry());

            target.copyFrom(source, 1, 2, 3);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        final int expected = x >= 1 && y >= 2 && z >= 3
                                ? (x - 1) | (z - 3) << 4 | (y - 2) << 8
                                : 0;
                        assertEquals(expected, target.get(x, y, z),
                                String.format("Mismatch at (%d, %d, %d)", x, y, z));
                    }
                }
            }
            assertCountsMatchContent(target);
        }

        @Test
        @DisplayName("Offset self copy falls back to per-entry copy")
        void offsetSelfCopyFallsBackToPerEntryCopy() {
            Palette palette = Palette.blocks();
            palette.setAll((x, _, _) -> x + 1);
            palette.optimize(Palette.Optimization.SIZE);
            assertNotNull(((PaletteImpl) palette).table);

            // Negative offset only: the per-entry loop ascends, so a positive offset
            // self copy would read cells it has already overwritten.
            palette.copyFrom(palette, -1, 0, 0);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        final int expected = x < 15 ? x + 2 : 16;
                        assertEquals(expected, palette.get(x, y, z),
                                String.format("Mismatch at (%d, %d, %d)", x, y, z));
                    }
                }
            }
            assertCountsMatchContent(palette);
        }

        @Test
        @DisplayName("Offset copy into direct target")
        void offsetCopyIntoDirectTarget() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            source.set(0, 0, 0, 500);
            source.set(1, 1, 1, 600);
            source.set(2, 3, 4, 700);
            assertNotNull(((PaletteImpl) source).table);
            target.setAll((x, y, z) -> x | z << 4 | y << 8);
            assertEquals(15, target.bitsPerEntry());
            assertNull(((PaletteImpl) target).table);

            target.copyFrom(source, 1, 1, 1);

            assertEquals(15, target.bitsPerEntry());
            assertNull(((PaletteImpl) target).table);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        final int expected = x >= 1 && y >= 1 && z >= 1
                                ? source.get(x - 1, y - 1, z - 1)
                                : x | z << 4 | y << 8;
                        assertEquals(expected, target.get(x, y, z),
                                String.format("Mismatch at (%d, %d, %d)", x, y, z));
                    }
                }
            }
            assertCountsMatchContent(target);
        }

        @Test
        @DisplayName("Offset copy grows target table mid-copy")
        void offsetCopyGrowsTargetTableMidCopy() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            for (int i = 0; i < 6; i++) source.set(i, 0, 0, 100 + i);
            target.setAll((x, y, _) -> (x % 4) + (y % 4) * 4 + 1);
            target.optimize(Palette.Optimization.SIZE);
            assertEquals(4, target.bitsPerEntry());

            target.copyFrom(source, 1, 1, 1);

            assertEquals(5, target.bitsPerEntry());
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        final int expected = x >= 1 && y >= 1 && z >= 1
                                ? source.get(x - 1, y - 1, z - 1)
                                : (x % 4) + (y % 4) * 4 + 1;
                        assertEquals(expected, target.get(x, y, z),
                                String.format("Mismatch at (%d, %d, %d)", x, y, z));
                    }
                }
            }
            assertCountsMatchContent(target);
        }

        @Test
        @DisplayName("Offset copy flips target to direct mid-copy")
        void offsetCopyFlipsTargetToDirectMidCopy() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            source.setAll((x, y, z) -> 1000 + (x + z * 16 + y * 256) % 20);
            source.optimize(Palette.Optimization.SIZE);
            assertNotNull(((PaletteImpl) source).table);
            target.setAll((x, y, z) -> (x + z * 16 + y * 256) % 250 + 1);
            target.optimize(Palette.Optimization.SIZE);
            assertEquals(8, target.bitsPerEntry());
            assertNotNull(((PaletteImpl) target).table);

            target.copyFrom(source, 1, 1, 1);

            assertEquals(15, target.bitsPerEntry());
            assertNull(((PaletteImpl) target).table);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        final int expected = x >= 1 && y >= 1 && z >= 1
                                ? 1000 + ((x - 1) + (z - 1) * 16 + (y - 1) * 256) % 20
                                : (x + z * 16 + y * 256) % 250 + 1;
                        assertEquals(expected, target.get(x, y, z),
                                String.format("Mismatch at (%d, %d, %d)", x, y, z));
                    }
                }
            }
            assertCountsMatchContent(target);
        }

        @Test
        @DisplayName("Offset copy into populated indirect target")
        void offsetCopyIntoPopulatedIndirectTarget() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            target.set(0, 0, 0, 70);
            target.set(2, 2, 2, 50);
            target.set(3, 3, 3, 60);
            source.set(1, 1, 1, 80);
            source.set(2, 2, 2, 90);
            assertNotNull(((PaletteImpl) source).table);
            assertNotNull(((PaletteImpl) target).table);

            target.copyFrom(source, 1, 1, 1);

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        final int expected;
                        if (x == 0 && y == 0 && z == 0) expected = 70;
                        else if (x >= 1 && y >= 1 && z >= 1) expected = source.get(x - 1, y - 1, z - 1);
                        else expected = 0;
                        assertEquals(expected, target.get(x, y, z),
                                String.format("Mismatch at (%d, %d, %d)", x, y, z));
                    }
                }
            }
            assertCountsMatchContent(target);
        }

        @Test
        @DisplayName("Zero offset copy delegates to exact copy")
        void zeroOffsetCopyDelegatesToExactCopy() {
            Palette source = Palette.blocks();
            Palette target = Palette.blocks();

            for (int x = 0; x < 16; x++) source.set(x, 0, 0, x + 1);
            assertEquals(5, source.bitsPerEntry());
            source.replace(16, 15);
            assertEquals(5, source.bitsPerEntry());

            target.copyFrom(source, 0, 0, 0);

            assertEquals(5, target.bitsPerEntry());
            assertTrue(target.compare(source));
        }

        @Test
        @DisplayName("Offset copy validates dimension and skips empty region")
        void offsetCopyValidatesDimensionAndSkipsEmptyRegion() {
            Palette blockPalette = Palette.blocks();
            Palette biomePalette = Palette.biomes(64);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> blockPalette.copyFrom(biomePalette, 1, 0, 0)
            );
            assertTrue(exception.getMessage().contains("must equal target palette dimension"));

            Palette source = Palette.blocks();
            Palette target = Palette.blocks();
            source.fill(7);

            target.copyFrom(source, 16, 0, 0);
            target.copyFrom(source, 0, 16, 0);
            target.copyFrom(source, 0, 0, 16);

            assertEquals(0, target.bitsPerEntry());
            assertEquals(0, nonZeroCount(target));
            assertAllEquals(0, target);
        }

        @Test
        @DisplayName("Offset copy handles axis aligned offsets")
        void offsetCopyHandlesAxisAlignedOffsets() {
            Palette source = Palette.blocks();
            source.fill(3);

            Palette target = Palette.blocks();
            target.copyFrom(source, 0, 1, 0);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        assertEquals(y >= 1 ? 3 : 0, target.get(x, y, z));
                    }
                }
            }
            assertCountsMatchContent(target);

            target = Palette.blocks();
            target.copyFrom(source, 0, 0, 1);
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        assertEquals(z >= 1 ? 3 : 0, target.get(x, y, z));
                    }
                }
            }
            assertCountsMatchContent(target);
        }
    }
}
