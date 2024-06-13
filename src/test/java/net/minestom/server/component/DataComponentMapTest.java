package net.minestom.server.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemComponent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataComponentMapTest {

    @Test
    void testBasicGet() {
        var map = DataComponentMap.patchBuilder()
                .set(ItemComponent.CUSTOM_MODEL_DATA, 1)
                .remove(ItemComponent.CUSTOM_NAME)
                .build();

        assertTrue(map.has(ItemComponent.CUSTOM_MODEL_DATA));
        assertEquals(1, map.get(ItemComponent.CUSTOM_MODEL_DATA));

        assertFalse(map.has(ItemComponent.CUSTOM_NAME));
        assertNull(map.get(ItemComponent.CUSTOM_NAME));

        assertFalse(map.has(ItemComponent.BANNER_PATTERNS));
        assertNull(map.get(ItemComponent.BANNER_PATTERNS));
    }

    @Test
    void testPatchedGet() {
        var prototype = DataComponentMap.patchBuilder()
                .set(ItemComponent.ITEM_NAME, Component.text("Hello"))
                .set(ItemComponent.CUSTOM_MODEL_DATA, 55)
                .set(ItemComponent.CUSTOM_NAME, Component.text("World"))
                .build();
        var map = DataComponentMap.patchBuilder()
                .set(ItemComponent.CUSTOM_MODEL_DATA, 1)
                .remove(ItemComponent.CUSTOM_NAME)
                .build();

        // Override
        assertTrue(map.has(prototype, ItemComponent.CUSTOM_MODEL_DATA));
        assertEquals(1, map.get(prototype, ItemComponent.CUSTOM_MODEL_DATA));

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
        var prototype = DataComponentMap.patchBuilder().set(ItemComponent.CUSTOM_MODEL_DATA, 1).build();
        var map = DataComponentMap.EMPTY;
        var diff = DataComponentMap.diff(prototype, map);

        assertNull(diff.get(ItemComponent.CUSTOM_MODEL_DATA));
    }

    @Test
    void testDiffCompleteDifference() {
        var prototype = DataComponentMap.patchBuilder().set(ItemComponent.CUSTOM_MODEL_DATA, 1).build();
        var map = DataComponentMap.patchBuilder().set(ItemComponent.CUSTOM_NAME, Component.text("Hello")).build();
        var diff = DataComponentMap.diff(prototype, map);

        assertNull(diff.get(ItemComponent.CUSTOM_MODEL_DATA));
        assertEquals(Component.text("Hello"), diff.get(ItemComponent.CUSTOM_NAME));
    }

    @Test
    void testDiffFlatten() {
        var prototype = DataComponentMap.builder().set(ItemComponent.CUSTOM_MODEL_DATA, 1).build();
        var map = DataComponentMap.builder().set(ItemComponent.CUSTOM_MODEL_DATA, 2).build();
        var diff = DataComponentMap.diff(prototype, map);

        assertEquals(2, diff.get(ItemComponent.CUSTOM_MODEL_DATA));
    }

    @Test
    void testBuilder() {
        var builder = DataComponentMap.builder();
        builder.set(ItemComponent.CUSTOM_MODEL_DATA, 1);

        // Builder is a getter for its own entries, so this should be valid
        assertEquals(1, builder.get(ItemComponent.CUSTOM_MODEL_DATA));
        var map1 = builder.build();
        assertEquals(1, map1.get(ItemComponent.CUSTOM_MODEL_DATA));

        // Old built map should be unaffected by change
        builder.set(ItemComponent.CUSTOM_MODEL_DATA, 2);
        var map2 = builder.build();
        assertEquals(1, map1.get(ItemComponent.CUSTOM_MODEL_DATA));
        assertEquals(2, map2.get(ItemComponent.CUSTOM_MODEL_DATA));
    }
}
