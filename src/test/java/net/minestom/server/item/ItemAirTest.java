package net.minestom.server.item;

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
        assertEquals(emptyItem, ItemStack.AIR, "AIR item can be compared to empty item");
        assertSame(emptyItem, ItemStack.AIR, "AIR item identity can be compared to empty item");

        assertSame(ItemStack.AIR, ItemStack.fromNBT(Material.DIAMOND, null, 0));
        assertSame(ItemStack.AIR, ItemStack.builder(Material.DIAMOND).amount(0).build());
    }
}
