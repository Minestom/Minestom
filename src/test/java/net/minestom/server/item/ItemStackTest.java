package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class ItemStackTest {

    @Test
    void hashIsCached(Env env) {
        ItemStack item = ItemStack.of(Material.DIAMOND_SWORD)
                .with(DataComponents.CUSTOM_NAME, Component.text("Blade"));

        assertSame(ItemStack.Hash.of(item), ItemStack.Hash.of(item));
    }

    @Test
    void withComponentsBatchesPatchChanges(Env env) {
        ItemStack item = ItemStack.of(Material.STONE)
                .withComponents(components -> components
                        .set(DataComponents.REPAIR_COST, 42)
                        .set(DataComponents.CUSTOM_NAME, Component.text("Name")));

        assertEquals(42, item.get(DataComponents.REPAIR_COST));
        assertEquals(Component.text("Name"), item.get(DataComponents.CUSTOM_NAME));
    }

    @Test
    void resetRevertsToMaterialDefault(Env env) {
        ItemStack apple = ItemStack.of(Material.APPLE).without(DataComponents.FOOD);

        assertFalse(apple.has(DataComponents.FOOD));
        assertTrue(apple.reset(DataComponents.FOOD).has(DataComponents.FOOD));
    }

    @Test
    void componentsReturnsResolvedView(Env env) {
        ItemStack item = ItemStack.of(Material.APPLE)
                .without(DataComponents.FOOD)
                .with(DataComponents.REPAIR_COST, 5);

        assertFalse(item.components().has(DataComponents.FOOD));
        assertEquals(5, item.components().get(DataComponents.REPAIR_COST));
        assertNull(item.componentPatch().get(DataComponents.FOOD));
    }
}
