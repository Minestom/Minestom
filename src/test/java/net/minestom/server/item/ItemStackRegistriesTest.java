package net.minestom.server.item;

import net.minestom.server.component.DataComponents;
import net.minestom.testing.RegistriesTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RegistriesTest
public class ItemStackRegistriesTest {

    @Test
    void resetRevertsToMaterialDefault() {
        ItemStack apple = ItemStack.of(Material.APPLE).without(DataComponents.FOOD);

        assertFalse(apple.has(DataComponents.FOOD));
        assertSame(apple, apple.reset(DataComponents.REPAIR_COST));
        assertTrue(apple.reset(DataComponents.FOOD).has(DataComponents.FOOD));
    }

    @Test
    void componentsReturnsResolvedView() {
        ItemStack item = ItemStack.of(Material.APPLE)
                .without(DataComponents.FOOD)
                .with(DataComponents.REPAIR_COST, 5);

        assertFalse(item.components().has(DataComponents.FOOD));
        assertEquals(5, item.components().get(DataComponents.REPAIR_COST));
        assertNull(item.componentPatch().get(DataComponents.FOOD));
    }
}
