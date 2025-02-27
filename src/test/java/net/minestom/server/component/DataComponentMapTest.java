package net.minestom.server.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataComponentMapTest {

    @Test
    void testBasicGet() {
        var map = DataComponentMap.patchBuilder()
                .set(ItemComponent.REPAIR_COST, 10)
                .remove(ItemComponent.CUSTOM_NAME)
                .build();

        assertTrue(map.has(ItemComponent.REPAIR_COST));
        assertEquals(10, map.get(ItemComponent.REPAIR_COST));

        assertFalse(map.has(ItemComponent.CUSTOM_NAME));
        assertNull(map.get(ItemComponent.CUSTOM_NAME));

        assertFalse(map.has(ItemComponent.BANNER_PATTERNS));
        assertNull(map.get(ItemComponent.BANNER_PATTERNS));
    }

    @Test
    void testPatchedGet() {
        var prototype = DataComponentMap.patchBuilder()
                .set(ItemComponent.ITEM_NAME, Component.text("Hello"))
                .set(ItemComponent.REPAIR_COST, 55)
                .set(ItemComponent.CUSTOM_NAME, Component.text("World"))
                .build();
        var map = DataComponentMap.patchBuilder()
                .set(ItemComponent.REPAIR_COST, 1)
                .remove(ItemComponent.CUSTOM_NAME)
                .build();

        // Override
        assertTrue(map.has(prototype, ItemComponent.REPAIR_COST));
        assertEquals(1, map.get(prototype, ItemComponent.REPAIR_COST));

        // Inherit
        assertTrue(map.has(prototype, ItemComponent.ITEM_NAME));
        assertEquals(Component.text("Hello"), map.get(prototype, ItemComponent.ITEM_NAME));

        // Delete
        assertFalse(map.has(prototype, ItemComponent.CUSTOM_NAME));
        assertNull(map.get(prototype, ItemComponent.CUSTOM_NAME));

        // Non-existent
        assertFalse(map.has(prototype, ItemComponent.BANNER_PATTERNS));
        assertNull(map.get(prototype, ItemComponent.BANNER_PATTERNS));
    }

    @Test
    void testDiffEmpty() {
        var prototype = DataComponentMap.patchBuilder().set(ItemComponent.REPAIR_COST, 42).build();
        var map = DataComponentMap.EMPTY;
        var diff = DataComponentMap.diff(prototype, map);

        assertNull(diff.get(ItemComponent.REPAIR_COST));
    }

    @Test
    void testDiffCompleteDifference() {
        var prototype = DataComponentMap.patchBuilder().set(ItemComponent.REPAIR_COST, 42).build();
        var map = DataComponentMap.patchBuilder().set(ItemComponent.CUSTOM_NAME, Component.text("Hello")).build();
        var diff = DataComponentMap.diff(prototype, map);

        assertNull(diff.get(ItemComponent.REPAIR_COST));
        assertEquals(Component.text("Hello"), diff.get(ItemComponent.CUSTOM_NAME));
    }

    @Test
    void testDiffFlatten() {
        var prototype = DataComponentMap.builder().set(ItemComponent.REPAIR_COST, 42).build();
        var map = DataComponentMap.builder().set(ItemComponent.REPAIR_COST, 24).build();
        var diff = DataComponentMap.diff(prototype, map);

        assertEquals(24, diff.get(ItemComponent.REPAIR_COST));
    }

    @Test
    void testBuilder() {
        var builder = DataComponentMap.builder();
        builder.set(ItemComponent.REPAIR_COST, 42);

        // Builder is a getter for its own entries, so this should be valid
        assertEquals(42, builder.get(ItemComponent.REPAIR_COST));
        var map1 = builder.build();
        assertEquals(42, map1.get(ItemComponent.REPAIR_COST));

        // Old built map should be unaffected by change
        builder.set(ItemComponent.REPAIR_COST, 24);
        var map2 = builder.build();
        assertEquals(42, map1.get(ItemComponent.REPAIR_COST));
        assertEquals(24, map2.get(ItemComponent.REPAIR_COST));
    }
}
