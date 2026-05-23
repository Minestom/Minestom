package net.minestom.server.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemAirTest {
    @Test
    public void testAir() {
        var item = ItemStack.of(Material.DIAMOND_SWORD);
        assertFalse(item.isAir());
        assertTrue(ItemStack.AIR.isAir());
        var emptyItem = item.withAmount(0);
        assertTrue(emptyItem.isAir());
        assertEquals(ItemStack.AIR, emptyItem, "AIR item can be compared to empty item");
        assertSame(ItemStack.AIR, emptyItem, "AIR item identity can be compared to empty item");

        assertSame(ItemStack.AIR, ItemStack.of(Material.DIAMOND, 0));
        assertSame(ItemStack.AIR, ItemStack.builder(Material.DIAMOND).amount(0).build());
    }

    @Test
    public void testAirWithComponent() {
        var item = ItemStack.AIR.with(DataComponents.CUSTOM_NAME, Component.text("Name"));
        assertSame(ItemStack.AIR, item);
    }
}
