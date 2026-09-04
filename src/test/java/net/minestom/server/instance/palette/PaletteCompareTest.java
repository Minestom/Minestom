package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PaletteCompareTest {
    @Test
    void dimensionMismatchReturnsFalse() {
        final Palette blocks = Palette.blocks();
        final Palette biomes = Palette.biomes(8);
        assertFalse(blocks.compare(biomes));
        assertFalse(biomes.compare(blocks));
    }

    @Test
    void equivalentContentComparesEqualAcrossStorageModes() {
        final Palette single = Palette.blocks();
        single.fill(5);
        assertEquals(0, single.bitsPerEntry());

        final Palette indirect = Palette.blocks();
        indirect.set(0, 0, 0, 9);
        indirect.replace(0, 5);
        indirect.replace(9, 5);
        assertEquals(4, indirect.bitsPerEntry());
        assertNotNull(((PaletteImpl) indirect).table);

        final Palette direct = Palette.blocks(15);
        direct.replace(0, 5);
        assertEquals(15, direct.bitsPerEntry());
        assertNull(((PaletteImpl) direct).table);

        assertTrue(single.compare(indirect));
        assertTrue(indirect.compare(single));
        assertTrue(single.compare(direct));
        assertTrue(direct.compare(single));

        final Palette indirectPattern = Palette.blocks();
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    indirectPattern.set(x, y, z, (x + y + z) % 4 + 1);
                }
            }
        }
        assertNotNull(((PaletteImpl) indirectPattern).table);
        final Palette directPattern = Palette.blocks();
        directPattern.setAll((x, y, z) -> (x + y + z) % 4 + 1);
        assertEquals(15, directPattern.bitsPerEntry());
        assertNull(((PaletteImpl) directPattern).table);
        assertTrue(indirectPattern.compare(directPattern));
        assertTrue(directPattern.compare(indirectPattern));

        final Palette otherSingle = Palette.blocks();
        otherSingle.fill(6);
        assertFalse(single.compare(otherSingle));
        assertFalse(otherSingle.compare(single));
    }

    @Test
    void sameLayoutDirectFastPathDetectsDifferences() {
        final Palette first = Palette.blocks();
        first.setAll((x, y, z) -> x | z << 4 | y << 8);
        final Palette second = Palette.blocks();
        second.setAll((x, y, z) -> x | z << 4 | y << 8);
        assertEquals(15, first.bitsPerEntry());
        assertEquals(15, second.bitsPerEntry());
        assertNull(((PaletteImpl) first).table);
        assertNull(((PaletteImpl) second).table);
        assertTrue(first.compare(second));
        assertTrue(second.compare(first));

        final int middle = second.get(8, 8, 8);
        second.set(8, 8, 8, 30_000);
        assertFalse(first.compare(second));
        assertFalse(second.compare(first));

        second.set(8, 8, 8, middle);
        assertTrue(first.compare(second));

        second.set(15, 15, 15, 30_000);
        assertFalse(first.compare(second));
        assertFalse(second.compare(first));
    }

    @Test
    void compareIgnoresLanesPastTheEnd() {
        final NetworkBuffer.Type<Palette> serializer = Palette.serializer(16, 1, 3, 6);
        final int[] raw = new int[Palettes.maxSize(16)];
        for (int index = 0; index < raw.length; index++) raw[index] = index % 64;
        final long[] packed = Palettes.pack(raw, 6);
        final long[] poisoned = packed.clone();
        // At 6 bits the final long holds 6 used lanes, so bits 36..63 are past the end
        poisoned[poisoned.length - 1] |= -1L << 36;
        assertFalse(Arrays.equals(packed, poisoned));

        final Palette clean = readDirect(serializer, packed);
        final Palette dirty = readDirect(serializer, poisoned);
        assertEquals(6, clean.bitsPerEntry());
        assertEquals(6, dirty.bitsPerEntry());
        assertNull(((PaletteImpl) clean).table);
        assertNull(((PaletteImpl) dirty).table);

        assertTrue(clean.compare(dirty));
        assertTrue(dirty.compare(clean));

        final long[] usedLaneDiffers = poisoned.clone();
        usedLaneDiffers[usedLaneDiffers.length - 1] ^= 1L << 30;
        final Palette differing = readDirect(serializer, usedLaneDiffers);
        assertFalse(clean.compare(differing));
        assertFalse(differing.compare(clean));
    }

    @Test
    void singleModeSidesCompareThroughTheSlowPath() {
        assertTrue(Palettes.compare(64, 1,
                0, 5, null, null,
                0, 5, null, null));
        assertFalse(Palettes.compare(64, 1,
                0, 5, null, null,
                0, 6, null, null));
    }

    @Test
    void compareStartSkipsEarlierEntriesOnTheVectorizedPath() {
        // 6 bit lanes hold 10 entries per long, so the picked entries cover a start at a long
        // boundary, inside a long, and past the final entry
        final int size = 64;
        final int[] raw = new int[size];
        for (int index = 0; index < size; index++) raw[index] = index % 64;
        final long[] first = Palettes.pack(raw, 6);
        for (final int differing : new int[]{0, 9, 10, 11, 63}) {
            final int[] changed = raw.clone();
            changed[differing] ^= 1;
            final long[] second = Palettes.pack(changed, 6);
            assertFalse(Palettes.compare(size, differing,
                    6, 0, first, null,
                    6, 0, second, null), "differing entry " + differing);
            assertTrue(Palettes.compare(size, differing + 1,
                    6, 0, first, null,
                    6, 0, second, null), "differing entry " + differing);
        }
    }

    @Test
    void compareStartSkipsEarlierEntriesOnTheSlowPath() {
        final int size = 64;
        final int[] raw = new int[size];
        Arrays.fill(raw, 5);
        for (final int differing : new int[]{0, 9, 10, 63}) {
            final int[] changed = raw.clone();
            changed[differing] = 6;
            final long[] packed = Palettes.pack(changed, 6);
            assertFalse(Palettes.compare(size, differing,
                    6, 0, packed, null,
                    0, 5, null, null), "differing entry " + differing);
            assertTrue(Palettes.compare(size, differing + 1,
                    6, 0, packed, null,
                    0, 5, null, null), "differing entry " + differing);
        }
    }

    @Test
    void equalWidthMixedLayoutsCompareLaneByLane() {
        final Palette direct = Palette.empty(4, 1, 2, 3);
        final Palette indirect = Palette.sized(4, 1, 3, 6, 3);
        for (int value = 1; value <= 4; value++) {
            direct.set(value - 1, 0, 0, value);
            indirect.set(value - 1, 0, 0, value);
        }
        assertEquals(3, direct.bitsPerEntry());
        assertNull(((PaletteImpl) direct).table);
        assertEquals(3, indirect.bitsPerEntry());
        assertNotNull(((PaletteImpl) indirect).table);

        assertTrue(direct.compare(indirect));
        assertTrue(indirect.compare(direct));

        indirect.set(3, 3, 3, 2);
        assertFalse(direct.compare(indirect));
        assertFalse(indirect.compare(direct));
    }

    @Test
    void slowPathDetectsDifferences() {
        final Palette first = Palette.blocks();
        final Palette second = Palette.blocks();
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    final int value = (x + y + z) % 4 + 1;
                    first.set(x, y, z, value);
                    second.set(x, y, z, value);
                }
            }
        }
        assertNotNull(((PaletteImpl) first).table);
        assertNotNull(((PaletteImpl) second).table);
        assertTrue(first.compare(second));

        second.set(7, 3, 5, 2);
        assertFalse(first.compare(second));
        assertFalse(second.compare(first));
    }

    // The direct read path adopts the raw longs unvalidated, keeping poisoned trailing lanes intact
    private static Palette readDirect(NetworkBuffer.Type<Palette> serializer, long[] data) {
        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(BYTE, (byte) 6);
        for (long packed : data) buffer.write(LONG, packed);
        return buffer.read(serializer);
    }
}
