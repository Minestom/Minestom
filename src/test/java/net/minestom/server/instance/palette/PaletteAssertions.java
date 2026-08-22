package net.minestom.server.instance.palette;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class PaletteAssertions {
    private PaletteAssertions() {
    }

    static List<Palette> testPalettes() {
        return List.of(
                Palette.sized(2, 1, 5, 15, 3),
                Palette.sized(4, 1, 5, 15, 3),
                Palette.sized(8, 1, 5, 15, 3),
                Palette.sized(16, 1, 5, 15, 3),
                Palette.blocks()
        );
    }

    static void assertAllEquals(int expected, Palette palette) {
        final int dim = palette.dimension();
        for (int y = 0; y < dim; y++) {
            for (int z = 0; z < dim; z++) {
                for (int x = 0; x < dim; x++) {
                    assertEquals(expected, palette.get(x, y, z),
                            "Mismatch at (" + x + "," + y + "," + z + ")");
                }
            }
        }
    }

    static int nonZeroCount(Palette palette) {
        return palette.maxSize() - palette.count(0);
    }

    static void assertCountsMatchContent(Palette palette) {
        final Int2IntOpenHashMap expectedCounts = new Int2IntOpenHashMap();
        palette.getAll((_, _, _, value) -> expectedCounts.addTo(value, 1));
        final Int2IntOpenHashMap reportedCounts = new Int2IntOpenHashMap();
        palette.getAllCounts(reportedCounts::put);
        assertEquals(expectedCounts, reportedCounts);
        expectedCounts.int2IntEntrySet().forEach(entry -> {
            final int value = entry.getIntKey();
            final int count = entry.getIntValue();
            assertEquals(count, palette.count(value));
            assertEquals(count != 0, palette.any(value));
            assertEquals(count == palette.maxSize(), palette.all(value));
        });
        assertEquals(0, palette.count(1_000_000));
        assertFalse(palette.any(1_000_000));
    }
}
