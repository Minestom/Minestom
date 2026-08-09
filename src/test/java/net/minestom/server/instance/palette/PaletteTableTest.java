package net.minestom.server.instance.palette;

import net.minestom.server.network.NetworkBuffer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.minestom.server.instance.palette.PaletteAssertions.assertCountsMatchContent;
import static net.minestom.server.network.NetworkBuffer.BYTE;
import static net.minestom.server.network.NetworkBuffer.LONG;
import static net.minestom.server.network.NetworkBuffer.VAR_INT;
import static net.minestom.server.network.NetworkBuffer.VAR_INT_ARRAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PaletteTableTest {
    @Test
    void linearAndHashLookupCoverFullIntDomain() {
        final int[] values = {0, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 7, -31, 42, 9,
                10, 11, 12, 13, 14, 15, 16, 17};
        final PaletteTable table = new PaletteTable(16);
        for (int index = 0; index < values.length; index++) {
            assertEquals(index, table.insert(values[index], index + 1));
            assertEquals(index, table.indexOf(values[index]));
        }
        assertFalse(table.linear());
        assertEquals(-1, table.indexOf(123_456_789));

        table.grow(32);
        assertFalse(table.linear());
        for (int index = 0; index < values.length; index++) {
            assertEquals(index, table.indexOf(values[index]));
            assertEquals(index + 1, table.count(values[index]));
        }
    }

    @Test
    void hashTableReusesDeadEntries() {
        final PaletteTable table = new PaletteTable(32);
        for (int value = 0; value < 32; value++) table.insert(value, 1);
        table.moveAll(7, 8);

        assertEquals(0, table.count(7));
        assertEquals(2, table.count(8));
        assertEquals(7, table.insert(10_000));
        assertEquals(7, table.indexOf(10_000));
        assertEquals(-1, table.indexOf(7));
        for (int value = 0; value < 32; value++) {
            if (value != 7) assertTrue(table.indexOf(value) >= 0, "missing value " + value);
        }
    }

    @Test
    void constructorAndGrowRejectInvalidCapacities() {
        assertThrows(IllegalArgumentException.class, () -> new PaletteTable(3));
        assertThrows(IllegalArgumentException.class, () -> new PaletteTable(0));
        assertThrows(IllegalArgumentException.class, () -> new PaletteTable(-4));
        final PaletteTable table = new PaletteTable(16);
        assertThrows(IllegalArgumentException.class, () -> table.grow(16));
        assertThrows(IllegalArgumentException.class, () -> table.grow(8));
        assertThrows(IllegalArgumentException.class, () -> table.grow(24));
    }

    @Test
    void linearTableReusesDeadEntries() {
        final PaletteTable table = new PaletteTable(2);
        assertEquals(0, table.insert(10, 1));
        assertEquals(1, table.insert(20, 1));
        table.moveAll(0, 1);
        assertTrue(table.linear());

        assertEquals(0, table.insert(30, 1));
        assertTrue(table.linear());
        assertEquals(0, table.indexOf(30));
        assertEquals(1, table.indexOf(20));
        assertEquals(-1, table.indexOf(10));
        assertEquals(2, table.count(20));
        assertEquals(1, table.count(30));
    }

    @Test
    void countBookkeepingRejectsUnderflowAndFullTable() {
        final PaletteTable table = new PaletteTable(4);
        for (int value = 0; value < 3; value++) table.insert(value * 10, 1);
        assertEquals(3, table.insert(30, 0));
        table.moveOne(0, 0);
        table.moveAll(1, 1);
        table.moveAll(3, 0);
        table.addCount(0, 0);
        for (int index = 0; index < 3; index++) assertEquals(1, table.countAt(index));
        assertEquals(0, table.countAt(3));

        table.addCount(3, 1);
        assertEquals(-1, table.insert(99, 5));
        assertEquals(-1, table.indexOf(99));

        final IllegalStateException single = assertThrows(IllegalStateException.class,
                () -> table.addCount(0, -2));
        assertEquals("Negative palette count", single.getMessage());
        final IllegalStateException bulk = assertThrows(IllegalStateException.class,
                () -> table.addCounts(new int[]{0, -2, 0, 0}));
        assertEquals("Negative palette count", bulk.getMessage());
    }

    @Test
    void hashRemovalShiftsCollidingClusters() {
        final PaletteTable table = new PaletteTable(32);
        final Map<Integer, Integer> liveIndices = new HashMap<>();
        final List<Integer> liveValues = new ArrayList<>();
        final List<Integer> retiredValues = new ArrayList<>();
        for (int value = 0; value < 32; value++) {
            liveIndices.put(value, table.insert(value, 1));
            liveValues.add(value);
        }
        assertFalse(table.linear());

        // Seed verified to drive removal shifts through colliding clusters on both
        // sides of the bucket wraparound
        final Random random = new Random(7613);
        int nextFresh = 1_000;
        for (int round = 0; round < 200; round++) {
            final int retired = liveValues.get(random.nextInt(liveValues.size()));
            int survivor = retired;
            while (survivor == retired) survivor = liveValues.get(random.nextInt(liveValues.size()));
            final int deadIndex = liveIndices.remove(retired);
            table.moveAll(deadIndex, liveIndices.get(survivor));
            liveValues.remove(Integer.valueOf(retired));
            retiredValues.add(retired);

            final int fresh = nextFresh++;
            assertEquals(deadIndex, table.insert(fresh, 1), "round " + round);
            liveIndices.put(fresh, deadIndex);
            liveValues.add(fresh);

            for (final int value : liveValues) {
                assertEquals(liveIndices.get(value).intValue(), table.indexOf(value), "live value " + value);
            }
            for (final int value : retiredValues) {
                assertEquals(-1, table.indexOf(value), "retired value " + value);
            }
        }
    }

    @Test
    void hashShiftWrapsAroundBucketArrayEnd() {
        final int capacity = 32;
        final int mask = (capacity << 1) - 1;
        final List<Integer> tailHomed = new ArrayList<>();
        final List<Integer> lastHomed = new ArrayList<>();
        final List<Integer> headHomed = new ArrayList<>();
        final List<Integer> awayHomed = new ArrayList<>();
        for (int value = 0; tailHomed.size() < 4 || lastHomed.size() < 2
                || headHomed.size() < 2 || awayHomed.size() < 10; value++) {
            final int slot = PaletteTable.homeSlot(value, mask);
            if (slot == mask - 1 && tailHomed.size() < 4) tailHomed.add(value);
            else if (slot == mask && lastHomed.size() < 2) lastHomed.add(value);
            else if (slot == 0 && headHomed.size() < 2) headHomed.add(value);
            else if (slot == capacity && awayHomed.size() < 10) awayHomed.add(value);
        }

        // Cluster spanning the bucket array end: entries homed at the two final
        // slots and slot zero probe into a contiguous wrapped run
        final PaletteTable table = new PaletteTable(capacity);
        final List<Integer> live = new ArrayList<>();
        live.addAll(tailHomed);
        live.addAll(lastHomed.subList(0, 1));
        live.addAll(headHomed);
        for (final int value : live) table.insert(value, 1);
        assertFalse(table.linear());

        // Replacements are homed far from the cluster so each removal exercises the
        // wrapped shift geometry undisturbed
        for (int victim = 0; victim < live.size(); victim++) {
            final int retired = live.get(victim);
            final int fresh = awayHomed.get(victim);
            table.replaceValue(table.indexOf(retired), fresh);
            live.set(victim, fresh);
            assertEquals(-1, table.indexOf(retired));
            for (final int value : live) {
                assertTrue(table.indexOf(value) >= 0, "missing value " + value);
                assertEquals(1, table.count(value));
            }
        }

        // Both final-slot homed entries wrap past their home, so removing the entry
        // before them scans over the wrapped pair without shifting either
        final PaletteTable wrapped = new PaletteTable(capacity);
        wrapped.insert(tailHomed.getFirst(), 1);
        wrapped.insert(lastHomed.get(0), 1);
        wrapped.insert(lastHomed.get(1), 1);
        wrapped.insert(awayHomed.get(7), 1);
        wrapped.insert(awayHomed.get(8), 1);
        assertFalse(wrapped.linear());
        wrapped.replaceValue(wrapped.indexOf(tailHomed.getFirst()), awayHomed.get(9));
        assertEquals(-1, wrapped.indexOf(tailHomed.getFirst()));
        assertTrue(wrapped.indexOf(lastHomed.get(0)) >= 0);
        assertTrue(wrapped.indexOf(lastHomed.get(1)) >= 0);
        assertTrue(wrapped.indexOf(awayHomed.get(9)) >= 0);
    }

    @Test
    void hashTableSupportsEverySixteenBitPaletteIndex() {
        final PaletteTable table = new PaletteTable(1 << 16);
        for (int value = 0; value < 1 << 16; value++) assertEquals(value, table.insert(value, 1));
        assertEquals((1 << 16) - 1, table.indexOf((1 << 16) - 1));
        table.replaceValue((1 << 16) - 1, Integer.MIN_VALUE);
        assertEquals((1 << 16) - 1, table.indexOf(Integer.MIN_VALUE));
    }

    @Test
    void paletteUsesLinearThenHashAndReusesDeadSlots() {
        final Palette palette = Palette.blocks();
        for (int value = 1; value < PaletteTable.LINEAR_MAX_SIZE; value++) palette.set(value, 0, 0, value);
        PaletteImpl implementation = (PaletteImpl) palette;
        assertEquals(4, palette.bitsPerEntry());
        assertNotNull(implementation.table);
        assertTrue(implementation.table.linear());

        palette.set(PaletteTable.LINEAR_MAX_SIZE, 0, 0, PaletteTable.LINEAR_MAX_SIZE);
        assertEquals(4, palette.bitsPerEntry());
        assertFalse(implementation.table.linear());

        for (int value = PaletteTable.LINEAR_MAX_SIZE + 1; value < 16; value++) {
            palette.set(value, 0, 0, value);
        }
        palette.replace(15, 14);
        final int bitsBeforeReuse = palette.bitsPerEntry();
        palette.set(1, 0, 0, 10_000);
        assertEquals(bitsBeforeReuse, palette.bitsPerEntry());
        assertEquals(10_000, palette.get(1, 0, 0));
    }

    @Test
    void oneUseEntryCanBeRenamedWithoutGrowing() {
        final Palette palette = Palette.blocks();
        for (int value = 1; value < 16; value++) palette.set(value, 0, 0, value);
        assertEquals(4, palette.bitsPerEntry());

        palette.set(15, 0, 0, 1000);

        assertEquals(4, palette.bitsPerEntry());
        assertEquals(0, palette.count(15));
        assertEquals(1, palette.count(1000));
        assertEquals(1000, palette.get(15, 0, 0));
    }

    @Test
    void bulkOperationsSelectSmallestRepresentation() {
        final Palette palette = Palette.blocks();
        palette.setAll((_, _, _) -> 7);
        assertEquals(0, palette.bitsPerEntry());
        assertEquals(7, palette.singleValue());

        PaletteImpl implementation = (PaletteImpl) palette;
        palette.setAll((x, y, z) -> (x + y + z) & 15);
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, palette.bitsPerEntry());
        palette.optimize(Palette.Optimization.SIZE);
        assertEquals(4, palette.bitsPerEntry());
        assertNotNull(implementation.table);
        assertFalse(implementation.table.linear());

        palette.setAll((x, y, z) -> (x | z << 4 | y << 8) % 17);
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, palette.bitsPerEntry());
        palette.optimize(Palette.Optimization.SIZE);
        assertEquals(5, palette.bitsPerEntry());
        assertFalse(implementation.table.linear());

        palette.setAll((x, y, z) -> x | z << 4 | y << 8);
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, palette.bitsPerEntry());
        assertNull(implementation.table);

        palette.replaceAll((_, _, _, value) -> value & 15);
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, palette.bitsPerEntry());
        palette.optimize(Palette.Optimization.SIZE);
        assertEquals(4, palette.bitsPerEntry());
    }

    @Test
    void sizeOptimizationCollapsesIndirectPaletteWithDeadEntries() {
        final Palette palette = Palette.blocks();
        palette.set(0, 0, 0, 1);
        palette.replace(0, 1);

        PaletteImpl implementation = (PaletteImpl) palette;
        assertEquals(4, palette.bitsPerEntry());
        assertNotNull(implementation.table);
        assertEquals(2, implementation.table.size());

        palette.optimize(Palette.Optimization.SIZE);

        assertEquals(0, palette.bitsPerEntry());
        assertEquals(1, palette.singleValue());
    }

    @Test
    void deadEntriesAreInvisibleToScans() {
        final Palette palette = Palette.blocks();
        PaletteImpl implementation = (PaletteImpl) palette;
        palette.set(0, 0, 0, 1);
        palette.replace(0, 1);
        assertNotNull(implementation.table);
        assertEquals(2, implementation.table.size());

        assertFalse(palette.any(value -> value == 0));
        assertEquals(0, palette.count(value -> value == 0));
        assertEquals(palette.maxSize(), palette.count(value -> value == 1));
        assertTrue(palette.all(value -> value == 1));
        final int[] callbacks = new int[1];
        palette.getAllCounts((value, count) -> {
            callbacks[0]++;
            assertEquals(1, value);
            assertEquals(palette.maxSize(), count);
        });
        assertEquals(1, callbacks[0]);
    }

    @Test
    void smallBiomeRegistryStaysIndirectAndSerializable() {
        final Palette palette = Palette.biomes(8);
        palette.setAll((x, y, z) -> (x + y + z) & 7);
        assertEquals(3, palette.bitsPerEntry());
        // Direct storage would be indistinguishable from indirect storage at this width
        assertNotNull(((PaletteImpl) palette).table);

        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        final NetworkBuffer.Type<Palette> serializer = Palette.biomeSerializer(8);
        buffer.write(serializer, palette);
        final NetworkBuffer encoded = buffer.copy(0, buffer.writeIndex()).index(0, buffer.writeIndex());
        assertEquals(3, encoded.read(BYTE).intValue());
        assertEquals(8, encoded.read(VAR_INT_ARRAY).length);
        final Palette decoded = buffer.read(serializer);
        assertTrue(palette.compare(decoded));
        assertNotNull(((PaletteImpl) decoded).table);
    }

    @Test
    void smallBiomeRegistryNeverEntersDirectMode() {
        final Palette palette = Palette.biomes(8);
        palette.setAll((x, y, z) -> (x + y + z) & 7);
        palette.optimize(Palette.Optimization.SPEED);
        assertNotNull(((PaletteImpl) palette).table);
        assertEquals(3, palette.bitsPerEntry());

        palette.replaceAll((x, y, z, _) -> (x + y + z + 1) & 7);
        assertNotNull(((PaletteImpl) palette).table);
        assertTrue(palette.bitsPerEntry() <= Palette.BIOME_PALETTE_MAX_BITS);
        palette.getAll((x, y, z, value) -> assertEquals((x + y + z + 1) & 7, value));
    }

    @Test
    void valuesBeyondTheIndirectSpaceAreRejected() {
        final Palette palette = Palette.biomes(8);
        assertThrows(IllegalArgumentException.class, () -> palette.setAll((x, y, z) -> x + y * 4 + z * 16));
    }

    @Test
    void copyAcrossConfigurationsRebuildsForTargetEncoding() {
        final Palette blockSource = Palette.sized(16, 1, 5, 15, 1);
        blockSource.set(0, 0, 0, 1);
        final Palette blocks = Palette.blocks();
        blocks.copyFrom(blockSource);
        assertEquals(4, blocks.bitsPerEntry());
        assertSerializable(blocks, Palette.BLOCK_SERIALIZER);

        final Palette wideIndirect = Palette.sized(4, 1, 6, 15, 6);
        wideIndirect.set(0, 0, 0, 1);
        final Palette biomes = Palette.sized(4, 1, 3, 6, 0);
        biomes.copyFrom(wideIndirect);
        assertTrue(biomes.bitsPerEntry() <= 3);
        assertSerializable(biomes, Palette.serializer(4, 1, 3, 6));

        final Palette smallBiomes = Palette.biomes(8);
        smallBiomes.setAll((x, y, z) -> (x + y + z) & 7);
        final Palette wideBiomes = Palette.biomes(9);
        wideBiomes.copyFrom(smallBiomes);
        assertEquals(3, wideBiomes.bitsPerEntry());
        assertNotNull(((PaletteImpl) wideBiomes).table);
        assertTrue(wideBiomes.compare(smallBiomes));
        assertSerializable(wideBiomes, Palette.biomeSerializer(9));
    }

    @Test
    void indexedValuesReturnsSnapshot() {
        final Palette palette = Palette.blocks();
        palette.set(0, 0, 0, 1);
        final long[] packed = palette.indexedValues();
        assertNotNull(packed);
        packed[0] = 0;
        assertEquals(1, palette.get(0, 0, 0));
        assertEquals(1, palette.count(1));
        assertTrue(palette.any(1));
    }

    @Test
    void paletteIndexResolutionPerMode() {
        final Palette single = Palette.blocks();
        assertEquals(5, single.paletteIndexToValue(5));

        final Palette indirect = Palette.blocks();
        indirect.set(0, 0, 0, 42);
        assertEquals(0, indirect.paletteIndexToValue(0));
        assertEquals(42, indirect.paletteIndexToValue(1));

        final Palette direct = Palette.blocks();
        direct.setAll((x, y, z) -> x | z << 4 | y << 8);
        assertNull(((PaletteImpl) direct).table);
        assertEquals(700, direct.paletteIndexToValue(700));
    }

    @Test
    void valueToPaletteIndexGrowsAndFlipsStorage() {
        final Palette direct = Palette.blocks();
        direct.setAll((x, y, z) -> x | z << 4 | y << 8);
        assertEquals(123, direct.valueToPaletteIndex(123));

        final Palette grown = Palette.blocks();
        for (int value = 1; value < 16; value++) grown.set(value, 0, 0, value);
        assertEquals(4, grown.bitsPerEntry());
        assertEquals(16, grown.valueToPaletteIndex(999));
        assertEquals(5, grown.bitsPerEntry());
        assertNotNull(((PaletteImpl) grown).table);
        assertEquals(999, ((PaletteImpl) grown).table.value(16));
        for (int value = 1; value < 16; value++) assertEquals(value, grown.get(value, 0, 0));
        assertCountsMatchContent(grown);

        final Palette flipped = Palette.blocks();
        flipped.setAll((x, y, z) -> (x | z << 4 | y << 8) % 256);
        flipped.optimize(Palette.Optimization.SIZE);
        assertEquals(8, flipped.bitsPerEntry());
        assertEquals(999, flipped.valueToPaletteIndex(999));
        assertEquals(Palette.BLOCK_PALETTE_DIRECT_BITS, flipped.bitsPerEntry());
        assertNull(((PaletteImpl) flipped).table);
        assertEquals(255, flipped.get(15, 15, 15));
        assertCountsMatchContent(flipped);
    }

    @Test
    void indexedValuesNullOnlyInSingleValueMode() {
        final Palette palette = Palette.blocks();
        assertEquals(0, palette.bitsPerEntry());
        assertEquals(0, palette.singleValue());
        assertNull(palette.indexedValues());

        palette.set(0, 0, 0, 7);
        assertEquals(0, palette.singleValue());
        assertNotNull(palette.indexedValues());
    }

    @Test
    void unchangedReplaceAllPreservesIndirectStorage() {
        final Palette palette = Palette.blocks();
        palette.setAll((x, y, z) -> (x + z * 3 + y * 5) & 15);
        palette.optimize(Palette.Optimization.SIZE);
        final PaletteImpl implementation = (PaletteImpl) palette;
        final PaletteTable table = implementation.table;
        final long[] values = implementation.values;

        palette.replaceAll((_, _, _, value) -> value);

        assertSame(table, implementation.table);
        assertSame(values, implementation.values);
        assertCountsMatchContent(palette);
    }

    @Test
    void indirectOffsetPreservesPackedIndicesAndCounts() {
        final Palette palette = Palette.blocks();
        palette.setAll((x, y, z) -> (x + z * 3 + y * 5) % 31);
        palette.optimize(Palette.Optimization.SIZE);
        final PaletteImpl implementation = (PaletteImpl) palette;
        final PaletteTable table = implementation.table;
        final long[] values = implementation.values;

        palette.offset(10_000);

        assertSame(table, implementation.table);
        assertSame(values, implementation.values);
        palette.getAll((x, y, z, value) ->
                assertEquals(10_000 + (x + z * 3 + y * 5) % 31, value));
        assertCountsMatchContent(palette);
    }

    @Test
    void duplicateNetworkEntriesAreCanonicalized() {
        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(BYTE, (byte) 4);
        buffer.write(VAR_INT_ARRAY, new int[]{5, 5});
        final long[] data = new long[Palettes.arrayLength(16, 4)];
        Palettes.fill(4, data, 1);
        for (long packed : data) buffer.write(LONG, packed);

        final Palette palette = buffer.read(Palette.BLOCK_SERIALIZER);

        assertEquals(4096, palette.count(5));
        assertNotNull(((PaletteImpl) palette).table);
        assertEquals(1, ((PaletteImpl) palette).table.size());
        assertEquals(5, palette.get(0, 0, 0));
        assertEquals(5, palette.get(15, 15, 15));
    }

    @Test
    void serializerRejectsInvalidBitsPerEntry() {
        for (final int bitsPerEntry : new int[]{3, 9, 14, 16}) {
            final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
            buffer.write(BYTE, (byte) bitsPerEntry);
            final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> buffer.read(Palette.BLOCK_SERIALIZER));
            assertEquals("Invalid bitsPerEntry: " + bitsPerEntry, exception.getMessage());
        }
    }

    @Test
    void serializerRejectsInvalidPaletteLength() {
        for (final int length : new int[]{0, 17}) {
            final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
            buffer.write(BYTE, (byte) 4);
            buffer.write(VAR_INT_ARRAY, new int[length]);
            final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> buffer.read(Palette.BLOCK_SERIALIZER));
            assertEquals("Invalid palette length: " + length, exception.getMessage());
        }
    }

    @Test
    void serializerRejectsOutOfRangeValues() {
        for (final int value : new int[]{-1, 1 << 15}) {
            final NetworkBuffer single = NetworkBuffer.resizableBuffer();
            single.write(BYTE, (byte) 0);
            single.write(VAR_INT, value);
            final IllegalArgumentException singleFailure = assertThrows(IllegalArgumentException.class,
                    () -> single.read(Palette.BLOCK_SERIALIZER));
            assertEquals("Invalid palette value: " + value, singleFailure.getMessage());

            final NetworkBuffer indirect = NetworkBuffer.resizableBuffer();
            indirect.write(BYTE, (byte) 4);
            indirect.write(VAR_INT_ARRAY, new int[]{0, value});
            final IllegalArgumentException indirectFailure = assertThrows(IllegalArgumentException.class,
                    () -> indirect.read(Palette.BLOCK_SERIALIZER));
            assertEquals("Invalid palette value: " + value, indirectFailure.getMessage());
        }
    }

    @Test
    void randomizedMutationMaintainsCountsAndContent() {
        final Random random = new Random(0x5EEDBEEFL);
        final Palette palette = Palette.blocks();
        final int[] expected = new int[palette.maxSize()];

        for (int operation = 1; operation <= 20_000; operation++) {
            final int index = random.nextInt(expected.length);
            final int x = index & 15;
            final int z = index >> 4 & 15;
            final int y = index >> 8;
            final int value = random.nextInt(96);
            palette.set(x, y, z, value);
            expected[index] = value;

            if (operation % 500 == 0) assertMatches(expected, palette);
        }
    }

    @Test
    void cloneCopyAndOffsetCopyKeepIndependentCounts() {
        final Palette source = Palette.blocks();
        source.setAll((x, y, z) -> (x + z * 3 + y * 5) % 31);
        source.optimize(Palette.Optimization.SIZE);
        assertNotNull(((PaletteImpl) source).table);
        final Palette clone = source.clone();
        final Palette copy = Palette.blocks();
        copy.copyFrom(source);
        final Palette offset = Palette.blocks();
        offset.copyFrom(source, 1, 1, 1);

        assertTrue(source.compare(clone));
        assertTrue(source.compare(copy));
        assertEquals(source.get(0, 0, 0), offset.get(1, 1, 1));
        assertEquals(source.get(14, 14, 14), offset.get(15, 15, 15));
        assertCountsMatchContent(clone);
        assertCountsMatchContent(copy);
        assertCountsMatchContent(offset);
    }

    @Test
    void offsetCopyOnlyMapsValuesInsideCopiedRegion() {
        final Palette source = Palette.blocks();
        for (int value = 1; value <= 8; value++) source.set(value - 1, 0, 0, value);
        final Palette target = Palette.sized(16, 1, 1, 15, 0);

        target.copyFrom(source, 15, 15, 15);

        assertEquals(1, target.bitsPerEntry());
        assertNotNull(((PaletteImpl) target).table);
        assertEquals(1, target.get(15, 15, 15));
        assertEquals(1, target.count(1));
        assertEquals(target.maxSize() - 1, target.count(0));
        assertCountsMatchContent(target);
    }

    private static void assertMatches(int[] expected, Palette palette) {
        final int dimension = palette.dimension();
        for (int index = 0; index < expected.length; index++) {
            final int x = index & dimension - 1;
            final int z = index / dimension & dimension - 1;
            final int y = index / (dimension * dimension);
            assertEquals(expected[index], palette.get(x, y, z), "content at index " + index);
        }
        assertCountsMatchContent(palette);
        final Palette clone = palette.clone();
        assertTrue(palette.compare(clone));
        assertCountsMatchContent(clone);
    }

    private static void assertSerializable(Palette palette, NetworkBuffer.Type<Palette> serializer) {
        final NetworkBuffer buffer = NetworkBuffer.resizableBuffer();
        buffer.write(serializer, palette);
        assertTrue(palette.compare(buffer.read(serializer)));
    }
}
