package net.minestom.server.component;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataComponentMapTest {

    @Test
    void testBasicGet() {
        var map = DataComponentMap.patchBuilder()
                .set(DataComponents.REPAIR_COST, 10)
                .remove(DataComponents.CUSTOM_NAME)
                .build();

        assertTrue(map.has(DataComponents.REPAIR_COST));
        assertEquals(10, map.get(DataComponents.REPAIR_COST));

        assertFalse(map.has(DataComponents.CUSTOM_NAME));
        assertNull(map.get(DataComponents.CUSTOM_NAME));

        assertFalse(map.has(DataComponents.BANNER_PATTERNS));
        assertNull(map.get(DataComponents.BANNER_PATTERNS));
    }

    @Test
    void testPatchedGet() {
        var prototype = DataComponentMap.patchBuilder()
                .set(DataComponents.ITEM_NAME, Component.text("Hello"))
                .set(DataComponents.REPAIR_COST, 55)
                .set(DataComponents.CUSTOM_NAME, Component.text("World"))
                .build();
        var map = DataComponentMap.patchBuilder()
                .set(DataComponents.REPAIR_COST, 1)
                .remove(DataComponents.CUSTOM_NAME)
                .build();

        // Override
        assertTrue(map.has(prototype, DataComponents.REPAIR_COST));
        assertEquals(1, map.get(prototype, DataComponents.REPAIR_COST));

        // Inherit
        assertTrue(map.has(prototype, DataComponents.ITEM_NAME));
        assertEquals(Component.text("Hello"), map.get(prototype, DataComponents.ITEM_NAME));

        // Delete
        assertFalse(map.has(prototype, DataComponents.CUSTOM_NAME));
        assertNull(map.get(prototype, DataComponents.CUSTOM_NAME));

        // Non-existent
        assertFalse(map.has(prototype, DataComponents.BANNER_PATTERNS));
        assertNull(map.get(prototype, DataComponents.BANNER_PATTERNS));
    }

    @Test
    void testDiffEmpty() {
        var prototype = DataComponentMap.patchBuilder().set(DataComponents.REPAIR_COST, 42).build();
        var map = DataComponentMap.EMPTY;
        var diff = DataComponentMap.diff(prototype, map);

        assertNull(diff.get(DataComponents.REPAIR_COST));
    }

    @Test
    void testDiffCompleteDifference() {
        var prototype = DataComponentMap.patchBuilder().set(DataComponents.REPAIR_COST, 42).build();
        var map = DataComponentMap.patchBuilder().set(DataComponents.CUSTOM_NAME, Component.text("Hello")).build();
        var diff = DataComponentMap.diff(prototype, map);

        assertNull(diff.get(DataComponents.REPAIR_COST));
        assertEquals(Component.text("Hello"), diff.get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void testDiffFlatten() {
        var prototype = DataComponentMap.builder().set(DataComponents.REPAIR_COST, 42).build();
        var map = DataComponentMap.builder().set(DataComponents.REPAIR_COST, 24).build();
        var diff = DataComponentMap.diff(prototype, map);

        assertEquals(24, diff.get(DataComponents.REPAIR_COST));
    }

    @Test
    void testBuilder() {
        var builder = DataComponentMap.builder();
        builder.set(DataComponents.REPAIR_COST, 42);

        // Builder is a getter for its own entries, so this should be valid
        assertEquals(42, builder.get(DataComponents.REPAIR_COST));
        var map1 = builder.build();
        assertEquals(42, map1.get(DataComponents.REPAIR_COST));

        // Old built map should be unaffected by change
        builder.set(DataComponents.REPAIR_COST, 24);
        var map2 = builder.build();
        assertEquals(42, map1.get(DataComponents.REPAIR_COST));
        assertEquals(24, map2.get(DataComponents.REPAIR_COST));
    }
}
